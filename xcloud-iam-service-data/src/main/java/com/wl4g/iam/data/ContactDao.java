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

import com.wl4g.iam.common.bean.Contact;

import java.util.List;

/**
 * {@link ContactDao}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@FeignClient("contactDao")
@RequestMapping("/contact")
public interface ContactDao {
	int deleteByPrimaryKey(@RequestParam("id") Long id);

	int insert(@RequestBody Contact record);

	int insertSelective(@RequestBody Contact record);

	Contact selectByPrimaryKey(@RequestParam("id") Long id);

	int updateByPrimaryKeySelective(@RequestBody Contact record);

	int updateByPrimaryKey(@RequestBody Contact record);

	List<Contact> list(@RequestParam("name") @Param("name") String name);

	List<Contact> getContactByGroupIds(@RequestParam("groupIds") @Param("groupIds") List<Long> groupIds);

}