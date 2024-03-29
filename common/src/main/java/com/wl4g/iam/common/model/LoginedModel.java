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
package com.wl4g.iam.common.model;

import javax.validation.constraints.NotBlank;

import com.wl4g.infra.common.serialize.JacksonUtils;

/**
 * Application grant ticket wrap
 * 
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年11月22日
 * @since ServiceTicket
 */
public final class LoginedModel {

    /**
     * Temporary authorization code(Used for fast-CAS login successfully
     * returned to application), only single use of work is effective.
     */
    @NotBlank
    private String grantTicket;

    public LoginedModel() {
        super();
    }

    public LoginedModel(String grantTicket) {
        this.setGrantTicket(grantTicket);
    }

    public final String getGrantTicket() {
        return grantTicket;
    }

    public final void setGrantTicket(String serviceTicket) {
        this.grantTicket = serviceTicket;
    }

    @Override
    public String toString() {
        return JacksonUtils.toJSONString(this);
    }

}