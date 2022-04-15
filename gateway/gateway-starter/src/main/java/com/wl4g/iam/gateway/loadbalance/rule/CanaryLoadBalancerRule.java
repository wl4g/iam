package com.wl4g.iam.gateway.loadbalance.rule;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats;
import com.wl4g.infra.core.framework.operator.Operator;

/**
 * {@link CanaryLoadBalancerRule}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-03 v3.0.0
 * @since v3.0.0
 */
public interface CanaryLoadBalancerRule extends Operator<CanaryLoadBalancerRule.LoadBalancerAlgorithm> {

    ServiceInstance choose(ServerWebExchange exchange, LoadBalancerStats stats, String serviceId);

    /**
     * see:https://www.cnblogs.com/pengpengboshi/p/13278440.html
     */
    public static enum LoadBalancerAlgorithm {
        R, RR, WR, WRR, DH, SH, LC, LT, WLC, WLT
    }

}
