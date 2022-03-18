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
package com.wl4g.iam.web.risk;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.TypeConverts.*;
import static com.wl4g.infra.common.web.WebUtils2.toQueryParams;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.*;
import static com.wl4g.iam.core.utils.IamAuthenticatingUtils.sessionStatus;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wl4g.infra.common.codec.Base58;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.iam.annotation.SimpleRiskController;
import com.wl4g.iam.handler.risk.SimpleRiskEvaluateHandler;
import com.wl4g.iam.web.BaseIamController;
import com.wl4g.iam.web.login.model.SimpleRiskTokenModel;

/**
 * Simple risk control controller.
 * 
 * Note: it is a simple version of the implementation of risk control
 * inspection. It is recommended to use a more professional external
 * RiskControlService in the production environment.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年3月25日
 * @since
 */
@SimpleRiskController
public class SimpleRiskEvaluateController extends BaseIamController {

    @Autowired
    protected SimpleRiskEvaluateHandler handler;

    /**
     * Apply umidToken, such as client submits UA and device fingerprint
     * information, and server returns session ID.
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = URI_IAM_SERVER_RCM_UMTOKEN_APPLY, method = { POST })
    @ResponseBody
    public RespBase<?> applyUmidToken(@RequestParam("umdata") String umdata, HttpServletRequest request) throws Exception {
        RespBase<Object> resp = RespBase.create(sessionStatus());
        log.info("Apply umidToken, parsed umdata: {}", umdata);

        // Decode umdata.
        umdata = URLDecoder.decode(umdata, "UTF-8");
        // Parse risk control parameter.
        Map<String, String> paramMap = parseUmdataParameters(umdata);

        // [Simple risk control processing]
        String umidToken = handler.applyUmidToken(paramMap);
        resp.setData(new SimpleRiskTokenModel(umidToken));
        return resp;
    }

    /**
     * Parse umdata to parameter.
     * 
     * @param umdata
     * @return
     */
    public static Map<String, String> parseUmdataParameters(String umdata) {
        if (isBlank(umdata) || !umdata.contains("!")) {
            throw new IllegalArgumentException("Invalid umdata");
        }

        // Original algorithm: base58 Re-encoding the fingerprint set data after
        // random iteration n%3+1 times
        int n = parseIntOrDefault(umdata.substring(0, umdata.indexOf("!")));
        String umItemData = umdata.substring(umdata.indexOf("!") + 1);
        int iterations = n % 3 + 1;
        for (int i = 0; i < iterations; i++) {
            umItemData = new String(Base58.decodeBase58(umItemData));
        }

        // To risk control parameters.
        return safeMap(toQueryParams(umItemData));
    }

}