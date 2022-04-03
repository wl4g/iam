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

        private SecretLoadStore secretLoadStore = SecretLoadStore.ENV;
        private String secretLoadPrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTHING_SIGN_TOKEN;
        private long secretLocalCacheSeconds = 6L;

        private boolean signReplayVerifyEnabled = true;
        private Long signReplayVerifyLocalCacheSeconds = 15 * 60L;

        /**
         * Skip or ignore authentication in JVM debug mode, often used for rapid
         * development and testing environments.
         */
        private boolean ignoredAuthingInJvmDebug = false;

        /**
         * Add the current authenticated client ID to the request header, this
         * will allow the back-end resource services to recognize the current
         * client ID.
         */
        private String addSignAuthClientIdHeader = DEFAULT_SIGN_AUTH_CLIENT_HEADER;

        // Default configuration options.
        public static final String DEFAULT_SIGN_AUTH_CLIENT_HEADER = "X-Sign-Auth-AppId";
    }

    public static enum SecretLoadStore {
        ENV, REDIS;
    }

}
