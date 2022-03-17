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

import com.wl4g.infra.core.page.PageHolder;
import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.iam.common.bean.Dict;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * {@link DictService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-08-13
 * @sine v1.0
 * @see
 */
@FeignConsumer("${provider.serviceId.iam-facade:dict-service}")
@RequestMapping("/dict-service")
public interface DictService {

	@RequestMapping(value = "/list", method = POST)
	PageHolder<Dict> list(@RequestBody PageHolder<Dict> pm, @RequestParam("key") String key, @RequestParam("label") String label,
			@RequestParam("type") String type, @RequestParam(name="description",required=false) String description);

	@RequestMapping(value = "/save", method = POST)
	void save(@RequestBody Dict dict, @RequestParam("isEdit") Boolean isEdit);

	@RequestMapping(value = "/detail", method = GET)
	Dict detail(@RequestParam("key") String key);

	@RequestMapping(value = "/del", method = POST)
	void del(@RequestParam("key") String key);

	@RequestMapping(value = "/getByType", method = GET)
	List<Dict> getByType(@RequestParam("type") String type);

	@RequestMapping(value = "/getByKey", method = GET)
	Dict getByKey(@RequestParam("key") String key);

	@RequestMapping(value = "/allType", method = GET)
	List<String> allType();

	@RequestMapping(value = "/loadInit", method = GET)
	Map<String, Object> loadInit();

}