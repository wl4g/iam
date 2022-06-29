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
package com.wl4g.iam.core.filter;

import static java.util.Objects.nonNull;
import static org.apache.shiro.web.util.WebUtils.toHttp;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.wl4g.infra.common.web.WebUtils2;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.iam.core.config.AbstractIamProperties;
import com.wl4g.iam.core.config.AbstractIamProperties.ParamProperties;
import com.wl4g.iam.core.risk.RiskSecurityHandler;

/**
 * {@link BasedRiskIamAuthenticationFilter}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-06-14 v1.0.0
 * @see v1.0.0
 */
public abstract class BasedRiskIamAuthenticationFilter<C extends AbstractIamProperties<? extends ParamProperties>>
        extends AbstractIamAuthenticationFilter<C> implements InitializingBean {

    @Autowired(required = false)
    protected List<RiskSecurityHandler> riskHandlers;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (nonNull(riskHandlers)) {
            AnnotationAwareOrderComparator.sort(riskHandlers);
        }
    }

    @Override
    public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        if (nonNull(riskHandlers)) {
            for (RiskSecurityHandler handler : riskHandlers) {
                try {
                    RespBase<Object> resp = handler.inspecting(toHttp(request), toHttp(response));
                    log.debug("called:onPreHandle inspected: {}", resp);
                    if (nonNull(resp)) {
                        WebUtils2.writeJson(toHttp(response), resp.asJson());
                        response.flushBuffer();
                        return false;
                    }
                } catch (IOException e) {
                    log.error("Failed to risk inspecting.", e);
                }
            }
        }
        return super.onPreHandle(request, response, mappedValue);
    }

}
