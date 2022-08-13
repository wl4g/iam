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
package com.wl4g.iam.common.bean;

import com.wl4g.infra.core.bean.BaseBean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Application cluster of environment configuration bean.
 * 
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2019年11月7日
 * @since
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class FastCasClientInfo extends BaseBean {
    private static final long serialVersionUID = -7546448616357790576L;
    private String appName;
    private Integer type;
    private String envType;
    private String viewExtranetBaseUri;
    private String extranetBaseUri;
    private String intranetBaseUri;

    public FastCasClientInfo(String appName, String viewExtranetBaseUri) {
        this.appName = appName;
        this.viewExtranetBaseUri = viewExtranetBaseUri;
    }

}