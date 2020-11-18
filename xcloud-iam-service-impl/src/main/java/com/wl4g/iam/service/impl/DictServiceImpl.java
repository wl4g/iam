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
package com.wl4g.iam.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageHelper;
import com.wl4g.components.core.web.model.PageModel;
import com.wl4g.components.support.redis.jedis.JedisService;
import com.wl4g.iam.common.bean.Dict;
import com.wl4g.iam.data.DictDao;
import com.wl4g.iam.service.DictService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wl4g.components.common.lang.Assert2.hasText;
import static com.wl4g.components.common.lang.Assert2.isNull;
import static com.wl4g.components.common.lang.Assert2.notEmpty;
import static com.wl4g.components.common.lang.Assert2.notNull;
import static com.wl4g.components.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.components.core.bean.BaseBean.DEL_FLAG_DELETE;
import static com.wl4g.iam.common.constant.ConfigIAMConstants.CACHE_DICT_INIT_EXPIRE_SEC;
import static com.wl4g.iam.common.constant.ConfigIAMConstants.CACHE_DICT_INIT_NAME;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * {@link DictService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-08-13
 * @sine v1.0
 * @see
 */
// @org.springframework.stereotype.Service
// @com.alibaba.dubbo.config.annotation.Service(group = "dictService")
@RestController
public class DictServiceImpl implements DictService {

	@Autowired
	private DictDao dictDao;

	@Autowired
	private JedisService jedisService;

	@Override
	public PageModel<Dict> list(PageModel<Dict> pm, String key, String label, String type, String description) {
		pm.page(PageHelper.startPage(pm.getPageNum(), pm.getPageSize(), true));
		pm.setRecords(dictDao.list(key, label, type, description, null));
		return pm;
	}

	@Override
	public void save(Dict dict, Boolean isEdit) {
		if (isEdit) {
			dict.preUpdate();
			dictDao.updateByPrimaryKeySelective(dict);
		} else {
			validateValues(dict);
			validateRepeat(dict);
			dict.preInsert();
			dictDao.insertSelective(dict);
		}
		jedisService.del(CACHE_DICT_INIT_NAME); // Cleanup cache.
	}

	@Override
	public Dict detail(String key) {
		return dictDao.selectByPrimaryKey(key);
	}

	@Override
	public void del(String key) {
		hasText(key, "id is null");
		Dict dict = new Dict();
		dict.setKey(key);
		dict.preUpdate();
		dict.setDelFlag(DEL_FLAG_DELETE);
		dictDao.updateByPrimaryKeySelective(dict);
	}

	@Override
	public List<Dict> getByType(String type) {
		return dictDao.selectByType(hasText(type, "dictType is requires"));
	}

	@Override
	public Dict getByKey(String key) {
		hasText(key, "key is blank");
		return dictDao.getByKey(key);
	}

	@Override
	public List<String> allType() {
		return dictDao.allType();
	}

	@Override
	public Map<String, Object> loadInit() {
		String s = jedisService.get(CACHE_DICT_INIT_NAME);
		Map<String, Object> result;
		if (!isBlank(s)) {
			result = parseJSON(s, new TypeReference<Map<String, Object>>() {
			});
		} else {
			result = new HashMap<>();
			List<Dict> dicts = dictDao.list(null, null, null, null, "1");
			notEmpty(dicts, "get dict from db is empty,Please check your db,table=sys_dict");

			Map<String, List<Dict>> dictList = new HashMap<>();
			Map<String, Map<String, Dict>> dictMap = new HashMap<>();
			for (Dict dict : dicts) {
				String type = dict.getType();
				// Dictionaries list
				List<Dict> list = dictList.getOrDefault(type, new ArrayList<>());
				list.add(dict);
				dictList.put(type, list);

				// Dictionaries map
				Map<String, Dict> map = dictMap.getOrDefault(type, new HashMap<>());
				map.put(dict.getValue(), dict);
				dictMap.put(type, map);
			}
			result.put("dictList", dictList);
			result.put("dictMap", dictMap);

			// Cache to redis
			String s1 = toJSONString(result);
			jedisService.set(CACHE_DICT_INIT_NAME, s1, CACHE_DICT_INIT_EXPIRE_SEC);
		}
		return result;
	}

	private void validateRepeat(Dict dict) {
		isNull(dictDao.selectByPrimaryKey(dict.getKey()), "Cannot add duplicate dictionary. %s", dict);
	}

	private void validateValues(Dict dict) {
		notNull(dict, "dict is null");
		hasText(dict.getKey(), "key is null");
		hasText(dict.getType(), "key is null");
		hasText(dict.getLabel(), "key is null");
	}

}