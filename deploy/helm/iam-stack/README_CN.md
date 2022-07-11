# Introduction

This chart bootstraps an IAM deployment on a Kubernetes cluster using the Helm package manager. 

## Prerequisites

+ Kubernetes 1.6+
+ Helm
+ Istio 1.12+ (Optional and recommends)

## Installing the Chart

- [helm charts values.yaml](iam/values.yaml)

To install the chart with the release name `iam`:

+ From github

```bash
git clone https://github.com/wl4g/iam.git
cd iam/deploy/charts/iam
helm -n iam upgrade --install iam .

# for debug template computed values.
#helm --debug -n iam upgrade --install iam .
```

+ or, From chart repos

```bash
helm repo add IAM https://helm.wl4g.io/iam/charts
helm -n iam upgrade --install iam wl4g/iam
```

> If you want to install an unstable version, you need to add `--devel` when you execute the `helm install` command.

+ Veifying example

```bash
# TODO
```

## Uninstalling the Chart

To uninstall/delete the `iam` deployment:

```bash
helm del iam
```

## Configurable

The following table lists the configurable parameters of the IAM chart and their default values.

| Parameter  | Description | Default Value |
| ---        |  ---        | ---           |
| `<app_name>.image.repository` | ShardingProxy Image name |wl4g/iam|
| `<app_name>.image.pullPolicy`  | The image pull policy  |IfNotPresent|
| `<app_name>.image.pullSecrets`  | The image pull secrets  |`[]` (does not add image pull secrets to deployed pods)|
| `<app_name>.envFromSecret` | The name pull a secret in the same kubernetes namespace which contains values that will be added to the environment | nil |
| `<app_name>.autoscaling.enabled` | Autoscaling enabled status. |true|
| `<app_name>.autoscaling.replicaCount` | Number of pods that are always running. | 2 |
| `<app_name>.persistence.enabled` | Enable IAM persistence using PVC |false|
| `<app_name>.persistence.storageClass` | Storage class of backing PVC |`nil` (uses alpha storage class annotation)|
| `<app_name>.persistence.existingClaim` | ShardingProxy data Persistent Volume existing claim name, evaluated as a template |""|
| `<app_name>.persistence.accessMode` | PVC Access Mode for IAM volume |ReadWriteOnce|
| `<app_name>.persistence.size` | PVC Storage Request for IAM volume |20Mi|
| `<app_name>.resources.enabled` | Enable resource requests/limits |false|
| `<app_name>.resources.limits.cpu` | CPU resource requests/limits |500m|
| `<app_name>.resources.limits.memory` | Memory resource requests/limits |1024Mi|
| `<app_name>.resources.requests.cpu` | CPU resource requests/limits |500m|
| `<app_name>.resources.requests.memory` | Memory resource requests/limits |1024Mi|
| `<app_name>.initContainers` | Containers that run before the creation of IAM containers. They can contain utilities or setup scripts. |`{}`|
| `<app_name>.podSecurityContext.enabled` | Pod security context enabled |true|
| `<app_name>.podSecurityContext.fsGroup` | Pod security fs group |1000|
| `<app_name>.podSecurityContext.fsGroupChangePolicy` | Enable pod security group policy |Always|
| `<app_name>.podSecurityContext.runAsUser` | Enable pod as uid |1000|
| `<app_name>.podSecurityContext.supplementalGroups` | Enable pod security supplemental groups |`[]`1000|
| `<app_name>.containerSecurityContext.enabled` | Enable container security context |false|
| `<app_name>.containerSecurityContext.runAsNonRoot` | Run container as root |true|
| `<app_name>.containerSecurityContext.runAsUser` | Run container as uid |1000|
| `<app_name>.nodeSelector` | Node labels for pod assignment |`{}`|
| `<app_name>.tolerations` | Toleration labels for pod assignment |`[]`|
| `<app_name>.affinity` | Map of node/pod affinities |`{}`|
| `<app_name>.applicationConfigs`  | IAM web configurations. see such to: [iam-server-starter-web](https://github.com/wl4g/iam/tree/master/server/server-starter-web/src/main/resources/)|`{}`|
| `<app_name>.service.type`  | Kubernetes Service type. |ClusterIP|
| `<app_name>.service.apiPortPort`  | Port for api. |18080|
| `<app_name>.service.prometheusPortPort`  | Port for prometheus. |10108|
| `<app_name>.service.nodePorts.api`  | Kubernetes node port for api. |  nil  |
| `<app_name>.service.nodePorts.prometheus`  | Kubernetes node port for prometheus. |  nil  |
| `<app_name>.service.loadBalancerIP`  | loadBalancerIP for Service |  nil |
| `<app_name>.service.loadBalancerSourceRanges` |  Address(es) that are allowed when service is LoadBalancer | [] |
| `<app_name>.service.externalIPs` |   ExternalIPs for the service | [] |
| `<app_name>.service.annotations` |   Service annotations | `{}` (evaluated as a template)|
| `<app_name>.ingress.api.enabled` | Enable ingress for IAM api | false |
| `<app_name>.ingress.api.ingressClassName` |    Set the ingress class for IAM api |  nginx  |
| `<app_name>.ingress.api.path` | Ingress path for IAM api |  / |
| `<app_name>.ingress.api.hosts` | Ingress hosts for IAM prometheus | api.<app_name>.iam.svc.cluster.local |
| `<app_name>.ingress.api.tls` | Ingress tls for IAM prometheus | [] |
| `<app_name>.ingress.api.annotations` | Ingress annotations for IAM prometheus | {} |
| `<app_name>.ingress.prometheus.enabled` |  Enable ingress for IAM prometheus |  false |
| `<app_name>.ingress.prometheus.ingressClassName` |    Set the ingress class for IAM prometheus |  nginx  |
| `<app_name>.ingress.prometheus.path` | Ingress path for IAM prometheus |    / |
| `<app_name>.ingress.prometheus.hosts` | Ingress hosts for IAM prometheus API |  prometheus.<app_name>.iam.svc.cluster.local |
| `<app_name>.ingress.prometheus.tls` | Ingress tls for IAM prometheus |  [] |
| `<app_name>.ingress.prometheus.annotations` | Ingress annotations for IAM prometheus | {} |

## FAQ

### How to troubleshoot Pods that are missing os tools

- Use ephemeral containers to debug running or crashed Pods: [kubernetes.io/docs/tasks/debug-application-cluster/debug-running-pod](https://kubernetes.io/docs/tasks/debug-application-cluster/debug-running-pod/)

- Parent charts override the property values of child charts see:
[whmzsu.github.io/helm-doc-zh-cn/chart_template_guide/subcharts_and_globals-zh_cn.html](https://whmzsu.github.io/helm-doc-zh-cn/chart_template_guide/subcharts_and_globals-zh_cn.html)
