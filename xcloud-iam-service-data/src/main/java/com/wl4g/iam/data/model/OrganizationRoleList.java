package com.wl4g.iam.data.model;

import com.wl4g.iam.common.bean.OrganizationRole;

import java.util.List;

/**
 * @author vjay
 * @date 2021-04-22 16:57:00
 */
public class OrganizationRoleList {

    List<OrganizationRole> groupRoles;

    public List<OrganizationRole> getGroupRoles() {
        return groupRoles;
    }

    public void setGroupRoles(List<OrganizationRole> groupRoles) {
        this.groupRoles = groupRoles;
    }
}
