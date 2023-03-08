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
package com.wl4g.iam.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.wl4g.iam.common.bean.OidcClient;
import com.wl4g.iam.data.OidcClientDao;
import com.wl4g.iam.service.OidcClientService;
import com.wl4g.infra.common.bean.BaseBean;

/**
 * {@link OidcClientServiceImpl}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-03-26 v3.0.0
 * @since v3.0.0
 */
@org.springframework.stereotype.Service
// @com.alibaba.dubbo.config.annotation.Service(group = "oidcClientService")
// @org.springframework.web.bind.annotation.RestController
public class OidcClientServiceImpl implements OidcClientService {

    private @Autowired OidcClientDao oidClientDao;

    // @Override
    // public OidcClient loadOidcClient(OidcClient record) {
    // return oidClientDao.selectForClient(record);
    // }

    @Override
    public List<OidcClient> findList(OidcClient record) {
        return oidClientDao.selectBySelective(record);
    }

    @Override
    public void save(OidcClient record) {
        if (record.getId() != null) {
            update(record);
        } else {
            insert(record);
        }
    }

    @Override
    public void del(Long id) {
        Assert.notNull(id, "id is null");
        OidcClient record = OidcClient.builder().id(id).delFlag(BaseBean.DEL_FLAG_DELETED).build();
        oidClientDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public OidcClient detail(Long id) {
        return oidClientDao.selectByPrimaryKey(id);
    }

    private void insert(OidcClient record) {
        record.preInsert();
        oidClientDao.insertSelective(record);
    }

    private void update(OidcClient record) {
        record.preUpdate();
        oidClientDao.updateByPrimaryKeySelective(record);
    }

}