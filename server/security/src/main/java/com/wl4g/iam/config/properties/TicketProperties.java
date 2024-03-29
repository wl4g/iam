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
package com.wl4g.iam.config.properties;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

import java.io.Serializable;

/**
 * IAM fast-CAS ticket authentication configuration properties
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年11月29日
 * @since
 */
public class TicketProperties implements Serializable {
    private static final long serialVersionUID = -2694422471852860689L;

    @Override
    public String toString() {
        return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
    }

}