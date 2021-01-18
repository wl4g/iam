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
package com.wl4g.iam.service;

import com.wl4g.component.core.bean.model.PageHolder;
import com.wl4g.component.rpc.springboot.feign.annotation.SpringBootFeignClient;
import com.wl4g.iam.common.bean.Role;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * {@link RoleService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-10-29
 * @sine v1.0
 * @see
 */
@SpringBootFeignClient("${spring.application.name:role-service}")
@RequestMapping("/role")
public interface RoleService {

	@RequestMapping(value = "/getLoginRoles", method = POST)
	List<Role> getLoginRoles();

	// has too many Body parameters:
	@RequestMapping(value = "/list", method = POST)
	PageHolder<Role> list(PageHolder<Role> pm, @RequestParam(value = "organizationId",required = false) String organizationId,
			@RequestParam(value = "name",required = false) String name, @RequestParam(value = "displayName",required = false) String displayName);

	@RequestMapping(value = "/save", method = POST)
	void save(Role role);

	@RequestMapping(value = "/del", method = POST)
	void del(@RequestParam("id") Long id);

	@RequestMapping(value = "/detail", method = GET)
	Role detail(@RequestParam("id") Long id);

	@RequestMapping(value = "/findByUserId", method = GET)
	List<Role> findByUserId(@RequestParam("userId") Long userId);

	@RequestMapping(value = "/findRoot", method = POST)
	List<Role> findRoot(@RequestParam(value = "groupIds", required = false) List<Long> groupIds,
			@RequestParam(value = "roleCode", required = false) String roleCode,
			@RequestParam(value = "nameZh", required = false) String nameZh);

}