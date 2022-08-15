# Introduction

This chart bootstraps an IAM deployment on a Kubernetes cluster using the Helm package manager. 

## 1. Features

- One-step support for istio-based dual-version (`baseline`/`upgrade`) canary (grayscale) deploy, percentage traffic load supported.

- Automatically calculate the number of Pods replicas based on the traffic percentage.

- The based on istio's traffic governance capabilities, it supports the necessary functions of distributed microservices such as `canary routing` , `request limiting`, `circuit breaker`, `fault injection`, and `request response filtering` etc.

- Automatically add the response header `x-app-version`, the standard microservice interface is friendly to gray service diagnosis.

## 2. Prerequisites

+ Kubernetes 1.6+
+ Helm
+ Istio 1.12+ (Optional and recommends)

## 3. Getting the Chart

+ [helm charts global values.yaml](./values.yaml)

+ From github

```bash
git clone https://github.com/wl4g/iam.git
cd iam/deploy/helm/
```

+ Or From chart repos

```bash
helm repo add iam https://registry.wl4g.io/repository/helm-release
```

> If you want to install an unstable version, you need to add `--devel` when you execute the `helm install` command.
> If you only want to test or simulate running, add the options `--dry-run --debug`

## 4. Installing with Canary

+ Step 1: Create and setup namespace

```bash
kubectl label ns iam istio-injection=enabled --overwrite
```

+ Step 2: (Choose One) Create image repository secret. for example:

```bash
kubectl -n iam create secret docker-registry ccr-tencentcloud-secret \
--docker-server='ccr.ccs.tencentyun.com/wl4g' \
--docker-username='<username>' \
--docker-password='<password>'

# Or:
#kubectl -n iam create secret docker-registry cr-aliyun-secret \
#--docker-server='registry.cn-shenzhen.aliyuncs.com/wl4g' \
#--docker-username='<username>' \
#--docker-password='<password>'

# Or:
#kubectl -n iam create secret docker-registry cr-nexus3-secret \
#--docker-server='cr.registry.wl4g.com/wl4g' \
#--docker-username='<username>' \
#--docker-password='<password>'

# Or:
#kubectl -n iam create secret docker-registry hub-docker-secret \
#--docker-server='docker.io/wl4g' \
#--docker-username='<username>' \
#--docker-password='<password>'
```

+ Step 3: Initial deploying. (baseline version only)

```bash
helm -n iam upgrade --install --create-namespace iam iam-stack --set="\
iam-web.image.baselineTag=1.0.0,\
iam-facade.image.baselineTag=1.0.0,\
iam-data.image.baselineTag=1.0.0"
```

+ Step 4: Upgrade deploying using canary mode. (weighted by traffic)

```bash
helm -n iam upgrade --install --create-namespace iam iam-stack --set="\
iam-web.image.baselineTag=1.0.0,\
iam-web.image.upgradeTag=1.0.1,\
iam-facade.image.baselineTag=1.0.0,\
iam-facade.image.upgradeTag=1.0.1,\
iam-data.image.baselineTag=1.0.0,\
iam-data.image.upgradeTag=1.0.1,\
iam-web.governance.istio.ingress.http.canary.baseline.weight=80,\
iam-web.governance.istio.ingress.http.canary.upgrade.weight=20,\
iam-facade.governance.istio.ingress.http.canary.baseline.weight=80,\
iam-facade.governance.istio.ingress.http.canary.upgrade.weight=20,\
iam-data.governance.istio.ingress.http.canary.baseline.weight=80,\
iam-data.governance.istio.ingress.http.canary.upgrade.weight=20,\
global.components.jaeger.internal.enabled=true,\
global.components.redis.internal.enabled=true,\
global.components.zookeeper.internal.enabled=true,\
global.components.kafka.internal.enabled=true,\
global.components.mysql.internal.enabled=true"
```

+ Step 5: After confirming that the upgrade is successful, use the new version as the benchmark, remove the old version, and switch all traffic to the new version

