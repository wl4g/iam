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

import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_ACCESSTOKEN_SIGN_NAME;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_AUTHC_HOST_NAME;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_DATA_CIPHER_NAME;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_LANG_NAME;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_PARENT_SESSIONID_NAME;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_REMEMBERME_NAME;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_SNS_AUTHORIZED_INFO;
import static com.wl4g.iam.common.constant.IAMConstants.KEY_IAM_SUBJECT_USER;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wl4g.iam.common.bean.SocialAuthorizeInfo;
import com.wl4g.iam.common.bean.User;

/**
 * IAM principal account information.
 * 
 * @author James Wong &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2018-04-31
 * @since
 */
public interface IamPrincipal extends Principal, Serializable {

    @Override
    default String getName() {
        return getPrincipal();
    }

    /**
     * Gets account principal Id.
     * 
     * @return
     */
    String getPrincipalId();

    /**
     * Gets account principal Id.
     * 
     * @return If null, the NOOP default value is returned
     */
    String principalId();

    /**
     * Gets account principal name.
     * 
     * @return
     */
    String getPrincipal();

    /**
     * Gets account principal name.
     * 
     * @return If null, the NOOP default value is returned
     */
    String principal();

    /**
     * Stored encrypted credentials
     * 
     * @return Encrypted credentials string
     */
    String getStoredCredentials();

    /**
     * Stored encrypted credentials
     * 
     * @return Encrypted credentials string, If null, the NOOP default value is
     *         returned
     */
    String storedCredentials();

    /**
     * Stored credentials public salt.
     * 
     * @return
     */
    String getPublicSalt();

    /**
     * Stored credentials public salt.
     * 
     * @return
     */
    String publicSalt();

    /**
     * Principal role codes. </br>
     * <p>
     * EG: sc_sys_mgt,sc_general_mgt,sc_general_operator,sc_user_jack
     * </p>
     *
     * @return principal role codes.
     */
    String getRoles();

    /**
     * Principal role codes. </br>
     * <p>
     * EG: sc_sys_mgt,sc_general_mgt,sc_general_operator,sc_user_jack
     * </p>
     *
     * @return principal role codes, If null, the NOOP default value is returned
     */
    String roles();

    /**
     * Principal permissions. </br>
     * <p>
     * e.g.: sys:user:view,sys:user:edit,goods:order:view,goods:order:edit
     * </p>
     *
     * @return principal permission identifiers.
     */
    String getPermissions();

    /**
     * Principal permissions. </br>
     * <p>
     * e.g.: sys:user:view,sys:user:edit,goods:order:view,goods:order:edit
     * </p>
     *
     * @return principal permission identifiers, If null, the NOOP default value
     *         is returned
     */
    String permissions();

    /**
     * Principal organization. </br>
     * <p>
     *
     * @return principal organizations identifiers.
     */
    PrincipalOrganization getOrganization();

    /**
     * Principal organization. </br>
     * <p>
     * 
     * @return principal organizations identifiers, If null, the new value is
     *         returned
     */
    PrincipalOrganization organization();

    /**
     * Gets account attributes.
     * 
     * @return
     */
    Attributes getAttributes();

    /**
     * Gets account attributes.
     * 
     * @return If null, the new value is returned
     */
    Attributes attributes();

    /**
     * Validation of principal information attribute.
     * 
     * @throws IllegalArgumentException
     */
    IamPrincipal validate() throws IllegalArgumentException;

    // --- Authenticating parameter's. ---

    /**
     * {@link IamPrincipal} attributes wrapper.
     * 
     * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
     * @version 2020年7月7日 v1.0.0
     * @see
     */
    public static class Attributes extends LinkedHashMap<String, String> {
        final private static long serialVersionUID = 3252340509258189795L;

        /**
         * Gets session locale language
         * 
         * @return
         */
        public String getSessionLang() {
            return get(KEY_LANG_NAME);
        }

        /**
         * Sets session locale language
         * 
         * @return
         */
        public Attributes setSessionLang(String lang) {
            if (!isBlank(lang)) {
                put(KEY_LANG_NAME, lang);
            }
            return this;
        }

        /**
         * Gets authenticating client remote host
         * 
         * @return
         */
        public String getClientHost() {
            return get(KEY_AUTHC_HOST_NAME);
        }

        /**
         * Sets authenticating client remote host
         * 
         * @return
         */
        public Attributes setClientHost(String clientHost) {
            if (!isBlank(clientHost)) {
                put(KEY_AUTHC_HOST_NAME, clientHost);
            }
            return this;
        }

