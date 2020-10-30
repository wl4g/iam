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
import com.wl4g.components.core.bean.iam.Menu;
import com.wl4g.devops.dao.iam.MenuDao;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.service.MenuService;
import com.wl4g.iam.service.OrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.wl4g.components.common.lang.Assert2.isTrue;
import static com.wl4g.components.common.lang.TypeConverts.parseLongOrNull;
import static com.wl4g.components.core.bean.BaseBean.DEFAULT_SUPER_USER;
import static com.wl4g.iam.common.utils.IamSecurityHolder.getPrincipalInfo;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Menu service implements.
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @author vjay
 * @date 2019-10-30 15:48:00
 */
@Service
public class MenuServiceImpl implements MenuService {

	@Autowired
	protected MenuDao menuDao;
	@Autowired
	protected OrganizationService groupService;

	@Override
	public Map<String, Object> getMenuTree() {
		Map<String, Object> result = new HashMap<>();
		Set<Menu> menusSet = getMenusSet();
		List<Menu> menuList = new ArrayList<>(getMenusSet());
		List<Menu> menus = new ArrayList<>(menusSet);
		dealWithRoutePath(menus);
		menus = set2Tree(menus);
		result.put("data", menus);
		result.put("data2", new ArrayList<>(menuList));
		return result;
	}

	private Menu getParent(List<Menu> menus, Long parentId) {
		for (Menu menu : menus) {
			if (parentId != null && menu.getId() != null && menu.getId().intValue() == parentId.intValue()) {
				return menu;
			}
		}
		return null;
	}

	@Override
	public List<Menu> getMenuList() {
		IamPrincipal info = getPrincipalInfo();
		List<Menu> result;
		if (DEFAULT_SUPER_USER.equals(info.getPrincipal())) {
			result = menuDao.selectWithRoot();// root
		} else {
			result = menuDao.selectByUserId(parseLongOrNull(info.getPrincipalId()));
		}
		// deal with route path
		dealWithRoutePath(result);
		return result;
	}

	private void dealWithRoutePath(List<Menu> list) {
		for (Menu menu : list) {
			menu.setRoutePath(menu.getRouteNamespace());
			if (menu.getParentId() != null && menu.getParentId() > 0 && StringUtils.isNotBlank(menu.getRouteNamespace())) {
				dealWithRoutePath(list, menu, menu.getParentId());
			}
		}
	}

	private void dealWithRoutePath(List<Menu> list, Menu menu, long parentId) {
		if (parentId != 0) {
			for (Menu m : list) {
				if (m.getId().equals(parentId)) {
					menu.setRoutePath(fixRouteNamespace(m.getRouteNamespace()) + fixRouteNamespace(menu.getRoutePath()));
					if (m.getParentId() != null && m.getParentId() > 0) {
						dealWithRoutePath(list, menu, m.getParentId());
					}
					break;
				}
			}
		}
	}

	private String fixRouteNamespace(String routeNamespace) {
		if (StringUtils.equals("/", routeNamespace)) {
			return "";
		}
		if (routeNamespace != null && routeNamespace.length() > 1) {
			if (!routeNamespace.startsWith("/")) {
				routeNamespace = "/" + routeNamespace;
			}
			while (routeNamespace.endsWith("/")) {
				routeNamespace = routeNamespace.substring(0, routeNamespace.length() - 1);
			}
			routeNamespace = routeNamespace.trim();
			while (routeNamespace.contains("//")) {
				routeNamespace = routeNamespace.replaceAll("//", "/");
			}
		}
		return routeNamespace;
	}

	@Override
	public void save(Menu menu) {
		checkRepeat(menu);
		checkMenu(menu);
		if (menu.getId() != null) {
			update(menu);
		} else {
			insert(menu);
		}
	}

	@Override
	public void del(Long id) {
		Assert.notNull(id, "id is null");
		Menu menu = new Menu();
		menu.setId(id);
		menu.setDelFlag(BaseBean.DEL_FLAG_DELETE);
		menuDao.updateByPrimaryKeySelective(menu);
	}

	@Override
	public Menu detail(Long id) {
		return menuDao.selectByPrimaryKey(id);
	}

