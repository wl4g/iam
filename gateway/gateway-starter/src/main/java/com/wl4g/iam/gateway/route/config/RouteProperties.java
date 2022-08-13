package com.wl4g.iam.gateway.route.config;

import com.wl4g.iam.gateway.route.Https2HttpGlobalFilter;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link IamSecurityProperties}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2020-07-23
 * @since
 */
@Getter
@Setter
public class RouteProperties {

    /**
     * Enabled to https to http forward filter. {@link Https2HttpGlobalFilter}
     */
    private boolean forwaredHttpsToHttp = true;

    private Long refreshDelayMs = 30_000L;

}
