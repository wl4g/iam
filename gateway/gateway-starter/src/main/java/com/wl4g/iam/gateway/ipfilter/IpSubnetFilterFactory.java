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
package com.wl4g.iam.gateway.ipfilter;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.ipfilter.config.IpFilterProperties;
import com.wl4g.iam.gateway.ipfilter.configurer.IpFilterConfigurer;
import com.wl4g.iam.gateway.ipfilter.configurer.IpFilterConfigurer.FilterStrategy;
import com.wl4g.iam.gateway.util.IamGatewayUtil;
import com.wl4g.infra.common.bean.ConfigBeanUtils;
import com.wl4g.infra.common.net.CIDR;
import com.wl4g.infra.common.web.WebUtils;

import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import reactor.netty.tcp.InetSocketAddressUtil;

/**
 * {@link IpSubnetFilterFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-05 v3.0.0
 * @since v3.0.0
 * @see {@link io.netty.handler.ipfilter.IpSubnetFilterRule}
 */
public class IpSubnetFilterFactory extends AbstractGatewayFilterFactory<IpSubnetFilterFactory.Config> {

    private final IpFilterProperties ipFilterConfig;
    private final IpFilterConfigurer configurer;

    public IpSubnetFilterFactory(IpFilterProperties ipListConfig, IpFilterConfigurer configurer) {
        super(IpSubnetFilterFactory.Config.class);
        this.ipFilterConfig = notNullOf(ipListConfig, "ipListConfig");
        this.configurer = notNullOf(configurer, "configurer");
    }

    @Override
    public String name() {
        return BEAN_NAME;
    }

    @Override
    public GatewayFilter apply(Config config) {
        applyDefaultToConfig(config);
        return (exchange, chain) -> {
            return exchange.getPrincipal().defaultIfEmpty(IamGatewayUtil.UNKNOWN_PRINCIPAL).flatMap(p -> {
                // Load strategy by routeId and principal.
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                return configurer.loadStrategy(route.getId(), p.getName())
                        .defaultIfEmpty(singletonList(ipFilterConfig.getDefaultFilter().getStrategy()))
                        .flatMap(strategys -> {
                            if (isAllowed(config, exchange, strategys)) {
                                return chain.filter(exchange);
                            }
                            ServerWebExchangeUtils.setResponseStatus(exchange, HttpStatusHolder.parse(config.getStatusCode()));
                            return exchange.getResponse().setComplete();
                        });
            });
        };
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new Config(), config, ipFilterConfig.getDefaultFilter());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    protected boolean isAllowed(Config config, ServerWebExchange exchange, List<FilterStrategy> strategys) {
        // Determine remote client address.
        // Note:This method does not send network resolutions
        InetSocketAddress remoteAddress = InetSocketAddressUtil.createInetSocketAddress(getClientAddress(config, exchange), 0,
                false);

        // The local address is allowed to pass by default.
        InetAddress address = remoteAddress.getAddress();
        if (config.isAnyLocalAddressAllowed()
                && (address.isAnyLocalAddress() || address.isLinkLocalAddress() || address.isLoopbackAddress())) {
            return true;
        }

        // Check if it is allowed to pass.
        //
        // matching white-list
        boolean isAccept = safeList(strategys).stream()
                .filter(s -> (nonNull(s.getAllow()) && s.getAllow()))
                .flatMap(s -> safeList(s.getCidrs()).stream())
                .anyMatch(cidr -> buildRule(cidr, IpFilterRuleType.ACCEPT).matches(remoteAddress));

        // matching blacklist
        boolean isReject = safeList(strategys).stream()
                .filter(s -> (nonNull(s.getAllow()) && !s.getAllow()))
                .flatMap(s -> safeList(s.getCidrs()).stream())
                .anyMatch(cidr -> buildRule(cidr, IpFilterRuleType.REJECT).matches(remoteAddress));

        // If none of the conditions are met, allow access.
        boolean allowed = isAccept && !isReject;
        if (!isAccept && isReject) {
            allowed = false;
        } else if (isAccept && isReject) {
            // Whether to use the blacklist first in case of conflict?
            allowed = !config.isPreferRejectOnCidrConflict();
        } else if (!isAccept && !isReject) {
            allowed = config.isAcceptNotMatchCidr();
        }
        return allowed;
    }

    private IpSubnetFilterRule buildRule(String cidr, IpFilterRuleType ruleType) {
        try {
            CIDR _cidr = CIDR.newCIDR(cidr);
            return new IpSubnetFilterRule(_cidr.getBaseAddress(), _cidr.getMask(), ruleType);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(format("Failed to parse cidr for '%s'", cidr));
        }
    }

    private String getClientAddress(Config config, ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String host = null;
        for (String header : config.getForwardHeaderNames()) {
            host = headers.getFirst(header);
            if (!isBlank(host) && !"Unknown".equalsIgnoreCase(host)) {
                return host;
            }
        }
        // Fall-back
        return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }

    @Getter
    @Setter
    @Validated
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Config {

        /**
         * When the white-list (allow) and the CIDRs of the blacklist (deny)
         * conflict, whether the blacklist (deny) has a higher priority.
         */
        private boolean preferRejectOnCidrConflict = true;

        /**
         * Whether to accept the request when neither the white-list nor the
         * blacklist CIDRs match.
         */
        private boolean acceptNotMatchCidr = true;

        /**
         * The allow all local addresses to pass.
         */
        private boolean anyLocalAddressAllowed = true;

        /**
         * HttpStatus to return when IpList is true, defaults to FORBIDDEN.
         */
        private String statusCode = HttpStatus.FORBIDDEN.name();

        /**
         * The according to the list of header names of the request header
         * current limiter, it can usually be used to obtain the actual IP after
         * being forwarded by the proxy to limit the current, or it can be
         * flexibly used for other purposes.
         */
        private List<String> forwardHeaderNames = asList(WebUtils.HEADER_REAL_IP);

    }

    public static final String BEAN_NAME = "IpFilter";

}
