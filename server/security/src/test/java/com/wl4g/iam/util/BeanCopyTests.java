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
package com.wl4g.iam.util;

import static java.util.Objects.nonNull;

import org.junit.Test;
import org.springframework.beans.BeanUtils;

import com.wl4g.iam.common.bean.User;
import com.wl4g.infra.common.bean.BeanUtils2;

/**
 * {@link BeanCopyTests}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-27 v3.0.0
 * @since v3.0.0
 */
public class BeanCopyTests {

    @Test
    public void testFailureSprintBeanUtilsCopyPropertiesIsSetFromNullableProperty() {
        User src = User.builder().name("admin").nickname("Mr.L").build();
        User dst = User.builder().givenName("W.L").build();
        BeanUtils.copyProperties(src, dst);
        System.out.println(src);
        System.out.println(dst);
        // assert nonNull(dst.getGivenName());
    }

    @Test
    public void testBeanUtils2CopyPropertiesIsSetFromNullableProperty() throws Exception {
        User src = User.builder().name("admin").nickname("Mr.L").build();
        User dst = User.builder().givenName("W.L").build();
        BeanUtils2.deepCopyFieldState(dst, src);
        System.out.println(src);
        System.out.println(dst);
        assert nonNull(dst.getGivenName());
    }

}
