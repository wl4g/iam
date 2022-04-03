package com.wl4g.iam.gateway.loadbalance.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link HostWeight}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @author vjay
 * @date 2020-07-22
 * @since
 */
@Getter
@Setter
@ToString
public class HostWeight implements Serializable {
    private static final long serialVersionUID = 8940373806493080114L;
    private String uri;
    private int weight = 0;
}
