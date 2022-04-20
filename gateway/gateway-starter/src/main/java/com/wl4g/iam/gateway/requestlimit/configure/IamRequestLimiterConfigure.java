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
package com.wl4g.iam.gateway.requestlimit.configure;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * User-level current limiting configuration interface (data plane), for
 * example, the current limiting configuration information can be loaded
 * according to the currently authenticated principal(rateLimitId).
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-20 v3.0.0
 * @since v3.0.0
 */
public interface IamRequestLimiterConfigure {

    @NotNull
    Mono<TokenRateLimiterConfig> load(@NotBlank String routeId, @NotBlank String rateLimitId);

    @Getter
    @Setter
    @ToString
    @Validated
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenRateLimiterConfig {
        private @Min(0) Integer burstCapacity;
        private @Min(1) Integer replenishRate;
        private @Min(1) Integer requestedTokens;
        private String intervalKeyResolverDatePattern;
        private List<String> headerKeyResolverNames;
    }

}
