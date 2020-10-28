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
import com.wl4g.components.core.bean.BaseBean;
import com.wl4g.components.core.bean.iam.*;
import com.wl4g.devops.dao.iam.*;
import com.wl4g.devops.page.PageModel;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.service.OrganizationService;
import com.wl4g.iam.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

import static com.wl4g.components.common.collection.Collections2.disDupCollection;
import static com.wl4g.components.core.bean.BaseBean.DEFAULT_SUPER_USER;
import static com.wl4g.iam.common.utils.IamSecurityHolder.getPrincipalInfo;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Role service implements.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @author vjay
 * @version v1.0 2019年11月6日
 * @since
 */
@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private RoleMenuDao roleMenuDao;

	@Autowired
	private MenuDao menuDao;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private OrganizationRoleDao groupRoleDao;

	@Override
	public List<Role> getRolesByUserGroups() {
		IamPrincipal info = getPrincipalInfo();

		if (DEFAULT_SUPER_USER.equals(info.getPrincipal())) {
			return roleDao.selectWithRoot(null, null, null);
		} else {
			// Groups of userId.
			Set<Organization> groups = organizationService.getGroupsSet(new User(info.getPrincipal()));
			List<Long> groupIds = new ArrayList<>();
			for (Organization group : groups) {
				groupIds.add(group.getId());
			}
			// Roles of group.
			List<Role> roles = roleDao.selectByGroupIdsAndUserId(groupIds, info.getPrincipalId(), null, null);
			return roles;
		}
	}

	@Override
	public PageModel list(PageModel pm, String organizationId, String roleCode, String displayName) {
		IamPrincipal info = getPrincipalInfo();

		List<Long> groupIds = null;
		if (isNotBlank(organizationId)) {
			Set<Long> set = new HashSet<>();
			set.add(Long.valueOf(organizationId));
			organizationService.getChildrensIds(Long.valueOf(organizationId), set);
			groupIds = new ArrayList<>(set);
		}
		if (DEFAULT_SUPER_USER.equals(info.getPrincipal())) {
			pm.page(PageHelper.startPage(pm.getPageNum(), pm.getPageSize(), true));
			List<Role> roles = roleDao.selectWithRoot(groupIds, roleCode, displayName);
			setMenuStrs(roles);
			pm.setRecords(roles);
		} else {
			pm.page(PageHelper.startPage(pm.getPageNum(), pm.getPageSize(), true));
			List<Role> roles = roleDao.selectByGroupIdsAndUserId(groupIds, info.getPrincipalId(), roleCode, displayName);
			setMenuStrs(roles);
			pm.setRecords(roles);
		}
		return pm;
	}

	private void setMenuStrs(List<Role> roles) {
		for (Role role : roles) {
			List<Menu> menus = menuDao.selectByRoleId(role.getId());
			if (isEmpty(menus)) {
				continue;
			}
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < menus.size(); i++) {
				if (i == menus.size() - 1) {
					stringBuilder.append(menus.get(i).getNameZh());
				} else {
					stringBuilder.append(menus.get(i).getNameZh()).append(",");
				}
			}
			role.setMenusStr(stringBuilder.toString());
		}
	}

	@Override
	public void save(Role role) {
		if (!isEmpty(role.getMenuIds())) { // Menus repeat
			role.setMenuIds((List<Long>) disDupCollection(role.getMenuIds()));
		}
		if (!isEmpty(role.getGroupIds())) { // Groups repeat
			role.setGroupIds((List<Long>) disDupCollection(role.getGroupIds()));
		}
		if (nonNull(role.getId())) {
			update(role);
		} else {
			insert(role);
		}
	}

	private void insert(Role role) {
		role.preInsert();
		roleDao.insertSelective(role);
		List<RoleMenu> roleMenus = new ArrayList<>();
		// menu
		for (Long menuId : role.getMenuIds()) {
			RoleMenu roleMenu = new RoleMenu();
			roleMenu.preInsert();
			roleMenu.setMenuId(menuId);
			roleMenu.setRoleId(role.getId());
			roleMenus.add(roleMenu);
		}
		if (!isEmpty(roleMenus)) {
			roleMenuDao.insertBatch(roleMenus);
		}
		// group
		List<OrganizationRole> groupRoles = new ArrayList<>();
		/*
		 * for (Long groupId : role.getGroupIds()) { OrganizationRole groupRole
		 * = new OrganizationRole(); groupRole.preInsert();
		 * groupRole.setGroupId(groupId); groupRole.setRoleId(role.getId());
		 * groupRoles.add(groupRole); }
		 */
		if (nonNull(role.getOrganizationId())) {
			OrganizationRole groupRole = new OrganizationRole();
			groupRole.preInsert();
			groupRole.setGroupId(role.getOrganizationId());
			groupRole.setRoleId(role.getId());
			groupRoles.add(groupRole);
		}
		if (!isEmpty(groupRoles)) {
			groupRoleDao.insertBatch(groupRoles);
		}
	}

	private void update(Role role) {
		role.preUpdate();
		roleDao.updateByPrimaryKeySelective(role);
		roleMenuDao.deleteByRoleId(role.getId());
		groupRoleDao.deleteByRoleId(role.getId());
		List<Long> menuIds = role.getMenuIds();
		// menu
		List<RoleMenu> roleMenus = new ArrayList<>();
		for (Long menuId : menuIds) {
			RoleMenu roleMenu = new RoleMenu();
			roleMenu.preInsert();
			roleMenu.setMenuId(menuId);
			roleMenu.setRoleId(role.getId());
			roleMenus.add(roleMenu);
		}
		if (!isEmpty(roleMenus)) {
			roleMenuDao.insertBatch(roleMenus);
		}
		// group
		List<OrganizationRole> groupRoles = new ArrayList<>();
		for (Long groupId : role.getGroupIds()) {
			OrganizationRole groupRole = new OrganizationRole();
			groupRole.preInsert();
			groupRole.setGroupId(groupId);
			groupRole.setRoleId(role.getId());
			groupRoles.add(groupRole);
		}
		if (!isEmpty(groupRoles)) {
			groupRoleDao.insertBatch(groupRoles);
		}
	}

	@Override
	public void del(Long id) {
		Assert.notNull(id, "id is null");
		Role role = new Role();
		role.setId(id);
		role.setDelFlag(BaseBean.DEL_FLAG_DELETE);
		roleDao.updateByPrimaryKeySelective(role);
	}

	@Override
	public Role detail(Long id) {
		Role role = roleDao.selectByPrimaryKey(id);
		List<Long> menuIds = roleMenuDao.selectMenuIdByRoleId(id);
		List<Long> groupIds = groupRoleDao.selectGroupIdByRoleId(id);
		role.setMenuIds(menuIds);
		role.setGroupIds(groupIds);
		return role;

	}

}