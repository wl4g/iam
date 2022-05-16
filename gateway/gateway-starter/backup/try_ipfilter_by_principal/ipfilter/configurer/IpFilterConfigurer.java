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
package com.wl4g.iam.gateway.ipfilter.configurer;

import static java.lang.String.valueOf;

import java.util.List;

import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import reactor.core.publisher.Mono;

/**
 * {@link IpFilterConfigurer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-05 v3.0.0
 * @since v3.0.0
 */
public interface IpFilterConfigurer {

    Mono<List<FilterStrategy>> loadStrategy(@NotBlank String routeId, @NotBlank String principalName);

    public static String getConfigKey(String routeId, String principalName) {
        return valueOf(routeId).concat(":").concat(principalName);
    }

    @Getter
    @Setter
    @Validated
    @ToString
    @With
    @SuperBuilder
    @AllArgsConstructor
    public static class FilterStrategy {
        private boolean allow;
        private List<String> cidrs;

        public FilterStrategy() {
            this.allow = false;
        }
    }

}
