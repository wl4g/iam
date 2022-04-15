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
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.gateway.loadbalance.CanaryLoadBalancerClientFilter;
import com.wl4g.iam.gateway.loadbalance.rule.CanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.RandomCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.RoundRobinCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.WeightLeastConnCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.WeightLeastTimeCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.WeightRandomCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.WeightRoundRobinCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.stats.DefaultLoadBalancerStats;
import com.wl4g.iam.gateway.loadbalance.rule.stats.InMemoryLoadBalancerCache;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerCache;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats;
import com.wl4g.iam.gateway.loadbalance.rule.stats.ReachableStrategy;
import com.wl4g.infra.core.framework.operator.GenericOperatorAdapter;

/**
 * {@link LoadbalanceAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-10-13 v1.0.0
 * @since v1.0.0
 */
public class LoadbalanceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_IAM_GATEWAY_LOADBANANER)
    public com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties loadBalancerProperties() {
        return new com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties();
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

    @Bean
    public DefaultLoadBalancerStats defaultLoadBalancerStats(
            com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties loadbalancerConfig,
            LoadBalancerCache loadBalancerCache,
            ReachableStrategy strategy) {
        return new DefaultLoadBalancerStats(loadbalancerConfig, loadBalancerCache, strategy);
    }

    // Load-balancer rules.

    @Bean
    public CanaryLoadBalancerRule randomCanaryLoadBalancerRule(
            com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties loadbalancerConfig,
            DiscoveryClient discoveryClient) {
        return new RandomCanaryLoadBalancerRule(loadbalancerConfig, discoveryClient);
    }

    @Bean
    public CanaryLoadBalancerRule roundRobinCanaryLoadBalancerRule(
            com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties loadbalancerConfig,
            DiscoveryClient discoveryClient) {
        return new RoundRobinCanaryLoadBalancerRule(loadbalancerConfig, discoveryClient);
    }

    @Bean
    public CanaryLoadBalancerRule weightRandomCanaryLoadBalancerRule(
            com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties loadbalancerConfig,
            DiscoveryClient discoveryClient) {
        return new WeightRandomCanaryLoadBalancerRule(loadbalancerConfig, discoveryClient);
    }

    @Bean
    public CanaryLoadBalancerRule weightRoundRobinCanaryLoadBalancerRule(
            com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties loadbalancerConfig,
            DiscoveryClient discoveryClient) {
        return new WeightRoundRobinCanaryLoadBalancerRule(loadbalancerConfig, discoveryClient);
    }

    @Bean
    public CanaryLoadBalancerRule weightLeastConnCanaryLoadBalancerRule(
            com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties loadbalancerConfig,
            DiscoveryClient discoveryClient) {
        return new WeightLeastConnCanaryLoadBalancerRule(loadbalancerConfig, discoveryClient);
    }

    @Bean
    public CanaryLoadBalancerRule weightLeastTimeCanaryLoadBalancerRule(
            com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties loadbalancerConfig,
            DiscoveryClient discoveryClient) {
        return new WeightLeastTimeCanaryLoadBalancerRule(loadbalancerConfig, discoveryClient);
    }

    @Bean
    public GenericOperatorAdapter<CanaryLoadBalancerRule.LoadBalancerAlgorithm, CanaryLoadBalancerRule> compositeCanaryLoadBalancerAdapter(
            List<CanaryLoadBalancerRule> rules) {
        return new GenericOperatorAdapter<CanaryLoadBalancerRule.LoadBalancerAlgorithm, CanaryLoadBalancerRule>(rules) {
        };
    }

    // Load-balancer filters.

    @Bean
    public CanaryLoadBalancerClientFilter canaryLoadBalancerClientFilter(
            LoadBalancerClientFactory clientFactory,
            com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties properties,
            GenericOperatorAdapter<CanaryLoadBalancerRule.LoadBalancerAlgorithm, CanaryLoadBalancerRule> ruleAdapter,
            LoadBalancerStats loadBalancerStats) {
        return new CanaryLoadBalancerClientFilter(clientFactory, properties, ruleAdapter, loadBalancerStats);
    }

}
