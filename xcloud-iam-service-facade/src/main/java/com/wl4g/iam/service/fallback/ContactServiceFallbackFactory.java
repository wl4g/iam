/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.iam.service.fallback;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;

import java.util.List;

import javax.validation.constraints.NotBlank;

import org.springframework.stereotype.Component;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.page.PageHolder;
import com.wl4g.iam.common.bean.Contact;
import com.wl4g.iam.service.ContactService;

import feign.hystrix.FallbackFactory;

/**
 * {@link ContactServiceFallbackFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-07
 * @sine v1.0
 * @see
 */
@Component
public class ContactServiceFallbackFactory implements FallbackFactory<ContactService> {
	protected final SmartLogger log = getLogger(getClass());

	@Override
	public ContactService create(Throwable cause) {
		return new ContactService() {

			@Override
			public void save(Contact contact) {
				log.warn("Cannot save, fallback handling...", cause);
			}

			@Override
			public void notification(@NotBlank NotificationParameter parameter) {
				log.warn(format("Cannot notification, fallback handling... - {}", parameter), cause);
			}

			@Override
			public PageHolder<Contact> list(PageHolder<Contact> pm, String name) {
				log.warn(format("Cannot list, fallback handling... - {}, {}", pm, name), cause);
				return null;
			}

			@Override
			public Contact detail(Long id) {
				log.warn(format("Cannot detail, fallback handling... - {}", id), cause);
				return null;
			}

			@Override
			public void del(Long id) {
				log.warn(format("Cannot del, fallback handling... - {}", id), cause);
			}

			@Override
			public List<Contact> getContactByGroupIds(List<Long> groupIds) {
				log.warn(format("Cannot findContactByGroupIds, fallback handling... - {}", groupIds), cause);
				return null;
			}
		};
	}

}
