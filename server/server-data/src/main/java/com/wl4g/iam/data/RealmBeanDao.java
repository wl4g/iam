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

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.iam.common.bean.RealmBean;
import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;

@FeignConsumer("${provider.serviceId.iam-data:iam-data}")
@RequestMapping("/realm-dao")
public interface RealmBeanDao {

    @RequestMapping(method = GET, value = "/deleteByPrimaryKey")
    int deleteByPrimaryKey(Long id);

    @RequestMapping(method = POST, value = "/insert")
    int insert(RealmBean row);

    @RequestMapping(method = POST, value = "/insertSelective")
    int insertSelective(RealmBean row);

    @RequestMapping(method = GET, value = "/selectByPrimaryKey")
    RealmBean selectByPrimaryKey(Long id);

    @RequestMapping(method = POST, value = "/selectBySelective")
    List<RealmBean> selectBySelective(RealmBean row);

    @RequestMapping(method = POST, value = "/updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(RealmBean row);

    @RequestMapping(method = POST, value = "/updateByPrimaryKey")
    int updateByPrimaryKey(RealmBean row);
}