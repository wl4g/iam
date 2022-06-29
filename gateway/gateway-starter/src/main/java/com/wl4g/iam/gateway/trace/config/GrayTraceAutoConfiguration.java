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
package com.wl4g.iam.gateway.trace.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_TRACE;

import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.SamplerFunction;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.SleuthWebProperties;
import org.springframework.cloud.sleuth.http.HttpClientHandler;
import org.springframework.cloud.sleuth.http.HttpClientRequest;
import org.springframework.cloud.sleuth.http.HttpClientResponse;
import org.springframework.cloud.sleuth.http.HttpRequest;
import org.springframework.cloud.sleuth.http.HttpRequestParser;
import org.springframework.cloud.sleuth.http.HttpResponseParser;
import org.springframework.cloud.sleuth.http.HttpServerHandler;
import org.springframework.cloud.sleuth.http.HttpServerRequest;
import org.springframework.cloud.sleuth.http.HttpServerResponse;
import org.springframework.cloud.sleuth.instrument.web.HttpClientRequestParser;
import org.springframework.cloud.sleuth.instrument.web.HttpClientResponseParser;
import org.springframework.cloud.sleuth.instrument.web.HttpClientSampler;
import org.springframework.cloud.sleuth.instrument.web.HttpServerRequestParser;
import org.springframework.cloud.sleuth.instrument.web.HttpServerResponseParser;
import org.springframework.cloud.sleuth.instrument.web.SkipPatternProvider;
import org.springframework.cloud.sleuth.instrument.web.TraceHandlerAdapter;
import org.springframework.cloud.sleuth.otel.bridge.OtelHttpClientHandler;
import org.springframework.cloud.sleuth.otel.bridge.OtelHttpServerHandler;
import org.springframework.cloud.sleuth.otel.bridge.SkipPatternSampler;
import org.springframework.cloud.sleuth.otel.bridge.SpringHttpClientAttributesGetter;
import org.springframework.cloud.sleuth.otel.bridge.SpringHttpServerAttributesGetter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.function.server.support.HandlerFunctionAdapter;

import com.wl4g.iam.gateway.trace.GrayTraceWebFilter;

import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientAttributesGetter;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerAttributesGetter;

/**
 * {@link GrayTraceAutoConfiguration}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class GrayTraceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_IAM_GATEWAY_TRACE)
    public GrayTraceProperties grayTraceProperties() {
        return new GrayTraceProperties();
    }

    //
    // ----------------------------------------------------------------------
    // [Begin]
    // ADD enabled GrayTraceWebFilter,
    // To enable custom grayscale tracing, the purpose of the following
    // configuration is to disable the default automatic configuration of
    // TraceWebFilter
    // ----------------------------------------------------------------------
    //

    /**
     * @see {@link org.springframework.cloud.sleuth.autoconfig.instrument.web.TraceWebFluxConfiguration.traceFilter(Tracer,HttpServerHandler,CurrentTraceContext,SleuthWebProperties)}
     */
    @Bean
    public GrayTraceWebFilter grayTraceWebFilter(
            GrayTraceProperties grayTraceConfig,
            Tracer tracer,
            HttpServerHandler httpServerHandler,
            CurrentTraceContext currentTraceContext,
            SleuthWebProperties sleuthWebProperties) {
        GrayTraceWebFilter traceWebFilter = new GrayTraceWebFilter(grayTraceConfig, tracer, httpServerHandler,
                currentTraceContext);
        traceWebFilter.setOrder(sleuthWebProperties.getFilterOrder());
        return traceWebFilter;
    }

    @Bean
    static TraceHandlerFunctionAdapterBeanPostProcessor traceHandlerFunctionAdapterBeanPostProcessor(BeanFactory beanFactory) {
        return new TraceHandlerFunctionAdapterBeanPostProcessor(beanFactory);
    }

    static class TraceHandlerFunctionAdapterBeanPostProcessor implements BeanPostProcessor {
        private final BeanFactory beanFactory;

        TraceHandlerFunctionAdapterBeanPostProcessor(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof HandlerFunctionAdapter) {
                return new TraceHandlerAdapter((HandlerAdapter) bean, this.beanFactory);
            }
            return bean;
        }
    }

    /**
     * @see {@link org.springframework.cloud.sleuth.autoconfig.otel.OtelBridgeConfiguration.TraceOtelHttpBridgeConfiguration}
     */
    //
    // ADD conditional
    //
    @AutoConfigureBefore(GrayTraceAutoConfiguration.class)
    @Configuration(proxyBeanMethods = false)
    //
    // close conditional
    //
    // @ConditionalOnSleuthWeb
    @EnableConfigurationProperties(SleuthWebProperties.class)
    static class TraceOtelHttpBridgeConfiguration {

        @Bean
        @ConditionalOnMissingBean
        HttpClientHandler otelHttpClientHandler(
                io.opentelemetry.api.OpenTelemetry openTelemetry,
                @Nullable @HttpClientRequestParser HttpRequestParser httpClientRequestParser,
                @Nullable @HttpClientResponseParser HttpResponseParser httpClientResponseParser,
                SamplerFunction<HttpRequest> samplerFunction,
                HttpClientAttributesGetter<HttpClientRequest, HttpClientResponse> otelHttpAttributesGetter) {
            return new OtelHttpClientHandler(openTelemetry, httpClientRequestParser, httpClientResponseParser, samplerFunction,
                    otelHttpAttributesGetter);
        }

        @Bean
        @ConditionalOnMissingBean
        HttpClientAttributesGetter<HttpClientRequest, HttpClientResponse> otelHttpClientAttributesGetter() {
            return new SpringHttpClientAttributesGetter();
        }

        @Bean
        @ConditionalOnMissingBean
        HttpServerHandler otelHttpServerHandler(
                io.opentelemetry.api.OpenTelemetry openTelemetry,
                @Nullable @HttpServerRequestParser HttpRequestParser httpServerRequestParser,
                @Nullable @HttpServerResponseParser HttpResponseParser httpServerResponseParser,
                ObjectProvider<SkipPatternProvider> skipPatternProvider,
                HttpServerAttributesGetter<HttpServerRequest, HttpServerResponse> otelHttpAttributesGetter) {
            return new OtelHttpServerHandler(openTelemetry, httpServerRequestParser, httpServerResponseParser,
                    skipPatternProvider.getIfAvailable(() -> () -> Pattern.compile("")), otelHttpAttributesGetter);
        }

        @Bean
        @ConditionalOnMissingBean
        HttpServerAttributesGetter<HttpServerRequest, HttpServerResponse> otelHttpServerAttributesGetter() {
            return new SpringHttpServerAttributesGetter();
        }

        @Bean
        @ConditionalOnMissingBean(name = HttpClientSampler.NAME)
        SamplerFunction<HttpRequest> defaultHttpClientSampler(SleuthWebProperties sleuthWebProperties) {
            String skipPattern = sleuthWebProperties.getClient().getSkipPattern();
            if (skipPattern == null) {
                return SamplerFunction.deferDecision();
            }
            return new SkipPatternSampler(Pattern.compile(skipPattern));
        }
    }

    //
    // ----------------------------------------------------------------------
    // [End]
    // ADD enabled GrayTraceWebFilter,
    // To enable custom grayscale tracing, the purpose of the following
    // configuration is to disable the default automatic configuration of
    // TraceWebFilter
    // ----------------------------------------------------------------------
    //

}
