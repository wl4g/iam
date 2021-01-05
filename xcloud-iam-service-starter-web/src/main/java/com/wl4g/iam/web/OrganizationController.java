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

import com.wl4g.component.common.web.rest.RespBase;
import com.wl4g.iam.common.bean.Organization;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.core.utils.IamOrganizationHolder;
import com.wl4g.iam.service.OrganizationService;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.wl4g.iam.core.utils.IamSecurityHolder.getPrincipalInfo;
import static org.apache.shiro.authz.annotation.Logical.AND;

/**
 * @author vjay
 * @date 2019-10-29 16:19:00
 */
@RestController
@RequestMapping("/organization")
public class OrganizationController {

	// @com.alibaba.dubbo.config.annotation.Reference
	@Autowired
	private OrganizationService groupService;

	@RequestMapping(value = "/getGroupsTree")
	@RequiresPermissions(value = { "iam:organization" }, logical = AND)
	public RespBase<?> getGroupsTree() {
		RespBase<Object> resp = RespBase.create();

		// Obtain login principal info.
		IamPrincipal info = getPrincipalInfo();
		List<Organization> groupsTree = groupService.getLoginOrganizationTree(info.getPrincipal(),info.getPrincipalId());

		resp.forMap().put("data", groupsTree);
		return resp;
	}

	@RequestMapping(value = "/save")
	@RequiresPermissions(value = { "iam:organization" }, logical = AND)
	public RespBase<?> save(@RequestBody Organization group) {
		RespBase<Object> resp = RespBase.create();
		groupService.save(group);
		return resp;
	}

	@RequestMapping(value = "/del")
	@RequiresPermissions(value = { "iam:organization" }, logical = AND)
	public RespBase<?> del(Long id) {
		RespBase<Object> resp = RespBase.create();
		groupService.del(id);
		return resp;
	}

	@RequestMapping(value = "/detail")
	@RequiresPermissions(value = { "iam:organization" }, logical = AND)
	public RespBase<?> detail(Long id) {
		RespBase<Object> resp = RespBase.create();
		Organization group = groupService.detail(id);
		resp.forMap().put("data", group);
		return resp;
	}

	@RequestMapping(value = "/getOrganizations")
	public RespBase<?> getOrganizationTree() {
		RespBase<Object> resp = RespBase.create();
		resp.forMap().put("tree", IamOrganizationHolder.getOrganizationTrees());
		resp.forMap().put("list", IamOrganizationHolder.getSessionOrganizations());
		return resp;
	}

}