        /**
         * Gets authentication parent session id.
         * 
         * @return
         */
        public String getParentSessionId() {
            return get(KEY_PARENT_SESSIONID_NAME);
        }

        /**
         * Sets authentication parent session id.
         * 
         * @return
         */
        public Attributes setParentSessionId(String parentSessionId) {
            if (!isBlank(parentSessionId)) {
                put(KEY_PARENT_SESSIONID_NAME, parentSessionId);
            }
            return this;
        }

        /**
         * Gets authentication data cipher.
         * 
         * @return
         */
        public String getDataCipher() {
            return get(KEY_DATA_CIPHER_NAME);
        }

        /**
         * Sets authentication data cipher.
         * 
         * @return
         */
        public Attributes setDataCipher(String dataCipher) {
            if (!isBlank(dataCipher)) {
                put(KEY_DATA_CIPHER_NAME, dataCipher);
            }
            return this;
        }

        /**
         * Gets authentication access_token signature.
         * 
         * @return
         */
        public String getAccessTokenSign() {
            return get(KEY_ACCESSTOKEN_SIGN_NAME);
        }

        /**
         * Sets authentication access_token signature.
         * 
         * @return
         */
        public Attributes setAccessTokenSign(String accessTokenSign) {
            if (!isBlank(accessTokenSign)) {
                put(KEY_ACCESSTOKEN_SIGN_NAME, accessTokenSign);
            }
            return this;
        }

        /**
         * Gets authentication rememberMe.
         * 
         * @return
         */
        public boolean getRememberMe() {
            return parseBoolean(valueOf(getOrDefault(KEY_REMEMBERME_NAME, FALSE.toString())));
        }

        /**
         * Sets authentication rememberMe.
         * 
         * @return
         */
        public Attributes setRememberMe(String rememberMe) {
            if (!isBlank(rememberMe)) {
                put(KEY_REMEMBERME_NAME, rememberMe);
            }
            return this;
        }

        /**
         * Gets sns {@link SocialAuthorizeInfo}
         * 
         * @return
         */
        public SocialAuthorizeInfo getSocialAuthorizeInfo() {
            return get(KEY_SNS_AUTHORIZED_INFO, SocialAuthorizeInfo.class);
        }

        /**
         * Sets sns {@link SocialAuthorizeInfo}
         * 
         * @return
         */
        public Attributes setSocialAuthorizeInfo(SocialAuthorizeInfo info) {
            return save(KEY_SNS_AUTHORIZED_INFO, info);
        }

        /**
         * Gets {@link User}
         * 
         * @return
         */
        public User getUser() {
            return get(KEY_IAM_SUBJECT_USER, User.class);
        }

        /**
         * Sets {@link User}
         * 
         * @return
         */
        public Attributes setUser(User user) {
            return save(KEY_IAM_SUBJECT_USER, user);
        }

        /**
         * Save attribute to IAM principal. {@link #get(String, Class)}
         * 
         * @param key
         * @param value
         * @return
         */
        public Attributes save(@NotBlank String key, @Nullable Object value) {
            hasTextOf(key, "attributeKey");
            if (!isNull(value)) {
                put(key, toJSONString(value));
            }
            return this;
        }

        /**
         * Load gets attribute from IAM principal. {@link #save(String, Object)}
         * 
         * @param key
         * @param valueType
         * @return
         */
        public <T> T get(@NotBlank String key, @NotNull Class<T> valueType) {
            hasTextOf(key, "attributeKey");
            notNullOf(valueType, "valueType");
            return parseJSON(get(key), valueType);
        }

