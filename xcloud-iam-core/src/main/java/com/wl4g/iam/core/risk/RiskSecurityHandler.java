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
package com.wl4g.iam.core.risk;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.core.Ordered;

import com.wl4g.component.common.web.rest.RespBase;

/**
 * Risk security checking handler.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-06-14 v1.0.0
 * @see v1.0.0
 */
public interface RiskSecurityHandler extends Ordered {

    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * Risk safety inspecting.
     * 
     * @param request
     * @param response
     * @return
     */
    @Nullable
    RespBase<Object> inspecting(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response);

}
