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
@SpringBootFeignClient("roleDao")
@RequestMapping("/role")
public interface RoleDao {

	@RequestMapping(value = "/deleteByPrimaryKey", method = { POST })
	int deleteByPrimaryKey(@RequestParam("id") @Param("id") Long id);

	@RequestMapping(method = { POST }, value = "/insert")
	int insert(@RequestBody Role record);

	@RequestMapping(method = { POST }, value = "/insertSelective")
	int insertSelective(@RequestBody Role record);

	@RequestMapping(method = { GET }, value = "/selectByPrimaryKey")
	Role selectByPrimaryKey(@RequestParam("id") Long id);

	@RequestMapping(method = { GET }, value = "/selectWithRoot")
	List<Role> selectWithRoot(@RequestParam("groupIds") @Param("groupIds") List<Long> groupIds,
			@RequestParam("roleCode") @Param("roleCode") String roleCode, @RequestParam("nameZh") @Param("nameZh") String nameZh);

	@RequestMapping(method = { POST }, value = "/updateByPrimaryKeySelective")
	int updateByPrimaryKeySelective(@RequestBody Role record);

	@RequestMapping(method = { POST }, value = "/updateByPrimaryKey")
	int updateByPrimaryKey(@RequestBody Role record);

	@RequestMapping(method = { GET }, value = "/selectByUserId")
	List<Role> selectByUserId(@RequestParam("userId") @Param("userId") Long userId);

	@RequestMapping(method = { GET }, value = "/selectByGroupId")
	List<Role> selectByGroupId(@RequestParam("groupId") @Param("groupId") Long groupId);

	@RequestMapping(method = { GET }, value = "/selectByGroupIdsAndUserId")
	List<Role> selectByGroupIdsAndUserId(@RequestParam("groupIds") @Param("groupIds") List<Long> groupIds,
			@RequestParam("userId") @Param("userId") String userId, @RequestParam("roleCode") @Param("roleCode") String roleCode,
			@RequestParam("nameZh") @Param("nameZh") String nameZh);

	@RequestMapping(method = { GET }, value = "/list")
	List<Role> list(@RequestParam("groupIds") @Param("groupIds") List<Long> groupIds,
			@RequestParam("roleCode") @Param("roleCode") String roleCode, @RequestParam("nameZh") @Param("nameZh") String nameZh);

	@RequestMapping(method = { GET }, value = "/countRoleUsers")
	int countRoleUsers(@RequestParam("roleId") Long roleId);

}