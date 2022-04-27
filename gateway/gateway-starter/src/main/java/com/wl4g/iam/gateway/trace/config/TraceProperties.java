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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.validation.annotation.Validated;

import com.wl4g.iam.gateway.logging.config.DyeingLoggingProperties;

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

    private ZipkinProperties zipkin = new ZipkinProperties();

    private JaegerProperties jaeger = new JaegerProperties();

    private OtlpProperties otel = new OtlpProperties();

    /**
     * BatchSpanProcessor configuration properties.s
     */
    private BSPProperties bsp = new BSPProperties();

    @Getter
    @Setter
    @ToString
    @Validated
    public static class OtlpProperties {

        /**
         * Otlp receiver server base URI.
         */
        private String endpoint = "http://localhost:4317";

        /**
         * The maximum waiting time, in milliseconds, allowed to send each
         * batch. Default is 10000.
         */
        private long timeout = 10000;

        /**
         * Span data addition request headers.
         */
        private Map<String, String> headers = new HashMap<>();

        // TODO Confirm whether to use otel system property or need to customize
        // properties configuration??

        /**
         * Otel system properties.
         * 
         * see:https://github.com/open-telemetry/opentelemetry-java/tree/v1.3.0/sdk-extensions/autoconfigure#sampler
         * see:https://github.com/open-telemetry/opentelemetry-java/tree/v1.3.0/sdk-extensions/autoconfigure#span-limits
         * see:https://github.com/open-telemetry/opentelemetry-java/tree/v1.3.0/sdk-extensions/autoconfigure#batch-span-processor
         */
        private Properties props = new Properties() {
            private static final long serialVersionUID = -3373063731653517759L;
            {
                put("otel.resource.attributes", "service.name=cn-south1-a1-iam-gateway,service.namespace=iam-gateway");
                // zipkin|jaeger
                put("otel.traces.exporter", "jaeger");
                // https://github.com/open-telemetry/opentelemetry-java/tree/v1.3.0/sdk-extensions/autoconfigure#sampler
                // always_on|always_off|traceidratio|parentbased_always_on|parentbased_always_off|parentbased_traceidratio
                put("otel.traces.sampler", "parentbased_always_on");
                put("otel.traces.sampler.arg", "0.99");
                // https://github.com/open-telemetry/opentelemetry-java/tree/v1.3.0/sdk-extensions/autoconfigure#span-limits
                put("otel.span.attribute.count.limit", "128");
                put("otel.span.event.count.limit", "128");
                put("otel.span.link.count.limit", "128");
                // https://github.com/open-telemetry/opentelemetry-java/tree/v1.3.0/sdk-extensions/autoconfigure#batch-span-processor
                put("otel.bsp.schedule.delay", "5000");
                put("otel.bsp.max.queue.size", "2048");
                put("otel.bsp.max.export.batch.size", "512");
                put("otel.bsp.export.timeout", "30000");
                put("otel.exporter.jaeger.endpoint", "http://localhost:14250");
                put("otel.exporter.jaeger.timeout", "10000");
                put("otel.exporter.zipkin.endpoint", "http://localhost:9411/api/v2/spans");
                put("otel.exporter.zipkin.timeout", "10000");
            }
        };

    }

    @Getter
    @Setter
    @ToString
    @Validated
    public static class ZipkinProperties {

        /**
         * Zipkin receiver server base URI.
         */
        private String endpoint = "http://localhost:9411/api/v2/spans";
    }

    @Getter
    @Setter
    @ToString
    @Validated
    public static class JaegerProperties {

        /**
         * Jaeger receiver server base URI.
         */
        private String endpoint = "http://localhost:14250";
    }

    /**
     * @see {@link io.opentelemetry.sdk.trace.export.BatchSpanProcessor}
     */
    @Getter
    @Setter
    @ToString
    @Validated
    public static class BSPProperties {
        private long scheduleDelay = 5000;
        private int maxQueueSize = 2048;
        private int maxExportBatchSize = 512;
        private long exportTimeout = 30000;
    }

}
