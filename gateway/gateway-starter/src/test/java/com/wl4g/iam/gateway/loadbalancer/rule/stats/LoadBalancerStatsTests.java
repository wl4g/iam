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
package com.wl4g.iam.gateway.loadbalancer.rule.stats;

import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.netty.buffer.Unpooled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

/**
 * {@link LoadBalancerStatsTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-14 v3.0.0
 * @since v3.0.0
 */
public class LoadBalancerStatsTests {

    // --- GET blocking

    @Test
    public void testSuccessGetRequestBlockTimeout() throws Exception {
        doTestGetRequestBlockTimeout(5_000);
    }

    @Test
    public void testFailGetRequestBlockTimeout() throws Exception {
        doTestGetRequestBlockTimeout(1);
    }

    private void doTestGetRequestBlockTimeout(long timeout) throws InterruptedException {
        System.out.println("=============== start...");
        final CountDownLatch latch = new CountDownLatch(1);

        HttpClient.create()
                .wiretap(false)
                .get()
                .uri("https://www.httpbin.org/")
                .responseContent()
                .asString()
                .collectList()
                .doOnSuccess(buffers -> {
                    latch.countDown();
                    buffers.forEach(body -> {
                        System.out.println(currentTimeMillis() + " - call Success: " + body);
                    });
                })
                .doOnError(ex -> {
                    latch.countDown();
                    System.out.println(currentTimeMillis() + " - call Error: " + ex);
                })
                .doFinally(signal -> {
                    latch.countDown();
                    System.out.println(currentTimeMillis() + " - call Finally: " + signal);
                })
                .block(Duration.ofMillis(timeout));

        System.out.println(currentTimeMillis() + " - await...");
        Assertions.assertTrue(latch.await(20, TimeUnit.SECONDS), () -> "Latch didn't time out");
    }

    // --- POST blocking

    @Test
    public void testSuccessPostRequestBlockTimeout() throws Exception {
        doTestPostRequestBlockTimeout("https://www.httpbin.org/get", 5_000);
    }

    @Test
    public void testFailPostRequestBlockTimeout() throws Exception {
        doTestPostRequestBlockTimeout("https://www.httpbin.org/post", 1);
    }

    private void doTestPostRequestBlockTimeout(String uri, long timeout) throws InterruptedException {
        System.out.println("=============== start...");
        final CountDownLatch latch = new CountDownLatch(1);

        // Duration responseTimeout = Duration.ofMillis(3_000);
        Flux<HttpClientResponse> responseFlux = HttpClient.create()
                .wiretap(false)
                .post()
                .uri(uri)
                .send((req, nettyOutbound) -> nettyOutbound.send(Mono.just(Unpooled.wrappedBuffer("Hello".getBytes()))))
                .responseConnection((res, connection) -> {
                    System.out.println(currentTimeMillis() + " - call response: " + res.status());
                    return Mono.just(res);
                })
                // .timeout(responseTimeout, Mono.fromRunnable(() -> {
                // latch.countDown();
                // System.out.println(currentTimeMillis() + " - call Timeout: "
                // + responseTimeout);
                // }))
                .doFinally(signal -> {
                    latch.countDown();
                    System.out.println(currentTimeMillis() + " - call Finally: " + signal);
                });

        System.out.println("responseFlux: " + responseFlux);
        responseFlux.blockFirst(Duration.ofMillis(timeout));

        System.out.println(currentTimeMillis() + " - await...");
        Assertions.assertTrue(latch.await(20, TimeUnit.SECONDS), () -> "Latch didn't time out");
    }

    // --- GET non-blocking

    @Test
    public void testSuccessGetRequestNonBlockTimeout() throws Exception {
        CountDownLatch successLatch = doTestGetRequestNonBlockTimeout(5000);
        Assertions.assertTrue(successLatch.await(5000, TimeUnit.MILLISECONDS), () -> "Latch didn't time out");
    }

