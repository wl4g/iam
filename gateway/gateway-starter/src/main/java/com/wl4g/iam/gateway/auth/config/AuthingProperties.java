package com.wl4g.iam.gateway.auth.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIMPLE_SIGN;

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

    private SimpleSignAuthingProperties simpleSign = new SimpleSignAuthingProperties();

    @Getter
    @Setter
    @ToString
    public static class SimpleSignAuthingProperties {

        /**
         * Load signing keys from that type of stored.
         */
        private SecretLoadStore secretLoadStore = SecretLoadStore.ENV;

        /**
         * Prefix when loading from signing keys stored.
         */
        private String secretLoadPrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIMPLE_SIGN;

        /**
         * Local cache expiration time for signing keys.
         */
        private long secretLocalCacheSeconds = 6L;

        /**
         * Whether to enable signature replay attack interception.
         */
        private boolean signReplayVerifyEnabled = true;

        /**
         * TODO use redis cached signing.</br>
         * 
         * Local cache expiration time for blocking replay attacks.
         */
        private Long signReplayVerifyLocalCacheSeconds = 15 * 60L;

        /**
         * Ignore authentication in JVM debug mode, often used for rapid
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
