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

import static org.springframework.web.bind.annotation.RequestMethod.*;


import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.component.core.bean.model.PageHolder;
import com.wl4g.component.rpc.springboot.feign.annotation.SpringBootFeignClient;
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
@SpringBootFeignClient("contactService")
@RequestMapping("/contact")
public interface ContactService {

	@RequestMapping(value = "/save", method = POST)
	void save(Contact contact);

	@RequestMapping(value = "/detail", method = GET)
	Contact detail(Long id);

	@RequestMapping(value = "/del", method = POST)
	void del(Long id);

	@RequestMapping(value = "/list", method = GET)
	PageHolder<Contact> list(PageHolder<Contact> pm, String name);

	/**
	 * Notification sending with template message .
	 * 
	 * @param templateKey
	 * @param parameters
	 * @param contactGroupIds
	 */
	@RequestMapping(value = "/notification", method = POST)
	void notification(@NotBlank String templateKey, @Nullable Map<String, Object> parameters,
			@Nullable List<Long> contactGroupIds);

}