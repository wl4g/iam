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
import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.System.getenv;
import static java.security.MessageDigest.isEqual;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Flux.just;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.validation.constraints.NotNull;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.cache.Cache;
import com.google.common.hash.Hashing;
import com.wl4g.iam.gateway.auth.config.AuthingProperties;
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
public class SignTokenAuthingFilter extends AbstractGatewayFilterFactory<SignTokenAuthingFilter.Config> {

    private final SmartLogger log = getLogger(getClass());

    private final AuthingProperties authingConfig;
    private final StringRedisTemplate stringTemplate;

    private final Cache<String, String> signReplayValidityStore;
    private final Cache<String, String> secretCacheStore;

    public SignTokenAuthingFilter(@NotNull AuthingProperties authingConfig, @NotNull StringRedisTemplate stringTemplate) {
        super(SignTokenAuthingFilter.Config.class);
        this.authingConfig = notNullOf(authingConfig, "authingConfig");
        this.stringTemplate = notNullOf(stringTemplate, "stringTemplate");
        this.signReplayValidityStore = newBuilder()
                .expireAfterWrite(authingConfig.getSignToken().getSignReplayVerifyLocalCacheSeconds(), SECONDS)
                .build();
        this.secretCacheStore = newBuilder().expireAfterWrite(authingConfig.getSignToken().getSecretLocalCacheSeconds(), SECONDS)
                .build();
    }

