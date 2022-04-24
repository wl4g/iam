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
package com.wl4g.iam.gateway.logging;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static org.springframework.http.MediaType.APPLICATION_ATOM_XML;
import static org.springframework.http.MediaType.APPLICATION_CBOR;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML;
import static org.springframework.http.MediaType.APPLICATION_RSS_XML;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_MARKDOWN;
import static org.springframework.http.MediaType.TEXT_PLAIN;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.logging.config.DyeingLoggingProperties;
import com.wl4g.iam.gateway.trace.config.TraceProperties;
import com.wl4g.infra.common.lang.TypeConverts;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.core.constant.CoreInfraConstants;
import com.wl4g.infra.core.web.matcher.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

import reactor.core.publisher.Mono;

/**
 * {@link AbstractDyeingLoggingFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
public abstract class AbstractDyeingLoggingFilter implements GlobalFilter, Ordered {

    protected final SmartLogger log = getLogger(getClass());
    protected final TraceProperties traceConfig;
    protected final DyeingLoggingProperties loggingConfig;
    protected final SpelRequestMatcher requestMatcher;

    public AbstractDyeingLoggingFilter(TraceProperties traceConfig, DyeingLoggingProperties loggingConfig) {
        this.traceConfig = notNullOf(traceConfig, "traceConfig");
        this.loggingConfig = notNullOf(loggingConfig, "loggingConfig");
        this.requestMatcher = new SpelRequestMatcher(loggingConfig.getPreferrdMatchRuleDefinitions());
    }

    /**
     * @see {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#loadFilters()}
     */
    @Override
    public int getOrder() {
        return ORDER_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(KEY_START_TIME, currentTimeMillis());
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        // Check if filtering flight logging is enabled.
        if (!isFilterLogging(request, headers)) {
            return chain.filter(exchange);
        }
        // Determine dyeing logs level.
        int verboseLevel = determineRequestVerboseLevel(exchange);
        if (verboseLevel <= 0) { // is disabled?
            return chain.filter(exchange);
        }
        String traceId = headers.getFirst(CoreInfraConstants.TRACE_REQUEST_ID_HEADER_NAME);
        String requestMethod = request.getMethodValue();
        String requestUri = request.getURI().getRawPath();

        // Sets the state of the dyed log request to notify the back-end
        // services to print the log for the current request.
        headers.set(loggingConfig.getSetDyeingLogStateRequestHeader(), traceId);

        return doFilterInternal(exchange, chain, headers, traceId, requestMethod, requestUri);
    }

    protected abstract Mono<Void> doFilterInternal(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod,
            String requestUri);

    /**
     * Check if filtering is required and logging logs.
     * 
     * @param request
     * @param headers
     * @return
     */
    protected boolean isFilterLogging(ServerHttpRequest request, HttpHeaders headers) {
        return loggingConfig.isEnabled()
                && requestMatcher.matches(new ReactiveRequestExtractor(request), loggingConfig.getPreferredOpenMatchExpression());
    }

    protected int determineRequestVerboseLevel(ServerWebExchange exchange) {
        Integer requestVerboseLevel = TypeConverts
                .parseIntOrNull(exchange.getRequest().getHeaders().getFirst(loggingConfig.getVerboseLevelRequestHeader()));
        int verboseLevel = isNull(requestVerboseLevel) ? loggingConfig.getDefaultVerboseLevel() : requestVerboseLevel;
        exchange.getAttributes().put(KEY_VERBOSE_LEVEL, verboseLevel);
        return verboseLevel;
    }

    /**
     * Check if the specified flight log level range is met.
     * 
     * @param exchange
     * @param lower
     * @param upper
     * @return
     */
    protected boolean isLoglevelRange(ServerWebExchange exchange, int lower, int upper) {
        int verboseLevel = exchange.getAttribute(KEY_VERBOSE_LEVEL);
        return verboseLevel >= lower && verboseLevel <= upper;
    }

    /**
     * Check if the media type of the request or response has a body.
     * 
     * @param mediaType
     * @return
     */
    protected boolean hasBody(MediaType mediaType) {
        if (isNull(mediaType)) {
            return false;
        }
        for (MediaType media : HAS_BODY_MEDIA_TYPES) {
            if (media.isCompatibleWith(mediaType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logging for generic HTTP headers.
     */
    public static final List<String> LOG_GENERIC_HEADERS = unmodifiableList(new ArrayList<String>() {
        private static final long serialVersionUID = 1616772712967733180L;
        {
            // Standard
            add(HttpHeaders.CONTENT_TYPE);
            add(HttpHeaders.CONTENT_ENCODING);
            add(HttpHeaders.CONTENT_LENGTH);
            add(HttpHeaders.CONTENT_RANGE);
            add(HttpHeaders.CONTENT_DISPOSITION);
            add(HttpHeaders.CONNECTION);
            add(HttpHeaders.CACHE_CONTROL);
            add(HttpHeaders.COOKIE);
            add(HttpHeaders.ACCEPT);
            add(HttpHeaders.ACCEPT_ENCODING);
            add(HttpHeaders.ACCEPT_LANGUAGE);
            add(HttpHeaders.REFERER);
            add(HttpHeaders.USER_AGENT);
            add(HttpHeaders.LOCATION);
            add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
            add(HttpHeaders.SERVER);
            add(HttpHeaders.DATE);
            add(HttpHeaders.UPGRADE);
            // Extension
            add("Content-MD5");
            add("Upgrade-Insecure-Requests");
        }
    });

    /**
     * The content-type definition of the request or corresponding body needs to
     * be recorded.
     */
    public static final List<MediaType> HAS_BODY_MEDIA_TYPES = unmodifiableList(new ArrayList<MediaType>() {
        private static final long serialVersionUID = 1616772712967733180L;
        {
            add(APPLICATION_JSON);
            add(TEXT_HTML);
            add(TEXT_PLAIN);
            add(TEXT_MARKDOWN);
            add(APPLICATION_FORM_URLENCODED);
            add(APPLICATION_XML);
            add(APPLICATION_ATOM_XML);
            add(APPLICATION_PROBLEM_XML);
            add(APPLICATION_CBOR);
            add(APPLICATION_RSS_XML);
        }
    });

    public static final String LOG_REQUEST_BEGIN = "\n--- <IAM Gateway Request> -----\n:: Headers ::\n";
    public static final String LOG_REQUEST_BODY = ":: Body    ::\n{}";
    public static final String LOG_REQUEST_END = "\n------------------------------\n";
    public static final String LOG_RESPONSE_BEGIN = "\n--- <IAM Gateway LimitedResult> ---\n:: Headers ::\n";
    public static final String LOG_RESPONSE_BODY = ":: Body    ::\n{}";
    public static final String LOG_RESPONSE_END = "\n------------------------------\n";
    public static final String KEY_START_TIME = AbstractDyeingLoggingFilter.class.getName() + ".startTime";
    public static final String KEY_VERBOSE_LEVEL = AbstractDyeingLoggingFilter.class.getName() + ".verboseLevel";
    public static final int ORDER_FILTER = Ordered.HIGHEST_PRECEDENCE + 20;

}
