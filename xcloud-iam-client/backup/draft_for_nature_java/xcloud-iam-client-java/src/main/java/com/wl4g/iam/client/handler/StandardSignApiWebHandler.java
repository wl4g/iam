/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.iam.client.handler;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.components.common.lang.Assert2.hasTextOf;
import static com.wl4g.components.common.lang.Assert2.isTrue;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.components.common.web.WebUtils2.getMultiMapFirstValue;
import static com.wl4g.components.common.web.rest.RespBase.RetCode.UNAUTHC;
import static com.wl4g.iam.client.handler.StandardSignApiWebHandler.SignUtil.generateSign;
import static java.lang.Long.parseLong;
import static java.lang.String.format;

import static java.security.MessageDigest.isEqual;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import com.google.common.hash.Hashing;
import com.wl4g.components.common.web.rest.RespBase;
import com.wl4g.iam.client.annotation.IamHandlerMapping;
import com.wl4g.iam.client.configure.IamConfigurer;
import com.wl4g.iam.client.core.HttpIamRequest;
import com.wl4g.iam.client.core.HttpIamResponse;
import com.wl4g.iam.client.exception.SignatureValidationException;

/**
 * {@link StandardSignApiWebHandler}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-02
 * @since
 */
@IamHandlerMapping(uriPatterns = {})
public class StandardSignApiWebHandler extends AbstractWebHandler {

	public StandardSignApiWebHandler(IamConfigurer configurer) {
		super(configurer);
	}

	@Override
	public boolean preHandle(HttpIamRequest request, HttpIamResponse response, Object handler) throws Exception {
		// Parse OpenApiToken
		SignatureToken token = resolveSignatureToken(request);

		try {
			// Assertion token
			assertSignatureToken(request, token);
		} catch (SignatureValidationException e) {
			log.warn("Illegal signture", e.getMessage());

			// Write unauthentication message
			response.write(new RespBase<>(UNAUTHC).withThrowable(e).asJson());
			return false; // No continue
		}

		return true;
	}

	/**
	 * Parse {@link SignatureToken}
	 * 
	 * @param request
	 * @return
	 */
	protected SignatureToken resolveSignatureToken(HttpIamRequest request) {

		// TODO generic auto reflection parsing
		Map<String, List<String>> params = request.getQueryParams();
		String appId = getMultiMapFirstValue(params, "appId");
		String appSecret = getMultiMapFirstValue(params, "appSecret");
		String timestamp = getMultiMapFirstValue(params, "timestamp");
		String signature = getMultiMapFirstValue(params, "signature");

		// TODO generic validate requires
		return new SignatureToken(appId, appSecret, parseLong(timestamp), signature);
	}

	/**
	 * Assertion for API signture token.
	 * 
	 * @param request
	 * @param token
	 */
	protected void assertSignatureToken(HttpIamRequest request, SignatureToken token) throws SignatureValidationException {
		// Gets appSecret token.
		byte[] appSecret = getStoreAppIdSecret(request, token);

		// Signature assertion
		isTrue(isEqual(generateSign(token, appSecret), token.getSignature().getBytes(UTF_8)), SignatureValidationException.class,
				format("Invalid signature"));
	}

	/**
	 * Gets stored appId secret.
	 * 
	 * @param request
	 * @param token
	 * @return
	 */
	protected byte[] getStoreAppIdSecret(HttpIamRequest request, SignatureToken token) {
		// Query secret
		String storeAppSecret = configurer.loadAppSecret(token.getAppId());
		hasTextOf(storeAppSecret, "storeAppSecret");

		return storeAppSecret.getBytes(UTF_8);
	}

	/**
	 * {@link SignatureToken}
	 * 
	 * @see
	 */
	public static class SignatureToken {

		@NotBlank
		private String appId;
		@NotBlank
		private String appSecret;
		@NotBlank
		private Long timestamp;
		@NotBlank
		private String signature;

		public SignatureToken() {
			super();
		}

		public SignatureToken(@NotBlank String appId, @NotBlank String appSecret, @NotBlank Long timestamp,
				@NotBlank String signature) {
			setAppId(appId);
			setAppSecret(appSecret);
			setTimestamp(timestamp);
			setSignature(signature);
		}

		public String getAppId() {
			return appId;
		}

		public void setAppId(String appId) {
			hasTextOf(appId, "appId");
			this.appId = appId;
		}

		public String getAppSecret() {
			return appSecret;
		}

		public void setAppSecret(String appSecret) {
			hasTextOf(appSecret, "appSecret");
			this.appSecret = appSecret;
		}

		public Long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Long timestamp) {
			notNullOf(timestamp, "timestamp");
			this.timestamp = timestamp;
		}

		public String getSignature() {
			return signature;
		}

		public void setSignature(String signature) {
			hasTextOf(signature, "signature");
			this.signature = signature;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
		}

	}

	/**
	 * {@link SignUtil}
	 *
	 * @since
	 */
	public static class SignUtil {

		/**
		 * Generate open API signature
		 * 
		 * @param token
		 * @param secToken
		 * @return
		 */
		public static byte[] generateSign(SignatureToken token, byte[] secToken) {
			// Join token parts
			StringBuffer signtext = new StringBuffer();
			signtext.append(token.getAppId());
			signtext.append(token.getAppSecret());
			signtext.append(token.getTimestamp());
			signtext.append(token.getTimestamp());

			// Ascii sort
			byte[] signInput = signtext.toString().getBytes(UTF_8);
			Arrays.sort(signInput);

			// Calc signature
			byte[] sign = Hashing.sha256().hashBytes(signInput).asBytes();
			return sign;
		}

	}

}
