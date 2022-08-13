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

import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.Arrays.asList;

import org.junit.Test;

import com.wl4g.iam.gateway.requestlimit.key.HeaderIamKeyResolver.HeaderKeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.IamKeyResolver.KeyResolverStrategy;

/**
 * {@link KeyStrategyTests}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-26 v3.0.0
 * @since v3.0.0
 */
public class KeyStrategyTests {

    @Test
    public void testKeyStrategySerialToJson() {
        HeaderKeyResolverStrategy strategy = new HeaderKeyResolverStrategy(
                asList("X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP"));
        System.out.println(toJSONString(strategy));
    }

    @Test
    public void testKeyStrategyParseFromJson() {
        String json = "{\"privoder\":\"Header\",\"headerNames\":[\"X-Forwarded-For\",\"Proxy-Client-IP\",\"WL-Proxy-Client-IP\"]}";
        KeyResolverStrategy strategy = parseJSON(json, KeyResolverStrategy.class);
        System.out.println(strategy);
    }

}
