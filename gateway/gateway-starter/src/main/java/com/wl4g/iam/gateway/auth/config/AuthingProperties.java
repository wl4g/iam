package com.wl4g.iam.gateway.auth.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_SECRET;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REPLAY_BLOOM;

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
        private String secretLoadPrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_SECRET;

        /**
         * Local cache expiration time for signing keys.
         */
        private long secretLocalCacheSeconds = 6L;

        /**
         * Ignore authentication in JVM debug mode, often used for rapid
         * development and testing environments.
         */
        private boolean ignoredAuthingInJvmDebug = false;

        /**
         * Prefix when loading from bloom filter replay keys stored.
         */
        private String signReplayVerifyBloomLoadPrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REPLAY_BLOOM;

        /**
         * Publish event bus threads.
         */
        private int publishEventBusThreads = 1;

    }

    public static enum SecretLoadStore {
        ENV, REDIS;
    }

}
