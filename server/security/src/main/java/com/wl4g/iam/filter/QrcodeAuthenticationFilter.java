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
package com.wl4g.iam.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wl4g.iam.authc.QrcodeAuthenticationToken;
import com.wl4g.iam.authc.ServerIamAuthenticationToken.RedirectInfo;
import com.wl4g.iam.core.annotation.IamFilter;

@IamFilter
public class QrcodeAuthenticationFilter extends AbstractServerIamAuthenticationFilter<QrcodeAuthenticationToken> {
    final public static String NAME = "qrcode";

    @Override
    protected QrcodeAuthenticationToken doCreateToken(
            String remoteHost,
            RedirectInfo redirectInfo,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getUriMapping() {
        return URI_BASE_MAPPING + NAME;
    }

}