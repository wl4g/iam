package com.wl4g.iam.gateway.auth.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REDIS_RECORDER_FAILURE;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REDIS_RECORDER_SUCCESS;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REPLAY_BLOOM;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_SECRET;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_SUFFIX_IAM_GATEWAY_REDIS_RECORDER;

import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

        private SimpleSignAuthingEventProperties event = new SimpleSignAuthingEventProperties();
    }

    public static enum SecretLoadStore {
        ENV, REDIS;
    }

    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SimpleSignAuthingEventProperties {

        /**
         * Publish event bus threads.
         */
        private int publishEventBusThreads = 1;

        /**
         * Based on whether the redis event logger enables logging, if it is
         * turned on, it can be used as a downgrade recovery strategy when data
         * is lost due to a catastrophic failure of the persistent accumulator.
         */
        private boolean redisEventRecoderLogEnabled = true;

        /**
         * Redis event recorder success accumulator key.
         */
        private String redisEventRecoderSuccessCumulatorPrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REDIS_RECORDER_SUCCESS;

        /**
         * Redis event recorder failure accumulator prefix.
         */
        private String redisEventRecoderFailureCumulatorPrefix = CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REDIS_RECORDER_FAILURE;

        /**
         * Redis event recorder accumulator suffix of date format pattern.
         */
        private String redisEventRecoderCumulatorSuffixOfDatePattern = CACHE_SUFFIX_IAM_GATEWAY_REDIS_RECORDER;

    }

}
