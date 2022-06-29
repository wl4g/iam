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
package com.wl4g.iam.sns.wechat.api.model.menu;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

/**
 * {@link WxmpMenu} bean. The menu object contains multiple menu items (up to
 * 3). These menu items can be sub menu items (Level 1 menu without secondary
 * menu) or parent menu items (menu items containing secondary menu)
 *
 * @author James Wong <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
public class WxmpMenu {

    private WxmpButton[] button;

    public WxmpButton[] getButton() {
        return button;
    }

    public void setButton(WxmpButton[] button) {
        this.button = button;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
    }

}