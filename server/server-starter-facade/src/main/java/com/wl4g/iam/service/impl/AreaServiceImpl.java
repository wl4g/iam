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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.iam.common.bean.Area;
import com.wl4g.iam.data.AreaDao;
import com.wl4g.iam.service.AreaService;

/**
 * {@link AreaServiceImpl}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2020-05-25
 * @sine v1.0
 * @see
 */
@org.springframework.stereotype.Service
// @com.alibaba.dubbo.config.annotation.Service(group = "areaService")
// @org.springframework.web.bind.annotation.RestController
public class AreaServiceImpl implements AreaService {

    @Autowired
    private AreaDao areaDao;

    @Override
    public List<Area> getAreaTree() {
        List<Area> areas = areaDao.findAreaAll();
        List<Area> tops = findTopAreas(areas);
        for (Area top : tops) {
            handleAreaChildren(areas, top);
        }
        return tops;
    }

    private void handleAreaChildren(List<Area> areas, Area top) {
        for (Area area : areas) {
            if (top.getId().equals(area.getParentId())) {
                getOrCreateChildren(top).add(area);
                handleAreaChildren(areas, area);
            }
        }
    }

    private List<Area> findTopAreas(List<Area> areas) {
        List<Area> list = new ArrayList<>();
        for (Area area : areas) {
            if (Objects.nonNull(area.getLevel()) && area.getLevel() == 0) {
                list.add(area);
            }
        }
        return list;
    }

    private List<Area> getOrCreateChildren(Area area) {
        if (Objects.isNull(area.getChildren())) {
            List<Area> children = new ArrayList<>();
            area.setChildren(children);
            return children;
        } else {
            return area.getChildren();
        }
    }

}
