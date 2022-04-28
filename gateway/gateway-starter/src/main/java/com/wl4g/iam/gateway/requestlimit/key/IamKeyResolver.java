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
package com.wl4g.iam.gateway.requestlimit.key;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.iam.gateway.requestlimit.key.HeaderIamKeyResolver.HeaderKeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.HostIamKeyResolver.HostKeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.IamKeyResolver.KeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.IntervalIamKeyResolver.IntervalKeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.IpRangeIamKeyResolver.IpRangeKeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.PathIamKeyResolver.PathKeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.PrincipalIamKeyResolver.PrincipalKeyResolverStrategy;
import com.wl4g.infra.core.framework.operator.Operator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link IamKeyResolver}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-20 v3.0.0
 * @since v3.0.0
 */
public interface IamKeyResolver<C extends KeyResolverStrategy> extends Operator<IamKeyResolver.KeyResolverProvider> {

    Mono<String> resolve(C strategy, ServerWebExchange exchange);

    @Getter
    @AllArgsConstructor
    public static enum KeyResolverProvider {
        Host(HostKeyResolverStrategy.class),

        IpRange(IpRangeKeyResolverStrategy.class),

        Principal(PrincipalKeyResolverStrategy.class),

        Path(PathKeyResolverStrategy.class),

        Header(HeaderKeyResolverStrategy.class),

        Interval(IntervalKeyResolverStrategy.class);

        private final Class<? extends KeyResolverStrategy> strategyClass;
    }

    // @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include =
    // JsonTypeInfo.As.PROPERTY, property = "privoder")
    // @JsonSubTypes({ @Type(value = HostKeyResolverStrategy.class,name="Host"),
    // @Type(value = PrincipalKeyResolverStrategy.class, name = "Principal"),
    // @Type(value = PathKeyResolverStrategy.class, name = "Path"),
    // @Type(value = HeaderKeyResolverStrategy.class, name = "Header"),
    // @Type(value = IntervalKeyResolverStrategy.class, name = "Interval"),
    // @Type(value = IpRangeKeyResolverStrategy.class, name = "IpRange"), })
    @Getter
    @Setter
    @ToString
    @Validated
    @NoArgsConstructor
    public static abstract class KeyResolverStrategy {
        public void applyDefaultIfNecessary(IamRequestLimiterProperties config) {
        }
    }

}
