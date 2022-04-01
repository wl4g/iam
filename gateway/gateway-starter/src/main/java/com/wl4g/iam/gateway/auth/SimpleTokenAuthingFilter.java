/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.iam.gateway.auth;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.System.getenv;
import static java.security.MessageDigest.isEqual;
import static org.apache.commons.lang3.StringUtils.isAnyEmpty;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Flux.just;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.Hashing;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.runtime.JvmRuntimeTool;
import com.wl4g.infra.common.web.rest.RespBase;

import reactor.core.publisher.Mono;

/**
 * {@link IgnoreGlobalFilterFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-01 v3.0.0
 * @since v3.0.0
 */
public class SimpleTokenAuthingFilter extends AbstractGatewayFilterFactory<Object> {

    private final InternalSimpleTokenAuthingFilter filter = new InternalSimpleTokenAuthingFilter();

    @Override
    public GatewayFilter apply(Object config) {
        return filter;
    }

    @Override
    public String name() {
        return SIMPLE_TOKEN_AUTH_FILTER;
    }

    /**
     * {@link OidcAuthingFilter}
     * 
     * For the source code of the gateway filter chain implementation,
     * see:{@link org.springframework.cloud.gateway.handler.FilteringWebHandler#handle(ServerWebExchange)}
     * 
     * Note: All requests will be filtered if
     * {@link org.springframework.cloud.gateway.filter.GlobalFilter} is
     * implemented.
     * 
     * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
     * @author vjay
     * @version v1.0 2020-07-04
     */
    public static class InternalSimpleTokenAuthingFilter implements GatewayFilter, Ordered {
        protected final SmartLogger log = getLogger(getClass());

        @Override
        public int getOrder() {
            return 0;
        }

        /**
         * for example: </br>
         * 
         * <pre>
         * storedAppSecret=5aUpyX5X7wzC8iLgFNJuxqj3xJdNQw8yS
         * 
         * curl http://wl4g.debug:14085/openapi/v2/test?appId=oi554a94bc416e4edd9ff963ed0e9e25e6c10545&nonce=0L9GyULPfwsD3Swg&timestamp=1599637679878&signature=5ac8747ccc2b1b332e8445b496d0c38529b38fba2c1b8ca8490cbf2932e06943
         * 
         * </pre>
         */
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            if (JvmRuntimeTool.isJvmInDebugging) {
                return chain.filter(exchange);
            }

            MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
            String appId = params.getFirst("appId");
            String nonce = params.getFirst("nonce");
            String timestamp = params.getFirst("timestamp");
            String signature = params.getFirst("signature");
            if (isAnyEmpty(appId, nonce, timestamp, signature)) {
                log.warn("appId/nonce/timestamp/signature is requires");
                return writeResponse(4000, "Invalid parameters", exchange);
            }

            // Check replay signature
            if (signReplayValidityStore.asMap().containsKey(signature)) {
                log.warn("Invalid signature locked. signature: {}, appId: {}", signature, appId);
                return writeResponse(4023, "Invalid signature locked", exchange);
            }

            // Gets stored appSecret token.
            String storedAppSecret = getenv("IAM_AUTHC_SIGN_APPSECRET_".concat(appId));
            hasTextOf(storedAppSecret, "storedAppSecret");

            // Join token parts
            StringBuffer signtext = new StringBuffer();
            signtext.append(appId);
            signtext.append(storedAppSecret);
            signtext.append(timestamp);
            signtext.append(nonce);

            // Ascii sort
            byte[] signInput = signtext.toString().getBytes(UTF_8);
            Arrays.sort(signInput);
            // Calc signature
            byte[] sign = Hashing.sha256().hashBytes(signInput).asBytes();

            // Signature assertion
            try {
                if (!isEqual(sign, Hex.decodeHex(signature.toCharArray()))) {
                    log.warn("Illegal signature. sign: {}, request sign: {}", new String(sign), signature);
                    return writeResponse(4003, "Invalid signature", exchange);
                }
            } catch (DecoderException e) {
                return writeResponse(4003, "Invalid signature", exchange);
            }

            // Save signature
            signReplayValidityStore.put(signature, appId);

            // Add authenticated information.
            ServerHttpRequest request = exchange.getRequest().mutate().header(SIMPLE_TOKEN_AUTH_CLIENT, appId).build();
            return chain.filter(exchange.mutate().request(request).build());

            // exchange.getAttributes().put(REQUEST_TIME_BEGIN,
            // System.currentTimeMillis());
            // log.info("token is " +
            // exchange.getRequest().getHeaders().get("token"));
            //
            // if (exchange.getRequest().getHeaders().containsKey("token")) {
            // return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
            // if (startTime != null) {
            // log.info(
            // exchange.getRequest().getURI().getRawPath() + ": " +
            // (System.currentTimeMillis() - startTime) + "ms");
            // }
            // }));
            // } else {
            // byte[] bytes = "{\"status\":429,\"msg\":\"Too Many
            // Requests\",\"data\":{}}".getBytes(StandardCharsets.UTF_8);
            // DataBuffer buffer =
            // exchange.getResponse().bufferFactory().wrap(bytes);
            // ServerHttpResponse serverHttpResponse = exchange.getResponse();
            // serverHttpResponse.setStatusCode(HttpStatus.OK);
            // return exchange.getResponse().writeWith(Flux.just(buffer));
            // }
        }

        private Mono<Void> writeResponse(int errcode, String errmsg, ServerWebExchange exchange) {
            RespBase<?> resp = RespBase.create().withCode(errcode).withMessage(errmsg);
            ServerHttpResponse response = exchange.getResponse();
            DataBuffer buffer = response.bufferFactory().wrap(resp.asJson().getBytes(UTF_8));
            response.setStatusCode(OK);
            return response.writeWith(just(buffer));
        }

        // private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";

        /**
         * Digesting string with sha256
         * 
         * @param str
         * @return
         * @throws UnsupportedEncodingException
         * @throws NoSuchAlgorithmException
         */
        public static String getSha256(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            return byte2Hex(messageDigest.digest());
        }

        /**
         * Bytes to hex string
         * 
         * @param bytes
         * @return
         */
        public static String byte2Hex(byte[] bytes) {
            StringBuffer stringBuffer = new StringBuffer();
            String temp = null;
            for (int i = 0; i < bytes.length; i++) {
                temp = Integer.toHexString(bytes[i] & 0xFF);
                if (temp.length() == 1) {
                    // 1 to get a bit of the complement 0 operation
                    stringBuffer.append("0");
                }
                stringBuffer.append(temp);
            }
            return stringBuffer.toString();
        }

        private final static Cache<String, String> signReplayValidityStore = CacheBuilder.newBuilder()
                .expireAfterWrite(20, TimeUnit.MINUTES)
                .build();

    }

    public static final String SIMPLE_TOKEN_AUTH_FILTER = "SimpleTokenAuthFilter";
    public final static String SIMPLE_TOKEN_AUTH_CLIENT = "X-Simple-Token-ClientId";

}