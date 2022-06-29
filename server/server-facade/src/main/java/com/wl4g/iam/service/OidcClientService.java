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
package com.wl4g.iam.service;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wl4g.iam.common.bean.OidcClient;
import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;

/**
 * {@link OidcClientService}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-10-30
 * @sine v1.0
 * @see
 */
@FeignConsumer("${provider.serviceId.iam-facade}")
@RequestMapping("/oidc-client-service")
public interface OidcClientService {

    // @RequestMapping(value = "/loadOidcClient", method = POST)
    // OidcClient loadOidcClient(@RequestBody OidcClient record);

    @RequestMapping(value = "/findList", method = POST)
    List<OidcClient> findList(@RequestBody OidcClient record);

    @PostMapping("/save")
    void save(@RequestBody OidcClient record);

    @RequestMapping(value = "/del", method = POST)
    void del(@RequestParam("id") Long id);

    @RequestMapping(value = "/detail", method = GET)
    OidcClient detail(@RequestParam("id") Long id);

}