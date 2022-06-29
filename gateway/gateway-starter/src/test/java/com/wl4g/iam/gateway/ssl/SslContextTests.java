///*
// * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
//package com.wl4g.iam.gateway.ssl;
//
//import static java.lang.System.out;
//
//import org.junit.Test;
//
///**
// * {@link SslContextTests}
// * 
// * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
// * @version 2022-04-09 v3.0.0
// * @since v3.0.0
// */
//public class SslContextTests {
//
//    @SuppressWarnings("restriction")
//    @Test
//    public void testSunSslProtocolVersions() {
//        out.println("called ...");
//        for (sun.security.ssl.ProtocolVersion version : sun.security.ssl.ProtocolVersion.values()) {
//            out.println(version);
//        }
//        // see:https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/sun/security/ssl/ProtocolVersion.java
//        // see:https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/security/ssl/ProtocolVersion.java
//        // out.println(sun.security.ssl.ProtocolVersion.namesOf(new String[] {
//        // "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3" }));
//    }
//
//    @Test
//    public void testBouncycastleSslProtocolVersions() {
//        out.println("called ...");
//        out.println(org.bouncycastle.crypto.tls.ProtocolVersion.SSLv3);
//        out.println(org.bouncycastle.crypto.tls.ProtocolVersion.TLSv10);
//        out.println(org.bouncycastle.crypto.tls.ProtocolVersion.TLSv11);
//        out.println(org.bouncycastle.crypto.tls.ProtocolVersion.TLSv12);
//    }
//
//}
