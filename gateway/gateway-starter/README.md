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

## 2. For Testing

- [docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/ReadDebug.html](https://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/ReadDebug.html)

### 2.1 Simple TLS

- Startup IamGateway(pseudo command-line)

```bash
java -Djavax.net.debug=all -jar iam-gateway.jar --server.ssl.client-auth=NONE
```

- Clients for `curl` testing

```bash
curl -v -k 'https://localhost:18085/alimarket/v1/hello?response_type=json'
```

### 2.2 Mutual TLS

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
'https://localhost:18085/alimarket/v1/hello?response_type=json'
```

