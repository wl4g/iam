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

package com.wl4g.iam.rcm.analytic.core;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;

import com.google.common.io.Resources;

/**
 * {@link LoginEventCepStreamingDemo}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-15 v3.0.0
 * @since v3.0.0
 */
@SuppressWarnings("deprecation")
public class LoginEventCepStreamingDemo {

    // 需求：检测一个用户在3秒内连续登陆失败。
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);

        // 1. 读取事件数据，创建简单事件流
        URL resource = Resources.getResource("loginevent/login_log.csv");
        DataStreamSource<String> loginEventStreamSource = env.readTextFile(resource.getPath());

        KeyedStream<LoginEvent, Long> loginEventStream = loginEventStreamSource.map(value -> {
            String[] columns = value.split(",");
            return new LoginEvent(Long.parseLong(trimToEmpty(columns[0])), trimToEmpty(columns[1]), trimToEmpty(columns[2]),
                    Long.parseLong(trimToEmpty(columns[3])));
        }).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<LoginEvent>(Time.seconds(5)) {
            private static final long serialVersionUID = 1L;

            @Override
            public long extractTimestamp(LoginEvent element) {
                return element.eventTime;
            }
        }).keyBy(new KeySelector<LoginEvent, Long>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Long getKey(LoginEvent value) throws Exception {
                return value.userId;
            }
        });

        // 2. 定义匹配模式
        Pattern<LoginEvent, LoginEvent> loginFailPattern = Pattern.begin("begin");
        loginFailPattern.where(new IterativeCondition<LoginEvent>() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean filter(LoginEvent value, Context<LoginEvent> ctx) throws Exception {
                return value.eventType.equalsIgnoreCase("fail");
            }
        }).next("next").where(new IterativeCondition<LoginEvent>() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean filter(LoginEvent value, Context<LoginEvent> ctx) throws Exception {
                return value.eventType.equalsIgnoreCase("fail");
            }
        }).within(Time.milliseconds(10000));

        // 3. 在事件流上应用模式，得到一个pattern stream
        PatternStream<LoginEvent> patternStream = CEP.pattern(loginEventStream, loginFailPattern);

        // 4. 从pattern stream上应用select function，检出匹配事件序列
        SingleOutputStreamOperator<Warning> loginFailDataStream = patternStream.select(new LoginFailMatch());

        loginFailDataStream.print();

        env.execute("login fail with cep job");
    }

    static class LoginFailMatch implements PatternSelectFunction<LoginEvent, Warning> {
        private static final long serialVersionUID = 1L;

        @Override
        public Warning select(Map<String, List<LoginEvent>> pattern) throws Exception {
            // 从map中按照名称取出对应的事件
            LoginEvent firstFail = pattern.get("begin").iterator().next();
            // Could't get of next???
            // LoginEvent lastFail = pattern.get("next").iterator().next();
            return new Warning(firstFail.userId, firstFail.eventTime,
                    /* lastFail.eventTime */-0L, "login fail!");
        }
    }

    // 输入的登录事件样例类
    static class LoginEvent {
        Long userId;
        String ip;
        String eventType;
        Long eventTime;

        public LoginEvent(Long userId, String ip, String eventType, Long eventTime) {
            super();
            this.userId = userId;
            this.ip = ip;
            this.eventType = eventType;
            this.eventTime = eventTime;
        }

        @Override
        public String toString() {
            return "LoginEvent [userId=" + userId + ", ip=" + ip + ", eventType=" + eventType + ", eventTime=" + eventTime + "]";
        }

    }

    // 输出的异常报警信息样例类
    static class Warning {
        Long userId;
        Long firstFailTime;
        Long lastFailTime;
        String warningMsg;

        public Warning(Long userId, Long firstFailTime, Long lastFailTime, String warningMsg) {
            this.userId = userId;
            this.firstFailTime = firstFailTime;
            this.lastFailTime = lastFailTime;
            this.warningMsg = warningMsg;
        }

        @Override
        public String toString() {
            return "Warning [userId=" + userId + ", firstFailTime=" + firstFailTime + ", lastFailTime=" + lastFailTime
                    + ", warningMsg=" + warningMsg + "]";
        }

    }

}
