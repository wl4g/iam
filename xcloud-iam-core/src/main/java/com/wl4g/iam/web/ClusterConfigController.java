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
import com.wl4g.components.core.web.BaseController;
import com.wl4g.iam.service.ClusterConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link ClusterConfigController}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-09-16
 * @sine v1.0.0
 * @see
 */
@RestController
@RequestMapping("/clusterConfig")
public class ClusterConfigController extends BaseController {

	@Autowired
	private ClusterConfigService clusterConfigService;

	/**
	 * System initialization load dict information.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/loadInit")
	public RespBase<?> loadInit() {
		RespBase<Object> resp = RespBase.create();
		resp.setData(clusterConfigService.loadInit());
		return resp;
	}

}