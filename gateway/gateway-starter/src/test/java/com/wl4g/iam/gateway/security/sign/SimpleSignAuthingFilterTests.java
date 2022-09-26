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
package com.wl4g.iam.gateway.security.sign;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.util.Arrays.asList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.security.config.IamSecurityProperties;
import com.wl4g.iam.gateway.security.sign.SimpleSignAuthingFilterFactory.SignHashingMode;
import com.wl4g.infra.common.eventbus.EventBusSupport;

/**
 * {@link SimpleSignAuthingFilterTests}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-05 v3.0.0
 * @since v3.0.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class SimpleSignAuthingFilterTests {

    private static final String TEST_APPID = "oi554a94bc416e4edd9ff963ed0e9e25e6c10545";
    private static final String TEST_APPSECRET = "5aUpyX5X7wzC8iLgFNJuxqj3xJdNQw8yS";
    private static final String TEST_ROUTE_PATH = "/v2/get";

    private @Autowired WebTestClient webClient;
    // @org.springframework.beans.factory.annotation.Value("${server.port}")
    // private int port;

    @TestConfiguration
    public static class TestEnvParameterSimpleParamsBytesSortedHashingS256Configuration {
        private @Autowired IamSecurityProperties authingConfig;
        private @Autowired StringRedisTemplate redisTemplate;
        private @Autowired IamGatewayMetricsFacade metricsFacade;
        private EventBusSupport eventBus = EventBusSupport.getDefault();

        // Unable to overwrite the original auto-configuration instance??
        //
        // @Bean
        // @Primary
        // public IamSecurityProperties authingProperties() {
        // IamSecurityProperties config = new IamSecurityProperties();
        // // custom secret store type.
        // config.getSimpleSign().setSecretLoadStore(SecretStore.REDIS);
        // return config;
        // }

        @Bean
        public RouteLocator configureTestRoutes(RouteLocatorBuilder builder) {
            // custom store secret.
            System.setProperty(authingConfig.getSimpleSign().getSecretStorePrefix() + ":" + TEST_APPID, TEST_APPSECRET);

            return builder.routes()
                    .route("test-route-with-" + SimpleSignAuthingFilterFactory.class.getSimpleName(),
                            p -> p.path(TEST_ROUTE_PATH).filters(f -> {
                                // for Add simple sign filter.
                                SimpleSignAuthingFilterFactory filter = new SimpleSignAuthingFilterFactory(
                                        new IamSecurityProperties(), redisTemplate, metricsFacade, eventBus);
                                SimpleSignAuthingFilterFactory.Config config = new SimpleSignAuthingFilterFactory.Config();
                                // custom sign parameter name.
                                config.setSignParam("signature");
                                // custom sign hashing mode.
                                config.setSignHashingMode(SignHashingMode.SimpleParamsBytesSortedHashing);
                                // custom sign hashing include parameters.
                                config.setSignHashingIncludeParams(asList("appId", "timestamp", "nonce"));
                                // custom sign hashing required include
                                // parameters.
                                config.setSignHashingRequiredIncludeParams(asList("appId", "timestamp", "nonce"));
                                return f.filter(filter.apply(config), Ordered.HIGHEST_PRECEDENCE);
                            }).uri("http://httpbin.org:80"))
                    .build();
        }
    }

    @Test
    public void testEnvStoreSimpleParamsBytesSortedHashingS256() throws Exception {
        String nonce = SimpleSignGenerateTool.generateNonce(16);
        long timestamp = currentTimeMillis();
        String sign = SimpleSignGenerateTool.generateSign(TEST_APPID, TEST_APPSECRET, nonce, timestamp, "SHA-256");

        // String uri =
        // format("http://localhost:%s%s?appId=%s&appSecret=%s&nonce=%s&timestamp=%s&signature=%s",
        // java.lang.String.valueOf(port), TEST_ROUTE_PATH, TEST_APPID,
        // TEST_APPSECRET, nonce, timestamp, sign);

        String uri = format("%s?appId=%s&appSecret=%s&nonce=%s&timestamp=%s&signature=%s", TEST_ROUTE_PATH, TEST_APPID,
                TEST_APPSECRET, nonce, timestamp, sign);

        webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(String.class)
                .consumeWith(result -> out.println("Result: " + result.getRawStatusCode() + " - " + result.getResponseBody()));

        Thread.sleep(1000);
    }

}
