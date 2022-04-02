package com.wl4g.iam.gateway.auth.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTHING_SIGN_TOKEN;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link AuthingProperties}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-23
 * @since
 */
@Getter
@Setter
@ToString
public class AuthingProperties {

    private GenericTokenAuthingProperties signToken = new GenericTokenAuthingProperties();

    @Getter
    @Setter
    @ToString
    public static class GenericTokenAuthingProperties {
        // Secret Load
        private SecretLoadFromType secretLoadFrom = SecretLoadFromType.ENV;
        private String secretLoadPrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTHING_SIGN_TOKEN;
        private Long secretLocalCacheSeconds = 6L;
        // Signature Replay Verify.
        private Boolean signReplayVerifyEnabled = true;
        private Long signReplayVerifyLocalCacheSeconds = 15 * 60L;
    }

    public static enum SecretLoadFromType {
        ENV, REDIS;
    }

}
