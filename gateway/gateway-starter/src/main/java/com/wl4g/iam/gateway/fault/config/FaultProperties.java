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
package com.wl4g.iam.gateway.fault.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link FaultProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-27 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@Validated
@ToString
public class FaultProperties {

    /**
     * Preferred to enable fault injection match SPEL match expression. Default
     * by '#{true}', which means never match. </br>
     * </br>
     * Tip: The built-in support to get the current routeId, such as:
     * '#{routeId.get().test($request)}'
     */
    private String preferredOpenMatchExpression = "#{true}";

    /**
     * Preferred to enable fault injection match rule definitions.
     */
    private List<MatchHttpRequestRule> preferrdMatchRuleDefinitions = new ArrayList<>();

    private InjectorProperties injector = new InjectorProperties();

    @Getter
    @Setter
    @Validated
    @ToString
    public static class InjectorProperties {

    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class FixedInjectorProperties {

    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class IntervalInjectorProperties {

    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class RandomInjectorProperties {

    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class WeightInjectorProperties {

    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class CanaryInjectorProperties {

    }

}
