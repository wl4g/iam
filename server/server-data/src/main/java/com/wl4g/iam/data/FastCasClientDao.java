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
package com.wl4g.iam.data;

import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.iam.common.bean.FastCasClientInfo;

import java.util.List;

/**
 * {@link FastCasClientInfoDao}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-0520
 * @sine v1.0
 * @see
 */
@FeignConsumer("${provider.serviceId.iam-data:iam-data}")
@RequestMapping("/fastCasConfig-dao")
public interface FastCasClientDao {

    @RequestMapping(value = "/deleteByPrimaryKey", method = { POST })
    int deleteByPrimaryKey(@RequestParam("id") Long id);

    @RequestMapping(value = "/insert", method = { POST })
    int insert(@RequestBody FastCasClientInfo record);

    @RequestMapping(value = "/insertSelective", method = { POST })
    int insertSelective(@RequestBody FastCasClientInfo record);

    @RequestMapping(value = "/selectByPrimaryKey", method = GET)
    FastCasClientInfo selectByPrimaryKey(@RequestParam("id") Long id);

    @RequestMapping(value = "/updateByPrimaryKeySelective", method = { POST })
    int updateByPrimaryKeySelective(@RequestBody FastCasClientInfo record);

    @RequestMapping(value = "/updateByPrimaryKey", method = { POST })
    int updateByPrimaryKey(@RequestBody FastCasClientInfo record);

    @RequestMapping(value = "/selectByAppNames", method = GET)
    List<FastCasClientInfo> selectByAppNames(
            @RequestParam(name = "appNames", required = false) @Param("appNames") String[] appNames,
            @RequestParam(name = "envType", required = false) @Param("envType") String envType,
            @RequestParam(name = "type", required = false) @Param("type") String type);

    @RequestMapping(value = "/getByAppName", method = GET)
    FastCasClientInfo getByAppName(
            @RequestParam(name = "appName", required = false) @Param("appName") String appName,
            @RequestParam(name = "envType", required = false) @Param("envType") String envType,
            @RequestParam(name = "type", required = false) @Param("type") String type);

    @RequestMapping(value = "/getIamServer", method = GET)
    List<FastCasClientInfo> getIamServer();

}