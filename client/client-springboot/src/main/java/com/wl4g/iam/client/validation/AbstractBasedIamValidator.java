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
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import static org.springframework.http.HttpMethod.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_BASE;
import static java.lang.String.format;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.utils.bean.BeanMapConvert;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.common.model.BaseValidateModel;

/**
 * Abstract validator implementation for tickets that must be validated against
 * a server.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月19日
 * @since
 */
@SuppressWarnings("deprecation")
public abstract class AbstractBasedIamValidator<R extends BaseValidateModel, A> implements IamValidator<R, A> {

	final protected SmartLogger log = getLogger(getClass());

	/**
	 * IAM client properties
	 */
	final protected IamClientProperties config;

	/**
	 * RestTemplate
	 */
	final protected RestTemplate restTemplate;

	/**
	 * Constructs a new TicketValidator with the casServerUrlPrefix.
	 *
	 * @param casServerUrlPrefix
	 *            the location of the CAS server.
	 */
	protected AbstractBasedIamValidator(IamClientProperties config, RestTemplate restTemplate) {
		Assert.notNull(config, "'iamClientProperties' cannot be null.");
		Assert.notNull(restTemplate, "'restTemplate' cannot be null.");
		this.config = config;
		this.restTemplate = restTemplate;
	}

	/**
	 * Contacts the CAS Server to retrieve the response for the ticket
	 * validation.
	 *
	 * @param endpoint
	 *            the validate API endpoint
	 * @param req
	 *            the ticket parameters.
	 * @return the response from the CAS server.
	 */
	protected RespBase<A> doIamRemoteValidate(String endpoint, R req) {
		hasTextOf(endpoint, "validateEndpoint");
		notNullOf(req, "validateParameters");

		StringBuffer url = new StringBuffer(config.getServerUri());
		url.append(URI_IAM_SERVER_BASE).append("/").append(endpoint).append("?");

		// To request query parameters
		Map<String, Object> params = new LinkedHashMap<String, Object>() {
			private static final long serialVersionUID = -7635430767361691087L;
			{
				put(config.getParam().getApplication(), req.getApplication());
			}
		};

		// Process URL query parameters
		postQueryParameterSet(req, params);

		// Append parameters to URL
		url.append(BeanMapConvert.toUriParmaters(params));
		log.debug("Validating to : {}", url);

		// Add headers
		HttpEntity<R> entity = new HttpEntity<>(req, new LinkedMultiValueMap<String, String>(1) {
			private static final long serialVersionUID = -630070874678386724L;
			{
				add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			}
		});

		// Request execute
		RespBase<A> resp = null;
		try {
			resp = restTemplate.exchange(url.toString(), POST, entity, getTypeReference()).getBody();
			log.debug("Validate retrieved: {}", resp);
		} catch (Throwable ex) {
			throw new RestClientException(format("Failed to validate from : %s", url), ex);
		}

		return resp;
	}

	/**
	 * URL and parameter subsequent processing setting. See: {@link Synchronize
	 * with xx.xx.session.mgt.IamSessionManager#getSessionId}
	 * 
	 * @param req
	 *            request model
	 * @param queryParams
	 *            Validation request parameters
	 */
	protected void postQueryParameterSet(R req, Map<String, Object> queryParams) {

	}

	/**
	 * Get parameterizedTypeReference
	 * 
	 * @return
	 */
	protected abstract ParameterizedTypeReference<RespBase<A>> getTypeReference();

}