```bash
helm -n iam upgrade --install --create-namespace iam iam-stack --set="\
iam-web.image.baselineTag=1.0.1,\
iam-web.image.upgradeTag=,\
iam-facade.image.baselineTag=1.0.1,\
iam-facade.image.upgradeTag=,\
iam-data.image.baselineTag=1.0.1,\
iam-data.image.upgradeTag=,\
iam-web.governance.istio.ingress.http.canary.baseline.weight=100,\
iam-web.governance.istio.ingress.http.canary.upgrade.weight=0,\
iam-facade.governance.istio.ingress.http.canary.baseline.weight=100,\
iam-facade.governance.istio.ingress.http.canary.upgrade.weight=0,\
iam-data.governance.istio.ingress.http.canary.baseline.weight=100,\
iam-data.governance.istio.ingress.http.canary.upgrade.weight=0
global.components.jaeger.internal.enabled=true,\
global.components.redis.internal.enabled=true,\
global.components.zookeeper.internal.enabled=true,\
global.components.kafka.internal.enabled=true,\
global.components.mysql.internal.enabled=true"
```

## 5. Rebuild dependents

- ***Notice:*** The following dependent third-party component charts are generated based on generic templates.
In fact, IAM's required dependencies are only a subset of them, which are enabled on demand, automatic deployment
of all third-party dependent components is disabled by default.

```bash
helm dependency build

helm dependency update

helm dependency list
NAME            VERSION     REPOSITORY                                  STATUS
iam-web         ~0.1.0      file://charts/iam-web                       ok
iam-facade      ~0.1.0      file://charts/iam-facade                    ok
iam-data        ~0.1.0      file://charts/iam-data                      ok
jaeger          ~0.57.1     https://jaegertracing.github.io/helm-charts ok    
jaeger-operator ~2.33.0     https://jaegertracing.github.io/helm-charts ok    
zookeeper       ~10.0.2     https://charts.bitnami.com/bitnami          ok    
kafka           ~18.0.3     https://charts.bitnami.com/bitnami          ok    
emqx            ~5.0.3      https://repos.emqx.io/charts                ok    
emqx-operator   ~1.0.9      https://repos.emqx.io/charts                ok    
redis           ~17.0.x     https://charts.bitnami.com/bitnami          ok    
mysql           ~9.2.x      https://charts.bitnami.com/bitnami          ok    
postgresql      ~11.6.17    https://charts.bitnami.com/bitnami          ok    
mongodb         ~12.1.27    https://charts.bitnami.com/bitnami          ok    
elasticsearch   ~19.1.6     https://charts.bitnami.com/bitnami          ok    
solr            ~6.0.6      https://charts.bitnami.com/bitnami          ok    
cassandra       ~9.2.11     https://charts.bitnami.com/bitnami          ok    
minio           ~11.7.13    https://charts.bitnami.com/bitnami          ok
...
```

## 6. Uninstalling the Chart

To uninstall/delete the `iam` deployment:

```bash
helm -n iam del iam
```

## 7. Configurable

The following table lists the configurable parameters of the SpringBoot APP(IAM) chart and their default values.

