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
package com.wl4g.iam.gateway.security.sign;

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
 * @author James Wong<jamewong1376@gmail.com>
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
    public static String generateSign(String appId, String appSecret, String nonce, long timestamp, String alg)
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
        return hashing(signInput, alg);
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
     * @param signInput
     * @param alg
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static String hashing(byte[] signInput, String alg) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(alg);
        messageDigest.update(signInput);
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
            System.out.println(
                    "Usage: java -jar iam-gateway-{version}.jar <myAppId> <myAppSecret> <hashingAlg, e.g: MD5|SHA-256|SHA-384|..>");
            System.exit(0);
        }

        String appId = "oi".concat(generateNonce(32)).toLowerCase();
        String appSecret = generateNonce(32).toLowerCase();
        String alg = "SHA-256";
        if (args.length >= 3) {
            appId = args[0];
            appSecret = args[1];
            alg = args[2];
        }
        String nonce = generateNonce(32);
        long timestamp = currentTimeMillis();
        String sign = generateSign(appId, appSecret, nonce, timestamp, alg);

        out.println("## Generated Simple Signature Mock Request (Shell Edition):\n");
        out.println(format("## Using: hashing algorithm is '%s'", alg));
        out.println(format("export APPID=%s", appId));
        out.println(format("export APPSECRET=%s", appSecret));
        out.println(format("export NONCE=%s", nonce));
        out.println(format("export TIMESTAMP=%s", timestamp));
        out.println(format("export SIGN=%s", sign));
        out.println(format("export REMOTE_IP=127.0.0.1"));
        out.println();

        out.println(format("## HTTP Request:\n"));
        out.println(format("curl -vL \\\n"
                + "-H 'X-Iscg-Trace: y' \\\n-H 'X-Iscg-Log: y' \\\n-H 'X-Iscg-Log-Level: 10' \\\n-H 'X-Response-Type: 10' \\\n"
                + "\"http://${REMOTE_IP}:18085/openapi/v2/hello?appId=${APPID}&nonce=${NONCE}&timestamp=${TIMESTAMP}&sign=${SIGN}\"",
                appId, nonce, timestamp, sign));
        out.println();
        out.println("## -------------");
        out.println();

        out.println(format("## HTTPs Request:\n"));
        out.println(format("curl -vsSkL \\\n--cacert a.pem \\\n--cert client1.pem \\\n--key client1-key.pem \\\n"
                + "-H 'X-Iscg-Trace: y' \\\n-H 'X-Iscg-Log: y' \\\n-H 'X-Iscg-Log-Level: 10' \\\n-H 'X-Response-Type: 10' \\\n"
                + "\"https://${REMOTE_IP}:18085/openapi/v2/hello?appId=${APPID}&nonce=${NONCE}&timestamp=${TIMESTAMP}&sign=${SIGN}\"",
                appId, nonce, timestamp, sign));
    }

}
