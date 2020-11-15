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
package com.wl4g.iam.service;

import java.util.List;
import java.util.Map;

import com.wl4g.components.data.page.PageModel;
import com.wl4g.iam.common.bean.Dict;

/**
 * {@link DictService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-08-13
 * @sine v1.0
 * @see
 */
public interface DictService {

	PageModel<Dict> list(PageModel<Dict> pm, String key, String label, String type, String description);

	void save(Dict dict, Boolean isEdit);

	Dict detail(String key);

	void del(String key);

	List<Dict> getByType(String type);

	Dict getByKey(String key);

	List<String> allType();

	Map<String, Object> loadInit();

}