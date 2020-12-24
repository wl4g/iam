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
import com.wl4g.iam.common.bean.Menu;
import com.wl4g.iam.common.bean.User;

import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.Set;

/**
 * {@link UserService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-10-28
 * @sine v1.0
 * @see
 */
@SpringBootFeignClient("userService")
@RequestMapping("/user")
public interface UserService {

	@RequestMapping(value = "/findSimpleUser", method = { GET })
	User findSimpleUser(Long id);

	@RequestMapping(value = "/getMenusByUser", method = { GET })
	Set<Menu> getMenusByUser(Long userId);

	@RequestMapping(path = "/list", method = RequestMethod.GET)
	PageHolder<User> list(PageHolder<User> pm, @RequestParam("principalId") String principalId,
			@RequestParam("principal") String principal, @RequestParam("userName") String userName,
			@RequestParam("displayName") String displayName, @RequestParam("roleId") Long roleId);

	@RequestMapping(value = "/save", method = { POST })
	void save(User user);

	@RequestMapping(value = "/del", method = { POST })
	void del(Long userId);

	@RequestMapping(value = "/detail", method = { GET })
	User detail(Long userId);

	@RequestMapping(value = "/findByUserName", method = { GET })
	User findByUserName(@RequestParam("userName") String userName);

	@RequestMapping(value = "/findByUnionIdOrOpenId", method = { GET })
	User findByUnionIdOrOpenId(@RequestParam("unionId") String unionId, @RequestParam("openId") String openId);

}