package com.wl4g.iam.common.bean;

import com.wl4g.infra.core.bean.BaseBean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link RealmBean}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt; * @version
 *         2022-03-30 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class RealmBean extends BaseBean {
    private static final long serialVersionUID = -9160052379887868918L;
    private String name;
    private String displayName;
    private Integer enable;
    private String deployFrontendUri;
    private Integer userRegistrationEnabled;
    private Integer forgotPasswordEnabled;
    private Integer rememberMeEnabled;
    private Integer emailLoginEnabled;
    private Integer smsLoginEnabled;
    private Integer editUsernameEnabled;
    private String securityDefenseJson;
    private String jwksJson;
}