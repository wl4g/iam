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

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.iam.common.bean.Area;

/**
 * {@link AreaDao}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@FeignClient(name = "areaDao"/* , path = "/area" */)
@RequestMapping("/area")
public interface AreaDao {

	@DeleteMapping("/deleteByPrimaryKey")
	int deleteByPrimaryKey(@RequestParam("id") Long id);

	@PostMapping("/insert")
	int insert(@RequestBody Area record);

	@PostMapping("/insertSelective")
	int insertSelective(@RequestBody Area record);

	@GetMapping("/selectByPrimaryKey")
	Area selectByPrimaryKey(@RequestParam("id") Long id);

	@GetMapping("/getTotal")
	List<Area> getTotal();

	@PostMapping("/updateByPrimaryKeySelective")
	int updateByPrimaryKeySelective(Area record);

	@PostMapping("/updateByPrimaryKey")
	int updateByPrimaryKey(Area record);

}