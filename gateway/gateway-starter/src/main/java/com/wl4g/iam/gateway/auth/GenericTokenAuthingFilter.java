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
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.security.MessageDigest.isEqual;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isAnyEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Flux.just;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link IgnoreGlobalFilterFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-01 v3.0.0
 * @since v3.0.0
 */
public class GenericTokenAuthingFilter extends AbstractGatewayFilterFactory<GenericTokenAuthingFilter.Config> {

    private final SmartLogger log = getLogger(getClass());
    private final Cache<String, String> signReplayValidityStore;
    private final Cache<String, String> secretCacheStore;

    private @Autowired StringRedisTemplate stringTemplate;

    public GenericTokenAuthingFilter() {
        super(GenericTokenAuthingFilter.Config.class);
        this.signReplayValidityStore = CacheBuilder.newBuilder().expireAfterWrite(15 * 60, TimeUnit.SECONDS).build();
        this.secretCacheStore = CacheBuilder.newBuilder().expireAfterWrite(6, TimeUnit.SECONDS).build();
    }

    @Override
    public String name() {
        return GENERIC_TOKEN_AUTH_FILTER;
    }

    /**
     * For the source code of the gateway filter chain implementation, see to:
     * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#handle(ServerWebExchange)}
     * 
     * {@link org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator.getRoutes()}
     * 
     * Note: All requests will be filtered if
     * {@link org.springframework.cloud.gateway.filter.GlobalFilter} is
     * implemented. </br>
     * for example:
     * 
     * <pre>
     * storedAppSecret=5aUpyX5X7wzC8iLgFNJuxqj3xJdNQw8yS
     * curl http://wl4g.debug:14085/openapi/v2/test?appId=oi554a94bc416e4edd9ff963ed0e9e25e6c10545&nonce=0L9GyULPfwsD3Swg&timestamp=1599637679878&signature=5ac8747ccc2b1b332e8445b496d0c38529b38fba2c1b8ca8490cbf2932e06943
     * </pre>
     * 
     * Filters are looked up on every request,
     * see:{@link org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory#apply()}
     */
    @Override
    public GatewayFilter apply(GenericTokenAuthingFilter.Config config) {
        return (exchange, chain) -> {
            if (JvmRuntimeTool.isJvmInDebugging) {
                return chain.filter(exchange);
            }

            MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
            String appId = params.getFirst(config.getParamNameAppId());
            String timestamp = params.getFirst(config.getParamNameTimestamp());
            String nonce = params.getFirst(config.getParamNameNonce());
            String sign = params.getFirst(config.getParamNameSignature());
            if (isAnyEmpty(appId, nonce, timestamp, sign)) {
                log.warn("resp:valid parameters is missing. appId={}, nonce={}, timestamp={}, signature={}", appId, timestamp,
                        nonce, sign);
                return writeResponse(4000, "missing_parameters", exchange);
            }

            // Check replay signature.
            if (signReplayValidityStore.asMap().containsKey(sign)) {
                log.warn("Invalid signature locked. - signature={}, appId={}", sign, appId);
                return writeResponse(4023, "illegal_signature", exchange);
            }

            // Calculation signature
            byte[] signBytes = doSignature(config, appId, timestamp, nonce);

            // Verify signature
            try {
                if (!isEqual(signBytes, Hex.decodeHex(sign.toCharArray()))) {
                    log.warn("Illegal signature. sign: {}, request sign: {}", new String(sign), signBytes);
                    return writeResponse(4003, "invalid_signature", exchange);
                }
                // Saving signature
                signReplayValidityStore.put(sign, appId);
            } catch (DecoderException e) {
                return writeResponse(4003, "invalid_signature", exchange);
            }

            // Add authenticated information.
            ServerHttpRequest request = exchange.getRequest().mutate().header(GENERIC_TOKEN_AUTH_CLIENT, appId).build();
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
            //
            // return exchange.getResponse().setComplete();
        };
    }

    @SuppressWarnings("deprecation")
    private byte[] doSignature(GenericTokenAuthingFilter.Config config, String appId, String timestamp, String nonce) {
        // Load stored secret.
        String storedAppSecret = loadStoredSecret(config, appId);

        // Join token parts
        StringBuffer signText = new StringBuffer();
        signText.append(appId);
        signText.append(storedAppSecret);
        signText.append(timestamp);
        signText.append(nonce);

        // ASCII sort
        byte[] signInput = signText.toString().getBytes(UTF_8);
        if (config.isSignatureForAsciiSorted()) {
            Arrays.sort(signInput);
        }

        switch (config.getSignatureAlgorithmType()) {
        case MD5:
            return Hashing.md5().hashBytes(signInput).asBytes();
        case S1:
            return Hashing.sha1().hashBytes(signInput).asBytes();
        case S256:
            return Hashing.sha256().hashBytes(signInput).asBytes();
        case S384:
            return Hashing.sha384().hashBytes(signInput).asBytes();
        case HS512:
            return Hashing.sha512().hashBytes(signInput).asBytes();
        default:
            throw new Error("Shouldn't be here");
        }
    }

