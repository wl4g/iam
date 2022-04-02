package com.wl4g.iam.gateway.bridge;

/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com,
 * 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * The main function of the class under this package is to bridge the original
 * spring webmvc/servlet request response model to webflux so that it can run in
 * the webflux environment. The main obstacles in the implementation process
 * are: for example, how to convert
 * {@linkplain javax.servlet.http.HttpServletRequest} to
 * {@linkplain org.springframework.http.server.reactive.ServerHttpRequest}.
 */