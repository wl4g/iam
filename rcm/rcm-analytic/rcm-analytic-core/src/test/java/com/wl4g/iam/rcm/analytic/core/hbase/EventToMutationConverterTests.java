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
package com.wl4g.iam.rcm.analytic.core.hbase;

import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;

import org.apache.hadoop.hbase.client.Mutation;
import org.junit.Test;

import com.wl4g.iam.rcm.analytic.core.model.IamEventAnalyticalModel;
import com.wl4g.iam.rcm.analytic.core.model.IamEventAnalyticalModel.IpGeoInformation;
import com.wl4g.iam.rcm.eventbus.common.IamEventBase.EventType;

/**
 * {@link EventToMutationConverterTests}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-08 v3.0.0
 * @since v3.0.0
 */
public class EventToMutationConverterTests {

    @Test
    public void testEventToMutationConverter() {
        IamEventAnalyticalModel model = IamEventAnalyticalModel.builder()
                .timestamp(currentTimeMillis())
                .eventType(EventType.AUTHC_SUCCESS)
                .principal("jack_6")
                .ipGeoInfo(IpGeoInformation.builder()
                        .ip("2.2.28.22")
                        .latitude("111.234235")
                        .longitude("20.124521")
                        .countryShort("CN")
                        .build())
                .message("my message 6")
                .build();

        EventToMutationConverter converter = new EventToMutationConverter();
        converter.open();
        Mutation mutation = converter.convertToMutation(model);

        System.out.println(mutation);
    }

}
