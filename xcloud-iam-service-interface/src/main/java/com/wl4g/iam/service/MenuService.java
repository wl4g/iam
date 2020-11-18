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

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.wl4g.iam.common.bean.Menu;

/**
 * {@link MenuService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-10-30
 * @sine v1.0
 * @see
 */
@FeignClient("menuService")
public interface MenuService {

	@GetMapping("/getMenuTree")
	Map<String, Object> getMenuTree();

	@GetMapping("/getMenuList")
	List<Menu> getMenuList();

	@GetMapping("/save")
	void save(Menu menu);

	@GetMapping("/del")
	void del(Long id);

	@GetMapping("/detail")
	Menu detail(Long id);

}