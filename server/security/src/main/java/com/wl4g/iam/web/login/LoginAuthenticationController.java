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
package com.wl4g.iam.web.login;

import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_ERR_SESSION_SAVED;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_LANG_NAME;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_LOGIN_APPLY_LOCALE;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_LOGIN_CHECK;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_LOGIN_ERRREAD;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_LOGIN_HANDSHAKE;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_LOGIN_PERMITS;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_VERIFY_APPLY_CAPTCHA;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_VERIFY_BASE;
import static com.wl4g.iam.core.config.AbstractIamProperties.IamVersion.V2_0_0;
import static com.wl4g.iam.core.security.xsrf.repository.XsrfTokenRepository.XsrfUtil.saveWebXsrfTokenIfNecessary;
import static com.wl4g.iam.core.utils.IamAuthenticatingUtils.sessionStatus;
import static com.wl4g.iam.core.utils.IamSecurityHolder.bind;
import static com.wl4g.iam.core.utils.IamSecurityHolder.checkSession;
import static com.wl4g.iam.core.utils.IamSecurityHolder.getBindValue;
import static com.wl4g.iam.core.utils.IamSecurityHolder.getPrincipalInfo;
import static com.wl4g.iam.core.utils.IamSecurityHolder.getSession;
import static com.wl4g.iam.core.utils.RiskSecurityUtils.getV1Factors;
import static com.wl4g.iam.web.login.model.CaptchaCheckModel.KEY_CAPTCHA_CHECK;
import static com.wl4g.iam.web.login.model.GenericCheckModel.KEY_GENERIC_CHECK;
import static com.wl4g.iam.web.login.model.SmsCheckModel.KEY_SMS_CHECK;
import static com.wl4g.infra.common.codec.Base58.encodeBase58;
import static com.wl4g.infra.common.lang.TypeConverts.parseLongOrNull;
import static com.wl4g.infra.common.web.WebUtils2.getHttpRemoteAddr;
import static com.wl4g.infra.common.web.WebUtils2.getRFCBaseURI;
import static com.wl4g.infra.common.web.WebUtils2.getRequestParam;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.shiro.web.util.WebUtils.getCleanParam;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wl4g.iam.annotation.LoginAuthController;
import com.wl4g.iam.authc.credential.secure.IamCredentialsSecurer;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.core.security.xsrf.repository.XsrfTokenRepository;
import com.wl4g.iam.crypto.SecureCryptService;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;
import com.wl4g.iam.handler.risk.RiskEvaluateHandler;
import com.wl4g.iam.verify.CompositeSecurityVerifierAdapter;
import com.wl4g.iam.verify.SecurityVerifier.VerifyCodeWrapper;
import com.wl4g.iam.verify.SecurityVerifier.VerifyKind;
import com.wl4g.iam.web.BaseIamController;
import com.wl4g.iam.web.login.model.CaptchaCheckModel;
import com.wl4g.iam.web.login.model.GenericCheckModel;
import com.wl4g.iam.web.login.model.HandshakeModel;
import com.wl4g.iam.web.login.model.IamPrincipalPermitsModel;
import com.wl4g.iam.web.login.model.SmsCheckModel;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.common.framework.operator.GenericOperatorAdapter;

/**
 * IAM login authentication controller
 *
 * @author wangl.sir
 * @version v1.0 2019年1月22日
 * @since
 */
@LoginAuthController
public class LoginAuthenticationController extends BaseIamController {

    /**
     * Composite verification handler.
     */
    @Autowired
    protected CompositeSecurityVerifierAdapter verifier;

    /**
     * IAM credentials securer
     */
    @Autowired
    protected IamCredentialsSecurer securer;

    /**
     * Risk control evaluator handler.
     */
    @Autowired
    protected RiskEvaluateHandler riskEvaluatorHandler;

    /**
     * Secure cryption service.
     */
    @Autowired
    protected GenericOperatorAdapter<CryptKind, SecureCryptService> cryptAdapter;

    /**
     * XSRF token repository. (If necessary)
     */
    @Autowired(required = false)
    protected XsrfTokenRepository xTokenRepository;

    /**
     * Apply session, applicable to mobile token session.
     *
     * @param request
     */
    @RequestMapping(value = URI_IAM_SERVER_LOGIN_HANDSHAKE, method = { POST })
    @ResponseBody
    public RespBase<?> handshake(HttpServletRequest request, HttpServletResponse response) {
        log.debug("called:handshake '{}' from '{}'", URI_IAM_SERVER_LOGIN_HANDSHAKE, request.getRemoteHost());
        checkSessionHandle(request, false);

        RespBase<Object> resp = RespBase.create(sessionStatus());
        // Generate refresh xsrf token.
        saveWebXsrfTokenIfNecessary(xTokenRepository, request, response, false);

        // Reponed handshake result.
        HandshakeModel handshake = new HandshakeModel(V2_0_0.getVersion());
        // Current supports crypt algorithms.
        handshake.setAlgorithms(
                cryptAdapter.getRunningKinds().stream().map(k -> encodeBase58(k.getAlgorithm())).collect(toList()));
        // Assgin sessionKeyId
        handshake.getSession().setSessionKey(config.getParam().getSid());
        handshake.getSession().setSessionValue(getSession(true).getId());
        resp.setData(handshake);
        return resp;
    }

