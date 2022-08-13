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

import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wl4g.iam.common.bean.OidcClient;
import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;

/**
 * {@link OidcClientDao}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-26 v3.0.0
 * @since v3.0.0
 */
@FeignConsumer("${provider.serviceId.iam-data:iam-data}")
@RequestMapping("/oidc-client-dao")
public interface OidcClientDao {

    @RequestMapping(method = GET, value = "/deleteByPrimaryKey")
    int deleteByPrimaryKey(@RequestParam("id") @Param("id") Long id);

    @RequestMapping(method = POST, value = "/insert")
    int insert(@RequestBody OidcClient row);

    @RequestMapping(method = POST, value = "/insertSelective")
    int insertSelective(@RequestBody OidcClient row);

    // @RequestMapping(method = POST, value = "/selectForClient")
    // OidcClient selectForClient(@RequestBody OidcClient row);

    @RequestMapping(method = GET, value = "/selectByPrimaryKey")
    OidcClient selectByPrimaryKey(@RequestParam("id") @Param("id") Long id);

    @RequestMapping(method = POST, value = "/selectBySelective")
    List<OidcClient> selectBySelective(@RequestBody OidcClient row);

    @RequestMapping(method = POST, value = "/updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(@RequestBody OidcClient row);

    @RequestMapping(method = POST, value = "/updateByPrimaryKey")
    int updateByPrimaryKey(@RequestBody OidcClient row);
}