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

import com.wl4g.component.rpc.springboot.feign.annotation.SpringBootFeignClient;
import com.wl4g.iam.common.bean.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * {@link UserDao}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@SpringBootFeignClient("${spring.application.name:user-dao}")
@RequestMapping("/user")
public interface UserDao {

	@RequestMapping(value = "/deleteByPrimaryKey", method = { POST })
	int deleteByPrimaryKey(@RequestParam("id") Long id);

	@RequestMapping(method = POST, value = "/insert")
	int insert(@RequestBody User record);

	@RequestMapping(method = POST, value = "/insertSelective")
	int insertSelective(@RequestBody User record);

	@RequestMapping(method = GET, value = "/selectByPrimaryKey")
	User selectByPrimaryKey(@RequestParam("id") Long id);

	@RequestMapping(method = POST, value = "/updateByPrimaryKeySelective")
	int updateByPrimaryKeySelective(@RequestBody User record);

	@RequestMapping(method = POST, value = "/updateByPrimaryKey")
	int updateByPrimaryKey(@RequestBody User record);

	@RequestMapping(method = GET, value = "/list")
	List<User> list(@RequestParam(value = "userId", required = false) @Param("userId") Long userId,
			@RequestParam(value = "userName", required = false) @Param("userName") String userName,
			@RequestParam(value = "nameZh", required = false) @Param("nameZh") String nameZh,
			@RequestParam(value = "roleId", required = false) @Param("roleId") Long roleId);

	@RequestMapping(method = GET, value = "/selectByUserName")
	User selectByUserName(@RequestParam("userName") String userName);

	@RequestMapping(method = GET, value = "/selectByUnionIdOrOpenId")
	User selectByUnionIdOrOpenId(@RequestParam("unionId") @Param("unionId") String unionId,
			@RequestParam("openId") @Param("openId") String openId);

}