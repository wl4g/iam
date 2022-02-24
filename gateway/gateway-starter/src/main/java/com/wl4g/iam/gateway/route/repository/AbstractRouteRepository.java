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
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinition;

import com.google.common.collect.Maps;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.iam.gateway.route.IRouteAlterSubscriber;
import com.wl4g.iam.gateway.route.IRouteCacheRefresh;
import com.wl4g.iam.gateway.route.NotifyType;
import com.wl4g.iam.gateway.route.StartedRoutesRefresher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Persistent routing information abstract class. < br > abstract class If you
 * need to extend the persistence method, please inherit this class and override
 * the following methods of this class {@link #getRouteDefinitionsByPermanent()}
 * and {@link #save(Mono)} and {@link #delete(Mono)} and
 * {@link #notifyAllRefresh(NotifyType)} } and {@link #initSubscriber()} and
 * {@link #refreshRoutesPermanentToMemery()}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-04
 * @since
 */
public abstract class AbstractRouteRepository extends InMemoryRouteDefinitionRepository
        implements IRouteCacheRefresh, IRouteAlterSubscriber {

    protected final SmartLogger log = getLogger(getClass());

    @Autowired
    private StartedRoutesRefresher routeAlterHandler;

    @Override
    public final Flux<RouteDefinition> getRouteDefinitions() {
        return getRouteDefinitionsByMemery();
    }

    /**
     * 从内存组件中获取路由信息
     * 
     * @return
     */
    public final Flux<RouteDefinition> getRouteDefinitionsByMemery() {
        return super.getRouteDefinitions();
    }

    /**
     * 从持久化组件中获取路由信息
     * 
     * @return
     */
    protected abstract Flux<RouteDefinition> getRouteDefinitionsByPermanent();

    /**
     * Refresh the routing information in memory and update it by comparing the
     * routing differences in memory and persistent storage.
     * 
     * @return
     */
    @Override
    public synchronized Mono<Void> refreshRoutesPermanentToMemery() {
        Flux<RouteDefinition> oldMemeryRoutes = getRouteDefinitionsByMemery();
        Flux<RouteDefinition> oldPermanentRoutes = getRouteDefinitionsByPermanent();

        Map<RouteDefinition, Boolean> newMemRoutes = Maps.newHashMap();
        oldMemeryRoutes.subscribe(route -> newMemRoutes.put(route, false));

        Map<RouteDefinition, Boolean> newPermanentRoutes = Maps.newHashMap();
        oldPermanentRoutes.subscribe(route -> newPermanentRoutes.put(route, false));

        // ADD routes.
        Flux.fromIterable(newPermanentRoutes.keySet()).filter(perRoute -> !newMemRoutes.containsKey(perRoute))
                .flatMap(route -> super.save(Mono.just(route))).subscribe();

        // REMOVE routes.
        Flux.fromIterable(newMemRoutes.keySet()).filter(perRoute -> !newPermanentRoutes.containsKey(perRoute))
                .flatMap(route -> super.delete(Mono.just(route.getId()))).subscribe();

        // Refreshing
        return routeAlterHandler.refresh(NotifyType.STATE);
    }

    @PostConstruct
    public synchronized Mono<Void> initMemeryByPermanent() {
        return refreshRoutesPermanentToMemery();
    }

}