# IAM Gateway

## 1. Generate self-certificate

```bash
cd src/main/resources/cert.d/
./cert-tool.sh ca
./cert-tool.sh pem-p12 ca.pem ca-key.pem

./cert-tool.sh cert wl4g.io 127.0.0.1,10.0.0.106
./cert-tool.sh pem-p12 wl4g.io.pem wl4g.io-key.pem

./cert-tool.sh cert client1 127.0.0.1,10.0.0.106
./cert-tool.sh pem-p12 client1.pem client1-key.pem
```

## 2. For Testing

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

