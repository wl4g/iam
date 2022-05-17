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
package com.wl4g.iam.gateway.logging.model;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link LogRecord}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-17 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LogRecord {
    private String traceId;
    private int verboseLevel;
    private long beginTime;
    private long endTime;

    private String requestScheme;
    private String requestMethod;
    private String requestPath;
    private Map<String, String> requestQuery = new HashMap<>();
    private Map<String, String> requestHeaders = new HashMap<>();
    // Note: will not show the full, only first few bytes are streamed
    // transformed to characters.
    private String requestBody;

    private String responseStatus;
    private Map<String, String> responseHeaders = new HashMap<>();
    // Note: will not show the full, only first few bytes are streamed
    // transformed to characters.
    private String responseBody;

    public LogRecord withTraceId(String traceId) {
        setTraceId(traceId);
        return this;
    }

    public LogRecord withVerboseLevel(int verboseLevel) {
        setVerboseLevel(verboseLevel);
        return this;
    }

    public LogRecord withBeginTime(long beginTime) {
        setBeginTime(beginTime);
        return this;
    }

    public LogRecord withEndTime(long endTime) {
        setEndTime(endTime);
        return this;
    }

    public LogRecord withRequestScheme(String requestScheme) {
        setRequestScheme(requestScheme);
        return this;
    }

    public LogRecord withRequestMethod(String requestMethod) {
        setRequestMethod(requestMethod);
        return this;
    }

    public LogRecord withRequestPath(String requestPath) {
        setRequestPath(requestPath);
        return this;
    }

    public LogRecord withRequestQuery(Map<String, String> requestQuery) {
        setRequestQuery(requestQuery);
        return this;
    }

    public LogRecord withRequestHeaders(Map<String, String> requestHeaders) {
        setRequestHeaders(requestHeaders);
        return this;
    }

    public LogRecord withRequestBody(String requestBody) {
        setRequestBody(requestBody);
        return this;
    }

    public LogRecord withResponseStatus(String responseStatus) {
        setResponseStatus(responseStatus);
        return this;
    }

    public LogRecord withResponseHeaders(Map<String, String> responseHeaders) {
        setResponseHeaders(responseHeaders);
        return this;
    }

    public LogRecord withResponseBody(String responseBody) {
        setResponseBody(responseBody);
        return this;
    }

}
