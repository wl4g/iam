package com.wl4g.iam.gateway.route.config;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link AuthingProperties}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-23
 * @since
 */
@Getter
@Setter
public class RouteProperties {

    private Long refreshDelayMs = 5_000L;

}
