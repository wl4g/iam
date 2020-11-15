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
package com.wl4g.iam.web;

import com.wl4g.components.common.web.rest.RespBase;
import com.wl4g.components.core.utils.bean.BeanMapConvert;
import com.wl4g.components.core.web.BaseController;
import com.wl4g.iam.common.bean.ClusterConfig;
import com.wl4g.iam.core.web.model.SessionAttributeModel;
import com.wl4g.iam.service.ClusterConfigService;
import com.wl4g.iam.web.model.SessionDestroyClientModel;
import com.wl4g.iam.web.model.SessionQueryClientModel;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_S_API_V1_BASE;
import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_S_API_V1_SESSION;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

/**
 * IAM management API v1 controller.</br>
 * For example, get the API of Iam service of remote independent deployment.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019年11月4日
 * @since
 */
@SuppressWarnings("deprecation")
@RestController
@RequestMapping("/mgr/v1")
public class IamManagerApiV1Controller extends BaseController {

	@Autowired
	protected RestTemplate restTemplate;

	@Autowired
	private ClusterConfigService clusterConfigService;

	/**
	 * Find IAM server list of sys_cluster_config.
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(path = "findIamServers")
	@RequiresPermissions(value = { "iam:online" })
	public RespBase<?> findIamServers() throws Exception {
		RespBase<Object> resp = RespBase.create();
		resp.setData(clusterConfigService.findOfIamServers());
		return resp;
	}

	/**
	 * Obtain remote IAM server sessions.
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(path = "getSessions")
	@RequiresPermissions(value = { "iam:online" })
	public RespBase<?> getRemoteSessions(@Validated SessionQueryClientModel query) throws Exception {
		log.info("Get remote sessions for <= {} ...", query);

		// Get remote IAM base URI.
		ClusterConfig config = clusterConfigService.getClusterConfig(query.getId());
		String url = getRemoteApiV1SessionUri(config.getExtranetBaseUri());
		url += "?".concat(new BeanMapConvert(query).toUriParmaters());
		log.info("Request get remote sessions for clusterConfigId: {}, URL: {}", query.getId(), url);

		// Do exchange.
		RespBase<SessionAttributeModel> resp = restTemplate
				.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<RespBase<SessionAttributeModel>>() {
				}).getBody();

		log.info("Got remote sessions response for => {}", resp);
		return resp;
	}

	/**
	 * Destroy cleanup remote session.
	 * 
	 * @param destroy
	 * @return
	 * @throws Exception
	 */
	@PostMapping(path = "destroySessions")
	@RequiresPermissions(value = { "iam:online" })
	public RespBase<?> destroyRemoteSessions(@Validated SessionDestroyClientModel destroy) throws Exception {
		log.info("Destroy remote sessions by <= {}", destroy);

		// Get cluster configuration.
		ClusterConfig config = clusterConfigService.getClusterConfig(destroy.getId());
		notNull(config, String.format("", destroy.getId()));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> entity = new HttpEntity<>(destroy, headers);
		String url = getRemoteApiV1SessionUri(config.getExtranetBaseUri());
		log.info("Request destroy remote sessions for clusterConfigId: {}, URL: {}", destroy.getId(), url);

		// Do request.
		RespBase<?> resp = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<RespBase<?>>() {
		}).getBody();

		log.info("Destroyed remote sessions response for => {}", resp);
		return resp;
	}

	/**
	 * Get remote API v1 session URI.
	 * 
	 * @param remoteBaseUri
	 * @return
	 */
	private String getRemoteApiV1SessionUri(String remoteBaseUri) {
		hasText(remoteBaseUri, "Iam mangement for to remoteApiBase URI must not be empty");
		return remoteBaseUri + URI_S_API_V1_BASE + URI_S_API_V1_SESSION;
	}

}