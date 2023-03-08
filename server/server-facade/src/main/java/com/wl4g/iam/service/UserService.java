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
package com.wl4g.iam.service;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Set;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.wl4g.infra.common.bean.page.PageHolder;
import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.iam.common.bean.Menu;
import com.wl4g.iam.common.bean.User;

/**
 * {@link UserService}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @author vjay
 * @date 2019-10-28
 * @sine v1.0
 * @see
 */
@FeignConsumer("${provider.serviceId.iam-facade:user-service}")
@RequestMapping("/user-service")
public interface UserService {

    @RequestMapping(value = "/findSimpleUser", method = GET)
    User findSimpleUser(@RequestParam("id") Long id);

    @RequestMapping(value = "/getMenusByUser", method = GET)
    Set<Menu> getMenusByUser(@RequestParam("userId") Long userId);

    @RequestMapping(path = "/list", method = RequestMethod.POST)
    PageHolder<User> list(
            @RequestBody PageHolder<User> pm,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "roleId", required = false) Long roleId);

    @RequestMapping(value = "/save", method = POST)
    void save(@RequestBody User user);

    @RequestMapping(value = "/del", method = POST)
    void del(@RequestParam("userId") Long userId);

    @RequestMapping(value = "/detail", method = GET)
    User detail(@RequestParam("userId") Long userId);

    @RequestMapping(value = "/findBySubject", method = GET)
    User findBySubject(@RequestParam("subject") String subject);

    @RequestMapping(value = "/findBySelective", method = POST)
    User findBySelective(@RequestBody User user);

}