        /**
         * NoOp {@link Attributes}
         */
        final public static Attributes NOOP = new Attributes() {
            private static final long serialVersionUID = 6965759508221042024L;

            @Override
            public String getSessionLang() {
                return null;
            }

            @Override
            public Attributes setSessionLang(String lang) {
                return null;
            }

            @Override
            public String getClientHost() {
                return null;
            }

            @Override
            public Attributes setClientHost(String clientHost) {
                return null;
            }

            @Override
            public String getParentSessionId() {
                return null;
            }

            @Override
            public Attributes setParentSessionId(String parentSessionId) {
                return null;
            }

            @Override
            public String getDataCipher() {
                return null;
            }

            @Override
            public Attributes setDataCipher(String dataCipher) {
                return null;
            }

            @Override
            public String getAccessTokenSign() {
                return null;
            }

            @Override
            public Attributes setAccessTokenSign(String accessTokenSign) {
                return null;
            }

            @Override
            public boolean getRememberMe() {
                return false;
            }

            @Override
            public Attributes setRememberMe(String rememberMe) {
                return null;
            }

            @Override
            public SocialAuthorizeInfo getSocialAuthorizeInfo() {
                return null;
            }

            @Override
            public Attributes setSocialAuthorizeInfo(SocialAuthorizeInfo info) {
                return null;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @Override
            public String get(Object key) {
                return null;
            }

            @Override
            public String getOrDefault(Object key, String defaultValue) {
                return null;
            }

            @Override
            public void clear() {
            }

            @Override
            protected boolean removeEldestEntry(Entry<String, String> eldest) {
                return false;
            }

            @Override
            public Set<String> keySet() {
                return null;
            }

            @Override
            public Collection<String> values() {
                return null;
            }

            @Override
            public Set<Entry<String, String>> entrySet() {
                return null;
            }

            @Override
            public void forEach(BiConsumer<? super String, ? super String> action) {
            }

            @Override
            public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public String put(String key, String value) {
                return null;
            }

            @Override
            public void putAll(Map<? extends String, ? extends String> m) {
            }

            @Override
            public String remove(Object key) {
                return null;
            }

            @Override
            public String putIfAbsent(String key, String value) {
                return null;
            }

            @Override
            public boolean remove(Object key, Object value) {
                return false;
            }

            @Override
            public boolean replace(String key, String oldValue, String newValue) {
                return false;
            }

            @Override
            public String replace(String key, String value) {
                return null;
            }

            @Override
            public String computeIfAbsent(String key, Function<? super String, ? extends String> mappingFunction) {
                return null;
            }

            @Override
            public String computeIfPresent(
                    String key,
                    BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
                return null;
            }

            @Override
            public String compute(String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
                return null;
            }

            @Override
            public String merge(
                    String key,
                    String value,
                    BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
                return null;
            }

            @Override
            public Object clone() {
                return this;
            }

        };

    }

    /**
     * Parameters for obtaining account information
     * 
     * @author wangl.sir
     * @version v1.0 2019年1月8日
     * @since
     */
    public static interface Parameter extends Serializable {

    }

    /**
     * SNS parameter definition
     * 
     * @author wangl.sir
     * @version v1.0 2019年1月8日
     * @since
     */
    public static interface SnsParameter extends Parameter {

        /**
         * If the provider is not empty, it means social network login. Provider
         * is optional: qq/wechat/sina/google/github/twitter/facebook/dingtalk,
         * etc.
         * 
         * @return
         */
        String getProvider();

        /**
         * Social networking services openId
         * 
         * @return
         */
        String getOpenId();

        /**
         * Social networking services unionId(optional)
         * 
         * @return
         */
        String getUnionId();

    }

    /**
     * Abstract base parameters definition
     * 
     * @author wangl.sir
     * @version v1.0 2019年1月8日
     * @since
     */
    public static abstract class BaseParameter implements Parameter {
        private static final long serialVersionUID = -898874009263858359L;

        final private String principal;

        public BaseParameter(String principal) {
            this.principal = notNullOf(principal, "principal");
        }

        public String getPrincipal() {
            return principal;
        }

    }

    /**
     * UsernamePassword sign-in parameter definition
     * 
     * @author wangl.sir
     * @version v1.0 2019年1月8日
     * @since
     */
    public static class SimpleParameter extends BaseParameter {
        private static final long serialVersionUID = -7501007252263127579L;

        public SimpleParameter(String principal) {
            super(principal);
        }
    }

    /**
     * SMS dynamic password sign-in parameter definition
     * 
     * @author wangl.sir
     * @version v1.0 2019年1月8日
     * @since
     */
    public static class SmsParameter extends BaseParameter {
        private static final long serialVersionUID = -7501007252263557579L;

        public SmsParameter(String principal) {
            super(principal);
        }

    }

    /**
     * SNS authorizing parameter definition
     * 
     * @author wangl.sir
     * @version v1.0 2019年1月8日
     * @since
     */
    public static class SnsAuthorizingParameter implements SnsParameter {
        private static final long serialVersionUID = -898874019263858359L;

        /**
         * If the provider is not empty, it means social network login. Provider
         * is optional: qq/wechat/sina/google/github/twitter/facebook/dingtalk,
         * etc.
         */
        final private String provider;

        /**
         * Social networking services openId
         */
        final private String openId;

        /**
         * Social networking services unionId
         */
        final private String unionId;

        /**
         * SnsAuthorizingParameter construction
         * 
         * @param provider
         * @param openId
         * @param unionId
         *            May be empty, Unionid is possible only when WeChat or
         *            Facebook public platforms
         */
        public SnsAuthorizingParameter(@NotBlank String provider, @Nullable String openId, @Nullable String unionId) {
            notNullOf(provider, "provider");
            this.provider = provider;
            this.openId = openId;
            this.unionId = unionId;
        }