    @Test
    public void testFailGetRequestNonBlockTimeout() throws Exception {
        CountDownLatch successLatch = doTestGetRequestNonBlockTimeout(1);
        Assertions.assertFalse(successLatch.await(1, TimeUnit.MILLISECONDS), () -> "Latch didn't time out");
    }

    private CountDownLatch doTestGetRequestNonBlockTimeout(long timeout) throws InterruptedException {
        System.out.println("=============== start...");
        final CountDownLatch successLatch = new CountDownLatch(1);

        Consumer<String> onSuccess = (String response) -> {
            successLatch.countDown();
            System.out.println(currentTimeMillis() + " - call2 Success: " + response);
        };
        Consumer<Throwable> onError = (Throwable ex) -> {
            // successLatch.countDown();
            System.out.println(currentTimeMillis() + " - call2 Error: " + ex);
        };
        Runnable onCompletion = () -> {
            // successLatch.countDown();
            System.out.println(currentTimeMillis() + " - call2 Completion: ");
        };

        HttpClient.create()
                .wiretap(false)
                .get()
                .uri("https://www.httpbin.org/")
                .responseContent()
                .aggregate()
                .asString()
                .timeout(Duration.ofMillis(timeout), Mono.fromRunnable(() -> {
                    System.out.println(currentTimeMillis() + " - call Timeout");
                }))
                .doOnSuccess(response -> {
                    System.out.println(currentTimeMillis() + " - call Success: " + response);
                })
                .doOnError(ex -> {
                    System.out.println(currentTimeMillis() + " - call Error: " + ex);
                })
                .doFinally(signal -> {
                    System.out.println(currentTimeMillis() + " - call Finally: " + signal);
                })
                .subscribe(onSuccess, onError, onCompletion);

        System.out.println(currentTimeMillis() + " - await...");
        return successLatch;
    }

    // --- POST non-blocking

    @Test
    public void testSuccessPostRequestNonBlockTimeout() throws Exception {
        CountDownLatch successLatch = doTestPostRequestNonBlockTimeout("https://www.httpbin.org/get", 5000);
        Assertions.assertTrue(successLatch.await(5000, TimeUnit.MILLISECONDS), () -> "Latch didn't time out");
    }

    @Test
    public void testFailPostRequestNonBlockTimeout() throws Exception {
        CountDownLatch successLatch = doTestPostRequestNonBlockTimeout("https://www.httpbin.org/post", 1);
        Assertions.assertFalse(successLatch.await(1, TimeUnit.MILLISECONDS), () -> "Latch didn't time out");
    }

    private CountDownLatch doTestPostRequestNonBlockTimeout(String uri, long timeout) throws InterruptedException {
        System.out.println("=============== start...");
        final CountDownLatch successLatch = new CountDownLatch(1);

        Consumer<String> onSuccess = (String response) -> {
            successLatch.countDown();
            System.out.println(currentTimeMillis() + " - call2 Success: " + response);
        };
        Consumer<Throwable> onError = (Throwable ex) -> {
            // successLatch.countDown();
            System.out.println(currentTimeMillis() + " - call2 Error: " + ex);
        };
        Runnable onCompletion = () -> {
            // successLatch.countDown();
            System.out.println(currentTimeMillis() + " - call2 Completion: ");
        };

        HttpClient.create()
                .wiretap(false)
                .post()
                .uri(uri)
                .send(ByteBufFlux.fromString(Mono.just("Hello")))
                .responseContent()
                .aggregate()
                .asString()
                .timeout(Duration.ofMillis(timeout), Mono.fromRunnable(() -> {
                    System.out.println(currentTimeMillis() + " - call Timeout");
                }))
                .doOnSuccess(response -> {
                    System.out.println(currentTimeMillis() + " - call Success: " + response);
                })
                .doOnError(ex -> {
                    System.out.println(currentTimeMillis() + " - call Error: " + ex);
                })
                .doFinally(signal -> {
                    System.out.println(currentTimeMillis() + " - call Finally: " + signal);
                })
                .subscribe(onSuccess, onError, onCompletion);

        System.out.println(currentTimeMillis() + " - await...");
        return successLatch;
    }

}
