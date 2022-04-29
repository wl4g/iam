package com.wl4g.iam.gateway.security.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_EVENT_FAILURE;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_EVENT_SUCCESS;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REPLAY_BLOOM;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_SECRET;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYYYMMDD;

import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link IamSecurityProperties}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-23
 * @since
 */
@Getter
@Setter
@ToString
public class IamSecurityProperties {

    private SimpleSignAuthingProperties simpleSign = new SimpleSignAuthingProperties();

    @Getter
    @Setter
    @ToString
    public static class SimpleSignAuthingProperties {

        /**
         * Load signing keys from that type of stored.
         */
        private SecretStore secretStore = SecretStore.ENV;

        /**
         * Prefix when loading from signing keys stored.
         */
        private String secretStorePrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_SECRET;

        /**
         * Local cache expiration time for signing keys.
         */
        private long secretLocalCacheSeconds = 6L;

        /**
         * Ignore authentication in JVM debug mode, often used for rapid
         * development and testing environments.
         */
        private boolean anonymousAuthingWithJvmDebug = false;

        /**
         * Prefix when loading from bloom filter replay keys stored.
         */
        private String signReplayVerifyBloomLoadPrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REPLAY_BLOOM;

        private EventRecorderProperties eventRecorder = new EventRecorderProperties();
    }

    public static enum SecretStore {
        ENV, REDIS;
    }

    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventRecorderProperties {

        /**
         * Publish eventRecorder bus threads.
         */
        private int publishEventBusThreads = 1;

        /**
         * Based on whether the redis eventRecorder logger enables logging, if
         * it is turned on, it can be used as a downgrade recovery strategy when
         * data is lost due to a catastrophic failure of the persistent
         * accumulator.
         */
        private boolean localLogEnabled = true;

        /**
         * Redis eventRecorder recorder configuration.
         */
        private RedisEventRecorderProperties redis = new RedisEventRecorderProperties();

    }

    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RedisEventRecorderProperties {

        private boolean enabled = true;

        /**
         * Redis eventRecorder recorder success accumulator key.
         */
        private String successCumulatorPrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_EVENT_SUCCESS;

        /**
         * Redis eventRecorder recorder failure accumulator prefix.
         */
        private String failureCumulatorPrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_EVENT_FAILURE;

        /**
         * Redis eventRecorder recorder accumulator suffix of date format
         * pattern.
         */
        private String cumulatorSuffixOfDatePattern = CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYYYMMDD;

    }

}
