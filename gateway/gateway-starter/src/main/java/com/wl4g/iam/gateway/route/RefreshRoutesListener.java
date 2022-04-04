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
package com.wl4g.iam.gateway.route;

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.actuate.GatewayControllerEndpoint;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import com.wl4g.infra.common.log.SmartLogger;

/**
 * {@link RefreshRoutesListener}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-21
 * @since
 */
public class RefreshRoutesListener implements ApplicationListener<RefreshRoutesEvent> {
    private final SmartLogger log = getLogger(getClass());

    private @Autowired ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(RefreshRoutesEvent event) {
        try {
            log.info(format("Routes refresh :: %s", event.getSource().toString()));
            if (event.getSource() instanceof GatewayControllerEndpoint || RefreshType.PERMANENT.equals(event.getSource())) {
                applicationContext.getBean(IRouteCacheRefresher.class).refreshRoutes();
            }
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    public static enum RefreshType {
        PERMANENT, STATE
    }

}