	private void insert(Menu menu) {
		menu.preInsert();
		Long parentId = menu.getParentId();
		// if menu type is button
		if (Objects.nonNull(menu.getType()) && menu.getType().intValue() == 3) {
			menu.setLevel(0);
		} else {
			// if has parent menu , set level = parent's level + 1
			if (Objects.nonNull(parentId) && 0 != parentId) {
				Menu parentMenu = menuDao.selectByPrimaryKey(parentId);
				Assert.notNull(parentMenu, "parentMenu is null");
				Assert.notNull(parentMenu.getLevel(), "parentMenu's level is null");
				menu.setLevel(parentMenu.getLevel() + 1);
			} else {// if is parent menu , set level = 1
				menu.setLevel(1);
			}
		}
		menuDao.insertSelective(menu);
	}

	private void update(Menu menu) {
		menu.preUpdate();
		menuDao.updateByPrimaryKeySelective(menu);
	}

	/**
	 * 父/子 动态 静态 按钮 动态 1 1 0 静态 1 1 1(无需设菜单文件夹的pagelocation和route_namespace为空)
	 * 按钮 0 0 0
	 *
	 * @param menu
	 */
	private void checkMenu(Menu menu) {
		String routeNamespace = menu.getRouteNamespace();
		final boolean isLegal = routeNamespace.startsWith("/") && routeNamespace.indexOf("/") == routeNamespace.lastIndexOf("/") && routeNamespace.length() > 1;
		isTrue(isLegal, "illegal routeNamespace: '%s', e.g: '/edit'", routeNamespace);

		if (menu.getParentId() == null) {
			return;
		}
		Menu parentMenu = menuDao.selectByPrimaryKey(menu.getParentId());
		if (null != parentMenu) {
			if (parentMenu.getType() == 1) {
				return;
			}
			if (parentMenu.getType() == 2 && menu.getType() == 3) {
				throw new UnsupportedOperationException("can not save because parentType=2(dynamic) and menuType=3(button)");
			}
			if (parentMenu.getType() == 3) {
				throw new UnsupportedOperationException("can not save because parentType=3(button)");
			}
		}
	}

	private void checkRepeat(Menu menu) {
		List<Menu> menus = menuDao.selectByParentId(menu.getParentId());
		for (Menu m : menus) {
			if(!m.getId().equals(menu.getId())){
				isTrue(!equalsIgnoreCase(m.getRouteNamespace(), menu.getRouteNamespace()),
						"menu's route path is repeat");
				isTrue(!menu.getSort().equals(m.getSort()),
						"menu's sort repeat");
			}
		}
	}

	private Set<Menu> getMenusSet() {
		IamPrincipal info = getPrincipalInfo();

		List<Menu> menus = null;
		if (DEFAULT_SUPER_USER.equals(info.getPrincipal())) {
			menus = menuDao.selectWithRoot();
		} else {
			Long userId = null;
			if (isNotBlank(info.getPrincipalId())) {
				userId = Long.parseLong(info.getPrincipalId());
			}
			menus = menuDao.selectByUserId(userId);
		}
		Set<Menu> set = new LinkedHashSet<>();
		set.addAll(menus);
		// Clean unused fields
		for (Menu menu : set) {
			menu.setRoutePath(null);
			menu.setLevel(null);
			menu.setClassify(null);
			// menu.setPageLocation(null);
			// menu.setRenderTarget(null);
			// menu.setRouteNamespace(null);
			menu.setStatus(null);
			// menu.setType(null);
			menu.setDelFlag(null);
			menu.setUpdateBy(null);
			menu.setUpdateDate(null);
			menu.setCreateDate(null);
			menu.setCreateBy(null);
			menu.setOrganizationCode(null);
		}
		return set;
	}

	private List<Menu> set2Tree(List<Menu> menus) {
		List<Menu> top = new ArrayList<>();
		for (Menu menu : menus) {
			Menu parent = getParent(menus, menu.getParentId());
			if (parent == null) {
				top.add(menu);
			}
		}
		for (Menu menu : top) {
			List<Menu> children = getChildren(menus, null, menu);
			if (!CollectionUtils.isEmpty(children)) {
				menu.setChildren(children);
			}
		}
		return top;
	}

	private List<Menu> getChildren(List<Menu> menus, List<Menu> children, Menu parent) {
		if (children == null) {
			children = new ArrayList<>();
		}
		for (Menu menu : menus) {
			if (menu.getParentId() != null && parent.getId() != null && menu.getParentId().longValue() == parent.getId().longValue()) {
				menu.setParentRoutePath(parent.getRoutePath());
				children.add(menu);
			}
		}
		for (Menu menu : children) {
			List<Menu> children1 = getChildren(menus, null, menu);
			if (!CollectionUtils.isEmpty(children1)) {
				menu.setChildren(children1);
			}
		}
		return children;
	}

}