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

import com.wl4g.component.core.bean.model.PageHolder;
import com.wl4g.iam.common.bean.ContactGroup;
import com.wl4g.iam.data.ContactGroupDao;
import com.wl4g.iam.service.ContactGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import static com.wl4g.component.core.bean.BaseBean.DEL_FLAG_NORMAL;
import static com.wl4g.component.core.bean.BaseBean.ENABLED;

import java.util.List;

/**
 * {@link ContactGroupServiceImpl}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-08-05
 * @sine v1.0
 * @see
 */
@org.springframework.stereotype.Service
// @com.alibaba.dubbo.config.annotation.Service(group = "contactGroupService")
// @org.springframework.web.bind.annotation.RestController
public class ContactGroupServiceImpl implements ContactGroupService {

	@Autowired
	private ContactGroupDao contactGroupDao;

	@Override
	public void save(ContactGroup contactGroup) {
		Assert.notNull(contactGroup, "contactGroup is null");
		if (contactGroup.getId() != null) {
			contactGroup.preUpdate();
			contactGroupDao.updateByPrimaryKeySelective(contactGroup);
		} else {
			contactGroup.preInsert();
			contactGroup.setDelFlag(DEL_FLAG_NORMAL);
			contactGroup.setEnable(ENABLED);
			contactGroupDao.insertSelective(contactGroup);
		}
	}

	@Override
	public void del(Long id) {
		ContactGroup contactGroup = new ContactGroup();
		contactGroup.preUpdate();
		contactGroup.setId(id);
		contactGroup.setDelFlag(1);
		contactGroupDao.updateByPrimaryKeySelective(contactGroup);
	}

	@Override
	public List<ContactGroup> findContactGroups(String name) {
		return contactGroupDao.list(name);
	}

	@Override
	public PageHolder<ContactGroup> list(PageHolder<ContactGroup> pm, String name) {
		pm.setCurrentPage();
		pm.setRecords(contactGroupDao.list(name));
		return pm;
	}

}