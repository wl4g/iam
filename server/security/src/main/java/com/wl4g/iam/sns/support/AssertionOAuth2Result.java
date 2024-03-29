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
package com.wl4g.iam.sns.support;

import java.io.Serializable;

import org.springframework.http.ResponseEntity;

/**
 * {@link AssertionOAuth2Result}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年2月9日 v1.0.0
 * @see
 */
public interface AssertionOAuth2Result extends Serializable {

    /**
     * Used to assert the correctness of oauth2 authorization information.
     * 
     * @param resp
     * @return
     */
    Object validate(ResponseEntity<String> resp);

}