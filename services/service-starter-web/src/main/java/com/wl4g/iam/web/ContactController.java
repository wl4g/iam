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
package com.wl4g.iam.web;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.web.BaseController;
import com.wl4g.infra.core.page.PageHolder;
import com.wl4g.iam.common.bean.Contact;
import com.wl4g.iam.service.ContactService;

/**
 * @author vjay
 * @date 2019-08-05 11:44:00
 */
@RestController
@RequestMapping("/contact")
public class ContactController extends BaseController {

	// @com.alibaba.dubbo.config.annotation.Reference
	@Autowired
	private ContactService contactService;

	@RequestMapping(value = "/list")
	@RequiresPermissions(value = { "iam:contact" })
	public RespBase<?> list(PageHolder<Contact> pm, String name) {
		RespBase<Object> resp = RespBase.create();
		resp.setData(contactService.list(pm, name));
		return resp;
	}

	@RequestMapping(value = "/save")
	@RequiresPermissions(value = { "iam:contact" })
	public RespBase<?> save(@RequestBody Contact contact) {
		log.info("into ProjectController.save prarms::" + "contact = {} ", contact);
		RespBase<Object> resp = RespBase.create();
		Assert.notNull(contact, "contact is null");
		// Assert.hasText(contact.getName(), "name is null");
		// Assert.hasText(contact.getEmail(), "email is null");
		// Assert.notEmpty(contact.getGroups(), "contactGroup is null");

		contactService.save(contact);
		return resp;
	}

	@RequestMapping(value = "/detail")
	@RequiresPermissions(value = { "iam:contact" })
	public RespBase<?> detail(Long id) {
		log.info("into ContactController.detail prarms::" + "id = {} ", id);
		RespBase<Object> resp = RespBase.create();
		Contact contact = contactService.detail(id);
		resp.forMap().put("contact", contact);
		return resp;
	}

	@RequestMapping(value = "/del")
	@RequiresPermissions(value = { "iam:contact" })
	public RespBase<?> del(Long id) {
		log.info("into ContactController.del prarms::" + "id = {} ", id);
		RespBase<Object> resp = RespBase.create();
		contactService.del(id);
		return resp;
	}

}