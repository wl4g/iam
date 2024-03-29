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
package com.wl4g.iam.core.security.xsrf.handler;

import javax.servlet.http.HttpServletRequest;

import com.wl4g.iam.core.security.xsrf.repository.XsrfToken;

/**
 * Thrown when an expected {@link XsrfToken} exists, but it does not match the
 * value present on the {@link HttpServletRequest}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2020年4月27日
 * @since
 */
public class InvalidXsrfTokenException extends XsrfException {
	private static final long serialVersionUID = -6917353257503001262L;

	public InvalidXsrfTokenException(XsrfToken expectedAccessToken, String actualAccessToken) {
		super("Invalid XSRF Token '" + actualAccessToken + "' was found");
	}

}