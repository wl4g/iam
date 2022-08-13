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

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.springframework.util.CollectionUtils;

import com.wl4g.infra.common.serialize.JacksonUtils;

/**
 * Session validation assertion.
 * 
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @Long 2018年11月22日
 * @since
 */
public final class SessionValidateModel extends BaseValidateModel {
    private static final long serialVersionUID = 5483716885382988025L;

    @NotEmpty
    private List<String> tickets = new ArrayList<>();

    public SessionValidateModel() {
    }

    public SessionValidateModel(String application) {
        super(application);
    }

    public List<String> getTickets() {
        return tickets;
    }

    public void setTickets(List<String> tickets) {
        if (!CollectionUtils.isEmpty(tickets)) {
            this.tickets.addAll(tickets);
        }
    }

    @Override
    public String toString() {
        return JacksonUtils.toJSONString(this);
    }

}