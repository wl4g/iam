# ISCG (IAM Spring Cloud Gateway)

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

### 2.1 Ingress TLS

- Startup arguments example:

```bash
java -Djavax.net.debug=all -jar iam-gateway-3.0.0-bin.jar --server.ssl.enabled=true --server.ssl.client-auth=NONE
```

- Clients for `curl` testing

```bash
curl -vsSkL -XGET \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
--cacert ca.pem \
--cert client1.pem \
--key client1-key.pem \
'https://localhost:18085/_fallback' | jq
```

### 2.2 Ingress mTLS

- Startup IamGateway(pseudo command-line)

```bash
java -Djavax.net.debug=all -jar iam-gateway-3.0.0-bin.jar --server.ssl.enabled=true --server.ssl.client-auth=NEED
```

- Clients for `curl` testing

```bash
curl -vsSkL -XGET \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
--cacert ca.pem \
--cert client1.pem \
--key client1-key.pem \
'https://localhost:18085/_fallback' | jq
```

### 2.3 IP Filter

- The following example requires startup corresponding configuration file: `src/test/resources/bootstrap.yml`

```yaml
spring:
  profiles:
    include: "service-discovery,route-filter-splitting"
```

```bash
export localIp=$(ifconfig|grep -A 4 -E '^em*|^eno*|^enp*|^ens*|^eth*|^wlp*'|grep 'inet'|awk '{print $2}'|head -1 2>/dev/null)

# for testing positive example
curl -vsSkL -XGET \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
"http://$localIp:18085/productpage-with-IpFilter/get"

# for testing negative example
curl -vsSkL -XPOST \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
-H 'Content-Type: application/json' \
-H 'X-Forwarded-For: 1.1.1.2' \
-d '{"name":"jack"}' \
"http://$localIp:18085/productpage-with-IpFilter/post"
```

### 2.4 Request Size

TODO

### 2.5 Fault Injection

- The following example requires startup corresponding configuration file: `src/test/resources/bootstrap.yml`

```yaml
spring:
  profiles:
    include: "service-discovery,route-filter-splitting"
```

```bash
# for testing positive example1
curl -vsSkL -XGET \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
-H 'X-Iscg-Fault: y' \
'http://localhost:18085/productpage-with-FaultInjector/get'

# for testing positive example2
curl -vsSkL -XPOST \
-H 'Content-Type: application/json' \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
-H 'X-Iscg-Fault: y' \
-d '{"name":"jack"}' \
'http://localhost:18085/productpage-with-FaultInjector/post'
```

### 2.6 Simple Sign Authing

TODO

### 2.7 Request Limiter

- Startup arguments example:

```bash
java -jar iam-gateway-3.0.0-bin.jar \
--spring.iam.gateway.requestlimit.defaultLimiter.rate.defaultBurstCapacity=1000 \
--spring.iam.gateway.requestlimit.defaultLimiter.rate.defaultReplenishRate=10 \
--spring.iam.gateway.requestlimit.defaultLimiter.rate.defaultRequestedTokens=1 \
--spring.iam.gateway.requestlimit.defaultLimiter.quota.requestCapacity=1000 \
--spring.iam.gateway.requestlimit.defaultLimiter.quota.cycleDatePattern=yyyyMMdd
```

```bash
# for testing positive example(non limited)
ab -n 500 -c 5 \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
-m POST \
'http://localhost:18085/productpage-with-IamRequestLimiter/get?response_type=json'

# for testing negative example(limited)
ab -n 2000 -c 15 \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
-m POST \
'http://localhost:18085/productpage-with-IamRequestLimiter/get?response_type=json'
```

### 2.8 Traffic Replication

- The following example requires startup corresponding configuration file: `src/test/resources/bootstrap.yml`

```yaml
spring:
  profiles:
    include: "service-discovery,route-filter-splitting"
```

```bash
# Mock actual upstream http server.
python3 -m http.server -b 0.0.0.0 8888

# Mock traffic mirror upstream http server. see: iam-gateway-route.yaml#secure-httpbin-service-route
# Online by default: http://httpbin.org/
# [Optional] You can also use docker to local build an httpbin server.
#docker run -d --name=httpbin -p 8889:80 kennethreitz/httpbin

# 1. Send a mock request and observe that both terminals have output.
# 2. Then observe the output of the simulated mirror http server, and the response of the simulated real http server.

# for testing positive example1
curl -vsSkL -XGET -H 'X-Iscg-Trace: y' -H 'X-Iscg-Log-Level: 10' 'http://localhost:18085/productpage-with-TrafficReplicator/get'

# for testing positive example2
curl -vsSkL -XPOST \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
-H 'Content-Type: application/json' \
-d '{"name":"jack"}' \
'http://localhost:18085/productpage-with-TrafficReplicator/post'
```

### 2.9 Response Cache

```bash

# for testing positive example
curl -vsSkL -XGET \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
-H 'X-Iscg-Cache: y' \
"http://localhost:18085/productpage-with-ResponseCache/get"

# for testing negative example
curl -vsSkL -XGET \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 10' \
-H 'X-Iscg-Cache: n' \
"http://localhost:18085/productpage-with-ResponseCache/get"
```

### 2.10 Retry

TODO

### 2.11 Circuit Breaker

TODO

### 2.12 Canary LoadBalancer

TODO

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
ab -n 10000 -c 1000 \
-H 'X-Iscg-Trace: y' \
-H 'X-Iscg-Log: y' \
-H 'X-Iscg-Log-Level: 0' \
-H 'X-Iscg-Fault: y' \
-H 'X-Iscg-Canary: v1' \
-m POST \
'http://localhost:18085/productpage/post?action=createInstance&aliUid=1221&orderBizId=12345&orderId=123456789&productCode=121&skuId=yuncode215700000&trial=1&token=ada175ba95d1fc2585b9da0bcb5de663&response_type=json'
```
