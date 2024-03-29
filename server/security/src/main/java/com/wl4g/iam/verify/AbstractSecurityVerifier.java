/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.iam.verify;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.web.WebUtils2.getRequestParam;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.BEAN_SESSION_RESOURCE_MSG_BUNDLER;
import static com.wl4g.iam.core.utils.IamSecurityHolder.bind;
import static com.wl4g.iam.core.utils.IamSecurityHolder.getBindValue;
import static com.wl4g.iam.core.utils.IamSecurityHolder.unbind;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import com.wl4g.infra.common.codec.Base58;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.iam.common.i18n.SessionResourceMessageBundler;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.configure.ServerSecurityConfigurer;
import com.wl4g.iam.core.cache.IamCacheManager;
import com.wl4g.iam.core.exception.VerificationException;
import com.wl4g.iam.core.session.IamSession.RelationAttrKey;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;
import com.wl4g.iam.verify.model.GenericVerifyModel;

/**
 * Abstract IAM verification handler
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年12月28日
 * @since
 */
public abstract class AbstractSecurityVerifier implements SecurityVerifier {

    final protected SmartLogger log = getLogger(getClass());

    /**
     * Verified token bit.
     */
    final public static int DEFAULT_VERIFIED_TOKEN_BIT = 128;

    /**
     * Verified token expiredMs.
     */
    final public static long DEFAULT_VERIFIED_TOKEN_EXPIREDMS = 60_000;

    /**
     * Server configuration properties
     */
    @Autowired
    protected IamProperties config;

    /**
     * IAM security configure handler
     */
    @Autowired
    protected ServerSecurityConfigurer configurer;

    /**
     * Enhanced cache manager.
     */
    @Autowired
    protected IamCacheManager cacheManager;

    /**
     * Delegate message source.
     */
    @Resource(name = BEAN_SESSION_RESOURCE_MSG_BUNDLER)
    protected SessionResourceMessageBundler bundle;

    /**
     * Validation
     */
    @Autowired
    protected Validator validator;

    /**
     * Get stored verify code of session
     *
     * @param assertion
     *            Do you need to assertion
     * @return Returns the currently valid verify-code (if create = true, the
     *         newly generated value or the old value)
     */
    @Override
    public VerifyCodeWrapper getVerifyCode(boolean assertion) {
        // Already created verify-code
        VerifyCodeWrapper code = getBindValue(new RelationAttrKey(getVerifyCodeStoredKey(), VerifyCodeWrapper.class), true);
        if (!isNull(code) && !isNull(code.getCode())) { // Assertion
            return code;
        }
        if (assertion) {
            log.warn("Assertion verifyCode expired. expireMs: {}", getVerifyCodeExpireMs());
            throw new VerificationException(bundle.getMessage("AbstractVerification.verify.expired"));
        }
        return null;
    }

    @Override
    public Object apply(String owner, @NotNull List<String> factors, @NotNull HttpServletRequest request) throws IOException {
        // Check limit attempts
        checkApplyAttempts(request, factors);
        // Renew or verify-code.
        reset(owner, true);

        // Do apply processing.
        return doApply(owner, factors, request);
    }

    /**
     * Apply verify code processing.
     *
     * @param owner
     * @param factors
     * @param request
     * @return
     * @throws IOException
     */
    public abstract Object doApply(String owner, @NotNull List<String> factors, @NotNull HttpServletRequest request)
            throws IOException;

