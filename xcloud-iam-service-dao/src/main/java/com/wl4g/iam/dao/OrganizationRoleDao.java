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
package com.wl4g.iam.dao;

import org.apache.ibatis.annotations.Param;

import com.wl4g.iam.common.bean.OrganizationRole;

import java.util.List;

public interface OrganizationRoleDao {
	int deleteByPrimaryKey(Long id);

	int deleteByRoleId(Long roleId);

	int deleteByGroupId(Long groupId);

	int insert(OrganizationRole record);

	int insertSelective(OrganizationRole record);

	int insertBatch(@Param("groupRoles") List<OrganizationRole> groupRoles);

	OrganizationRole selectByPrimaryKey(Long id);

	List<Long> selectGroupIdByRoleId(Long roleId);

	List<Long> selectRoleIdsByGroupId(Long groupId);

	int updateByPrimaryKeySelective(OrganizationRole record);

	int updateByPrimaryKey(OrganizationRole record);
}