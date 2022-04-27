# IAM Gateway

## 1. Generate self-certificate

```bash
cd src/main/resources/cert.d/
./cert-tool.sh ca
./cert-tool.sh pem-p12 ca.pem ca-key.pem

./cert-tool.sh cert wl4g.io 192.168.88.2,10.88.8.5
./cert-tool.sh pem-p12 wl4g.io.pem wl4g.io-key.pem

./cert-tool.sh cert client1 192.168.88.2,10.88.8.5
./cert-tool.sh pem-p12 client1.pem client1-key.pem
```

- Note: To create a certificate, you must specify a list of hosts, otherwise, for example, an error will be
reported when executing `curl`: `no alternative certificate subject name matches target host name '192.168.88.3'`,
Using the above script tool will contain `localhost,127.0.0.1` by default.

## 2. Testing

- [docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/ReadDebug.html](https://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/ReadDebug.html) , The `-Djavax.net.debug` options are `all|ssl|handshake|warning|...`

### 2.1 Gateway Simple TLS

- Startup IamGateway(pseudo command-line)

```bash
java -Djavax.net.debug=all -jar iam-gateway.jar --server.ssl.client-auth=NONE
```


- Clients for `curl` testing

```bash
curl -v -k 'https://localhost:18085/alimarket/v1/hello?response_type=json'
```

### 2.2 Gateway mTLS

- Startup IamGateway(pseudo command-line)

```bash
java -Djavax.net.debug=all -jar iam-gateway.jar --server.ssl.client-auth=NEED
```

- Clients for `curl` testing

```bash
curl -v \
--cacert ca.pem \
--cert client1.pem \
--key client1-key.pem \
'https://localhost:18085/alimarket/v1/hello?response_type=json' | jq
```

### 2.3 Trffic imager

```bash
# New terminal start the mock backend http server.
python3 -m http.server -b 0.0.0.0 8888

# New terminal start the mock traffic http server.
python3 -m http.server -b 0.0.0.0 29092

# 1. Send a mock request and observe that both terminals have output.
# 2. Then stop the simulated mirror http service and observe whether the real service still has output.
curl -v -k 'https://localhost:18085/alimarket/v1/hello?response_type=json'
```

## 3. Admin API

- [docs.spring.io/spring-cloud-gateway/docs/2.2.6.RELEASE/reference/html/#actuator-api](https://docs.spring.io/spring-cloud-gateway/docs/2.2.6.RELEASE/reference/html/#actuator-api)

- Routes(for example):

```bash
curl -v 'http://localhost:18086/actuator/gateway/routes' | jq
```

- Actuator Metrics(for example):

```bash
curl -v 'http://localhost:18086/actuator/metrics' | jq
```

- Prometheus Metrics(for example):

```bash
curl -v 'http://localhost:18086/actuator/prometheus'
```

## 4. Performace

```bash
ab -n 10000 -c 1000 -H 'X-Iscg-Log: y' -H 'X-Iscg-Log-Level: 0' -H 'X-Iscg-Canary: v1' 'http://localhost:18085/alimarket/v1/createInstance?action=createInstance&aliUid=1221&orderBizId=12345&orderId=123456789&productCode=121&skuId=yuncode215700000&trial=1&token=ada175ba95d1fc2585b9da0bcb5de663&response_type=json'
```
