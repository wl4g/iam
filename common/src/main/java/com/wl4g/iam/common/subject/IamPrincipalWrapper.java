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
package com.wl4g.iam.common.subject;

/**
 * {@link IamPrincipalWrapper}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020年7月7日 v1.0.0
 * @see
 */
public class IamPrincipalWrapper {

    /**
     * {@link IamPrincipal}
     */
    private IamPrincipal info;

    public IamPrincipalWrapper() {
        super();
    }

    public IamPrincipalWrapper(IamPrincipal info) {
        super();
        this.info = info;
    }

    public IamPrincipal getInfo() {
        return info;
    }

    public void setInfo(IamPrincipal info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "IamPrincipalInfoWrapper [info=" + info + "]";
    }

}