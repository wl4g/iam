# Introduction

This chart bootstraps an IAM deployment on a Kubernetes cluster using the Helm package manager. 

## 1. Prerequisites

+ Kubernetes 1.6+
+ Helm
+ Istio 1.12+ (Optional and recommends)

## 2. Getting the Chart

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

## 3. Installing with Canary

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

## 4. Rebuild dependents

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

## 4. Uninstalling the Chart

To uninstall/delete the `iam` deployment:

```bash
helm -n iam del iam
```

## 5. Configurable

The following table lists the configurable parameters of the SpringBoot APP(IAM) chart and their default values.

| Parameter  | Description | Default Value |
| ---        |  ---        | ---           |
| `<app>.enabled` | SpringBoot APP image name | true |
| `<app>.image.repository` | SpringBoot APP image name | wl4g/&lt;app&gt; |
| `<app>.image.tag` | SpringBoot APP Image name | latest |
| `<app>.image.pullPolicy`  | The image pull policy  |IfNotPresent|
| `<app>.image.pullSecrets`  | The image pull secrets  |`[]` (does not add image pull secrets to deployed pods)|
| `<app>.envFromSecret` | The name pull a secret in the same kubernetes namespace which contains values that will be added to the environment | nil |
| `<app>.autoscaling.enabled` | Autoscaling enabled status. |true|
| `<app>.autoscaling.replicaCount` | Number of pods that are always running. | 2 |
| `<app>.persistence.enabled` | Enable APP persistence using PVC |false|
| `<app>.persistence.storageClass` | Storage class of backing PVC |`nil` (uses alpha storage class annotation)|
| `<app>.persistence.existingClaim` | SpringBoot APP data Persistent Volume existing claim name, evaluated as a template |""|
| `<app>.persistence.accessMode` | PVC Access Mode for APP volume |ReadWriteOnce|
| `<app>.persistence.size` | PVC Storage Request for APP volume |20Mi|
| `<app>.resources.enabled` | Enable resource requests/limits |false|
| `<app>.resources.limits.cpu` | CPU resource requests/limits |500m|
| `<app>.resources.limits.memory` | Memory resource requests/limits |1024Mi|
| `<app>.resources.requests.cpu` | CPU resource requests/limits |500m|
| `<app>.resources.requests.memory` | Memory resource requests/limits |1024Mi|
| `<app>.initContainers` | Containers that run before the creation of APP containers. They can contain utilities or setup scripts. |`{}`|
| `<app>.podSecurityContext.enabled` | Pod security context enabled |true|
| `<app>.podSecurityContext.fsGroup` | Pod security fs group |1000|
| `<app>.podSecurityContext.fsGroupChangePolicy` | Enable pod security group policy |Always|
| `<app>.podSecurityContext.runAsUser` | Enable pod as uid |1000|
| `<app>.podSecurityContext.supplementalGroups` | Enable pod security supplemental groups |`[]`1000|
| `<app>.containerSecurityContext.enabled` | Enable container security context |false|
| `<app>.containerSecurityContext.runAsNonRoot` | Run container as root |true|
| `<app>.containerSecurityContext.runAsUser` | Run container as uid |1000|
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
| `<app>.governance.provider` | Service governance provider.(Ingress/Istio) | Istio |
| `<app>.governance.ingress.api.enabled` | Enable api governance with legacy ingress | false |
| `<app>.governance.ingress.api.ingressClassName` | Set the legacy ingress class for APP api |  nginx  |
| `<app>.governance.ingress.api.path` | Ingress path for APP api |  / |
| `<app>.governance.ingress.api.customHosts` | Ingress hosts for APP prometheus | e.g: &lt;app&gt;.APP.svc.cluster.local |
| `<app>.governance.ingress.api.tls` | Ingress tls for APP prometheus | [] |
| `<app>.governance.ingress.api.annotations` | Ingress annotations for APP management | {} |
| `<app>.governance.ingress.management.enabled` |  Enable ingress for APP management |  false |
| `<app>.governance.ingress.management.ingressClassName` |    Set the ingress class for APP management |  nginx  |
| `<app>.governance.ingress.management.path` | Ingress path for APP management |    / |
| `<app>.governance.ingress.management.customHosts` | Ingress hosts for APP management API | e.g: &lt;app&gt;.iam.svc.cluster.local |
| `<app>.governance.ingress.management.tls` | Ingress tls for APP management |  [] |
| `<app>.governance.ingress.management.annotations` | Ingress annotations for APP management | {} |
| `<app>.governance.istio.ingress.domain` | Istio ingress top domain | wl4g.io |
| `<app>.governance.istio.ingress.customHosts` | Istio ingress hosts | e.g: some-example.com |
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
| `<app>.governance.istio.ingress.tcp.enabled` | Enable tcp istio ingress | false |
| `<app>.governance.istio.ingress.tcp.frontPort` | Enable tcp istio ingress | 1883 |
| `<app>.governance.istio.ingress.tcp.backendPort` | Enable tcp istio ingress | 1883 |
| --- (Optional) Global Dependents Components. --- | | |
| `global.redis.enabled` | Enable redis cluster component. | true |
| `global.redis.provider` | Depends of redis cluster. (internal/external) | external |
| `global.redis.internal.enabled` | Enable internal redis cluster | false |
| `global.redis.external.ips` | External redis cluster node ips | false |
| `global.redis.external.ports` | External redis cluster node ports | 6379,6380,6381,7379,7380,7381 |
| `global.redis.external.password` | External redis cluster password | nil |
| `global.mysql.enabled` | Enable mysql component. | true |
| `global.mysql.provider` | Depends of mysql provider. (internal/external) | external |
| `global.mysql.internal.enabled` | Enable internal mysql.() | false |
| `global.mysql.external.host` | External mysql host(mysql) | false |
| `global.mysql.external.port` | External mysql port(mysql) | 3306 |
| `global.mysql.external.username` | External mysql username | nil |
| `global.mysql.external.password` | External mysql password | nil |
| `global.postgresql.enabled` | Enable postgresql component.| false |
| `global.postgresql.provider` | Use postgresql provider. (internal/external) | external |
| `global.postgresql.internal.enabled` | Enable internal postgresql. | false |
| `global.postgresql.external.ips` | External postgresql server ips. | `127.0.0.1` |
| `global.postgresql.external.ports` | External postgresql server ports. | `5432` |
| `global.zookeeper.enabled` | Enable zookeeper component.| false |
| `global.zookeeper.provider` | Use zookeeper provider. (internal/external) | external |
| `global.zookeeper.internal.enabled` | Enable internal zookeeper. | false |
| `global.zookeeper.external.ips` | External zookeeper server ips. | `127.0.0.1` |
| `global.zookeeper.external.ports` | External zookeeper server ports. | `5432` |
| `global.kafka.enabled` | Enable kafka component.| false |
| `global.kafka.provider` | Use kafka provider. (internal/external) | external |
| `global.kafka.internal.enabled` | Enable internal kafka. | false |
| `global.kafka.external.ips` | External kafka server ips. | `127.0.0.1` |
| `global.kafka.external.ports` | External kafka server ports. | `5432` |
| `global.mongodb.enabled` | Enable mongodb component.| false |
| `global.mongodb.provider` | Use mongodb provider. (internal/external) | external |
| `global.mongodb.internal.enabled` | Enable internal mongodb. | false |
| `global.mongodb.external.ips` | External mongodb server ips. | `127.0.0.1` |
| `global.mongodb.external.ports` | External mongodb server ports. | `27017` |
| `global.elasticsearch.enabled` | Enable elasticsearch component.| false |
| `global.elasticsearch.provider` | Use elasticsearch provider. (internal/external) | external |
| `global.elasticsearch.internal.enabled` | Enable internal elasticsearch. | false |
| `global.elasticsearch.external.ips` | External elasticsearch server ips. | `127.0.0.1` |
| `global.elasticsearch.external.ports` | External elasticsearch server ports. | `9300` |
| `global.solr.enabled` | Enable solr component.| false |
| `global.solr.provider` | Use solr provider. (internal/external) | external |
| `global.solr.internal.enabled` | Enable internal solr. | false |
| `global.solr.external.ips` | External solr server ips. | `127.0.0.1` |
| `global.solr.external.ports` | External solr server ports. | `8983` |
| `global.minio.enabled` | Enable minio component.| false |
| `global.minio.provider` | Use minio provider. (internal/external) | external |
| `global.minio.internal.enabled` | Enable internal minio. | false |
| `global.minio.external.ips` | External minio server ips. | `127.0.0.1` |
| `global.minio.external.ports` | External minio server ports. | `9000` |
| `global.trace.enabled` | Enable trace component. | false |
| `global.trace.provider` | Trace receiver provider. (jaeger/ziplin/otel) | jaeger |
| `global.trace.sample_rate` | Trace simpler rate. | 1 |
| `global.trace.jaeger.endpoint` | Jaeger endpoint. | `http://10.0.0.114:4318` |
| `global.trace.jaeger.username` | Jaeger username. | `nil` |
| `global.trace.jaeger.password` | Jaeger password. | `nil` |
| `global.trace.jaeger.agent_host` | Jaeger agent host. | `hostname` |
| `global.trace.jaeger.agent_port` | Jaeger agent port. | `6831` |
| `global.trace.otel.endpoint` | Otel endpoint. | `http://10.0.0.114:4318` |
| `global.trace.otel.url_path` | Otel endpoint path. | `/v1/traces` |
| `global.trace.otel.compression` | Otel enable compression. | false |
| `global.trace.otel.insecure` | Otel insecure | true |
| `global.trace.otel.timeout` | Otel timeout. | 10s |

## 6. FAQ

### How to troubleshoot Pods that are missing os tools

- Use ephemeral containers to debug running or crashed Pods: [kubernetes.io/docs/tasks/debug-application-cluster/debug-running-pod](https://kubernetes.io/docs/tasks/debug-application-cluster/debug-running-pod/)

- Parent charts override the property values of child charts see:
[github.com/whmzsu/helm-doc-zh-cn/blob/master/chart_template_guide/subcharts_and_globals-zh_cn.md](https://github.com/whmzsu/helm-doc-zh-cn/blob/master/chart_template_guide/subcharts_and_globals-zh_cn.md)
