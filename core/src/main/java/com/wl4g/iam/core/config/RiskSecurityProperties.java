package com.wl4g.iam.core.config;

import static com.wl4g.iam.common.constant.BaseIAMConstants.KEY_IAM_CONFIG_PREFIX;

import java.io.Serializable;

/**
 * {@link RiskSecurityProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-06-14 v1.0.0
 * @see v1.0.0
 */
public class RiskSecurityProperties implements Serializable {
    private static final long serialVersionUID = 8618009279751170611L;

    /**
     * Check whether the request IP is the same as the time of login (e.g. multi
     * NIC Internet switch, hacker interception attack).
     */
    private boolean checkRequestIpSameLogin = true;

    public boolean isCheckRequestIpSameLogin() {
        return checkRequestIpSameLogin;
    }

    public void setCheckRequestIpSameLogin(boolean checkRequestIpSameLogin) {
        this.checkRequestIpSameLogin = checkRequestIpSameLogin;
    }

    public static final String KEY_RISK_PREFIX = KEY_IAM_CONFIG_PREFIX + ".risk";

}
