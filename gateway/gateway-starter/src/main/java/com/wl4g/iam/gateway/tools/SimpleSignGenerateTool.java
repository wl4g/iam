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
package com.wl4g.iam.gateway.tools;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * {@link SimpleSignGenerateTool}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-04
 * @since
 */
public class SimpleSignGenerateTool {

    /**
     * Generate IAM open API signature.
     * 
     * @param appId
     * @param appSecret
     * @param nonce
     * @param timestamp
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws Exception
     */
    public static String generateSign(String appId, String appSecret, String nonce, long timestamp)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // Join token parts
        StringBuffer signtext = new StringBuffer();
        signtext.append(appId);
        signtext.append(appSecret);
        signtext.append(timestamp);
        signtext.append(nonce);

        // ASCII sort
        byte[] signInput = signtext.toString().getBytes("UTF-8");
        Arrays.sort(signInput);

        // Signature.
        // Hex.encodeHexString(Hashing.sha256().hashBytes(signInput).asBytes());
        return hashing(new String(signInput, "UTF-8"));
    }

    /**
     * New generate random string.
     * 
     * @param len
     * @return
     */
    public static String generateNonce(int len) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int number = random.nextInt(str.length());
            char charAt = str.charAt(number);
            sb.append(charAt);
        }
        return sb.toString();
    }

    /**
     * Digesting hashing with sha256
     * 
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static String hashing(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(str.getBytes("UTF-8"));
        return byte2Hex(messageDigest.digest());
    }

    /**
     * Bytes to hex string
     * 
     * @param bytes
     * @return
     */
    public static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                // 1 to get a bit of the complement 0 operation
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (args.length == 1 && args[0].endsWith("help")) {
            System.out.println("Usage: java -jar iam-gateway-{version}.jar <myAppId> <myAppSecret>");
            System.exit(0);
        }

        String appId = "oi".concat(generateNonce(32)).toLowerCase();
        String appSecret = generateNonce(32).toLowerCase();
        if (args.length >= 2) {
            appId = args[0];
            appSecret = args[1];
        }
        String nonce = generateNonce(32);
        long timestamp = currentTimeMillis();
        String signature = generateSign(appId, appSecret, nonce, timestamp);

        out.println("--- ::: Generated Simple Signature Parameters ::: ---\n");
        out.println(format("appId:     %s", appId));
        out.println(format("appSecret: %s", appSecret));
        out.println(format("nonce:     %s", nonce));
        out.println(format("timestamp: %s", timestamp));
        out.println(format("signature: %s", signature));
        out.println();

        out.println(format("--- HTTP Request: ---\n"));
        out.println("export remoteIp=127.0.0.1");
        out.println(format("curl -vL \\\n"
                + "-H 'X-Iscg-Trace: y' \\\n-H 'X-Iscg-Log: y' \\\n-H 'X-Iscg-Log-Level: 10' \\\n-H 'X-Response-Type: 10' \\\n"
                + "\"http://$remoteIp:18085/openapi/v2/hello?appId=%s&nonce=%s&timestamp=%s&signature=%s\"", appId, nonce,
                timestamp, signature));
        out.println();
        out.println("----------------------");
        out.println();

        out.println(format("--- HTTPs Request: ---\n"));
        out.println("export remoteIp=127.0.0.1");
        out.println(format("curl -vsSkL \\\n--cacert a.pem \\\n--cert client1.pem \\\n--key client1-key.pem \\\n"
                + "-H 'X-Iscg-Trace: y' \\\n-H 'X-Iscg-Log: y' \\\n-H 'X-Iscg-Log-Level: 10' \\\n-H 'X-Response-Type: 10' \\\n"
                + "\"https://$remoteIp:18085/openapi/v2/hello?appId=%s&nonce=%s&timestamp=%s&signature=%s\"", appId, nonce,
                timestamp, signature));
    }

}
