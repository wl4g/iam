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
package com.wl4g.iam.common.i18n;

import static com.wl4g.components.core.constants.IAMDevOpsConstants.KEY_LANG_NAME;
import static com.wl4g.iam.common.utils.IamSecurityHolder.getBindValue;

import java.util.Locale;
import java.util.Objects;

import com.wl4g.components.core.i18n.AbstractResourceMessageBundler;

/**
 * Session delegate resource bundle message source.
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年3月24日
 * @since
 */
public class SessionResourceMessageBundler extends AbstractResourceMessageBundler {

	public SessionResourceMessageBundler(Class<?> withClassPath) {
		super(withClassPath);
	}

	@Override
	protected Locale getSessionLocale() {
		Object loc = getBindValue(KEY_LANG_NAME);
		Locale locale = null;
		if (loc instanceof Locale) {
			locale = (Locale) loc;
		} else if (loc instanceof String) {
			locale = new Locale((String) loc);
		}
		return Objects.isNull(locale) ? Locale.SIMPLIFIED_CHINESE : locale;
	}

}