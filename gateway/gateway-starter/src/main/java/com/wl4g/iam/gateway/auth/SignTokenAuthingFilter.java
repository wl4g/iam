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
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.security.MessageDigest.isEqual;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
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
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
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
            if (JvmRuntimeTool.isJvmInDebugging && authingConfig.getSignToken().isIgnoredAuthingInJvmDebug()) {
                return chain.filter(exchange);
            }

            // Gets parameter signature. (required)
            String sign = null;
            try {
                sign = hasText(exchange.getRequest().getQueryParams().getFirst(config.getSignParam()), "%s missing",
                        config.getSignParam());
            } catch (IllegalArgumentException e) {
                log.warn("Bad request missing signature. - {}", exchange.getRequest().getURI());
                return writeResponse(HttpStatus.BAD_REQUEST, exchange, "bad_request - hint '%s'", e.getMessage());
            }
            String appId = getRequestAppId(config, exchange);

            // Check replay.
            if (signReplayValidityStore.asMap().containsKey(sign)) {
                log.warn("Illegal signature locked. - sign={}, appId={}", sign, appId);
                return writeResponse(HttpStatus.LOCKED, exchange, "illegal_signature");
            }

            // Verify signature
            try {
                byte[] signBytes = doSignature(config, exchange, appId, sign);
                if (!isEqual(signBytes, Hex.decodeHex(sign.toCharArray()))) {
                    log.warn("Invalid request sign='{}', sign='{}'", new String(sign), signBytes);
                    return writeResponse(HttpStatus.UNAUTHORIZED, exchange, "invalid_signature");
                }
                signReplayValidityStore.put(sign, appId);
            } catch (DecoderException e) {
                return writeResponse(HttpStatus.INTERNAL_SERVER_ERROR, exchange, "unavailable");
            } catch (IllegalArgumentException e) {
                return writeResponse(HttpStatus.BAD_REQUEST, exchange, "invalid_signature - hint '%s'", e.getMessage());
            }

            // Add the current authenticated client ID to the request header,
            // this will allow the back-end resource services to recognize the
            // current client ID.
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header(authingConfig.getSignToken().getAddSignAuthClientIdHeader(), appId)
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    private byte[] doSignature(SignTokenAuthingFilter.Config config, ServerWebExchange exchange, String appId, String sign) {
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
        switch (authingConfig.getSignToken().getSecretLoadStore()) {
        case ENV:
            String storedSecret = getenv(loadKey);
            if (isBlank(storedSecret)) {
                log.warn("No found storedSecret from environment via '{}'", loadKey);
            }
            return hasText(storedSecret, "No enables application secret?");
        case REDIS:
            storedSecret = secretCacheStore.asMap().get(loadKey);
            if (isBlank(storedSecret)) {
                synchronized (loadKey) {
                    storedSecret = secretCacheStore.asMap().get(loadKey);
                    if (isBlank(storedSecret)) {
                        storedSecret = stringTemplate.opsForValue().get(loadKey);
                        if (isBlank(storedSecret)) {
                            log.warn("No found storedSecret from environment via '{}'", loadKey);
                            throw new IllegalArgumentException(format("No enables application secret?"));
                        }
                        secretCacheStore.asMap().put(loadKey, storedSecret);
                        return storedSecret.getBytes(UTF_8);
                    }
                }
            }
            return storedSecret.getBytes(UTF_8);
        default:
            throw new Error("Shouldn't be here");
        }
    }

    private String getRequestAppId(SignTokenAuthingFilter.Config config, ServerWebExchange exchange) {
        // Note: In some special business platform
        // scenarios, the signature authentication protocol may not define
        // appId (such as Alibaba Cloud Market SaaS product authentication
        // API), then the uniqueness of the client application can only be
        // determined according to the request route ID.
        return config.getAppIdExtractMode().getFunction().apply(new Object[] { config, exchange });
    }

    private Mono<Void> writeResponse(HttpStatus status, ServerWebExchange exchange, String fmtMessage, Object... args) {
        RespBase<?> resp = RespBase.create().withCode(status.value()).withMessage(format(fmtMessage, args));
        ServerHttpResponse response = exchange.getResponse();
        DataBuffer buffer = response.bufferFactory().wrap(resp.asJson().getBytes(UTF_8));
        response.setStatusCode(status);
        return response.writeWith(just(buffer));
    }

    @Getter
    @Setter
    @ToString
    public static class Config {
        // AppId parameter extract configuration.
        private AppIdExtractMode appIdExtractMode = AppIdExtractMode.Parameter;
        // Only valid when appId extract mode is parameter.
        private String appIdParam = "appId";
        // Note: It is only used to concatenate plain-text string salts when
        // hashing signatures. (not required as a request parameter)
        private String secretParam = "appSecret";
        // Signature parameters.
        private String signParam = "sign";
        private SignAlgorithm signAlgorithm = SignAlgorithm.S256;
        private SignHashingMode signHashingMode = SignHashingMode.SimpleParamsBytesSortedHashing;
        private List<String> signHashingIncludeParams = new ArrayList<>(4);
        private List<String> signHashingExcludeParams = new ArrayList<>(4);
        private List<String> signHashingRequiredIncludeParams = new ArrayList<>(4);
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

    @Getter
    public static enum AppIdExtractMode {

        Parameter(args -> {
            Config config = (Config) args[0];
            ServerWebExchange exchange = (ServerWebExchange) args[1];
            return hasText(exchange.getRequest().getQueryParams().getFirst(config.getAppIdParam()), "%s missing",
                    config.getAppIdParam());
        }),

        /**
         * In some special business platform scenarios, the signature
         * authentication protocol may not define appId (such as Alibaba Cloud
         * Market SaaS product authentication API), then the uniqueness of the
         * client application can only be determined according to the request
         * route ID.
         */
        @SuppressWarnings("unused")
        RouteId(args -> {
            Config config = (Config) args[0];
            ServerWebExchange exchange = (ServerWebExchange) args[1];
            return ((Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR)).getId();
        });

        private final Function<Object[], String> function;

        private AppIdExtractMode(Function<Object[], String> function) {
            this.function = function;
        }
    }

    @SuppressWarnings("deprecation")
    @Getter
    public static enum SignAlgorithm {
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

        private SignAlgorithm(Function<byte[][], byte[]> function) {
            this.function = function;
        }
    }

    @Getter
    public static enum SignHashingMode {

        @Deprecated
        SimpleParamsBytesSortedHashing(args -> {
            Config config = (Config) args[0];
            byte[] storedAppSecret = (byte[]) args[1];
            ServerHttpRequest request = (ServerHttpRequest) args[2];
            Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();
            String[] params = getEffectiveHashingParamNames(config, queryParams);
            StringBuffer signPlaintext = new StringBuffer();
            for (Object key : params) {
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

        UriParamsKeySortedHashing(args -> {
            Config config = (Config) args[0];
            byte[] storedAppSecret = (byte[]) args[1];
            ServerHttpRequest request = (ServerHttpRequest) args[2];
            Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();
            String[] params = getEffectiveHashingParamNames(config, queryParams);
            // ASCII sort by parameters key.
            Arrays.sort(params);
            StringBuffer signPlaintext = new StringBuffer();
            for (Object name : params) {
                if (!config.getSignParam().equals(name)) {
                    signPlaintext.append(name).append("=").append(queryParams.get(name)).append("&");
                }
            }
            // Add stored secret.
            signPlaintext.append(config.getSecretParam()).append("=").append(new String(storedAppSecret, UTF_8));
            return signPlaintext.toString().getBytes(UTF_8);
        });

        private final Function<Object[], byte[]> function;

        private SignHashingMode(Function<Object[], byte[]> function) {
            this.function = function;
        }

        private static String[] getEffectiveHashingParamNames(Config config, Map<String, String> queryParams) {
            List<String> hashingParamNames = queryParams.keySet()
                    .stream()
                    .filter(n -> config.isIncludeAll() || safeList(config.getSignHashingIncludeParams()).contains(n))
                    .filter(n -> !safeList(config.getSignHashingExcludeParams()).contains(n))
                    .collect(toList());

            // Validation required parameters.
            boolean allMatch = safeList(config.getSignHashingRequiredIncludeParams()).stream()
                    .allMatch(p -> hashingParamNames.contains(p));
            if (!allMatch) {
                throw new IllegalArgumentException(format("Parameters missing, These parameters are required: %s",
                        config.getSignHashingRequiredIncludeParams()));
            }
            return hashingParamNames.toArray(new String[0]);
        }
    }

    public static final String SIGN_TOKEN_AUTH_FILTER = "SignTokenAuthing";

}