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
package com.wl4g.iam.web.login.model;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

/**
 * Simple Risk control UMID result
 * 
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2020年3月25日
 * @since
 */
public class SimpleRiskTokenModel implements Serializable {
    private static final long serialVersionUID = -3434524148761808680L;

    /**
     * Risk control UMID token.
     */
    @NotBlank
    private String umidToken;

    public SimpleRiskTokenModel() {
        super();
    }

    public SimpleRiskTokenModel(String umidToken) {
        super();
        this.umidToken = umidToken;
    }

    public String getUmidToken() {
        return umidToken;
    }

    public void setUmidToken(String umidToken) {
        this.umidToken = umidToken;
    }

}