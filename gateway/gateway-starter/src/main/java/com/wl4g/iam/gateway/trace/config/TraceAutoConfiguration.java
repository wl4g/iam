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
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.gateway.trace.SimpleTraceFilter;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

/**
 * {@link TraceAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
public class TraceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_IAM_GATEWAY_TRACE)
    public TraceProperties traceProperties() {
        return new TraceProperties();
    }

//    @Bean
//    public SimpleTraceFilter simpleTraceFilter() {
//        return new SimpleTraceFilter();
//    }
//
//    // @Bean
//    // public OpentelemetryFilter opentelemetryFilter() {
//    // return new OpentelemetryFilter();
//    // }
//
//    @Bean
//    @ConditionalOnClass(OtlpGrpcSpanExporterBuilder.class)
//    // TODO
//    @ConditionalOnExpression("'otlp'.equalsIgnoreCase('${aaa.bbb.cc:" + DEFAULT_EXPORTER + "}')")
//    public SpanProcessor otlpProcessor(TraceProperties config) {
//        OtlpGrpcSpanExporterBuilder builder = OtlpGrpcSpanExporter.builder()
//                .setEndpoint(config.getOtel().getEndpoint())
//                .setTimeout(config.getOtel().getTimeout(), TimeUnit.MILLISECONDS);
//        safeMap(config.getOtel().getHeaders()).forEach((key, value) -> builder.addHeader(key, value));
//        return batchSpanProcessor(config, builder.build());
//    }
//
//    @Bean
//    @ConditionalOnClass(JaegerGrpcSpanExporter.class)
//    // TODO
//    @ConditionalOnExpression("'otlp'.equalsIgnoreCase('${aaa.bbb.cc:" + DEFAULT_EXPORTER + "}')")
//    public SpanProcessor jaegerProcessor(TraceProperties config) {
//        JaegerGrpcSpanExporter spanExporter = JaegerGrpcSpanExporter.builder()
//                .setEndpoint(config.getJaeger().getEndpoint())
//                .build();
//        return batchSpanProcessor(config, spanExporter);
//    }
//
//    @Bean
//    @ConditionalOnClass(ZipkinSpanExporter.class)
//    // TODO
//    @ConditionalOnExpression("'otlp'.equalsIgnoreCase('${aaa.bbb.cc:" + DEFAULT_EXPORTER + "}')")
//    public SpanProcessor zipkinProcessor(TraceProperties config) {
//        ZipkinSpanExporter spanExporter = ZipkinSpanExporter.builder().setEndpoint(config.getJaeger().getEndpoint()).build();
//        return batchSpanProcessor(config, spanExporter);
//    }
//
//    @Bean
//    public OpenTelemetry openTelemetry(TraceProperties config, SpanProcessor spanProcessor) {
//        Resource serviceNameResource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "TODO"));
//
//        // Sets to process the spans by the Zipkin/Jaeger Exporter
//        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
//                .addSpanProcessor(spanProcessor)
//                .setResource(Resource.getDefault().merge(serviceNameResource))
//                .build();
//
//        // Add a shutdown hook to shut down the SDK
//        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));
//
//        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
//                .setTracerProvider(tracerProvider)
//                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
//                .buildAndRegisterGlobal();
//
//        // return the configured instance so it can be used for instrumentation.
//        return openTelemetry;
//    }
//
//    private BatchSpanProcessor batchSpanProcessor(TraceProperties config, SpanExporter spanExporter) {
//        // or:io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
//        return BatchSpanProcessor.builder(spanExporter)
//                .setScheduleDelay(config.getBsp().getScheduleDelay(), TimeUnit.MILLISECONDS)
//                .setExporterTimeout(config.getBsp().getExportTimeout(), TimeUnit.MILLISECONDS)
//                .setMaxExportBatchSize(config.getBsp().getMaxExportBatchSize())
//                .setMaxQueueSize(config.getBsp().getMaxQueueSize())
//                .build();
//    }

    public static final String DEFAULT_EXPORTER = "jaeger";

}
