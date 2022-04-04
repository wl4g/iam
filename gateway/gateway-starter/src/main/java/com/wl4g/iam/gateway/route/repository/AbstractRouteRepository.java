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
package com.wl4g.iam.gateway.route.repository;

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;

import com.google.common.collect.Maps;
import com.wl4g.iam.gateway.route.IRouteCacheRefresher;
import com.wl4g.iam.gateway.route.RefreshRoutesListener.RefreshType;
import com.wl4g.infra.common.log.SmartLogger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Abstract routes configuration repository. </br>
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-04
 * @since
 */
public abstract class AbstractRouteRepository extends InMemoryRouteDefinitionRepository implements IRouteCacheRefresher {

    protected final SmartLogger log = getLogger(getClass());
    protected @Autowired ApplicationEventPublisher publisher;

    @PostConstruct
    public synchronized Mono<Void> init() {
        return refreshRoutes();
    }

    /**
     * Refresh the routes configuration in memory and update it by comparing the
     * routing differences in memory and persistent storage.
     */
    @Override
    public synchronized Mono<Void> refreshRoutes() {
        Flux<RouteDefinition> memeryRoutes = getRouteDefinitions();
        Flux<RouteDefinition> permanentRoutes = loadPermanentRouteDefinitions();

        Map<RouteDefinition, Boolean> newMemRoutes = Maps.newHashMap();
        memeryRoutes.subscribe(route -> newMemRoutes.put(route, false));

        Map<RouteDefinition, Boolean> newPermanentRoutes = Maps.newHashMap();
        permanentRoutes.subscribe(route -> newPermanentRoutes.put(route, false));

        // ADD routes.
        Flux.fromIterable(newPermanentRoutes.keySet())
                .filter(perRoute -> !newMemRoutes.containsKey(perRoute))
                .flatMap(route -> super.save(Mono.just(route)))
                .subscribe();

        // REMOVE routes.
        Flux.fromIterable(newMemRoutes.keySet())
                .filter(perRoute -> !newPermanentRoutes.containsKey(perRoute))
                .flatMap(route -> super.delete(Mono.just(route.getId())))
                .subscribe();

        publisher.publishEvent(new RefreshRoutesEvent(RefreshType.STATE));
        return Mono.empty();
    }

    /**
     * DO load routes configuration from persistent store.
     */
    protected abstract Flux<RouteDefinition> loadPermanentRouteDefinitions();

}