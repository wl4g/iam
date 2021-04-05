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

import com.wl4g.component.core.page.PageHolder;
import com.wl4g.component.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.iam.common.bean.ContactGroup;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * {@link ContactGroupService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-08-05
 * @sine v1.0
 * @see
 */
@FeignConsumer("${provider.serviceId.iam-facade:contactGroup-service}")
@RequestMapping("/contactGroup-service")
public interface ContactGroupService {

	@RequestMapping(value = "/list", method = POST)
	PageHolder<ContactGroup> list(@RequestBody PageHolder<ContactGroup> pm, @RequestParam("name") String name);

	@RequestMapping(value = "/save", method = POST)
	void save(@RequestBody ContactGroup contactGroup);

	@RequestMapping(value = "/del", method = { DELETE })
	void del(@RequestParam("id") Long id);

	@RequestMapping(value = "/findContactGroups", method = GET)
	List<ContactGroup> findContactGroups(@RequestParam(value = "name", required = false) String name);


	@RequestMapping(value = "/getById", method = GET)
	ContactGroup getById(@RequestParam(value = "id", required = false) Long id);

}