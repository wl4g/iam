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
package com.wl4g.iam.web;

import static com.wl4g.infra.common.lang.Assert2.notEmpty;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wl4g.iam.common.bean.RealmBean;
import com.wl4g.iam.service.RealmService;
import com.wl4g.infra.common.web.rest.RespBase;

/**
 * {@link RealmController}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-26 v3.0.0
 * @since v3.0.0
 */
@RestController
@RequestMapping("/realm")
public class RealmController {

    // @com.alibaba.dubbo.config.annotation.Reference
    @Autowired
    private RealmService realmService;

    @RequestMapping(value = "/list")
    public RespBase<?> findList(RealmBean record) {
        RespBase<Object> resp = RespBase.create();

        List<RealmBean> realms = realmService.findList(record);
        notEmpty(realms, "No found realm");
        resp.setData(realms);

        return resp;
    }

    @RequestMapping(value = "/save")
    @RequiresPermissions(value = { "iam:realm" })
    public RespBase<?> save(@RequestBody RealmBean oidcClient) {
        RespBase<Object> resp = RespBase.create();
        realmService.save(oidcClient);
        return resp;
    }

    @RequestMapping(value = "/del")
    @RequiresPermissions(value = { "iam:realm" })
    public RespBase<?> del(Long id) {
        RespBase<Object> resp = RespBase.create();
        realmService.del(id);
        return resp;
    }

    @RequestMapping(value = "/detail")
    @RequiresPermissions(value = { "iam:realm" })
    public RespBase<?> detail(Long id) {
        RespBase<Object> resp = RespBase.create();
        RealmBean oidcClient = realmService.detail(id);
        resp.setData(oidcClient);
        return resp;
    }

}