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

import com.wl4g.component.core.bean.model.PageModel;
import com.wl4g.iam.common.bean.Role;
import com.wl4g.iam.common.subject.IamPrincipal;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * {@link RoleService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-10-29
 * @sine v1.0
 * @see
 */
@FeignClient("roleService")
//@RequestMapping("/role")
public interface RoleService {

	@GetMapping("/getLoginRoles")
	List<Role> getLoginRoles(@RequestBody IamPrincipal info);

	// TODO @RequestBody (feign)??
	@GetMapping("/list")
	PageModel<Role> list(PageModel<Role> pm, IamPrincipal info, String organizationId, String name, String displayName);

	@PostMapping("/save")
	void save(Role role);

	@DeleteMapping("/del")
	void del(Long id);

	@GetMapping("/detail")
	Role detail(Long id);

	@GetMapping("/findByUserId")
	List<Role> findByUserId(@RequestParam("userId") Long userId);

	@GetMapping("/findRoot")
	List<Role> findRoot(@RequestParam("groupIds") List<Long> groupIds, @RequestParam("roleCode") String roleCode,
			@RequestParam("nameZh") String nameZh);

}