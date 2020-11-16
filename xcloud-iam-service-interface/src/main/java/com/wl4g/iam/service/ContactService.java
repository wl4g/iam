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

import javax.validation.constraints.NotBlank;

import com.wl4g.components.common.annotation.Nullable;
import com.wl4g.components.core.web.model.PageModel;
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
public interface ContactService {

	void save(Contact contact);

	Contact detail(Long id);

	void del(Long id);

	PageModel<Contact> list(PageModel<Contact> pm, String name);

	/**
	 * Sending with template message notification.
	 * 
	 * @param singleToTarget
	 * @param templateKey
	 * @param contactGroupIds
	 */
	void notificationWithTemplate(@NotBlank String templateKey, @Nullable Map<String, Object> parameters,
			@Nullable List<Long> contactGroupIds);

}