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

import com.wl4g.components.core.bean.BaseBean;
import com.wl4g.components.core.bean.iam.Organization;
import com.wl4g.components.core.bean.iam.User;
import com.wl4g.devops.dao.iam.*;
import com.wl4g.iam.common.subject.IamPrincipalInfo;
import com.wl4g.iam.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.wl4g.components.core.bean.BaseBean.DEFAULT_SUPER_USER;
import static com.wl4g.iam.common.utils.IamSecurityHolder.getPrincipalInfo;
import static java.util.Objects.nonNull;

/**
 * Group service implements.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @author vjay
 * @date 2019-10-29 16:19:00
 */
@Service
public class OrganizationServiceImpl implements OrganizationService {

	@Autowired
	private OrganizationDao organizationDao;

	@Autowired
	private GroupMenuDao groupMenuDao;

	@Autowired
	private OrganizationRoleDao groupRoleDao;

	@Autowired
	private ParkDao parkDao;

	@Autowired
	private CompanyDao companyDao;

	@Autowired
	private DepartmentDao departmentDao;

	@Autowired
	private GroupUserDao groupUserDao;

	@Override
	public List<Organization> getGroupsTree() {
		IamPrincipalInfo info = getPrincipalInfo();
		Set<Organization> groupsSet = getGroupsSet(new User(info.getPrincipal()));
		ArrayList<Organization> groups = new ArrayList<>(groupsSet);
		return set2Tree(groups);
	}

	private List<Organization> set2Tree(List<Organization> groups) {
		List<Organization> top = new ArrayList<>();
		for (Organization group : groups) {
			Organization parent = getParent(groups, group.getParentId());
			if (parent == null) {
				top.add(group);
			}
		}
		for (Organization group : top) {
			List<Organization> children = getChildren(groups, null, group.getId());
			if (!CollectionUtils.isEmpty(children)) {
				group.setChildren(children);
			}
		}
		return top;
	}

	private List<Organization> getChildren(List<Organization> groups, List<Organization> children, Long parentId) {
		if (children == null) {
			children = new ArrayList<>();
		}
		for (Organization group : groups) {
			if (group.getParentId() != null && parentId != null && group.getParentId().intValue() == parentId.intValue()) {
				children.add(group);
			}
		}
		for (Organization group : children) {
			List<Organization> children1 = getChildren(groups, null, group.getId());
			if (!CollectionUtils.isEmpty(children1)) {
				group.setChildren(children1);
			}
		}
		return children;
	}

	public Organization getParent(List<Organization> groups, Long parentId) {
		for (Organization group : groups) {
			if (parentId != null && group.getId() != null && group.getId().intValue() == parentId.intValue()) {
				return group;
			}
		}
		return null;
	}

	@Override
	public Set<Organization> getGroupsSet(User user) {
		// IamPrincipalInfo info = getPrincipalInfo();

		List<Organization> groups = null;
		if (DEFAULT_SUPER_USER.equals(user.getUserName())) {
			groups = organizationDao.selectByRoot();
		} else {
			groups = organizationDao.selectByUserId(user.getId());
		}

		Set<Organization> set = new HashSet<>();
		set.addAll(groups);
		for (Organization group : groups) {
			getChildrensList(group.getId(), set);
		}
		return set;
	}

	private void getChildrensList(Long parentId, Set<Organization> set) {
		List<Organization> childrens = organizationDao.selectByParentId(parentId);
		set.addAll(childrens);
		for (Organization group : childrens) {
			getChildrensList(group.getId(), set);
		}
	}

	public void getChildrensIds(Long parentId, Set<Long> set) {
		List<Organization> childrens = organizationDao.selectByParentId(parentId);
		for(Organization organization: childrens){
			set.add(organization.getId());
		}
		for (Organization group : childrens) {
			getChildrensIds(group.getId(), set);
		}
	}


	@Override
	public void save(Organization group) {
		if (nonNull(group.getId())) {
			update(group);
		} else {
			insert(group);
		}
	}

	private void insert(Organization group) {
		group.preInsert();
		organizationDao.insertSelective(group);
	}

	private void update(Organization group) {
		group.preUpdate();
		organizationDao.updateByPrimaryKeySelective(group);
	}

	@Override
	public void del(Long id) {
		Assert.notNull(id, "id is null");
		Organization group = new Organization();
		group.setId(id);
		group.setDelFlag(BaseBean.DEL_FLAG_DELETE);
		organizationDao.updateByPrimaryKeySelective(group);
	}

	@Override
	public Organization detail(Long id) {
		Assert.notNull(id, "id is null");
		Organization group = organizationDao.selectByPrimaryKey(id);
		Assert.notNull(group, "group is null");
		//List<Long> menuIds = groupMenuDao.selectMenuIdsByGroupId(id);
		List<Long> roleIds = groupRoleDao.selectRoleIdsByGroupId(id);
		//group.setMenuIds(menuIds);
		group.setRoleIds(roleIds);
		return group;
	}

}