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

import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.iam.common.bean.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * {@link UserDao}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@FeignConsumer("${provider.serviceId.iam-data:iam-data}")
@RequestMapping("/user-dao")
public interface UserDao {

    @RequestMapping(value = "/deleteByPrimaryKey", method = { POST })
    int deleteByPrimaryKey(@RequestParam("id") Long id);

    @RequestMapping(method = POST, value = "/insert")
    int insert(@RequestBody User record);

    @RequestMapping(method = POST, value = "/insertSelective")
    int insertSelective(@RequestBody User record);

    @RequestMapping(method = GET, value = "/selectByPrimaryKey")
    User selectByPrimaryKey(@RequestParam("id") Long id);

    @RequestMapping(method = POST, value = "/updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(@RequestBody User record);

    @RequestMapping(method = POST, value = "/updateByPrimaryKey")
    int updateByPrimaryKey(@RequestBody User record);

    @RequestMapping(method = GET, value = "/list")
    List<User> list(
            @RequestParam(value = "userId", required = false) @Param("userId") Long userId,
            @RequestParam(value = "subject", required = false) @Param("subject") String subject,
            @RequestParam(value = "name", required = false) @Param("name") String name,
            @RequestParam(value = "givenName", required = false) @Param("givenName") String givenName,
            @RequestParam(value = "roleId", required = false) @Param("roleId") Long roleId);

    @RequestMapping(method = GET, value = "/selectBySubject")
    User selectBySubject(@RequestParam("subject") String subject);

    @RequestMapping(method = GET, value = "/selectBySelective")
    User selectBySelective(@RequestBody User user);

}