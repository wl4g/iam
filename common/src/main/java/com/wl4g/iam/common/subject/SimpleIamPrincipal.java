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
package com.wl4g.iam.common.subject;

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

/**
 * Simple IAM principal information.
 * 
 * @author James Wong &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2018-04-31
 * @since
 */
public class SimpleIamPrincipal implements IamPrincipal {
    private static final long serialVersionUID = -2148910955172545592L;

    /** Authenticate principal ID. */
    @NotBlank
    private String principalId;

    /** Authenticate principal name. */
    @NotBlank
    private String principal;

    /** Authenticate principal DB stored credenticals. */
    private String storedCredentials;

    /** Authenticate principal DB stored public salt(hex). */
    private String publicSalt;

    /** Authenticate principal role codes. */
    private String roles;

    /** Authenticate principal permission. */
    private String permissions;

    /** Authenticate principal organization. */
    private PrincipalOrganization organization;

    /** Authenticate principal attributes. */
    private Attributes attributes;

    public SimpleIamPrincipal() {
        super();
    }

    public SimpleIamPrincipal(@NotBlank IamPrincipal info) {
        this(info.getPrincipalId(), info.getPrincipal(), info.getStoredCredentials(), info.getPublicSalt(), info.getRoles(),
                info.getPermissions(), info.getOrganization(), info.attributes());
    }

    public SimpleIamPrincipal(@NotBlank String principalId, String principal, String storedCredentials, String publicSalt,
            String roles, String permissions, PrincipalOrganization organization) {
        this(principalId, principal, storedCredentials, publicSalt, roles, permissions, organization, null);
    }

    public SimpleIamPrincipal(@NotBlank String principalId, String principal, String storedCredentials, String publicSalt,
            String roles, String permissions, PrincipalOrganization organization, Attributes attributes) {
        setPrincipalId(principalId);
        setPrincipal(principal);
        setStoredCredentials(storedCredentials);
        setPublicSalt(publicSalt);
        setRoles(roles);
        setPermissions(permissions);
        setOrganization(organization);
        setAttributes(attributes);
    }

    @Override
    public final String getPrincipalId() {
        return principalId;
    }

    @Override
    public String principalId() {
        return isBlank(principalId) ? EMPTY : principalId;
    }

    public final void setPrincipalId(@NotBlank String principalId) {
        this.principalId = hasTextOf(principalId, "principalId");
    }

    public final SimpleIamPrincipal withPrincipalId(@NotBlank String principalId) {
        setPrincipalId(principalId);
        return this;
    }

    @Override
    public final String getPrincipal() {
        return principal;
    }

    @Override
    public String principal() {
        return isBlank(principal) ? EMPTY : principal;
    }

    public final void setPrincipal(@NotBlank String principal) {
        this.principal = hasTextOf(principal, "principal");
    }

    public final SimpleIamPrincipal withPrincipal(@NotBlank String principal) {
        setPrincipal(principal);
        return this;
    }

    @Override
    public final String getStoredCredentials() {
        return storedCredentials;
    }

    @Override
    public String storedCredentials() {
        return isBlank(storedCredentials) ? EMPTY : storedCredentials;
    }

    public final void setStoredCredentials(@Nullable String storedCredentials) {
        this.storedCredentials = storedCredentials;
    }

    public final SimpleIamPrincipal withStoredCredentials(@Nullable String storedCredentials) {
        setStoredCredentials(storedCredentials);
        return this;
    }

    @Override
    public final String getPublicSalt() {
        return publicSalt;
    }

    @Override
    public String publicSalt() {
        return isBlank(publicSalt) ? EMPTY : publicSalt;
    }

    /**
     * Sets user account public salt hex string.
     * 
     * @param publicSaltHex
     */
    public final void setPublicSalt(@NotBlank String publicSaltHex) {
        this.publicSalt = hasTextOf(publicSaltHex, "publicSaltHex");
    }

    public final SimpleIamPrincipal withPublicSalt(@NotBlank String publicSalt) {
        setPublicSalt(publicSalt);
        return this;
    }

    @Override
    public final String getRoles() {
        return roles;
    }

    @Override
    public String roles() {
        return isBlank(roles) ? EMPTY : roles;
    }

    public final void setRoles(@Nullable String roles) {
        this.roles = roles;
    }

    public final SimpleIamPrincipal withRoles(@Nullable String roles) {
        setRoles(roles);
        return this;
    }

    @Override
    public final String getPermissions() {
        return permissions;
    }

    @Override
    public String permissions() {
        return isBlank(permissions) ? EMPTY : permissions;
    }

    public final void setPermissions(@Nullable String permissions) {
        this.permissions = permissions;
    }

    public final SimpleIamPrincipal withPermissions(@Nullable String permissions) {
        setPermissions(permissions);
        return this;
    }

    @Override
    public final PrincipalOrganization getOrganization() {
        return organization;
    }

    @Override
    public PrincipalOrganization organization() {
        return isNull(organization) ? (organization = new PrincipalOrganization()) : organization;
    }

    public void setOrganization(PrincipalOrganization organization) {
        // notNullOf(organization, "organization");
        this.organization = organization;
    }

    public SimpleIamPrincipal withOrganization(PrincipalOrganization organization) {
        setOrganization(organization);
        return this;
    }

    @Override
    public final Attributes getAttributes() {
        // notNull(attributes, "Principal attributes can't null");
        return attributes;
    }

    @Override
    public final Attributes attributes() {
        return isNull(attributes) ? (attributes = new Attributes()) : attributes;
    }

    /**
     * Sets principal account attributes.
     * 
     * @param attributes
     * @return
     */
    public final void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    /**
     * Sets with principal account attributes.
     * 
     * @param attributes
     * @return
     */
    public final SimpleIamPrincipal withAttributes(Attributes attributes) {
        setAttributes(attributes);
        return this;
    }

    @Override
    public String toString() {
        return "SimplePrincipalInfo [principalId=" + principalId + ", principal=" + principal + ", storedCredentials="
                + storedCredentials + ", roles=" + roles + ", permissions=" + permissions + ", attributes=" + attributes + "]";
    }

    /**
     * Validation.
     */
    @Override
    public final IamPrincipal validate() throws IllegalArgumentException {
        hasText(getPrincipalId(), "Could not authentication principalId be empty");
        hasText(getPrincipal(), "Could not authentication principal be empty");
        // hasText(getRoles(), "Could not authentication principal be empty");
        // notNull(getOrganization(), "Could not authentication principal be
        // empty");
        // hasText(getPermissions(), "Could not authentication principal be
        // empty");
        return this;
    }

}