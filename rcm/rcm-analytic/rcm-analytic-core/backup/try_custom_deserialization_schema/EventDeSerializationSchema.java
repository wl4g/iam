package com.wl4g.iam.rcm.analytic.core;
///*
// * Copyright 2017 ~ 2025 the original authors James Wong.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.wl4g.iam.rcm.analytic.core;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//
//import org.apache.flink.api.common.serialization.DeserializationSchema;
//import org.apache.flink.api.common.serialization.SerializationSchema;
//import org.apache.flink.api.common.typeinfo.TypeInformation;
//
//import com.wl4g.iam.rcm.eventbus.common.IamEvent;
//import com.wl4g.iam.rcm.eventbus.common.IamEvent.EventType;
//
///**
// * A serializer and deserializer for the {@link Event} schema.
// * 
// * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
// * @version 2022-05-31 v3.0.0
// * @since v3.0.0
// */
//public class EventDeSerializationSchema implements DeserializationSchema<IamEvent>, SerializationSchema<IamEvent> {
//    private static final long serialVersionUID = -6570965211361070016L;
//
//    @Override
//    public byte[] serialize(IamEvent evt) {
//        ByteBuffer byteBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
//        byteBuffer.putInt(0, evt.sourceAddress());
//        byteBuffer.putInt(4, evt.type().ordinal());
//        return byteBuffer.array();
//    }
//
//    @Override
//    public IamEvent deserialize(byte[] message) throws IOException {
//        ByteBuffer buffer = ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN);
//        int address = buffer.getInt(0);
//        int typeOrdinal = buffer.getInt(4);
//        return new IamEvent(EventType.values()[typeOrdinal], address);
//    }
//
//    @Override
//    public boolean isEndOfStream(IamEvent nextElement) {
//        return false;
//    }
//
//    @Override
//    public TypeInformation<IamEvent> getProducedType() {
//        // TODO
//        return TypeInformation.of(IamEvent.class);
//    }
//
//}
