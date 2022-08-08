# IAM (Identity Authentication and Access Identification Management)

An enterprise-level open source unified identity authentication and access control management platform, out-of-the-box, supports WeChat/qq/google/facebook and other SNS and openldap joint authentication, AOP implements API-level multi-factor authentication; among them, the enterprise-level gateway module is enhanced: Supports such as canary request-based response cache filter, canary load balancer, universal signature authentication filter, oidc v1/oauth2.x authentication filter, ip filter, traffic replication filter, quota-based request limiter filter Injector, canary-based fault injector filter, and canary-based humanized log filter; among them, the message bus and real-time analysis modules based on Flink/Kafka/Pulsar/Rabbitmq/HBase/ES/Hive support functions such as abnormal events or Real-time risk warning and early warning, as well as historical event analysis reports, etc.

<font color=red>Reminder: The latest version and documents are currently being sorted out and improved. It is recommended to deploy in the test environment first. If you have any questions or suggestions, please submit an Issue</font>

<p align="center">
    <img src="https://github.com/wl4g/iam/raw/master/shots/iam-logo.png" width="150">
    <h3 align="center">IAM</h3>
    <p align="center">
        A enterprise-level universal unified authentication and authorization 5A management platform based on cloud native and Spring Cloud.
        <br>
        <a href="https://github.com/wl4g/iam/"><strong>-- Home Page --</strong></a>
        <br>
        <br>
        <a href="https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-sso/">
            <img src="https://img.shields.io/badge/Maven-3.5+-green.svg" >
        </a>
         <a href="https://github.com/wl4g/iam/releases">
             <img src="https://img.shields.io/badge/release-v2.0.0-green.svg" >
         </a>
        <a href="http://www.apache.org/licenses/LICENSE-2.0">
            <img src="https://img.shields.io/badge/license-Apache2.0+-green.svg" >
        </a>
    </p>    
</p>

中文文档 [here](README_CN.md)

## 1. Features