        @Override
        public String getProvider() {
            return provider;
        }

        @Override
        public String getOpenId() {
            return openId;
        }

        @Override
        public String getUnionId() {
            return unionId;
        }

    }

    /**
     * Principal organization tree info.
     * 
     * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
     * @version 2020年5月18日 v1.0.0
     * @see
     */
    public static class PrincipalOrganization implements Serializable {
        private static final long serialVersionUID = -8256334665161483288L;

        /**
         * Principal organization identification. Organization structure, unique
         * ID, Uneditable.
         */
        private List<OrganizationInfo> organizations = new ArrayList<>(4);

        public PrincipalOrganization() {
            super();
        }

        public PrincipalOrganization(List<OrganizationInfo> organizations) {
            setOrganizations(organizations);
        }

        public List<OrganizationInfo> getOrganizations() {
            return organizations;
        }

        public List<OrganizationInfo> organizations() {
            return organizations;
        }

        public void setOrganizations(List<OrganizationInfo> organizations) {
            // if (!isEmpty(organizations)) {
            // this.organizations.addAll(organizations);
            // }
            this.organizations = organizations;
        }

        public PrincipalOrganization withOrganizations(List<OrganizationInfo> organizations) {
            setOrganizations(organizations);
            return this;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " => " + toJSONString(this);
        }

        /**
         * NoOp {@link PrincipalOrganization}
         */
        final public static PrincipalOrganization NOOP = new PrincipalOrganization() {

            private static final long serialVersionUID = 6965751108221042024L;

            @Override
            public List<OrganizationInfo> getOrganizations() {
                return null;
            }

            @Override
            public List<OrganizationInfo> organizations() {
                return null;
            }

            @Override
            public void setOrganizations(List<OrganizationInfo> organizations) {
            }

            @Override
            public PrincipalOrganization withOrganizations(List<OrganizationInfo> organizations) {
                return null;
            }

        };

    }

    /**
     * Organization info.
     *
     * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
     * @version 2020年5月18日 v1.0.0
     * @see
     */
    public static class OrganizationInfo implements Serializable {
        private static final long serialVersionUID = -8256333665161483288L;

        /**
         * Organization Unique identification
         */
        private String organizationCode;

        /**
         * Parent organization code
         */
        private String parent;

        /**
         * Type
         */
        private Integer type;

        /**
         * Name
         */
        private String name;

        private Long areaId;

        public OrganizationInfo() {
            super();
        }

        public OrganizationInfo(String organizationCode, String parent, Integer type, String name, Long areaId) {
            setOrganizationCode(organizationCode);
            setParent(parent);
            setType(type);
            setName(name);
            setAreaId(areaId);
        }

        public String getOrgCode() {
            return organizationCode;
        }

        public String organizationCode() {
            return isBlank(organizationCode) ? EMPTY : organizationCode;
        }

        public void setOrganizationCode(String organizationCode) {
            // hasTextOf(organizationCode, "organizationCode");
            this.organizationCode = organizationCode;
        }

        public OrganizationInfo withOrgCode(String organizationCode) {
            // hasTextOf(organizationCode, "organizationCode");
            this.organizationCode = organizationCode;
            return this;
        }

        public String getParent() {
            return parent;
        }

        public String parent() {
            return isBlank(parent) ? EMPTY : parent;
        }

        public void setParent(String parent) {
            // hasTextOf(parent, "parent");
            this.parent = parent;
        }

        public OrganizationInfo withParent(String parent) {
            setParent(parent);
            return this;
        }

        public Integer getType() {
            return type;
        }

        public Integer type() {
            return isNull(type) ? -1 : type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public OrganizationInfo withType(Integer type) {
            setType(type);
            return this;
        }

        public String getName() {
            return name;
        }

        public String name() {
            return isBlank(name) ? EMPTY : name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public OrganizationInfo withName(String name) {
            setName(name);
            return this;
        }

        public Long getAreaId() {
            return areaId;
        }

        public Long areaId() {
            return isNull(areaId) ? -1 : areaId;
        }

        public void setAreaId(Long areaId) {
            this.areaId = areaId;
        }

        public OrganizationInfo withAreaId(Long areaId) {
            this.areaId = areaId;
            return this;
        }

        /**
         * NoOp {@link OrganizationInfo}
         */
        final public static OrganizationInfo NOOP = new OrganizationInfo() {
            private static final long serialVersionUID = 6965751111221042024L;

        };

    }

}