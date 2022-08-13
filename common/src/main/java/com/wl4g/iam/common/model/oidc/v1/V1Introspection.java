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
package com.wl4g.iam.common.model.oidc.v1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link V1Introspection}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v1.0.0
 * @see https://datatracker.ietf.org/doc/html/rfc7662#section-2.2
 * @see https://datatracker.ietf.org/doc/html/rfc7519
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class V1Introspection {
    private String iss; // (Issuer) Claim
    private String sub; // (Subject) Claim
    private Boolean active;
    private String scope;
    private String client_id;
    private String username;
    private String token_type;

    /**
     * https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.4
     */
    private Long exp; // (Expiration Time) Claim

    /**
     * https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.6
     */
    private Long iat; // (Issued At) Claim

    /**
     * https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.5
     */
    private Long nbf; // (Not Before) Claim
}