    private String loadStoredSecret(GenericTokenAuthingFilter.Config config, String appId) {
        String loadKey = config.getSecretLoadPrefix().concat(appId);
        switch (config.getSecretLoadFromType()) {
        case ENV:
            return hasTextOf(getenv(loadKey), "storedSecret");
        case REDIS:
            String secret = secretCacheStore.asMap().get(loadKey);
            if (isBlank(secret)) {
                synchronized (loadKey) {
                    secret = secretCacheStore.asMap().get(loadKey);
                    if (isBlank(secret)) {
                        secret = stringTemplate.opsForValue().get(loadKey);
                        secretCacheStore.asMap().put(loadKey, secret);
                        return secret;
                    }
                }
            }
            return secret;
        default:
            throw new Error("Shouldn't be here");
        }
    }

    private Mono<Void> writeResponse(int errcode, String errmsg, ServerWebExchange exchange) {
        RespBase<?> resp = RespBase.create().withCode(errcode).withMessage(errmsg);
        ServerHttpResponse response = exchange.getResponse();
        DataBuffer buffer = response.bufferFactory().wrap(resp.asJson().getBytes(UTF_8));
        response.setStatusCode(OK);
        return response.writeWith(just(buffer));
    }

    @Getter
    @Setter
    @ToString
    public static class Config {
        private String paramNameAppId = "appId";
        private String paramNameTimestamp = "timestamp";
        private String paramNameNonce = "nonce";
        private String paramNameSignature = "signature";
        private String signatureAlgorithm = "SHA-256";
        private boolean signatureForAsciiSorted = true;
        private String secretLoadFrom;
        private String secretLoadPrefix = "IAM_GW_GENERIC_SECRET_";
        private int secretLoadLocalCacheSeconds = 6;
        // Temporary fields.
        private transient HashingAlgorithmType signatureAlgorithmType = HashingAlgorithmType.getDefault();
        private transient SecretLoadFromType secretLoadFromType = SecretLoadFromType.getDefault();

        public void setSignatureAlgorithm(String signatureAlgorithm) {
            HashingAlgorithmType type = HashingAlgorithmType.safeOf(signatureAlgorithm);
            if (isNull(type)) {
                throw new IllegalStateException(format("Invalid hashing sign algorithm '%s', suppported is '%s'",
                        signatureAlgorithm, HashingAlgorithmType.getNames()));
            }
            this.signatureAlgorithm = signatureAlgorithm;
            this.signatureAlgorithmType = type;
        }

        public void setSecretLoadFrom(String secretLoadFrom) {
            SecretLoadFromType type = SecretLoadFromType.safeOf(secretLoadFrom);
            if (isNull(type)) {
                throw new IllegalStateException(format("Invalid secret load from '%s', suppported is '%s'", secretLoadFrom,
                        SecretLoadFromType.getNames()));
            }
            this.secretLoadFrom = secretLoadFrom;
            this.secretLoadFromType = type;
        }
    }

    @Getter
    public static enum HashingAlgorithmType {
        MD5, S1, S256(true), S384, S512, HS256, HS384, HS512;

        private final boolean isDefault;

        private HashingAlgorithmType() {
            this.isDefault = false;
        }

        private HashingAlgorithmType(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public static HashingAlgorithmType getDefault() {
            HashingAlgorithmType defaultValue = null;
            for (HashingAlgorithmType v : values()) {
                if (v.isDefault) {
                    if (defaultValue != null) {
                        throw new IllegalStateException("There can only be one default value");
                    }
                    defaultValue = v;
                }
            }
            return defaultValue;
        }

        public static List<String> getNames() {
            return asList(HashingAlgorithmType.values()).stream().map(v -> v.name().toUpperCase()).collect(toList());
        }

        public static HashingAlgorithmType safeOf(String name) {
            for (HashingAlgorithmType v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v;
                }
            }
            return null;
        }

        public static HashingAlgorithmType of(String name) {
            HashingAlgorithmType result = safeOf(name);
            if (nonNull(result)) {
                return result;
            }
            throw new IllegalArgumentException(format("unsupported prompt for '%s'", name));
        }
    }

    public static enum SecretLoadFromType {
        ENV(true), REDIS;

        private final boolean isDefault;

        private SecretLoadFromType() {
            this.isDefault = false;
        }

        private SecretLoadFromType(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public static SecretLoadFromType getDefault() {
            SecretLoadFromType defaultValue = null;
            for (SecretLoadFromType v : values()) {
                if (v.isDefault) {
                    if (defaultValue != null) {
                        throw new IllegalStateException("There can only be one default value");
                    }
                    defaultValue = v;
                }
            }
            return defaultValue;
        }

        public static List<String> getNames() {
            return asList(SecretLoadFromType.values()).stream().map(v -> v.name().toUpperCase()).collect(toList());
        }

        public static SecretLoadFromType safeOf(String name) {
            for (SecretLoadFromType v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v;
                }
            }
            return null;
        }

        public static SecretLoadFromType of(String name) {
            SecretLoadFromType result = safeOf(name);
            if (nonNull(result)) {
                return result;
            }
            throw new IllegalArgumentException(format("unsupported prompt for '%s'", name));
        }
    }

    // private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";
    public static final String GENERIC_TOKEN_AUTH_FILTER = "GenericTokenAuthFilter";
    public static final String GENERIC_TOKEN_AUTH_CLIENT = "X-Generic-Token-appId";

}