    /**
     * Login before environmental security check.
     *
     * @param request
     */
    @RequestMapping(value = URI_IAM_SERVER_LOGIN_CHECK, method = { POST })
    @ResponseBody
    public RespBase<?> check(HttpServletRequest request) {
        log.debug("called:check '{}' from '{}'", URI_IAM_SERVER_LOGIN_CHECK, request.getRemoteHost());
        checkSessionHandle(request, true);

        RespBase<Object> resp = RespBase.create(sessionStatus());
        //
        // --- Check generic authenticating environments. ---
        //
        // Gets choosed secure algorithm.
        String alg = getRequestParam(request, config.getParam().getSecretAlgKindName(), true);
        // Login account number or mobile number(Optional)
        String principal = getCleanParam(request, config.getParam().getPrincipalName());
        // Limit factors
        List<String> factors = getV1Factors(getHttpRemoteAddr(request), principal);

        // When the login page is loaded, the parameter 'principal' will be
        // empty, no need to generate a key. When submitting the login
        // request parameter 'principal' will not be empty, you need to
        // generate 'secret'.
        GenericCheckModel generic = new GenericCheckModel();
        if (!isBlank(principal)) {
            // Apply credentials encryption secret(pubKey)
            generic.setSecretKey(securer.applySecret(CryptKind.of(alg), principal));
        }
        // Assign a session ID to the current request. If not, create a new one.
        resp.forMap().put(KEY_GENERIC_CHECK, generic);

        //
        // --- Check captcha authenticating environments. ---
        //
        CaptchaCheckModel captcha = new CaptchaCheckModel(false);
        if (verifier.forOperator(request).isEnabled(factors)) {
            captcha.setEnabled(true);
            captcha.setSupport(VerifyKind.SUPPORT_ALL); // Default
            captcha.setApplyUri(
                    getRFCBaseURI(request, true) + URI_IAM_SERVER_VERIFY_BASE + "/" + URI_IAM_SERVER_VERIFY_APPLY_CAPTCHA);
        }
        resp.forMap().put(KEY_CAPTCHA_CHECK, captcha);

        //
        // --- Check SMS authenticating environments. ---
        //
        // When the SMS verification code is not empty, this creation
        // time-stamp is returned (used to display the current remaining
        // number of seconds before the front end can re-send the SMS
        // verification code).
        VerifyCodeWrapper code = verifier.forOperator(VerifyKind.TEXT_SMS).getVerifyCode(false);

        // SMS apply owner(mobile number).
        Long mobileNum = null;
        if (nonNull(code)) {
            mobileNum = parseLongOrNull(code.getOwner());
        }

        // Remaining delay.
        Long remainDelay = null;
        if (Objects.nonNull(code)) {
            remainDelay = code.getRemainDelay(config.getMatcher().getFailFastSmsDelay());
        }
        resp.forMap().put(KEY_SMS_CHECK, new SmsCheckModel(nonNull(mobileNum), mobileNum, remainDelay));

        return resp;
    }

    /**
     * Used for page Jump mode, to read operation exception messages.
     *
     * @param request
     * @return
     */
    @RequestMapping(value = URI_IAM_SERVER_LOGIN_ERRREAD, method = { GET })
    @ResponseBody
    public RespBase<?> readError(HttpServletRequest request) {
        log.debug("called:readError '{}' from '{}'", URI_IAM_SERVER_LOGIN_ERRREAD, request.getRemoteHost());
        checkSessionHandle(request, true);

        RespBase<String> resp = RespBase.create(sessionStatus());
        // Read error message from session
        String errmsg = getBindValue(KEY_ERR_SESSION_SAVED, true);
        errmsg = isBlank(errmsg) ? "" : errmsg;
        resp.forMap().put(KEY_ERR_SESSION_SAVED, errmsg);

        return resp;
    }

    /**
     * Used for page Jump mode, to read authenticated roles/permissions/...
     * info.
     *
     * @param request
     * @return
     */
    @RequestMapping(value = URI_IAM_SERVER_LOGIN_PERMITS, method = { GET })
    @ResponseBody
    public RespBase<?> readPermits(HttpServletRequest request) {
        log.debug("called:readPermits '{}' from '{}'", URI_IAM_SERVER_LOGIN_PERMITS, request.getRemoteHost());
        checkSessionHandle(request, true);

        RespBase<Object> resp = RespBase.create(sessionStatus());
        // Gets current session authentication permits info.
        IamPrincipal info = getPrincipalInfo();
        IamPrincipalPermitsModel result = new IamPrincipalPermitsModel();
        result.setRoles(info.getRoles()); // Roles
        result.setPermissions(info.getPermissions()); // Permissions
        resp.setData(result);
        return resp;
    }

    /**
     * Apply international locale.</br>
     * See:{@link com.wl4g.iam.common.i18n.SessionResourceMessageBundler}
     * See:{@link org.springframework.context.support.MessageSourceAccessor}
     *
     * @param response
     */
    @RequestMapping(value = URI_IAM_SERVER_LOGIN_APPLY_LOCALE, method = { POST })
    @ResponseBody
    public RespBase<?> applyLocale(HttpServletRequest request) {
        log.debug("called:applyLocale '{}' from '{}'", URI_IAM_SERVER_LOGIN_APPLY_LOCALE, request.getRemoteHost());
        checkSessionHandle(request, true);

        RespBase<Locale> resp = RespBase.create(sessionStatus());
        String lang = getCleanParam(request, config.getParam().getI18nLang());
        if (isBlank(lang)) { // Fallback
            lang = request.getLocale().getLanguage();
        }

        // Save apply locale.
        bind(KEY_LANG_NAME, lang);
        resp.forMap().put(KEY_LANG_NAME, lang);
        return resp;
    }

    /**
     * Check session pre-handle.
     * 
     * @param request
     */
    private void checkSessionHandle(HttpServletRequest request, boolean checkSession) {
        if (checkSession) {
            // Check sessionKeyId
            // @see:com.wl4g.devops.iam.web.LoginAuthenticatorController#handhake()
            checkSession();
        }
        // Check umidToken validatity.
        riskEvaluatorHandler.checkEvaluation(request);
    }

}