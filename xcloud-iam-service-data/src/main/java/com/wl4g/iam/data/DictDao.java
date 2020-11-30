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
package com.wl4g.iam.data;

import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.cloud.openfeign.FeignClient;

import com.wl4g.iam.common.bean.Dict;

import java.util.List;

/**
 * {@link DictDao}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@FeignClient("dictDao")
@RequestMapping("/dict")
public interface DictDao {
	int deleteByPrimaryKey(@RequestParam("key") String key);

	int insert(@RequestBody Dict record);

	int insertSelective(@RequestBody Dict record);

	Dict selectByPrimaryKey(@RequestParam("key") String key);

	int updateByPrimaryKeySelective(@RequestBody Dict record);

	int updateByPrimaryKey(@RequestBody Dict record);

	List<Dict> selectByType(@RequestParam("type") String type);

	List<String> allType();

	Dict getByKey(@RequestParam("key") String key);

	List<Dict> list(@RequestParam("key") @Param("key") String key, @RequestParam("label") @Param("label") String label,
			@RequestParam("type") @Param("type") String type,
			@RequestParam("description") @Param("description") String description,
			@RequestParam("orderBySort") @Param("orderBySort") String orderBySort);

}