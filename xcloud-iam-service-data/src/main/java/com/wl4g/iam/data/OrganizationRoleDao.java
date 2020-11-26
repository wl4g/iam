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
import org.springframework.cloud.openfeign.FeignClient;

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
@FeignClient("organizationRoleDao")
@RequestMapping("/organizationRole")
public interface OrganizationRoleDao {

	int deleteByPrimaryKey(@RequestParam("id") Long id);

	int deleteByRoleId(@RequestParam("roleId") Long roleId);

	int deleteByGroupId(@RequestParam("groupId") Long groupId);

	int insert(@RequestBody OrganizationRole record);

	int insertSelective(@RequestBody OrganizationRole record);

	int insertBatch(@Param("groupRoles") List<OrganizationRole> groupRoles);

	OrganizationRole selectByPrimaryKey(@RequestParam("id") Long id);

	List<Long> selectGroupIdByRoleId(@RequestParam("roleId") Long roleId);

	List<Long> selectRoleIdsByGroupId(@RequestParam("groupId") Long groupId);

	int updateByPrimaryKeySelective(@RequestBody OrganizationRole record);

	int updateByPrimaryKey(@RequestBody OrganizationRole record);

}