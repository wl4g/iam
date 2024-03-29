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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wl4g.infra.common.web.WebUtils2;
import com.wl4g.infra.common.web.rest.RespBase;

/**
 * {@link DefaultXsrfRejectHandler}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2020年4月27日
 * @since
 */
public class DefaultXsrfRejectHandler implements XsrfRejectHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, XsrfException xe)
			throws IOException, ServletException {
		RespBase<String> resp = RespBase.create();
		resp.handleError(xe);
		WebUtils2.writeJson(response, resp.asJson());
	}

}