| Parameter  | Description | Default Value |
| ---        |  ---        | ---           |
| `<app>.enabled` | SpringBoot APP image name | true |
| `<app>.image.repository` | SpringBoot APP image name | `wl4g/&lt;app&gt;` |
| `<app>.image.baselineTag` | SpringBoot APP Image baseline tag name | `latest` |
| `<app>.image.upgradeTag` | SpringBoot APP Image upgrade tag name | `nil` |
| `<app>.image.pullPolicy`  | The image pull policy  | `IfNotPresent` |
| `<app>.image.pullSecrets`  | The image pull secrets  | `{hub-docker-secret, cr-aliyun-secret, ccr-tencentyun-secret, cr-nexus3-secret}` |
| `<app>.envFromSecret` | The name pull a secret in the same kubernetes namespace which contains values that will be added to the environment | nil |
| `<app>.autoscaling.enabled` | Autoscaling enabled status. | `true` |
| `<app>.autoscaling.replicaCount` | The total number of replicas for this application. (ie: baseline deployment pods + upgrade deployment pods) | 1 |
| `<app>.updateStrategy.type` | Pods update strategy. | `RollingUpdate` |
| `<app>.podAnnotations` | pod annotations | `{prometheus.io/scrape=true, prometheus.io/path=/actuator/prometheus, prometheus.io/port=10108}` |
| `<app>.persistence.enabled` | Enable APP persistence using PVC | `false` |
| `<app>.persistence.storageClass` | Storage class of backing PVC |`nil` (uses alpha storage class annotation)|
| `<app>.persistence.existingClaim` | SpringBoot APP data Persistent Volume existing claim name, evaluated as a template |""|
| `<app>.persistence.accessMode` | PVC Access Mode for APP volume | `ReadWriteOnce` |
| `<app>.persistence.size` | PVC Storage Request for APP volume | `20Mi` |
| `<app>.resources.enabled` | Enable resource requests/limits | `true` |
| `<app>.resources.requests.cpu` | CPU resource requests/limits | `100m` |
| `<app>.resources.requests.memory` | Memory resource requests/limits | `256Mi` |
| `<app>.resources.limits.cpu` | CPU resource requests/limits | `900m` |
| `<app>.resources.limits.memory` | Memory resource requests/limits | `1024Mi` |
| `<app>.initContainers` | Containers that run before the creation of APP containers. They can contain utilities or setup scripts. |`{}`|
| `<app>.podSecurityContext.enabled` | Pod security context enabled | `true` |
| `<app>.podSecurityContext.fsGroup` | Pod security fs group | `1000` |
| `<app>.podSecurityContext.fsGroupChangePolicy` | Enable pod security group policy | `Always` |
| `<app>.podSecurityContext.runAsUser` | Enable pod as uid |1000|
| `<app>.podSecurityContext.supplementalGroups` | Enable pod security supplemental groups | `[]` |
| `<app>.containerSecurityContext.enabled` | Enable container security context | `true` |
| `<app>.containerSecurityContext.runAsNonRoot` | Run container as root | `true` |
| `<app>.containerSecurityContext.runAsUser` | Run container as uid | `1000` |
| `<app>.nodeSelector` | Node labels for pod assignment |`{}`|
| `<app>.tolerations` | Toleration labels for pod assignment |`[]`|
| `<app>.affinity` | Map of node/pod affinities |`{}`|
| `<app>.envConfigs` | SpringBoot APP startup environments. | JAVA_OPTS="-Djava.awt.headless=true"</br>APP_ACTIVE="pro"</br>SPRING_SERVER_PORT="8080" |
| `<app>.agentConfig` | SpringBoot APP startup javaagent configuration.(Usually no configuration is required) |`{}`|
| `<app>.appConfigs`  | for example IAM web configurations. see to: [github.com/wl4g/iam/tree/master/server/server-starter-web/src/main/resources/](https://github.com/wl4g/iam/tree/master/server/server-starter-web/src/main/resources/)|`{}`|
| `<app>.service.provider`  | Kubernetes Service provider. | ClusterIP |
| `<app>.service.apiPortPort`  | Port for api. |18080|
| `<app>.service.prometheusPortPort`  | Port for prometheus. |10108|
| `<app>.service.nodePorts.api`  | Kubernetes node port for api. |  nil  |
| `<app>.service.nodePorts.prometheus`  | Kubernetes node port for prometheus. |  nil  |
| `<app>.service.loadBalancerIP`  | loadBalancerIP for Service |  nil |
| `<app>.service.loadBalancerSourceRanges` |  Address(es) that are allowed when service is LoadBalancer | [] |
| `<app>.service.externalIPs` |   ExternalIPs for the service | [] |
| `<app>.service.annotations` |   Service annotations | `{}` (evaluated as a template)|
| `<app>.governance.provider` | Service governance provider.(`Ingress`/`Istio`) | `Istio` |
| `<app>.governance.ingress.<name>.enabled` | Enable app governance with legacy ingress | false |
| `<app>.governance.ingress.<name>.ingressClassName` | Set the legacy ingress class for APP api |  nginx  |
| `<app>.governance.ingress.<name>.path` | Ingress app path |  / |
| `<app>.governance.ingress.<name>.customHosts` | Ingress app hosts | e.g: &lt;app&gt;.APP.svc.cluster.local |
| `<app>.governance.ingress.<name>.tls` | Ingress app tls | [] |
| `<app>.governance.ingress.<name>.annotations` | Ingress annotations for APP management | {} |
| `<app>.governance.istio.ingress.domain` | Istio ingress top domain | wl4g.io |
| `<app>.governance.istio.ingress.customHosts` | Istio ingress hosts | e.g: some-example.com |
| `<app>.governance.istio.ingress.http.primaryHttpServiceExposeName` | - | `api` |
| `<app>.governance.istio.ingress.http.canary.uriPrefix` | - | `/` |
| `<app>.governance.istio.ingress.http.canary.cookieRegex` | - | `^(.*?;)?(email=[^;]*@wl4g.io)(;.*)?$` |
| `<app>.governance.istio.ingress.http.canary.baseline.loadBalancer` | - | ROUND_ROBIN |
| `<app>.governance.istio.ingress.http.canary.baseline.weight` | - | 80 |
| `<app>.governance.istio.ingress.http.canary.upgrade.loadBalancer` | - | ROUND_ROBIN |
| `<app>.governance.istio.ingress.http.canary.upgrade.weight` | - | 20 |
| `<app>.governance.istio.ingress.http.scheme` | - | http |
| `<app>.governance.istio.ingress.http.tls.mode` | - | SIMPLE |
| `<app>.governance.istio.ingress.http.fault.delay.percentage.value` | - | 0.1 |
| `<app>.governance.istio.ingress.http.fault.delay.fixedDelay` | - | 5s |
| `<app>.governance.istio.ingress.http.fault.abort.percentage.value` | - | 0.1 |
| `<app>.governance.istio.ingress.http.fault.abort.fixedDelay` | - | 5s |
| `<app>.governance.istio.ingress.http.fault.abort.httpStatus` | - | 400 |
| `<app>.governance.istio.ingress.http.retries.attempts` | - | 5 |
| `<app>.governance.istio.ingress.http.retries.perTryTimeout` | - | 30s |
| `<app>.governance.istio.ingress.http.outlierDetection.consecutive5xxErrors` | - | 7 |
| `<app>.governance.istio.ingress.http.outlierDetection.interval` | - | 5m |
| `<app>.governance.istio.ingress.http.outlierDetection.baseEjectionTime` | - | 15m |
| `<app>.governance.istio.ingress.tcp.enabled` | Enable tcp istio ingress | `false` |
| `<app>.governance.istio.ingress.tcp.frontPort` | Enable tcp istio ingress | `1883` |
| `<app>.governance.istio.ingress.tcp.backendPort` | Enable tcp istio ingress | `1883` |
| `<app>.governance.istio.egress[].name` | External service name | `example-wx-payment` |
| `<app>.governance.istio.egress[].serviceAccount` | External service serviceAccount. | `nil` |
| `<app>.governance.istio.egress[].labels` | External service entries labels. | `classify=external-service, version=v1` |
| `<app>.governance.istio.egress[].instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `<app>.governance.istio.egress[].instancePorts[].targetPort` | External service entries instance ports protocol. | `443` |
| `<app>.governance.istio.egress[].instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `<app>.governance.istio.egress[].instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `<app>.governance.istio.egress[].location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `<app>.governance.istio.egress[].resolution` | Mesh egess service entries resolution. | `DNS` |
| --- (Optional) Global Dependents Components. --- | | |
| `global.preStartScript` | Container pre-start hook scripts. | `nil` |
| `global.envConfigs` | Container start environments. | `{}` |
| `global.agentConfigs` | JVM start agent config. | `{}` |
| `global.appConfigs` | SpringBoot startup application configs. | `{}` |
| `global.otlp.internal.enabled` | Enable internal service. | `false` |
| `global.otlp.external.enabled` | Enable external service. | `false` |
| `global.otlp.external.namespace` | External service namespace. | `nil` |
| `global.otlp.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.otlp.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.otlp.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.otlp.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `4318` |
| `global.otlp.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.otlp.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.otlp.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.otlp.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.jaeger.internal.enabled` | Enable internal service. | `false` |
| `global.jaeger.external.enabled` | Enable external service. | `false` |
| `global.jaeger.external.namespace` | External service namespace. | `nil` |
| `global.jaeger.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.jaeger.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.jaeger.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.jaeger.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `14268` |
| `global.jaeger.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.jaeger.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.jaeger.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.jaeger.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.zookeeper.internal.enabled` | Enable internal service. | `false` |
| `global.zookeeper.external.enabled` | Enable external service. | `false` |
| `global.zookeeper.external.namespace` | External service namespace. | `nil` |
| `global.zookeeper.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.zookeeper.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.zookeeper.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.zookeeper.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `2181` |
| `global.zookeeper.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.zookeeper.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.zookeeper.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.zookeeper.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.kafka.internal.enabled` | Enable internal service. | `false` |
| `global.kafka.external.enabled` | Enable external service. | `false` |
| `global.kafka.external.namespace` | External service namespace. | `nil` |
| `global.kafka.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.kafka.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.kafka.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.kafka.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `9092` |
| `global.kafka.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.kafka.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.kafka.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.kafka.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.emqx.internal.enabled` | Enable internal service. | `false` |
| `global.emqx.external.enabled` | Enable external service. | `false` |
| `global.emqx.external.namespace` | External service namespace. | `nil` |
| `global.emqx.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.emqx.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.emqx.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.emqx.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `1883` |
| `global.emqx.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.emqx.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.emqx.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.emqx.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.mysql.internal.enabled` | Enable internal service. | `false` |
| `global.mysql.external.enabled` | Enable external service. | `false` |
| `global.mysql.external.namespace` | External service namespace. | `nil` |
| `global.mysql.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.mysql.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.mysql.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.mysql.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `3306` |
| `global.mysql.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.mysql.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.mysql.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.mysql.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.postgresql.internal.enabled` | Enable internal service. | `false` |
| `global.postgresql.external.enabled` | Enable external service. | `false` |
| `global.postgresql.external.namespace` | External service namespace. | `nil` |
| `global.postgresql.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.postgresql.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.postgresql.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.postgresql.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `5432` |
| `global.postgresql.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.postgresql.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.postgresql.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.postgresql.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.redis.internal.enabled` | Enable internal service. | `false` |
| `global.redis.external.enabled` | Enable external service. | `false` |
| `global.redis.external.namespace` | External service namespace. | `nil` |
| `global.redis.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.redis.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.redis.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.redis.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `6379` |
| `global.redis.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.redis.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.redis.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.redis.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.cassandra.internal.enabled` | Enable internal service. | `false` |
| `global.cassandra.external.enabled` | Enable external service. | `false` |
| `global.cassandra.external.namespace` | External service namespace. | `nil` |
| `global.cassandra.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.cassandra.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.cassandra.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.cassandra.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `9042` |
| `global.cassandra.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.cassandra.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.cassandra.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.cassandra.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.minio.internal.enabled` | Enable internal service. | `false` |
| `global.minio.external.enabled` | Enable external service. | `false` |
| `global.minio.external.namespace` | External service namespace. | `nil` |
| `global.minio.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.minio.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.minio.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.minio.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `9000,9090` |
| `global.minio.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.minio.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.minio.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.minio.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.mongodb.internal.enabled` | Enable internal service. | `false` |
| `global.mongodb.external.enabled` | Enable external service. | `false` |
| `global.mongodb.external.namespace` | External service namespace. | `nil` |
| `global.mongodb.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.mongodb.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.mongodb.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.mongodb.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `27017` |
| `global.mongodb.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.mongodb.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.mongodb.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.mongodb.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.elasticsearch.internal.enabled` | Enable internal service. | `false` |
| `global.elasticsearch.external.enabled` | Enable external service. | `false` |
| `global.elasticsearch.external.namespace` | External service namespace. | `nil` |
| `global.elasticsearch.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.elasticsearch.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.elasticsearch.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.elasticsearch.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `9200,9300` |
| `global.elasticsearch.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.elasticsearch.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.elasticsearch.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.elasticsearch.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.solr.internal.enabled` | Enable internal service. | `false` |
| `global.solr.external.enabled` | Enable external service. | `false` |
| `global.solr.external.namespace` | External service namespace. | `nil` |
| `global.solr.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.solr.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.solr.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.solr.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `7983,8983` |
| `global.solr.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.solr.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.solr.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.solr.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.hmaster.internal.enabled` | Enable internal service. | `false` |
| `global.hmaster.external.enabled` | Enable external service. | `false` |
| `global.hmaster.external.namespace` | External service namespace. | `nil` |
| `global.hmaster.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.hmaster.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.hmaster.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.hmaster.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `16000,16010` |
| `global.hmaster.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.hmaster.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.hmaster.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.hmaster.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.hregionserver.internal.enabled` | Enable internal service. | `false` |
| `global.hregionserver.external.enabled` | Enable external service. | `false` |
| `global.hregionserver.external.namespace` | External service namespace. | `nil` |
| `global.hregionserver.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.hregionserver.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.hregionserver.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.hregionserver.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `16020,16030` |
| `global.hregionserver.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.hregionserver.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.hregionserver.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.hregionserver.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.namenode.internal.enabled` | Enable internal service. | `false` |
| `global.namenode.external.enabled` | Enable external service. | `false` |
| `global.namenode.external.namespace` | External service namespace. | `nil` |
| `global.namenode.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.namenode.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.namenode.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.namenode.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `9870,8020,8088` |
| `global.namenode.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.namenode.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.namenode.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.namenode.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.nodemanager.internal.enabled` | Enable internal service. | `false` |
| `global.nodemanager.external.enabled` | Enable external service. | `false` |
| `global.nodemanager.external.namespace` | External service namespace. | `nil` |
| `global.nodemanager.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.nodemanager.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.nodemanager.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.nodemanager.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `8040,8041,7337,8042,13562` |
| `global.nodemanager.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.nodemanager.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.nodemanager.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.nodemanager.external.resolution` | Mesh egess service entries resolution. | `NONE` |
| `global.datanode.internal.enabled` | Enable internal service. | `false` |
| `global.datanode.external.enabled` | Enable external service. | `false` |
| `global.datanode.external.namespace` | External service namespace. | `nil` |
| `global.datanode.external.serviceAccount` | External service serviceAccount. | `nil` |
| `global.datanode.external.labels` | External service entries labels. | `classify=external-service, version=v1` |
| `global.datanode.external.instancePorts[].protocol` | External service entries instance ports protocol. | `TCP` |
| `global.datanode.external.instancePorts[].targetPort` | External service entries instance ports protocol. | `9867,9864,9866,32828` |
| `global.datanode.external.instanceAddresses[].ip` | External service entries instance IP. | `nil` |
| `global.datanode.external.instanceAddresses[].hosts[]` | External service entries instance hosts. | `[]` |
| `global.datanode.external.location` | Mesh egess service entries location. | `MESH_EXTERNAL` |
| `global.datanode.external.resolution` | Mesh egess service entries resolution. | `NONE` |

## 8. FAQ

### How to troubleshoot Pods that are missing os tools

- Use ephemeral containers to debug running or crashed Pods: [kubernetes.io/docs/tasks/debug-application-cluster/debug-running-pod](https://kubernetes.io/docs/tasks/debug-application-cluster/debug-running-pod/)

- Parent charts override the property values of child charts see:
[github.com/whmzsu/helm-doc-zh-cn/blob/master/chart_template_guide/subcharts_and_globals-zh_cn.md](https://github.com/whmzsu/helm-doc-zh-cn/blob/master/chart_template_guide/subcharts_and_globals-zh_cn.md)
