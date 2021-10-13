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
package com.wl4g.iam.client.console;

import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.iam.client.session.mgt.IamClientSessionManager;
import com.wl4g.shell.common.annotation.ShellMethod;
import com.wl4g.shell.core.handler.SimpleShellContext;
import com.wl4g.shell.springboot.annotation.ShellComponent;

/**
 * {@link ClientSecurityConsole}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-11-05
 * @since
 */
@ShellComponent
public class ClientSecurityConsole {

    @Autowired
    private IamClientSessionManager clientSessionManager;

    @ShellMethod(keys = { "validateSessions" }, help = "Manual start validating sessions.", group = DEFAULT_CONSOLE_GROUP)
    public void validateSessions(SimpleShellContext ctx) {
        ctx.printf("Manual validating sessions ...");

        clientSessionManager.validateSessions();

        ctx.printf("Validated sessions completed!");
        ctx.completed();
    }

    public static final String DEFAULT_CONSOLE_GROUP = "IAM console for client";

}
