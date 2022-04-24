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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link PathIamKeyResolver}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-30 v1.0.0
 * @since v1.0.0
 */
public class PathIamKeyResolver implements IamKeyResolver<PathIamKeyResolver.PathKeyResolverStrategy> {

    @Override
    public KeyResolverProvider kind() {
        return KeyResolverProvider.PATH;
    }

    @Override
    public Mono<String> resolve(PathKeyResolverStrategy strategy, ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest().getURI().getPath());
    }

    @Getter
    @Setter
    @ToString
    @Validated
    public static class PathKeyResolverStrategy extends IamKeyResolver.KeyResolverStrategy {
    }

}