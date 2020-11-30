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
package com.wl4g.iam.service.impl;

import com.github.pagehelper.PageHelper;
import com.wl4g.components.core.bean.model.PageModel;
import com.wl4g.components.core.framework.operator.GenericOperatorAdapter;
import com.wl4g.components.support.notification.GenericNotifyMessage;
import com.wl4g.components.support.notification.MessageNotifier;
import com.wl4g.components.support.notification.MessageNotifier.NotifierKind;
import com.wl4g.iam.common.bean.Contact;
import com.wl4g.iam.common.bean.ContactChannel;
import com.wl4g.iam.common.bean.ContactGroupRef;
import com.wl4g.iam.data.ContactChannelDao;
import com.wl4g.iam.data.ContactDao;
import com.wl4g.iam.data.ContactGroupRefDao;
import com.wl4g.iam.service.ContactService;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import static com.wl4g.components.common.collection.Collections2.safeList;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.core.bean.BaseBean.DEL_FLAG_NORMAL;
import static com.wl4g.components.core.bean.BaseBean.ENABLED;
import static java.util.Objects.isNull;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

/**
 * Notification to contacts service implements.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @version v1.0 2019-08-05
 * @sine v1.0
 * @see
 */
@org.springframework.stereotype.Service
// @com.alibaba.dubbo.config.annotation.Service(group = "contactService")
// @org.springframework.web.bind.annotation.RestController
public class ContactServiceImpl implements ContactService {

	@Autowired
	private ContactDao contactDao;

	@Autowired
	private ContactGroupRefDao contactGroupRefDao;

	@Autowired
	private ContactChannelDao contactChannelDao;

	@Autowired
	private GenericOperatorAdapter<NotifierKind, MessageNotifier> notifier;

	@Override
	public void save(Contact contact) {
		if (null != contact.getId() && contact.getId() > 0) {
			contact.preUpdate();
			contactGroupRefDao.deleteByContactId(contact.getId());
			contactDao.updateByPrimaryKeySelective(contact);
		} else {
			contact.preInsert();
			contact.setDelFlag(DEL_FLAG_NORMAL);
			contact.setEnable(ENABLED);
			contactDao.insertSelective(contact);
		}
		Long[] groups = contact.getGroups();
		if (null != groups) {
			for (Long group : groups) {
				ContactGroupRef contactGroupRef = new ContactGroupRef();
				contactGroupRef.preInsert();
				contactGroupRef.setContactGroupId(group);
				contactGroupRef.setContactId(contact.getId());
				contactGroupRefDao.insertSelective(contactGroupRef);
			}
		}

		// TODO add 0313
		contactChannelDao.deleteByContactId(contact.getId());
		List<ContactChannel> contactChannels = contact.getContactChannels();
		for (ContactChannel contactChannel : contactChannels) {
			contactChannel.preInsert();
			contactChannel.setContactId(contact.getId());
			contactChannelDao.insertSelective(contactChannel);
		}

	}

	@Override
	public Contact detail(Long id) {
		Assert.notNull(id, "id can not be null");
		Contact contact = contactDao.selectByPrimaryKey(id);
		List<ContactGroupRef> contactGroupRefs = contactGroupRefDao.selectByContactId(id);
		if (CollectionUtils.isNotEmpty(contactGroupRefs)) {
			Long[] groups = new Long[contactGroupRefs.size()];
			for (int i = 0; i < contactGroupRefs.size(); i++) {
				groups[i] = contactGroupRefs.get(i).getContactGroupId();
			}
			contact.setGroups(groups);
		} else {
			contact.setGroups(new Long[0]);
		}
		return contact;
	}

	@Override
	public void del(Long id) {
		Assert.notNull(id, "id can not be null");
		Contact contact = new Contact();
		contact.preUpdate();
		contact.setId(id);
		contact.setDelFlag(1);
		contactDao.updateByPrimaryKeySelective(contact);
	}

	@Override
	public PageModel<Contact> list(PageModel<Contact> pm, String name) {
		pm.page(PageHelper.startPage(pm.getPageNum(), pm.getPageSize(), true));
		pm.setRecords(contactDao.list(name));
		return pm;
	}

	@Override
	public void notification(@NotBlank String templateKey, Map<String, Object> parameters, List<Long> contactGroupIds) {
		notNullOf(templateKey, "templateKey");
		if (isNull(contactGroupIds)) {
			return;
		}

		List<Contact> contacts = contactDao.getContactByGroupIds(safeList(contactGroupIds));
		for (Contact contact : contacts) {
			for (ContactChannel ch : safeList(contact.getContactChannels())) {
				if (ch.getEnable() == ENABLED) {
					GenericNotifyMessage msg = new GenericNotifyMessage(ch.getPrimaryAddress(), templateKey);
					msg.addParameters(parameters);
					notifier.forOperator(ch.getKind()).send(msg);
				}
			}
		}

	}

}