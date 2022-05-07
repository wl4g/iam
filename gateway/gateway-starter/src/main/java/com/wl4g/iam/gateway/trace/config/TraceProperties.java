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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;

import com.wl4g.iam.gateway.logging.config.DyeingLoggingProperties;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link DyeingLoggingProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
@Validated
public class TraceProperties {

    private boolean enabled = true;

    /**
     * Preferred to enable tracing samples match SPEL match expression. Default
     * by '${false}', which means never no match. </br>
     * </br>
     * Tip: The built-in support to get the current routeId, such as:
     * '#{routeId.get().test($request)}'
     */
    private String preferOpenMatchExpression = "#{true}";

    /**
     * Preferred to enable tracing samples match rule definitions.
     */
    private List<MatchHttpRequestRule> preferMatchRuleDefinitions = new ArrayList<>();

    @Value("${spring.application.name:iam-gateway}")
    private @NotBlank String serviceName;

    private ExporterProperties exporter = new ExporterProperties();

    private BSPProperties bsp = new BSPProperties();

    private SpanLimitsProperties spanLimits = new SpanLimitsProperties();

    private SamplerProperties sampler = new SamplerProperties();

    @Getter
    @Setter
    @ToString
    @Validated
    public static class ExporterProperties {
        private OtlpExporterProperties otlp = new OtlpExporterProperties();
        private JaegerExporterProperties jaeger = new JaegerExporterProperties();
        private ZipkinExporterProperties zipkin = new ZipkinExporterProperties();
    }

    @Getter
    @Setter
    @ToString
    @Validated
    public static class OtlpExporterProperties {

        /**
         * Otlp receiver server base URI.
         */
        private @NotBlank String endpoint = "http://localhost:4317";

        /**
         * The maximum waiting time, in milliseconds, allowed to send each
         * batch. Default is 10000.
         */
        private long timeoutMs = 10000;

        /**
         * Span data addition request headers.
         */
        private Map<String, String> headers = new HashMap<>();

    }

    /**
     * @see https://www.jaegertracing.io/docs/1.21/client-libraries/#propagation-format
     */
    @Getter
    @Setter
    @ToString
    @Validated
    public static class JaegerExporterProperties {

        /**
         * Jaeger receiver server base URI.
         */
        private @NotBlank String endpoint = "http://localhost:14250";
    }

    @Getter
    @Setter
    @ToString
    @Validated
    public static class ZipkinExporterProperties {

        /**
         * Zipkin receiver server base URI.
         */
        private @NotBlank String endpoint = "http://localhost:9411/api/v2/spans";
    }

    /**
     * @see {@link io.opentelemetry.sdk.trace.export.BatchSpanProcessor}
     * @see https://github.com/open-telemetry/opentelemetry-java/tree/v1.3.0/sdk-extensions/autoconfigure#batch-span-processor
     */
    @Getter
    @Setter
    @ToString
    @Validated
    public static class BSPProperties {

        /**
         * The interval, in milliseconds, between two consecutive exports.
         * Default is 5000.
         */
        private long scheduleDelay = 5000;

        /**
         * The maximum queue size. Default is 2048.
         */
        private int maxQueueSize = 2048;

        /**
         * The maximum batch size. Default is 512.
         */
        private int maxExportBatchSize = 512;

        /**
         * The maximum allowed time, in milliseconds, to export data. Default is
         * 30000.
         */
        private long exportTimeout = 30000;
    }

    /**
     * @see https://github.com/open-telemetry/opentelemetry-java/tree/v1.3.0/sdk-extensions/autoconfigure#span-limits
     */
    @Getter
    @Setter
    @ToString
    @Validated
    public static class SpanLimitsProperties {

        /**
         * see: otlp.span.attribute.count.limit see:
         * {@link io.opentelemetry.sdk.trace.SpanLimitsBuilder#maxNumEvents}
         */
        private int maxNumEvents = 128;

        /**
         * see: otlp.span.link.count.limit see:
         * {@link io.opentelemetry.sdk.trace.SpanLimitsBuilder#maxNumLinks}
         */
        private int maxNumLinks = 128;

        /**
         * see: otlp.span.attribute.count.limit see:
         * {@link io.opentelemetry.sdk.trace.SpanLimitsBuilder#maxNumAttributes}
         */
        private int maxNumAttributes = 128;

        /**
         * see:
         * {@link io.opentelemetry.sdk.trace.SpanLimitsBuilder#maxNumAttributesPerEvent}
         */
        private int maxNumAttributesPerEvent = 128;

        /**
         * see:
         * {@link io.opentelemetry.sdk.trace.SpanLimitsBuilder#maxNumAttributesPerLink}
         */
        private int maxNumAttributesPerLink = 128;
    }

    /**
     * @see https://github.com/open-telemetry/opentelemetry-java/tree/v1.3.0/sdk-extensions/autoconfigure#sampler
     */
    @Getter
    @Setter
    @ToString
    @Validated
    public static class SamplerProperties {

        private SamplerType type = SamplerType.TRACEIDRATIO;

        /**
         * The traceId ratio based sampler ratio. range of [0.0-1.0]
         */
        private @Max(1) @Min(0) double radio = 1d;
    }

    /**
     * @see {@link io.opentelemetry.sdk.autoconfigure.TracerProviderConfiguration#configureSampler}
     */
    public static enum SamplerType {
        ALWAYS_ON, ALWAYS_OFF, TRACEIDRATIO, PARENTBASED_ALWAYS_ON, PARENTBASED_ALWAYS_OFF, PARENTBASED_TRACEIDRATIO
    }

}
