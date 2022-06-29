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
package com.wl4g.iam.client.validation;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.common.web.rest.RespBase.RetCode;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.common.subject.SimpleIamPrincipal;
import com.wl4g.iam.common.model.ServiceTicketValidateRequest;
import com.wl4g.iam.common.model.ServiceTicketValidateModel;
import com.wl4g.iam.core.exception.IllegalApplicationAccessException;
import com.wl4g.iam.core.exception.InvalidGrantTicketException;
import com.wl4g.iam.core.exception.ServiceTicketValidateException;

import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_VALIDATE;
import static java.util.Objects.nonNull;

import java.util.Map;

/**
 * Fast-CAS ticket validator
 * 
 * @author James Wong <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月29日
 * @since
 */
public class FastCasTicketIamValidator
        extends AbstractBasedIamValidator<ServiceTicketValidateRequest, ServiceTicketValidateModel<SimpleIamPrincipal>> {

    public FastCasTicketIamValidator(IamClientProperties config, RestTemplate restTemplate) {
        super(config, restTemplate);
    }

    @Override
    protected void postQueryParameterSet(ServiceTicketValidateRequest req, Map<String, Object> queryParams) {
        queryParams.put(config.getParam().getGrantTicket(), req.getTicket());
    }

    @Override
    public ServiceTicketValidateModel<SimpleIamPrincipal> validate(ServiceTicketValidateRequest req)
            throws ServiceTicketValidateException {
        final RespBase<ServiceTicketValidateModel<SimpleIamPrincipal>> resp = doIamRemoteValidate(URI_IAM_SERVER_VALIDATE, req);
        if (!RespBase.isSuccess(resp)) {
            // Only if the error is not authenticated, can it be redirected to
            // the IAM server login page, otherwise the client will display the
            // error page directly (to prevent unlimited redirection).
            /** See:{@link CentralAuthenticatorController#validate()} */
            if (RespBase.eq(resp, RetCode.UNAUTHC)) {
                throw new InvalidGrantTicketException(resp.getMessage());
            } else if (RespBase.eq(resp, RetCode.UNAUTHZ)) {
                throw new IllegalApplicationAccessException(resp.getMessage());
            }
            throw new ServiceTicketValidateException(nonNull(resp) ? resp.getMessage() : "Unknown error");
        }
        return resp.getData();
    }

    @Override
    protected ParameterizedTypeReference<RespBase<ServiceTicketValidateModel<SimpleIamPrincipal>>> getTypeReference() {
        return new ParameterizedTypeReference<RespBase<ServiceTicketValidateModel<SimpleIamPrincipal>>>() {
        };
    }

}