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
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
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
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.expression.EvaluationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.base.Predicates;
import com.wl4g.iam.gateway.logging.config.LoggingProperties;
import com.wl4g.iam.gateway.util.IamGatewayUtil.SafeFilterOrdered;
import com.wl4g.infra.common.lang.TypeConverts;
import com.wl4g.infra.common.web.WebUtils.WebRequestExtractor;
import com.wl4g.infra.core.constant.CoreInfraConstants;
import com.wl4g.infra.core.logging.LoggingMessageUtil;
import com.wl4g.infra.core.logging.reactive.BaseLoggingWebFilter;
import com.wl4g.infra.core.utils.web.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link BasedLoggingGlobalFilter}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public abstract class BasedLoggingGlobalFilter implements GlobalFilter, Ordered {

    protected final LoggingProperties loggingConfig;
    protected final SpelRequestMatcher requestMatcher;

    public BasedLoggingGlobalFilter(LoggingProperties loggingConfig) {
        this.loggingConfig = notNullOf(loggingConfig, "loggingConfig");
        // Build gray request matcher.
        this.requestMatcher = new SpelRequestMatcher(loggingConfig.getPreferMatchRuleDefinitions());
        // Check expression.
        try {
            // Pre-compile and warm up to evaluate checks at service startup,
            // and avoid performance issues at runtime for unexpected errors,
            // such as throwing stack information when evaluating exceptions.
            this.requestMatcher.matches(new WebRequestExtractor() {
            }, loggingConfig.getPreferOpenMatchExpression());
        } catch (EvaluationException e) {
            throw new EvaluationException(format("Invalid evaluation expression for reason: %s", e.getMessage()), e);
        }
    }

    /**
     * @see {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#loadFilters()}
     */
    @Override
    public int getOrder() {
        return SafeFilterOrdered.ORDER_LOGGING;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Check if filtering flight logging is enabled.
        if (!isLoggingRequest(exchange)) {
            if (log.isDebugEnabled()) {
                ServerHttpRequest request = exchange.getRequest();
                log.debug("Not to meet the conditional rule to enable logging. - uri: {}, headers: {}, queryParams: {}",
                        request.getURI(), request.getHeaders(), request.getQueryParams());
            }
            return chain.filter(exchange);
        }

        final long beginTime = currentTimeMillis();
        exchange.getAttributes().put(KEY_START_TIME, beginTime);
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        // Determine dyeing logs level.
        int verboseLevel = determineRequestVerboseLevel(exchange);
        if (verboseLevel <= 0) { // is disabled?
            return chain.filter(exchange);
        }
        String traceId = headers.getFirst(CoreInfraConstants.TRACE_REQUEST_ID_HEADER);
        String requestMethod = request.getMethodValue();

        // Sets the state of the dyed log request to notify the back-end
        // services to print the log for the current request.
        request.mutate().header(loggingConfig.getDyeingLogStateRequestHeader(), traceId).build();

        return doFilterInternal(exchange, chain, headers, traceId, requestMethod);
    }

    protected abstract Mono<Void> doFilterInternal(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod);

    /**
     * Check if enable print logs needs to be filtered
     * 
     * @param exchange
     * @return
     */
    protected boolean isLoggingRequest(ServerWebExchange exchange) {
        if (!loggingConfig.isEnabled()) {
            return false;
        }
        // Gets current request route.
        Route route = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

        // Add routeId temporary predicates.
        Map<String, Supplier<Predicate<String>>> routeIdPredicateSupplier = singletonMap(VAR_ROUTE_ID,
                () -> (Predicate<String>) Predicates.equalTo(route.getId()));

        try {
            // Check if printing the log of the current request is enabled based
            // on the current request parameters.
            return (requestMatcher.matches(new ReactiveRequestExtractor(exchange.getRequest()),
                    loggingConfig.getPreferOpenMatchExpression(), routeIdPredicateSupplier))
                    || (determineRequestVerboseLevel(exchange) > 10
                            && requestMatcher.matches(new ReactiveRequestExtractor(exchange.getRequest()),
                                    exchange.getRequest().getHeaders().getFirst(loggingConfig.getDyeingLogStateRequestHeader()),
                                    routeIdPredicateSupplier));
        } catch (Exception e) {
            log.warn("Evaluating failed, logging disabled by default, reason: %s", e.getMessage());
            return false;
        }
    }

    /**
     * Determine request verbose logging level.
     * 
     * @param exchange
     * @return
     */
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
    protected boolean isCompatibleWithPlainBody(MediaType mediaType) {
        return LoggingMessageUtil.isCompatibleWithPlainBody(mediaType);
    }

    /**
     * Check if the media type is binary, i.e. file upload.
     * 
     * @param mediaType
     * @return
     */
    protected boolean isUploadStreamMedia(MediaType mediaType) {
        return LoggingMessageUtil.isUploadStreamMedia(mediaType);
    }

    /**
     * Check if the media type is binary, i.e. file download.
     * 
     * @param mediaType
     * @return
     */
    protected boolean isDownloadStreamMedia(MediaType mediaType) {
        return LoggingMessageUtil.isDownloadStreamMedia(mediaType);
    }

    /**
     * Reading to logging characters from request body stream segment or
     * response body stream segment.
     * 
     * @param bodySegment
     * @param expectMaxLen
     * @return
     */
    protected String readToLogString(byte[] bodySegment, int expectMaxLen) {
        return BaseLoggingWebFilter.readToLogString(bodySegment, expectMaxLen);
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

    public static final String LOG_REQUEST_BEGIN = LINE_SEPARATOR + "--- <Iscg Request> -------" + LINE_SEPARATOR;
    public static final String LOG_REQUEST_BODY = LINE_SEPARATOR + "\\r\\n" + LINE_SEPARATOR + "{}";
    public static final String LOG_REQUEST_END = LINE_SEPARATOR + "EOF" + LINE_SEPARATOR;
    public static final String LOG_RESPONSE_BEGIN = LINE_SEPARATOR + "--- <Iscg Response> ------" + LINE_SEPARATOR;
    public static final String LOG_RESPONSE_BODY = LINE_SEPARATOR + "\\r\\n" + LINE_SEPARATOR + "{}";
    public static final String LOG_RESPONSE_END = LINE_SEPARATOR + "EOF" + LINE_SEPARATOR;
    public static final String VAR_ROUTE_ID = "routeId";
    public static final String KEY_START_TIME = BasedLoggingGlobalFilter.class.getName() + ".startTime";
    public static final String KEY_VERBOSE_LEVEL = BasedLoggingGlobalFilter.class.getName() + ".verboseLevel";
    public static final String KEY_LOG_RECORD = BasedLoggingGlobalFilter.class.getName() + ".logRecord";

}
