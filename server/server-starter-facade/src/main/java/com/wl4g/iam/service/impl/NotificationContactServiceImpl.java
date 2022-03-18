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

import com.wl4g.iam.common.bean.NotificationContact;
import com.wl4g.iam.data.NotificationContactDao;
import com.wl4g.iam.service.NotificationContactService;

/**
 * Notification to contacts service implements.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @version v1.0 2019-08-05
 * @sine v1.0
 * @see
 */
@org.springframework.stereotype.Service
// @com.alibaba.dubbo.config.annotation.Service(group="notificationContactService")
// @org.springframework.web.bind.annotation.RestController
public class NotificationContactServiceImpl implements NotificationContactService {

    @Autowired
    private NotificationContactDao notificationContactDao;

    @Override
    public int save(NotificationContact record) {
        return notificationContactDao.insertSelective(record);
    }

    @Override
    public List<NotificationContact> getByRecordId(Long id) {
        return notificationContactDao.getByRecordId(id);
    }

}