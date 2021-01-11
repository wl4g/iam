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
package com.wl4g.iam.data;

import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.wl4g.component.rpc.springboot.feign.annotation.SpringBootFeignClient;
import com.wl4g.iam.common.bean.OrganizationRole;

import java.util.List;

/**
 * {@link OrganizationRoleDao}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@SpringBootFeignClient("organizationRole-dao")
@RequestMapping("/organizationRole")
public interface OrganizationRoleDao {

	@RequestMapping(value = "/deleteByPrimaryKey", method = { POST })
	int deleteByPrimaryKey(@RequestParam("id") Long id);

	@RequestMapping(method = POST, value = "/deleteByRoleId")
	int deleteByRoleId(@RequestParam("roleId") Long roleId);

	@RequestMapping(method = POST, value = "/deleteByGroupId")
	int deleteByGroupId(@RequestParam("groupId") Long groupId);

	@RequestMapping(method = POST, value = "/insert")
	int insert(@RequestBody OrganizationRole record);

	@RequestMapping(method = POST, value = "/insertSelective")
	int insertSelective(@RequestBody OrganizationRole record);

	@RequestMapping(method = POST, value = "/insertBatch")
	int insertBatch(@Param("groupRoles") List<OrganizationRole> groupRoles);

	@RequestMapping(method = GET, value = "/selectByPrimaryKey")
	OrganizationRole selectByPrimaryKey(@RequestParam("id") Long id);

	@RequestMapping(method = GET, value = "/selectGroupIdByRoleId")
	List<Long> selectGroupIdByRoleId(@RequestParam("roleId") Long roleId);

	@RequestMapping(method = GET, value = "/selectRoleIdsByGroupId")
	List<Long> selectRoleIdsByGroupId(@RequestParam("groupId") Long groupId);

	@RequestMapping(method = POST, value = "/updateByPrimaryKeySelective")
	int updateByPrimaryKeySelective(@RequestBody OrganizationRole record);

	@RequestMapping(method = POST, value = "/updateByPrimaryKey")
	int updateByPrimaryKey(@RequestBody OrganizationRole record);

}