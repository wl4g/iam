/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
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

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.iam.common.bean.Company;

/**
 * {@link CompanyDao}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@FeignConsumer("${provider.serviceId.iam-data:iam-data}")
@RequestMapping("/company-dao")
public interface CompanyDao {

    @RequestMapping(value = "/deleteByPrimaryKey", method = { POST })
    int deleteByPrimaryKey(@RequestParam("id") Long id);

    @RequestMapping(method = POST, value = "/insert")
    int insert(@RequestBody Company record);

    @RequestMapping(method = POST, value = "/insertSelective")
    int insertSelective(@RequestBody Company record);

    @RequestMapping(value = "/selectByPrimaryKey", method = GET)
    Company selectByPrimaryKey(@RequestParam("id") Long id);

    @RequestMapping(value = "/selectByGroupId", method = GET)
    Company selectByGroupId(@RequestParam("groupId") Long groupId);

    @RequestMapping(value = "/updateByPrimaryKeySelective", method = { POST })
    int updateByPrimaryKeySelective(@RequestBody Company record);

    @RequestMapping(value = "/updateByPrimaryKey", method = { POST })
    int updateByPrimaryKey(@RequestBody Company record);

}