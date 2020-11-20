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
import org.springframework.cloud.openfeign.FeignClient;

import com.wl4g.iam.common.bean.Role;

import java.util.List;

/**
 * {@link RoleDao}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@FeignClient("roleDao")
public interface RoleDao {

	int deleteByPrimaryKey(Long id);

	int insert(Role record);

	int insertSelective(Role record);

	Role selectByPrimaryKey(Long id);

	List<Role> selectWithRoot(@Param("groupIds") List<Long> groupIds, @Param("roleCode") String roleCode,
			@Param("nameZh") String nameZh);

	int updateByPrimaryKeySelective(Role record);

	int updateByPrimaryKey(Role record);

	List<Role> selectByUserId(Long userId);

	List<Role> selectByGroupId(Long groupId);

	List<Role> selectByGroupIdsAndUserId(@Param("groupIds") List<Long> groupIds, @Param("userId") String userId,
			@Param("roleCode") String roleCode, @Param("nameZh") String nameZh);

	List<Role> list(@Param("groupIds") List<Long> groupIds, @Param("roleCode") String roleCode, @Param("nameZh") String nameZh);

	int countRoleUsers(Long roleId);

}