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

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.wl4g.component.rpc.springboot.feign.annotation.SpringBootFeignClient;
import com.wl4g.iam.common.bean.ContactGroupRef;

/**
 * {@link ContactGroupRefDao}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@SpringBootFeignClient("${spring.application.name:contactGroupRef-dao}")
@RequestMapping("/contactGroupRef")
public interface ContactGroupRefDao {

	@RequestMapping(value = "/deleteByPrimaryKey", method = { POST })
	int deleteByPrimaryKey(@RequestParam("id") Long id);

	@RequestMapping(method = POST, value = "/insert")
	int insert(ContactGroupRef record);

	@RequestMapping(method = POST, value = "/insertSelective")
	int insertSelective(ContactGroupRef record);

	@RequestMapping(method = GET, value = "/selectByPrimaryKey")
	ContactGroupRef selectByPrimaryKey(@RequestParam("id") Long id);

	@RequestMapping(method = POST, value = "/updateByPrimaryKeySelective")
	int updateByPrimaryKeySelective(ContactGroupRef record);

	@RequestMapping(method = POST, value = "/updateByPrimaryKey")
	int updateByPrimaryKey(ContactGroupRef record);

	@RequestMapping(method = POST, value = "/deleteByContactId")
	int deleteByContactId(@RequestParam("id") Long id);

	@RequestMapping(method = GET, value = "/selectByContactId")
	List<ContactGroupRef> selectByContactId(@RequestParam("id") Long id);

}