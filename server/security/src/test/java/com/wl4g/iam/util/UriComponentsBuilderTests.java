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
package com.wl4g.iam.util;

import static java.lang.System.out;

import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * {@link UriComponentsBuilderTests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-03-28 v3.0.0
 * @since v3.0.0
 */
public class UriComponentsBuilderTests {

    @Test
    public void testBuildWithMapParams() {
        // String redirect_uri = "https://www.google.com";
        MultiValueMap<String, String> redirectParams = new LinkedMultiValueMap<>(8);
        redirectParams.add("state", "sdagfhsdgsfaf");
        redirectParams.add("access_token", "23tfgjdgfdgffffffff");

        out.println(UriComponentsBuilder.newInstance().queryParams(redirectParams).build().toUriString().substring(1));
    }

}
