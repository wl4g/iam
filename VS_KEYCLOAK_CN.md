|                        Feature                     |  IAM  | KeyCloak  |
| ---------------------------------------------- | ------------------- | -------------- |
| oidc 1.0 支持  | √√  |  √√ |
| 为每个客户端独立配置(如: oidc access_token sign alg) | √√  | √√  |
| 默认支持国外主流 Identity Privoder |  √√ | √√  |
| 默认支持国内主流 Identity Privoder(如: Wechat) |  √√ | x  |
| 支持动态 Roles/socpes 映射  |  √√ | √√  |
| 支持精细化 Identity Privoder 请求代理(如: 仅连接到 github social 认证时走 http/socks proxy)  |  √√ | x  |
| 默认内嵌 H2 数据库 |  50% |  √√ |
| 默认支持 Redis Cluster 缓存  | √√  | x  |
| 默认支持 Captcha 附加验证  | √√  | ?  |
| 默认支持 Jigsaw 滑块附加验证  | √√  | x  |
| 默认支持 SMS 附加验证  | √√  | x  |
| 默认支持 Dict 爆破自动检测锁定  | √√  | √√  |
| 默认支持自动识别登录IP退出 (仅 FastCas 模式下 iam-server/iam-client-springboot 支持)  | √  | x  |
| 默认支持密码登录时双向 RSA/ECDSA 加密(如: 登录提交时是否将password字段明文在表单中传递)  | √√  | x  |
| 对 Spring 生态友好  |  √√ | x  |
| 支持 Gateway 部署(如: 对开放式业务 API 服务的认证/计费/限流/路由一站式管理) |  90% | x  |
| 支持 Docker 部署  |  √√ | √√  |
| 支持 Helm 部署  |  80% | x  |   |
| 支持 Kubernetes 部署  |  80% | √  |   |
| (高性能)支持基于 Spring Native 微服务分布式部署  | 50% | x  |   |
| 支持基于 Feign + Spring Boot 微服务分布式部署  | √√  | x  |
| 支持基于 Feign + Spring Boot + Istio 微服务分布式部署  | 80%  | x  |
| 支持基于 Feign + Spring Cloud 微服务分布式部署  | √√  | x  |
| 支持基于 Feign + Dubbo 微服务分布式部署  | 80%  | x  |
| 支持私有认证协议 (如: Fast Cas) |  √√ |  x |
| 支持基于 (Kafka/Pulsar/RocketMQ) + Flink (异常)事件实时分析告警 | 50%  | x  |
| saml2.0 支持  | x  |  √√ |
| ldap 支持 | x  |  √√ |
| kerberos 支持 | x  |  √√ |
| 前端统一入口（即, 当有多个外部客户端应用时，可以配置动态菜单将这些系统的前端页面也能整合在一起，打破了 IAM 仅做为授权的接口服务，这特别适用于企业级多系统统一认证整合） | √√  |  x |

- 说明：其中 `√` 表示支持；`√√` 表示支持完善；`x` 表示不支持；`?` 表示不确定是否支持；如 `80%` 表示已开发完善成度。
