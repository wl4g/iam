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
package com.wl4g.iam.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wl4g.infra.core.bean.BaseBean;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Menu extends BaseBean implements Serializable {
    private static final long serialVersionUID = 381411777614066880L;

    private String nameEn;

    private String nameZh;

    private Integer type;

    private Integer status;

    private Integer level;

    private Long parentId;

    private String permission;

    private String pageLocation;

    private String routePath;

    private String routeNamespace;

    private String renderTarget;

    private String icon;

    private Integer sort;

    private String classify;

    private List<Menu> children;

    private String parentRoutePath;

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn == null ? null : nameEn.trim();
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setNameZh(String nameZh) {
        this.nameZh = nameZh == null ? null : nameZh.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission == null ? null : permission.trim();
    }

    public String getPageLocation() {
        return pageLocation;
    }

    public void setPageLocation(String pageLocation) {
        this.pageLocation = pageLocation == null ? null : pageLocation.trim();
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon == null ? null : icon.trim();
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public List<Menu> getChildren() {
        return children;
    }

    public void setChildren(List<Menu> children) {
        this.children = children;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public String getRenderTarget() {
        return renderTarget;
    }

    public void setRenderTarget(String renderTarget) {
        this.renderTarget = renderTarget;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public String getRouteNamespace() {
        return routeNamespace;
    }

    public void setRouteNamespace(String routeNamespace) {
        this.routeNamespace = routeNamespace;
    }

    public String getParentRoutePath() {
        return parentRoutePath;
    }

    public void setParentRoutePath(String parentRoutePath) {
        this.parentRoutePath = parentRoutePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Menu menu = (Menu) o;
        return Objects.equals(getId(), menu.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}