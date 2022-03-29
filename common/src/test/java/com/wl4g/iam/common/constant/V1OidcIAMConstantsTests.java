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
package com.wl4g.iam.common.constant;

import static java.lang.System.out;

import org.junit.Test;

import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardResponseType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardScope;

/**
 * {@link V1OidcIAMConstantsTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-28 v3.0.0
 * @since v3.0.0
 */
public class V1OidcIAMConstantsTests {

    @Test
    public void testToSpaceSeparatedParams_token_id_token_code() {
        String response_type = " token   id_token%20%20code ";
        StandardResponseType result = StandardResponseType.of(response_type);
        out.println(result);
        assert result == StandardResponseType.code_id_token_token;
    }

    @Test
    public void testResponseTypeOf_id_token_code() {
        String response_type = "id_token code";
        StandardResponseType result = StandardResponseType.of(response_type);
        out.println(result);
        assert result == StandardResponseType.code_id_token; // expect is True
    }

    @Test
    public void testResponseTypeOf_id_token_token() {
        String response_type = "token id_token";
        StandardResponseType result = StandardResponseType.of(response_type);
        out.println(result);
        assert result == StandardResponseType.id_token_token; // expect is True
    }

    @Test
    public void testResponseTypeOf_token_code() {
        String response_type = "token code";
        StandardResponseType result = StandardResponseType.of(response_type);
        out.println(result);
        assert result == StandardResponseType.code_token; // expect is True
    }

    @Test
    public void testResponseTypeOf_id_token_token_code() {
        String response_type = "token id_token code";
        StandardResponseType result = StandardResponseType.of(response_type);
        out.println(result);
        assert result == StandardResponseType.code_id_token_token;
    }

    @Test
    public void testResponseType_code_containsIn_id_token_code() {
        String response_type = "id_token code";
        boolean result = StandardResponseType.code.containsIn(response_type);
        out.println(result);
        assert result; // expect is True
    }

    @Test
    public void testResponseType_code_id_token_containsIn_code_id_token() {
        String response_type = "id_token code";
        boolean result = StandardResponseType.code_id_token.containsIn(response_type);
        out.println(result);
        assert result; // expect is True
    }

    @Test
    public void testScopeType_openid_containsIn_openid_profile_email() {
        String scope = "openid%20profile%20email%20phone%20address";
        boolean result = StandardScope.openid.containsIn(scope);
        out.println(result);
        assert result; // expect is True
    }

}
