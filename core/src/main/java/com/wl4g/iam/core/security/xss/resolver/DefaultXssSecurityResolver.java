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
package com.wl4g.iam.core.security.xss.resolver;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.iam.core.config.XssProperties;
import com.wl4g.iam.core.config.XssProperties.CharTranslator;

/**
 * {@link DefaultXssSecurityResolver}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2020年5月7日
 * @since
 */
public class DefaultXssSecurityResolver implements XssSecurityResolver {

	@Autowired
	protected XssProperties config;

	@Override
	public String doResolve(final Object controller, final Method method, final int index, final String value) {
		String safeValue = value;
		for (CharTranslator translator : config.getEscapeTranslators()) {
			safeValue = translator.getTranslator().translate(safeValue);
		}
		return safeValue;
	}

}