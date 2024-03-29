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

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.iam.common.bean.Menu;

/**
 * {@link MenuService}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @author vjay
 * @date 2019-10-30
 * @sine v1.0
 * @see
 */
@FeignConsumer("${provider.serviceId.iam-facade:menu-service}")
@RequestMapping("/menu-service")
public interface MenuService {

    @RequestMapping(value = "/findMenuTree", method = POST)
    Map<String, Object> findMenuTree();

    @RequestMapping(value = "/findMenuList", method = POST)
    List<Menu> findMenuList();

    @PostMapping("/save")
    void save(@RequestBody Menu menu);

    @RequestMapping(value = "/del", method = POST)
    void del(@RequestParam("id") Long id);

    @RequestMapping(value = "/detail", method = GET)
    Menu detail(@RequestParam("id") Long id);

    @RequestMapping(value = "/findRoot", method = GET)
    List<Menu> findRoot();

    @RequestMapping(value = "/findByUserId", method = GET)
    List<Menu> findByUserId(@RequestParam("userId") Long userId);

}