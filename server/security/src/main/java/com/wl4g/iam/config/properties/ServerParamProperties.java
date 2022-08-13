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

import com.wl4g.iam.core.config.AbstractIamProperties.ParamProperties;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;

/**
 * IAM server parameters configuration properties
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年11月29日
 * @since
 */
public class ServerParamProperties extends ParamProperties {
    private static final long serialVersionUID = 3258460473711285504L;

    /**
     * Password parameter name at login time of account password.
     */
    private String credentialsName = "credential";

    /**
     * Client type reference parameter name at login time of account password.
     */
    private String clientRefName = "client_ref";

    /**
     * UmidToken parameter name.
     */
    private String umidTokenName = "umidToken";

    /**
     * Secure asymmetric cryptic algorithm name.
     * 
     * @see {@link CryptKind }
     */
    private String secretAlgKindName = "alg";

    /**
     * Verification verifiedToken parameter name.
     */
    private String verifiedTokenName = "verifiedToken";

    /**
     * Dynamic verification code operation action type parameter key-name.
     */
    private String smsActionName = "action";

    public String getCredentialsName() {
        return credentialsName;
    }

    public void setCredentialsName(String loginPassword) {
        this.credentialsName = loginPassword;
    }

    public String getClientRefName() {
        return clientRefName;
    }

    public void setClientRefName(String clientRefName) {
        this.clientRefName = clientRefName;
    }

    public String getUmidTokenName() {
        return umidTokenName;
    }

    public void setUmidTokenName(String umidTokenName) {
        this.umidTokenName = umidTokenName;
    }

    public String getSecretAlgKindName() {
        return secretAlgKindName;
    }

    public void setSecretAlgKindName(String cryptAlgKindName) {
        this.secretAlgKindName = cryptAlgKindName;
    }

    public String getVerifiedTokenName() {
        return verifiedTokenName;
    }

    public void setVerifiedTokenName(String verifiedTokenName) {
        this.verifiedTokenName = verifiedTokenName;
    }

    public String getSmsActionName() {
        return smsActionName;
    }

    public void setSmsActionName(String smsActionName) {
        this.smsActionName = smsActionName;
    }

}