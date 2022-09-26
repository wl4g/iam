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

import com.wl4g.iam.common.bean.RealmBean;
import com.wl4g.iam.data.RealmBeanDao;
import com.wl4g.iam.service.RealmService;
import com.wl4g.infra.common.bean.BaseBean;

/**
 * {@link RealmServiceImpl}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-03-26 v3.0.0
 * @since v3.0.0
 */
@org.springframework.stereotype.Service
// @com.alibaba.dubbo.config.annotation.Service(group = "realmService")
// @org.springframework.web.bind.annotation.RestController
public class RealmServiceImpl implements RealmService {

    private @Autowired RealmBeanDao realmDao;

    @Override
    public List<RealmBean> findList(RealmBean record) {
        return realmDao.selectBySelective(record);
    }

    @Override
    public void save(RealmBean record) {
        if (record.getId() != null) {
            update(record);
        } else {
            insert(record);
        }
    }

    @Override
    public void del(Long id) {
        Assert.notNull(id, "id is null");
        RealmBean record = RealmBean.builder().id(id).delFlag(BaseBean.DEL_FLAG_DELETED).build();
        realmDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public RealmBean detail(Long id) {
        return realmDao.selectByPrimaryKey(id);
    }

    private void insert(RealmBean record) {
        record.preInsert();
        realmDao.insertSelective(record);
    }

    private void update(RealmBean record) {
        record.preUpdate();
        realmDao.updateByPrimaryKeySelective(record);
    }

}