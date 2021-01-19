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
package com.wl4g.iam.common.constant;

/**
 * Configuration IAM constnats.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-17
 * @sine v1.0
 * @see
 */
public interface ConfigIAMConstants {

	public static final String KEY_IAM_CONFIG_PREFIX = "spring.xcloud.iam";

	/**
	 * System dictionaries cache key.
	 */
	public static final String CACHE_DICT_INIT_NAME = "dict_init_cache";

	/**
	 * System dictionaries cache time(sec)
	 */
	public static final int CACHE_DICT_INIT_EXPIRE_SEC = 60;

}
