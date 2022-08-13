/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link PrincipalIamKeyResolver}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-20 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.gateway.filter.ratelimit.PrincipalNameKeyResolver}
 */
public class PrincipalIamKeyResolver extends AbstractIamKeyResolver<PrincipalIamKeyResolver.PrincipalKeyResolverStrategy> {

    @Override
    public KeyResolverProvider kind() {
        return KeyResolverProvider.Principal;
    }

    /**
     * {@link com.wl4g.iam.gateway.security.sign.SimpleSignAuthingFilterFactory#bindSignedToContext()}
     */
    @Override
    public Mono<String> resolve(PrincipalKeyResolverStrategy strategy, ServerWebExchange exchange) {
        return exchange.getPrincipal().flatMap(p -> Mono.justOrEmpty(p.getName()));
    }

    @Getter
    @Setter
    @ToString
    @Validated
    public static class PrincipalKeyResolverStrategy extends IamKeyResolver.KeyResolverStrategy {
        @Override
        public void applyDefaultIfNecessary(IamRequestLimiterProperties config) {
            // Ignore
        }
    }

}
