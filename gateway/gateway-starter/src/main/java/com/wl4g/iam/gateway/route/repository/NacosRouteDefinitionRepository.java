///*
// * Copyright 2017 ~ 2025 the original authors James Wong.
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
//import static java.util.Objects.isNull;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.route.RouteDefinition;
//
//import com.alibaba.cloud.nacos.NacosConfigManager;
//import com.alibaba.cloud.nacos.NacosConfigProperties;
//import com.alibaba.nacos.api.config.ConfigService;
//import com.alibaba.nacos.api.exception.NacosException;
//import com.fasterxml.jackson.core.type.TypeReference;
//
//import lombok.extern.slf4j.Slf4j;
//import reactor.core.publisher.Flux;
//
///**
// * {@link NacosRouteDefinitionRepository}
// * 
// * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
// * @version 2021-09-05 v3.0.0
// * @since v3.0.0
// */
//@Slf4j
//public class NacosRouteDefinitionRepository extends AbstractRouteRepository {
//
//    private @Autowired NacosConfigManager configManager;
//
//    @Override
//    protected Flux<RouteDefinition> loadPermanentRouteDefinitions() {
//        return Flux.defer(() -> {
//            NacosConfigProperties config = configManager.getNacosConfigProperties();
//            ConfigService configService = configManager.getConfigService();
//            try {
//                // configService.addListener(config.getName(),
//                // config.getGroup(), new Listener() {
//                // @Override
//                // public void receiveConfigInfo(String configInfo) {
//                // RouteDefinition routes = parseJSON(configInfo, new
//                // TypeReference<RouteDefinition>() {
//                // });
//                // }
//                // @Override
//                // public Executor getExecutor() {
//                // return Executors.newSingleThreadExecutor();
//                // }
//                // });
//                String configInfo = configService.getConfig(config.getName(), config.getGroup(), config.getTimeout());
//                RouteDefinition routes = parseJSON(configInfo, new TypeReference<RouteDefinition>() {
//                });
//                return isNull(routes) ? Flux.empty() : Flux.just(routes);
//            } catch (NacosException e) {
//                log.error("", e);
//            }
//            return Flux.empty();
//        });
//    }
//
//}