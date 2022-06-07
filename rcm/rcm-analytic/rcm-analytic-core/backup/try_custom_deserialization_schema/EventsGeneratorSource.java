package com.wl4g.iam.rcm.analytic.core;
///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.wl4g.iam.rcm.analytic.core;
//
//import static org.apache.flink.util.Preconditions.checkArgument;
//
//import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
//
//import com.wl4g.iam.rcm.eventbus.common.IamEvent;
//
///**
// * A event stream source that generates the events on the fly. Useful for
// * self-contained demos.
// */
//@SuppressWarnings("serial")
//public class EventsGeneratorSource extends RichParallelSourceFunction<IamEvent> {
//
//    private volatile boolean running = true;
//
//    @Override
//    public void run(SourceContext<IamEvent> sourceContext) throws Exception {
//
//        final int range = Integer.MAX_VALUE / getRuntimeContext().getNumberOfParallelSubtasks();
//        final int min = range * getRuntimeContext().getIndexOfThisSubtask();
//        final int max = min + range;
//
//        while (running) {
//            sourceContext.collect(generator.next(min, max));
//        }
//    }
//
//    @Override
//    public void cancel() {
//        this.running = false;
//    }
//
//}
