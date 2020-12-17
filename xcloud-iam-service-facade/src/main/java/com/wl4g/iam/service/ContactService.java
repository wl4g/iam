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

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.component.core.bean.model.PageModel;
import com.wl4g.iam.common.bean.Contact;

/**
 * {@link ContactService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @version v1.0 2019-08-05
 * @sine v1.0
 * @see
 */
@FeignClient("contactService")
//@RequestMapping("/contact")
public interface ContactService {

	@PostMapping("/save")
	void save(Contact contact);

	@GetMapping("/detail")
	Contact detail(Long id);

	@DeleteMapping("/del")
	void del(Long id);

	@GetMapping("/list")
	PageModel<Contact> list(PageModel<Contact> pm, String name);

	/**
	 * Notification sending with template message .
	 * 
	 * @param templateKey
	 * @param parameters
	 * @param contactGroupIds
	 */
	@GetMapping("/notification")
	void notification(@NotBlank String templateKey, @Nullable Map<String, Object> parameters,
			@Nullable List<Long> contactGroupIds);

}