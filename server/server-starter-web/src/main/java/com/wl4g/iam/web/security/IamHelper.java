/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.iam.web.security;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

/**
 * {@link IamHelper}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-04
 * @sine v1.0
 * @see
 */
@Component
public class IamHelper {

    @Autowired
    private ConfigurableEnvironment environment;

    public final String getApplicationActiveEnvironmentType() {
        String active = environment.getRequiredProperty("spring.profiles.active");

        Set<String> envFlags = new HashSet<>();
        Matcher matcher = Pattern.compile(ENV_DEFINITIONS).matcher(active);
        while (matcher.find()) {
            envFlags.add(matcher.group());
        }
        if (envFlags.size() != 1) {
            throw new IllegalStateException(
                    format("Unable initialization secure configuration. Ambiguous environments flag. - %s", active));
        }
        return envFlags.iterator().next();
    }

    /**
     * <pre>
     * LOCAL - Local Development environment
     *   DEV - Development environment
     *   FWS - Feature Web Service Test environment
     *   FAT - Feature Acceptance Test environment
     *   LPT - Load and Performance Test environment
     *   UAT - User Acceptance Test environment
     *   PRO - Production environment
     * </pre>
     */
    public static final String ENV_DEFINITIONS = "local|devel|develop|dev|test|fat|fws|uat|lpt|pro|prod|online";

}
