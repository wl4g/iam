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
package com.wl4g.iam.gateway.loadbalance.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_LOADBANANER;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.gateway.loadbalance.CanaryLoadBalancerClientFilter;
import com.wl4g.iam.gateway.loadbalance.rule.CanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.DestinationHashCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.LeastConnCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.LeastTimeCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.RandomCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.RoundRobinCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.SourceHashCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.WeightLeastConnCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.WeightLeastTimeCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.WeightRandomCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.WeightRoundRobinCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.stats.InMemoryLoadBalancerCache;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerCache;
import com.wl4g.iam.gateway.loadbalance.rule.stats.ReachableStrategy;
import com.wl4g.infra.core.framework.operator.GenericOperatorAdapter;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

/**
 * {@link CanaryLoadbalanceAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-10-13 v1.0.0
 * @since v1.0.0
 */
public class CanaryLoadbalanceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_IAM_GATEWAY_LOADBANANER)
    public CanaryLoadBalancerProperties canaryLoadBalancerProperties() {
        return new CanaryLoadBalancerProperties();
    }

    // Load-balancer stats.

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancerCache inMemoryLoadBalancerCache() {
        return new InMemoryLoadBalancerCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReachableStrategy defaultReachableStrategy() {
        return ReachableStrategy.DEFAULT;
    }

    // Load-balancer rules.

    @Bean(BEAN_CANARY_LB_REQUEST_MATCHER)
    public SpelRequestMatcher canaryLoadBalancerSpelRequestMatcher(CanaryLoadBalancerProperties config) {
        return new SpelRequestMatcher(config.getMatchRuleDefinitions());
    }

    @Bean
    public CanaryLoadBalancerRule destinationHashCanaryLoadBalancerRule() {
        return new DestinationHashCanaryLoadBalancerRule();
    }

    @Bean
    public CanaryLoadBalancerRule leastConnCanaryLoadBalancerRule() {
        return new LeastConnCanaryLoadBalancerRule();
    }

    @Bean
    public CanaryLoadBalancerRule leastTimeCanaryLoadBalancerRule() {
        return new LeastTimeCanaryLoadBalancerRule();
    }

    @Bean
    public CanaryLoadBalancerRule randomCanaryLoadBalancerRule() {
        return new RandomCanaryLoadBalancerRule();
    }

    @Bean
    public CanaryLoadBalancerRule roundRobinCanaryLoadBalancerRule() {
        return new RoundRobinCanaryLoadBalancerRule();
    }

    @Bean
    public CanaryLoadBalancerRule sourceHashCanaryLoadBalancerRule() {
        return new SourceHashCanaryLoadBalancerRule();
    }

    @Bean
    public CanaryLoadBalancerRule weightRandomCanaryLoadBalancerRule() {
        return new WeightRandomCanaryLoadBalancerRule();
    }

    @Bean
    public CanaryLoadBalancerRule weightRoundRobinCanaryLoadBalancerRule() {
        return new WeightRoundRobinCanaryLoadBalancerRule();
    }

    @Bean
    public CanaryLoadBalancerRule weightLeastConnCanaryLoadBalancerRule() {
        return new WeightLeastConnCanaryLoadBalancerRule();
    }

    @Bean
    public CanaryLoadBalancerRule weightLeastTimeCanaryLoadBalancerRule() {
        return new WeightLeastTimeCanaryLoadBalancerRule();
    }

    @Bean
    public GenericOperatorAdapter<CanaryLoadBalancerRule.LoadBalancerAlgorithm, CanaryLoadBalancerRule> compositeCanaryLoadBalancerAdapter(
            List<CanaryLoadBalancerRule> rules) {
        return new GenericOperatorAdapter<CanaryLoadBalancerRule.LoadBalancerAlgorithm, CanaryLoadBalancerRule>(rules) {
        };
    }

    // Load-balancer filters.

    @Bean
    public CanaryLoadBalancerClientFilter canaryLoadBalancerClientFilter() {
        return new CanaryLoadBalancerClientFilter();
    }

    public static final String BEAN_CANARY_LB_REQUEST_MATCHER = "canaryLoadBalancerSpelRequestMatcher";

}
