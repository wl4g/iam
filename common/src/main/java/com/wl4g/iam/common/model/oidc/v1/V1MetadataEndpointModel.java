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
package com.wl4g.iam.common.model.oidc.v1;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Wither;

/**
 * {@link V1MetadataEndpointModel}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-17 v1.0.0
 * @since v1.0.0
 */
@Getter
@Setter
@ToString
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V1MetadataEndpointModel {
    private String issuer; // REQUIRED
    private String authorization_endpoint; // REQUIRED
    // Unless only the Implicit-Flow is used.
    private String token_endpoint; // REQUIRED
    private String userinfo_endpoint; // RECOMMENDED
    private String jwks_uri; // REQUIRED
    private String introspection_endpoint;
    private List<String> scopes_supported; // RECOMMENDED
    private List<String> response_types_supported; // REQUIRED
    private List<String> grant_types_supported; // OPTIONAL
    private List<String> subject_types_supported; // REQUIRED
    private List<String> id_token_signing_alg_values_supported; // REQUIRED
    private List<String> claims_supported;
    // PKCE support advertised
    private List<String> code_challenge_methods_supported;
}
