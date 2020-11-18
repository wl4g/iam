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

import com.wl4g.components.core.web.model.PageModel;
import com.wl4g.iam.common.bean.ContactGroup;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * {@link ContactGroupService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-08-05
 * @sine v1.0
 * @see
 */
@FeignClient("contactGroupService")
public interface ContactGroupService {

	@GetMapping("/save")
	void save(ContactGroup contactGroup);

	@GetMapping("/del")
	void del(Long id);

	@GetMapping("/findContactGroups")
	List<ContactGroup> findContactGroups(String name);

	@GetMapping("/list")
	PageModel<ContactGroup> list(PageModel<ContactGroup> pm, String name);

}