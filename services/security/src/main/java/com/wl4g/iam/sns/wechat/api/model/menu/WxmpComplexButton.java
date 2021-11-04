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

import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;

/**
 * The complex button (parent button) contains the first level menu of secondary
 * menu items. This type of menu item has two properties: name and sub_ Button,
 * and sub_ Button is an array of submenu items
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
public class WxmpComplexButton extends WxmpButton {

    private WxmpButton[] sub_button;

    public WxmpButton[] getSub_button() {
        return sub_button;
    }

    public void setSub_button(WxmpButton[] sub_button) {
        this.sub_button = sub_button;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
    }

}