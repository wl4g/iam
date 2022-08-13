/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
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

import com.wl4g.infra.core.bean.BaseBean;
import com.wl4g.iam.common.bean.Organization;
import com.wl4g.iam.common.bean.User;
import com.wl4g.iam.common.utils.RpcContextIamSecurityUtils;
import com.wl4g.iam.data.OrganizationDao;
import com.wl4g.iam.data.OrganizationRoleDao;
import com.wl4g.iam.service.OrganizationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.core.bean.BaseBean.DEFAULT_SUPER_USER;
import static java.util.Objects.nonNull;

/**
 * Organization service implements.
 * 
 * @author James Wong<jamewong1376@gmail.com>
 * @author vjay
 * @date 2019-10-29 16:19:00
 */
@org.springframework.stereotype.Service
// @com.alibaba.dubbo.config.annotation.Service(group = "organizationService")
// @org.springframework.web.bind.annotation.RestController
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private OrganizationDao organizationDao;

    @Autowired
    private OrganizationRoleDao organizationRoleDao;

    @Override
    public List<Organization> getLoginOrganizationTree() {
        String principal = RpcContextIamSecurityUtils.currentIamPrincipalName();
        Set<Organization> orgs = getUserOrganizations(new User(principal));
        return transfromOrganTree(new ArrayList<>(orgs));
    }

    @Override
    public Set<Organization> getUserOrganizations(User user) {
        List<Organization> orgs = null;
        if (DEFAULT_SUPER_USER.equals(user.getSubject())) {
            orgs = organizationDao.selectByRoot();
        } else {
            orgs = organizationDao.selectByUserId(user.getId());
        }

        Set<Organization> orgSet = new HashSet<>(orgs);
        for (Organization org : orgs) {
            Set<Long> orgIds = new HashSet<>();
            orgIds.add(org.getId());
            fillChildrenIds(org.getId(), orgIds);

            org.setRoleCount(organizationDao.countRoleByOrganizationId(orgIds));
            getChildrensList(org.getId(), orgSet);
        }
        return orgSet;
    }

    void fillChildrenIds(Long parentId, Set<Long> orgSet) {
        List<Organization> childrens = organizationDao.selectByParentId(parentId);
        for (Organization org : childrens) {
            orgSet.add(org.getId());
        }
        for (Organization org : childrens) {
            fillChildrenIds(org.getId(), orgSet);
        }
    }

    @Override
    public void save(Organization org) {
        if (nonNull(org.getId())) {
            org.preUpdate();
            organizationDao.updateByPrimaryKeySelective(org);
        } else {
            org.preInsert();
            organizationDao.insertSelective(org);
        }
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
        notNullOf(id, "orgId");
        Organization org = notNullOf(organizationDao.selectByPrimaryKey(id), "organization");
        List<Long> roleIds = organizationRoleDao.selectRoleIdsByGroupId(id);
        org.setRoleIds(roleIds);
        return org;
    }

    private void getChildrensList(Long parentId, Set<Organization> set) {
        List<Organization> childrens = organizationDao.selectByParentId(parentId);
        set.addAll(childrens);
        for (Organization org : childrens) {
            getChildrensList(org.getId(), set);
        }
    }

    private List<Organization> transfromOrganTree(List<Organization> groups) {
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

    private Organization getParent(List<Organization> orgs, Long parentId) {
        for (Organization group : orgs) {
            if (parentId != null && group.getId() != null && group.getId().longValue() == parentId.longValue()) {
                return group;
            }
        }
        return null;
    }

    private List<Organization> getChildren(List<Organization> groups, List<Organization> children, Long parentId) {
        if (children == null) {
            children = new ArrayList<>();
        }
        for (Organization group : groups) {
            if (group.getParentId() != null && parentId != null && group.getParentId().longValue() == parentId.longValue()) {
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

}