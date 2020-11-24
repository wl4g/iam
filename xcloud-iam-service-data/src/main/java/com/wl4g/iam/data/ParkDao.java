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

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;

import com.wl4g.iam.common.bean.Park;

/**
 * {@link ParkDao}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@FeignClient("parkDao")
public interface ParkDao {

	int deleteByPrimaryKey(@RequestParam("id") Long id);

	int insert(@RequestBody Park record);

	int insertSelective(@RequestBody Park record);

	Park selectByPrimaryKey(@RequestParam("id") Long id);

	Park selectByGroupId(@RequestParam("groupId") Long groupId);

	int updateByPrimaryKeySelective(@RequestBody Park record);

	int updateByPrimaryKey(@RequestBody Park record);

}