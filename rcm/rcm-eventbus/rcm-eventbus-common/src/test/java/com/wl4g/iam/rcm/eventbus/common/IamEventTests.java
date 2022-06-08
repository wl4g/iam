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
package com.wl4g.iam.rcm.eventbus.common;

import org.junit.Test;

import com.wl4g.iam.rcm.eventbus.common.IamEventBase.EventType;

/**
 * {@link IamEventTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-07 v3.0.0
 * @since v3.0.0
 */
public class IamEventTests {

    @SuppressWarnings({ "serial", "deprecation" })
    @Test
    public void testIamEventTo() {
        IamEventBase event = new IamEventBase(EventType.AUTHC_SUCCESS, "user1", "1.1.1.1", "113.12314,23.234235", "successful") {
        };
        System.out.println(IamEventBase.to(event));
    }

    @SuppressWarnings({ "deprecation" })
    @Test
    public void testIamEventFrom() {
        String json = "{\"eventType\":\"UNKNOWN\",\"source\":\"user1\",\"message\":\"successful\",\"timestamp\":1654589341786}";
        IamEventBase event = IamEventBase.from(json);
        System.out.println(event);
    }

}
