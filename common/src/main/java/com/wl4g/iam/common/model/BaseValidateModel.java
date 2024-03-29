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

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import org.springframework.util.Assert;

import com.wl4g.infra.common.serialize.JacksonUtils;

/**
 * {@link BaseValidateModel}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2018-08-13
 * @since
 */
public class BaseValidateModel implements Serializable {
    private static final long serialVersionUID = 151897009229689455L;

    @NotBlank
    private String application;

    public BaseValidateModel() {
        super();
    }

    public BaseValidateModel(String application) {
        setApplication(application);
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        Assert.hasText(application, "Application name must not be empty.");
        if (!"NULL".equalsIgnoreCase(application)) {
            this.application = application;
        }
    }

    @Override
    public String toString() {
        return JacksonUtils.toJSONString(this);
    }

}