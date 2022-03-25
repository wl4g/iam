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
package com.wl4g.iam.service.impl;

import com.wl4g.iam.common.bean.FastCasClientInfo;
import com.wl4g.iam.data.FastCasClientDao;
import com.wl4g.iam.service.FastCasClientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link FastCasClientServiceImpl}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2019-11-14 11:47:00
 * @sine v1.0.0
 * @see
 */
@org.springframework.stereotype.Service
// @com.alibaba.dubbo.config.annotation.Service(group = "clusterConfigService")
// @org.springframework.web.bind.annotation.RestController
public class FastCasClientServiceImpl implements FastCasClientService {

    @Autowired
    private FastCasClientDao fastCasClientDao;

    @Override
    public Map<String, Object> loadInit(String envType) {
        List<FastCasClientInfo> list = fastCasClientDao.selectByAppNames(null, envType, null);
        Assert.notEmpty(list, "not found cluster config info , Please Check your db , table = 'sys_cluster_config'");
        Map<String, Object> map = new HashMap<>();
        for (FastCasClientInfo entryAddress : list) {
            map.put(entryAddress.getAppName(), entryAddress);
        }
        return map;
    }

    @Override
    public FastCasClientInfo getFastCasClientInfo(Long clusterConfigId) {
        return fastCasClientDao.selectByPrimaryKey(clusterConfigId);
    }

    @Override
    public List<FastCasClientInfo> findByAppNames(String[] appNames, String envType, String type) {
        return fastCasClientDao.selectByAppNames(appNames, envType, type);
    }

    @Override
    public List<FastCasClientInfo> findOfIamServers() {
        return fastCasClientDao.getIamServer();
    }

}