- 1. Concise: The API is intuitive and concise, allowing you to get started quickly
- 2. Lightweight: less dependent on the environment, lower deployment and access costs
- 3. Single sign-on: You only need to log in once to access all mutually trusted application systems
- 4. Distributed: applications that access the IAM/SSO authentication center, support distributed deployment
- 5. HA: Both Server and Client support cluster deployment to improve system availability
- 6. Cross-domain: support cross-domain application access to IAM/SSO authentication center
- 7. Both Cookie and Token are supported: both Cookie-based and Token-based access methods are supported, and both provide Sample projects
- 8. Both Web+APP support: support Web and APP access
- 9. Real-time: system login and logout status, all controlled by IAM Server and quasi-synchronized with the client
- 10. CS structure: based on CS structure, including Server "certification center" and Client "protected application"
- 11. Path exclusion: Support multiple custom exclusion paths, support Ant expressions. Used to exclude paths that IAM/SSO clients do not need to filter
- 12. Support multiple modes of deployment and operation (local mode): Traditional single application, no authentication client, that is, IAM server and BizApp are in the same JVM process, the advantage is that deployment, operation and maintenance are simple, suitable for small management projects .
- 13. Support multiple modes of deployment and operation (cluster mode): separate the authentication center from the business application and authentication client, that is, the IAM client and BizApp are in the same JVM process, and the IAM server is in the same process, which is suitable for microservices or cross-site cross-border Multiple applications in a domain require unified authentication.
- 14. Support multiple modes of deployment and operation (gateway mode): Similar to cluster, the difference is that the authentication client is placed on the gateway, so that BizApp can focus on providing business services, and realize the complete separation of gateway, business application and authentication center, namely , gateway+IAM client, BizApp, IAM server, very suitable for authentication center deployment of complete microservice architecture (tailored)
- 15. OIDC Support
- 16. Comparison [keycloak](https://github.com/keycloak/keycloak-quickstarts) [please refer to here](VS_KEYCLOAK.md)

## 2. Server Deploy

- Docker

TODO

- Bare metal host

TODO

## 3. Client Integration

### 3.1 Gateway Mode (Recommends)

The architectural philosophy of this mode is sidecar, which is based on the idea of separating the business layer and the general layer as much as possible. In traditional enterprise applications, because the business application code and SDKs such as authentication and middleware are strongly coupled in the form of dependencies, it is difficult to upgrade the SDK. Error-prone, etc., seriously affect the rapid iteration of business applications and the stability of services, and the use of gateway to separate common logic such as authentication, so that it only focuses on business logic, greatly reducing the probability of large-scale microservice deployment errors, and each component is composed of Dedicated maintenance from different teams can greatly improve stability and rapid iteration capabilities, and maximize corporate profits.

TODO

### 3.1 Spring Boot SDK Mode

- 3.1, PC integration (front and rear separation)
- 3.2, [Android Access (Global Authentication Interceptor)](iam-client-example/src/main/java/com/wl4g/iam/example/android/AndroidIamUserCoordinator.java)
- 3.3, WeChat public account integration,
- 3.4, all supported yml configurations on the server side (and defaults):

```xml
<dependency>
    <groupId>com.wl4g</groupId>
    <artifactId>iam-client-springboot</artifactId>
    <version>${latest}</version>
</dependency>
```

## 4. Private Fast-Cas theory

TODO

## 5. Operation Monitoring and Observability

- [opentelemetry-java-instrumentation](https://github.com/wl4g-collect/opentelemetry-java-instrumentation)

- [opentelemetry-autoconfigure-jaeger](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#jaeger-exporter)

- ADD opentelemetry instrumentation(javaagent) Example

```bash
export OTEL_TRACES_EXPORTER=jaeger
export OTEL_EXPORTER_JAEGER_ENDPOINT=http://localhost:14250
export OTEL_EXPORTER_JAEGER_TIMEOUT=10000
export OTEL_METRICS_EXPORTER=prometheus
export OTEL_EXPORTER_PROMETHEUS_HOST=localhost
export OTEL_EXPORTER_PROMETHEUS_PORT=9090
java -javaagent:/opt/apps/some-javaagent/opentelemetry/opentelemetry-javaagent.jar -jar iam-web-{version}-bin.jar
```

## 6. Developer's Guide

### Maven mirrors Settings

```xml
mv $HOME/.m2/settings.xml $HOME/.m2/settings_bak.xml

cat <<-'EOF' >$HOME/.m2/settings.xml
<mirrors>
    <mirror>
        <id>alimaven-public</id>
        <mirrorOf>public</mirrorOf>
        <name>alimaven</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
    <mirror>
        <id>alimaven-central</id>
        <mirrorOf>central</mirrorOf>
        <name>alimaven</name>
        <url>https://maven.aliyun.com/repository/central</url>
    </mirror>
    <mirror>
        <id>alimaven-grails-core</id>
        <mirrorOf>grails-core</mirrorOf>
        <name>aliyun maven</name>
        <url>https://maven.aliyun.com/repository/grails-core</url>
    </mirror>
    <mirror>
        <id>alimaven-google</id>
        <mirrorOf>google</mirrorOf>
        <name>aliyun maven</name>
        <url>https://maven.aliyun.com/repository/google</url>
    </mirror>
    <mirror>
        <id>alimaven-spring</id>
        <mirrorOf>spring</mirrorOf>
        <name>aliyun maven</name>
        <url>https://maven.aliyun.com/repository/spring</url>
    </mirror>
  </mirrors>
EOF
```

### Building distribution

```bash
cd iam

# Build as a generic release package (directory structure).
mvn -U clean install -DskipTests -T 2C -Pbuild:tar -Pbuild:framework:feign-istio

# Build as spring boot single executable jar.
mvn -U clean install -DskipTests -T 2C -Pbuild:springjar -Pbuild:framework:feign-istio

# Build as a docker image based on the tar distribution.
mvn -U clean install -DskipTests -T 2C -Pbuild:tar:docker -Pbuild:framework:feign-istio

# Build an ELF native executable based on the graalvm native image.
mvn -U clean install -DskipTests -T 2C -Pbuild:native -Pbuild:framework:feign-istio
```

- Supports profiles are:

  - `-Pbuild:tar`
  - `-Pbuild:springjar`
  - `-Pbuild:tar:docker`
  - `-Pbuild:native` (alpha)
  - `-Pbuild:framework:feign-istio`
  - `-Pbuild:framework:feign-springcloud`
  - `-Pbuild:framework:feign-dubbo`

### 7.1 Integrate the server with SDK for custom development

- 5.1, stand-alone operation mode, using iam's database table, suitable for new system integration;
- 5.2, rely on embedded mode, use external custom database table, suitable for old system retrofit integration;

TODO

## Development

In early 2018, I created an IAM project repository on github and submitted the first commit, followed by system structure design, UI selection, interaction design...
So far, IAM/SSO has been connected to the production environment of an IoT platform, running stably for 1 year+, and access scenarios such as e-commerce business, O2O business, and dynamic configuration of core middleware, etc.
Welcome everyone's attention and use, IAM/SSO will also embrace changes and continue to develop.

## Contributing

Contributions to the project are welcome! For example, submit a PR to fix a bug, or create a new [Issue](https://github.com/wl4g/iam/issues/) to discuss new features or changes.

## Copyright and License

This product is open source and free, and will continue to provide free community technical support. Individual or enterprise users are free to access and use.

- Licensed under the Apache License v2.
- Copyright (c) 2018-present, wanglsir.

The product is open source free, and will continue to provide free community technical support. Individuals or enterprises can freely access and use.

## Stargazers over time

[![Stargazers over time](https://starchart.cc/wl4g/iam.svg)](https://starchart.cc/wl4g/iam)