    @Override
    public String verify(@NotBlank String params, @NotNull HttpServletRequest request, @NotNull List<String> factors)
            throws VerificationException {
        Assert.isTrue(!CollectionUtils.isEmpty(factors), "Verify factors must not be empty.");
        VerifyCodeWrapper storedCode = null;
        try {
            /*
             * If required is true, the forced verification policy is executed,
             * otherwise the maximum retry policy check is performed (that is,
             * the verification needs to be started only when the number of
             * times is retried).
             */
            if (!isEnabled(factors)) {
                return null; // not enabled
            }
            // Gets choosed secure algorithm.
            CryptKind kind = CryptKind.of(getRequestParam(request, config.getParam().getSecretAlgKindName(), true));

            // Decoding
            params = new String(Base58.decodeBase58(params), UTF_8);
            // Gets request verifyCode.
            Object submitCode = getRequestVerifyCode(params, request);
            // Stored verifyCode.
            storedCode = getVerifyCode(true);
            if (!doMatch(kind, storedCode, submitCode)) {
                log.error("Verification mismatched. {} => {}", submitCode, storedCode);
                throw new VerificationException(bundle.getMessage("AbstractVerification.verify.mismatch"));
            }

            // Storage verified token.
            String verifiedToken = "vefdt" + randomAlphabetic(DEFAULT_VERIFIED_TOKEN_BIT);
            log.info("Saving to verified token: {}", verifiedToken);
            return bind(new RelationAttrKey(getVerifiedTokenStoredKey(), getVerifiedTokenExpireMs()), verifiedToken);
        } finally {
            if (!isNull(storedCode)) {
                reset(storedCode.getOwner(), false); // Reset or create
            }
        }
    }

    @Override
    public void validate(@NotNull List<String> factors, @NotNull String verifiedToken, boolean required)
            throws VerificationException {
        // No verification required.
        if (!(required || isEnabled(factors))) {
            return;
        }

        // Check stored token.
        String storedVerifiedToken = getBindValue(new RelationAttrKey(getVerifiedTokenStoredKey(), String.class), true);
        if (isBlank(storedVerifiedToken)) {
            throw new VerificationException(bundle.getMessage("General.parameter.invalid"));
        }
        // Assertion verified token.
        if (!StringUtils.equals(storedVerifiedToken, verifiedToken)) {
            throw new VerificationException(bundle.getMessage("General.parameter.illegal"));
        }
    }

    /**
     * Reset the validate code to indicate a new generation when create is true
     *
     * @param owner
     *            Validate code owner(Optional).
     * @param renew
     *            is new create.
     */
    protected void reset(String owner, boolean renew) {
        RelationAttrKey verifyCodeKey = new RelationAttrKey(getVerifyCodeStoredKey(), getVerifyCodeExpireMs());
        unbind(verifyCodeKey);
        if (renew) {
            // Store verify-code in the session
            bind(verifyCodeKey, new VerifyCodeWrapper(owner, generateCode()));
        }
    }

    /**
     * Get submitted verify code.
     *
     * @param params
     * @param request
     * @return
     */
    protected abstract Object getRequestVerifyCode(@NotBlank String params, @NotNull HttpServletRequest request);

    /**
     * Match submitted validation code
     *
     * @param request
     * @param storedCode
     * @param submitCode
     * @return
     */
    protected boolean doMatch(@NotNull CryptKind kind, VerifyCodeWrapper storedCode, Object submitCode) {
        if (Objects.isNull(submitCode)) {
            return false;
        }
        if (submitCode instanceof GenericVerifyModel) {
            return trimToEmpty(storedCode.getCode()).equalsIgnoreCase(((GenericVerifyModel) submitCode).getVerifyCode());
        }
        throw new UnsupportedOperationException(String.format("Unsupported verify-code: %s, Override the doMatch() method",
                submitCode.getClass().getSimpleName()));
    }

    /**
     * Generate verify code
     *
     * @return Verify code object
     */
    protected Object generateCode() {
        return randomAlphabetic(5); // By-default
    }

    /**
     * Check the number of attempts to apply.
     *
     * @param request
     * @param response
     * @param factors
     *            Safety limiting factor(e.g. Client remote IP and login
     *            user-name)
     */
    protected abstract void checkApplyAttempts(@NotNull HttpServletRequest request, @NotNull List<String> factors);

    /**
     * Validity of the verification code (in milliseconds).
     *
     * @return
     */
    protected abstract long getVerifyCodeExpireMs();

    /**
     * Validity of the verified token (in milliseconds).
     *
     * @return
     */
    protected long getVerifiedTokenExpireMs() {
        return DEFAULT_VERIFIED_TOKEN_EXPIREDMS;
    }

    /**
     * Get verification code stored sessionKey.
     *
     * @return
     */
    private String getVerifyCodeStoredKey() {
        return "VERIFY_CODE." + kind().name();
    }

    /**
     * Get verification code stored sessionKey.
     *
     * @return
     */
    private String getVerifiedTokenStoredKey() {
        return "VERIFIED_TOKEN." + kind().name();
    }

}