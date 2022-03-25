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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.RandomUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wl4g.infra.common.codec.CodecSource;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.web.BaseController;
import com.wl4g.infra.core.page.PageHolder;
import com.wl4g.iam.authc.credential.secure.CredentialsSecurer;
import com.wl4g.iam.authc.credential.secure.CredentialsToken;
import com.wl4g.iam.common.bean.User;
import com.wl4g.iam.core.session.mgt.IamSessionDAO;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;
import com.wl4g.iam.service.UserService;

/**
 * @author vjay
 * @date 2019-10-29 10:10:00
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    // @com.alibaba.dubbo.config.annotation.Reference
    @Autowired
    private UserService userService;

    @Autowired
    private IamSessionDAO sessionDAO;

    @Autowired
    private CredentialsSecurer securer;

    @RequestMapping(value = "/list")
    @RequiresPermissions(value = { "iam:user" })
    public RespBase<?> list(PageHolder<User> pm, String userName, String displayName, Long roleId) {
        RespBase<Object> resp = RespBase.create();
        resp.setData(userService.list(pm, userName, displayName, roleId));
        return resp;
    }

    @RequestMapping(value = "/detail")
    @RequiresPermissions(value = { "iam:user" })
    public RespBase<?> detail(Long userId) {
        Assert.notNull(userId, "userId is null");
        RespBase<Object> resp = RespBase.create();
        User detail = userService.detail(userId);
        resp.forMap().put("data", detail);
        return resp;
    }

    @RequestMapping(value = "/del")
    @RequiresPermissions(value = { "iam:user" })
    public RespBase<?> del(Long userId) {
        Assert.notNull(userId, "userId is null");
        RespBase<Object> resp = RespBase.create();
        userService.del(userId);
        return resp;
    }

    @RequestMapping(value = "/save")
    @RequiresPermissions(value = { "iam:user" })
    public RespBase<?> save(@RequestBody User user) {
        Assert.notNull(user, "user is null");
        RespBase<Object> resp = RespBase.create();

        if (isNotBlank(user.getPassword())) { // update-passwd
            // TODO Dynamic choose algorithm!!! Default use RSA
            CredentialsToken crToken = new CredentialsToken(user.getSubject(), user.getPassword(), CryptKind.RSA);
            CodecSource publicSalt = new CodecSource(RandomUtils.nextBytes(16));
            String sign = securer.signature(crToken, publicSalt);
            user.setPassword(sign); // ciphertext
            user.setPubSalt(publicSalt.toHex());
        }

        // Save IAM session.
        userService.save(user);

        // Update the password, need to logout the user
        sessionDAO.removeAccessSession(user.getSubject());

        return resp;
    }

}