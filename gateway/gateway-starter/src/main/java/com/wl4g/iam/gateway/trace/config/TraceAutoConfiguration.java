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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.gateway.trace.SimpleTraceFilter;

;

//import java.util.concurrent.TimeUnit;
//import com.wl4g.iam.gateway.trace.OpentelemetryGlobalFilter;
//import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
//import io.opentelemetry.sdk.trace.SpanProcessor;
//import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

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

    @Bean
    public SimpleTraceFilter simpleTraceFilter() {
        return new SimpleTraceFilter();
    }

    // @Bean
    // public OpentelemetryGlobalFilter opentelemetryGlobalFilter() {
    // return new OpentelemetryGlobalFilter();
    // }
    //
    // private SpanProcessor getOtlpProcessor() {
    // OtlpGrpcSpanExporter spanExporter =
    // OtlpGrpcSpanExporter.builder().setTimeout(2, TimeUnit.SECONDS).build();
    // return BatchSpanProcessor.builder(spanExporter).setScheduleDelay(100,
    // TimeUnit.MILLISECONDS).build();
    // }

}
