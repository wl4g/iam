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
package com.wl4g.iam.gateway.circuitbreaker;

import static java.lang.System.out;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class CircuitbreakerTests {

    // total number of tested
    private static int i = 0;
    private @Autowired WebTestClient webClient;

    @Test
    @RepeatedTest(100)
    public void testHelloPredicates() throws InterruptedException {
        // 低于50次时，在0和1之间切换，也就是一次正常一次超时，
        // 超过50次时，固定为0，此时每个请求都不会超时
        int gen = (i < 50) ? (i % 2) : 0;
        i++;
        String tag = "[" + i + "]";

        // 发起web请求
        webClient.get()
                .uri("/hello/account/" + gen)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(String.class)
                .consumeWith(result -> out.println(tag + result.getRawStatusCode() + " - " + result.getResponseBody()));
        Thread.sleep(1000);
    }
}
