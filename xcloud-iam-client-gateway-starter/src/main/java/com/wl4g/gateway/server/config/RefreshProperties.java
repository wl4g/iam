package com.wl4g.gateway.server.config;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link RefreshProperties}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-23
 * @since
 */
@Getter
@Setter
public class RefreshProperties {

	private Long refreshDelayMs = 5_000L;

}
