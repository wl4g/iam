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
package com.wl4g.iam.web;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wl4g.iam.common.bean.Dict;
import com.wl4g.iam.service.DictService;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.page.PageHolder;
import com.wl4g.infra.core.web.BaseController;

/**
 * Dictionaries controller
 * 
 * @author vjay
 * @date 2019-06-24 14:23:00
 */
@RestController
@RequestMapping("/dict")
public class DictController extends BaseController {

    // @com.alibaba.dubbo.config.annotation.Reference
    private @Autowired DictService dictService;

    /**
     * System initialization load dict information.
     */
    @RequestMapping(value = "/loadInit")
    public RespBase<?> loadInit() {
        RespBase<Object> resp = RespBase.create();
        Map<String, Object> result = dictService.loadInit();
        resp.setData(result);
        return resp;
    }

    @RequestMapping(value = "/list")
    @RequiresPermissions(value = { "iam:dict" })
    public RespBase<?> list(PageHolder<Dict> pm, String key, String label, String type, String description) {
        RespBase<Object> resp = RespBase.create();
        resp.setData(dictService.list(pm, key, label, type, description));
        return resp;
    }

    @RequestMapping(value = "/save")
    @RequiresPermissions(value = { "iam:dict" })
    public RespBase<?> save(Dict dict, Boolean isEdit) {
        RespBase<Object> resp = RespBase.create();
        dictService.save(dict, isEdit);
        return resp;
    }

    @RequestMapping(value = "/detail")
    @RequiresPermissions(value = { "iam:dict" })
    public RespBase<?> detail(String key) {
        RespBase<Object> resp = RespBase.create();
        Dict dict = dictService.detail(key);
        resp.setData(dict);
        return resp;
    }

    @RequestMapping(value = "/del")
    @RequiresPermissions(value = { "iam:dict" })
    public RespBase<?> del(String key) {
        RespBase<Object> resp = RespBase.create();
        dictService.del(key);
        return resp;
    }

    @RequestMapping(value = "/getByType")
    public RespBase<?> getByType(String type) {
        RespBase<Object> resp = RespBase.create();
        List<Dict> list = dictService.getByType(type);
        resp.forMap().put("list", list);
        return resp;
    }

    @RequestMapping(value = "/getByKey")
    public RespBase<?> getByKey(String key) {
        RespBase<Object> resp = RespBase.create();
        Dict dict = dictService.getByKey(key);
        resp.forMap().put("dict", dict);
        return resp;
    }

    @RequestMapping(value = "/allType")
    public RespBase<?> allType() {
        RespBase<Object> resp = RespBase.create();
        List<String> list = dictService.allType();
        resp.forMap().put("list", list);
        return resp;
    }

}