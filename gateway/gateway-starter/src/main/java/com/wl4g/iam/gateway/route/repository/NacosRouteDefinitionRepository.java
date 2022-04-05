///*
// * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.wl4g.iam.gateway.route.repository;
//
//import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
//
//import org.springframework.cloud.gateway.route.RouteDefinition;
//
//import com.alibaba.nacos.api.NacosFactory;
//import com.alibaba.nacos.api.config.ConfigService;
//import com.alibaba.nacos.api.exception.NacosException;
//import com.fasterxml.jackson.core.type.TypeReference;
//
//import reactor.core.publisher.Flux;
//
///**
// * {@link NacosRouteDefinitionRepository}
// * 
// * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
// * @version 2022-04-05 v3.0.0
// * @since v3.0.0
// */
//public class NacosRouteDefinitionRepository extends AbstractRouteRepository {
//
//    @Override
//    protected Flux<RouteDefinition> loadPermanentRouteDefinitions() {
//        return Flux.defer(() -> {
//            ConfigService configService = null;
//            try {
//                configService = NacosFactory.createConfigService("//TODO serverAddr");
//            } catch (NacosException e) {
//                e.printStackTrace();
//            }
//            // configService.addListener("//TODO dataId", "//TODO group",
//            // new com.alibaba.nacos.api.config.listener.Listener() {
//            // @Override
//            // public void receiveConfigInfo(String configInfo) {
//            // refreshRoutes();
//            // }
//            //
//            // @Override
//            // public Executor getExecutor() {
//            // return null;
//            // }
//            // });
//            String initConfig = null;
//            try {
//                initConfig = configService.getConfig("//TODO dataId", "//TODO group", 5000);
//            } catch (NacosException e) {
//                e.printStackTrace();
//            }
//            RouteDefinition routes = parseJSON(initConfig, new TypeReference<RouteDefinition>() {
//            });
//            return Flux.just(routes);
//        });
//    }
//
//}