    @Override
    public String name() {
        return SIGN_TOKEN_AUTH_FILTER;
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
    public GatewayFilter apply(SignTokenAuthingFilter.Config config) {
        return (exchange, chain) -> {
            if (JvmRuntimeTool.isJvmInDebugging) {
                return chain.filter(exchange);
            }

            // Extract required parameters.
            Map<String, String> queryParams = exchange.getRequest().getQueryParams().toSingleValueMap();
            String appId = hasText(queryParams.get(config.getAppIdParam()), "%s missing", config.getAppIdParam());
            String timestamp = hasText(queryParams.get(config.getTimestampParam()), "%s missing", config.getTimestampParam());
            String sign = hasText(queryParams.get(config.getSignParam()), "%s missing", config.getSignParam());

            // Check replay.
            if (signReplayValidityStore.asMap().containsKey(sign)) {
                log.warn("Illegal signature locked. - sign={}, appId={}, timestamp={}", sign, appId, timestamp);
                return writeResponse(4023, "illegal_signature", exchange);
            }

            // Verify signature
            byte[] signBytes = doSignature(config, exchange, appId, timestamp, sign);
            try {
                if (!isEqual(signBytes, Hex.decodeHex(sign.toCharArray()))) {
                    log.warn("Invalid request sign='{}', sign='{}'", new String(sign), signBytes);
                    return writeResponse(4003, "invalid_signature", exchange);
                }
                signReplayValidityStore.put(sign, appId);
            } catch (DecoderException e) {
                return writeResponse(4003, "invalid_signature", exchange);
            }

            // Add authenticated information.
            ServerHttpRequest request = exchange.getRequest().mutate().header(SIGN_TOKEN_AUTH_CLIENT, appId).build();
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

    private byte[] doSignature(
            SignTokenAuthingFilter.Config config,
            ServerWebExchange exchange,
            String appId,
            String timestamp,
            String sign) {
        // Load stored secret.
        byte[] storedAppSecret = loadStoredSecret(config, appId);

        // Make signature plain text.
        byte[] signPlainBytes = config.getSignHashingMode().getFunction().apply(
                new Object[] { config, storedAppSecret, exchange.getRequest() });

        // Hashing signature.
        return config.getSignAlgorithm().getFunction().apply(new byte[][] { storedAppSecret, signPlainBytes });
    }

    private byte[] loadStoredSecret(SignTokenAuthingFilter.Config config, String appId) {
        String loadKey = authingConfig.getSignToken().getSecretLoadPrefix().concat(appId);
        switch (authingConfig.getSignToken().getSecretLoadFrom()) {
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
                        return secret.getBytes(UTF_8);
                    }
                }
            }
            return secret.getBytes(UTF_8);
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
        // Required parameters.
        private String appIdParam = "appId";
        // Note: It is only used to concatenate plain-text string salts when
        // hashing signatures. (not required as a request parameter)
        private String appSecretParam = "appSecret";
        private String timestampParam = "timestamp";
        // Signature parameters.
        private String signParam = "sign";
        private SignAlgorithmType signAlgorithm = SignAlgorithmType.S256;
        private SignHashingModeType signHashingMode = SignHashingModeType.SimpleParamsBytesSorted;
        private List<String> signHashingIncludeParams = new ArrayList<>(4);
        private List<String> signHashingExcludeParams = new ArrayList<>(4);
        //
        // Temporary fields.
        //
        private transient Boolean isIncludeAll;

        public boolean isIncludeAll() {
            if (nonNull(isIncludeAll)) {
                return isIncludeAll;
            }
            return (isIncludeAll = safeList(getSignHashingIncludeParams()).stream().anyMatch(n -> eqIgnCase("*", n)));
        }
    }

    @SuppressWarnings("deprecation")
    @Getter
    public static enum SignAlgorithmType {
        MD5(input -> Hashing.md5().hashBytes(input[1]).asBytes()),

        S1(input -> Hashing.sha1().hashBytes(input[1]).asBytes()),

        S256(input -> Hashing.sha256().hashBytes(input[1]).asBytes()),

        S384(input -> Hashing.sha384().hashBytes(input[1]).asBytes()),

        S512(input -> Hashing.sha512().hashBytes(input[1]).asBytes()),

        HMD5(input -> Hashing.hmacMd5(input[0]).hashBytes(input[1]).asBytes()),

        HS1(input -> Hashing.hmacSha1(input[0]).hashBytes(input[1]).asBytes()),

        HS256(input -> Hashing.hmacSha256(input[0]).hashBytes(input[1]).asBytes()),

        HS512(input -> Hashing.hmacSha512(input[0]).hashBytes(input[1]).asBytes());

        private final Function<byte[][], byte[]> function;

        private SignAlgorithmType(Function<byte[][], byte[]> function) {
            this.function = function;
        }
    }

    @Getter
    public static enum SignHashingModeType {

        @Deprecated
        SimpleParamsBytesSorted(args -> {
            Config config = (Config) args[0];
            String storedAppSecret = (String) args[1];
            ServerHttpRequest request = (ServerHttpRequest) args[2];
            Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();
            String[] hashingParams = getHashingParamNames(config, queryParams);
            StringBuffer signPlaintext = new StringBuffer();
            for (Object key : hashingParams) {
                if (!config.getSignParam().equals(key)) {
                    signPlaintext.append(queryParams.get(key));
                }
            }
            // Add stored secret.
            signPlaintext.append(storedAppSecret);
            // ASCII sort characters.
            byte[] signPlainBytes = signPlaintext.toString().getBytes(UTF_8);
            Arrays.sort(signPlainBytes);
            return signPlainBytes;
        }),

        UriParamsKeySorted(args -> {
            Config config = (Config) args[0];
            String storedAppSecret = (String) args[1];
            ServerHttpRequest request = (ServerHttpRequest) args[2];
            Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();
            String[] hashingParams = getHashingParamNames(config, queryParams);
            // ASCII sort by parameters key.
            Arrays.sort(hashingParams);
            StringBuffer signPlaintext = new StringBuffer();
            for (Object key : hashingParams) {
                if (!config.getSignParam().equals(key)) {
                    signPlaintext.append(key).append("=").append(queryParams.get(key)).append("&");
                }
            }
            // Add stored secret.
            signPlaintext.append(config.getAppSecretParam()).append("=").append(storedAppSecret);
            return signPlaintext.toString().getBytes(UTF_8);
        });

        private final Function<Object[], byte[]> function;

        private SignHashingModeType(Function<Object[], byte[]> function) {
            this.function = function;
        }

        private static String[] getHashingParamNames(Config config, Map<String, String> queryParams) {
            String[] hashingParamNames = queryParams.keySet()
                    .stream()
                    .filter(n -> config.isIncludeAll() || safeList(config.getSignHashingIncludeParams()).contains(n))
                    .filter(n -> !safeList(config.getSignHashingExcludeParams()).contains(n))
                    .collect(toList())
                    .toArray(new String[0]);
            return hashingParamNames;
        }
    }

    // private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";
    public static final String SIGN_TOKEN_AUTH_FILTER = "SignTokenAuthing";
    public static final String SIGN_TOKEN_AUTH_CLIENT = "X-Sign-Token-AppId";

}