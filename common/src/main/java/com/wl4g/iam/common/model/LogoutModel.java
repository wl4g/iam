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

/**
 * {@link LogoutModel}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2018-08-13
 * @since
 */
public final class LogoutModel extends BaseValidateModel {
    private static final long serialVersionUID = 1383145313778896117L;

    public LogoutModel() {
        super();
    }

    public LogoutModel(String application) {
        super(application);
    }

}