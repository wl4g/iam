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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.wl4g.iam.common.bean.oidc.OidcMapper;
import com.wl4g.iam.data.OidcMapperDao;
import com.wl4g.iam.service.OidcMapperService;
import com.wl4g.infra.core.bean.BaseBean;

/**
 * {@link OidcMapperServiceImpl}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-26 v3.0.0
 * @since v3.0.0
 */
@org.springframework.stereotype.Service
// @com.alibaba.dubbo.config.annotation.Service(group = "oidcClientService")
// @org.springframework.web.bind.annotation.RestController
public class OidcMapperServiceImpl implements OidcMapperService {

    private @Autowired OidcMapperDao oidcMapperDao;

    @Override
    public List<OidcMapper> findList(OidcMapper record) {
        return oidcMapperDao.selectBySelective(record);
    }

    @Override
    public List<OidcMapper> findByClientId(String clientId) {
        return oidcMapperDao.selectByClientId(clientId);
    }

    @Override
    public void save(OidcMapper record) {
        if (record.getId() != null) {
            update(record);
        } else {
            insert(record);
        }
    }

    @Override
    public void del(Long id) {
        Assert.notNull(id, "id is null");
        OidcMapper record = new OidcMapper();
        record.setId(id);
        record.setDelFlag(BaseBean.DEL_FLAG_DELETE);
        oidcMapperDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public OidcMapper detail(Long id) {
        return oidcMapperDao.selectByPrimaryKey(id);
    }

    private void insert(OidcMapper record) {
        record.preInsert();
        oidcMapperDao.insertSelective(record);
    }

    private void update(OidcMapper record) {
        record.preUpdate();
        oidcMapperDao.updateByPrimaryKeySelective(record);
    }

}