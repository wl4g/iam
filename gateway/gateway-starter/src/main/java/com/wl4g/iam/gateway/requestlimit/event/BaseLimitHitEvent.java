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
package com.wl4g.iam.gateway.requestlimit.event;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

/**
 * {@link BaseLimitHitEvent}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-19 v3.0.0
 * @since v3.0.0
 */
@Getter
public class BaseLimitHitEvent extends ApplicationEvent {
    private static final long serialVersionUID = -7137748823573974641L;

    private final String routeId;
    private final String limitKey;
    private final String requsetPath;

    public BaseLimitHitEvent(String routeId, String limitKey, String requsetPath) {
        super(limitKey);
        this.routeId = hasTextOf(routeId, "routeId");
        this.limitKey = hasTextOf(limitKey, "limitKey");
        this.requsetPath = hasTextOf(requsetPath, "requsetPath");
    }

}
