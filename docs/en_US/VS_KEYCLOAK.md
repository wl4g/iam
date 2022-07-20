# VS Keycloak

|                        Feature                     |  IAM  | KeyCloak  |
| ---------------------------------------------- | ------------------- | -------------- |
| oidc 1.0 support | √√ | √√ |
| Independent configuration for each client (eg: oidc access_token sign alg) | √√ | √√ |
| Support foreign mainstream Identity Privoder by default | √√ | √√ |
| Default supports domestic mainstream Identity Privoder (eg: Wechat) | √√ | x |
| Support dynamic Roles/socpes mapping | √√ | √√ |
| Support refined Identity Privoder request proxy (eg: http/socks proxy only when connecting to github social authentication) | √√ | x |
| Default embedded H2 database | 50% | √√ |
| Support Redis Cluster cache by default | √√ | x |
| Support Captcha additional authentication by default | √√ | ? |
| Support Jigsaw slider additional validation by default | √√ | x |
| Support SMS additional authentication by default | √√ | x |
| By default, Dict blasting is automatically detected and locked | √√ | √√ |
| By default, it supports automatic identification of login IP and exit (only supported by iam-server/iam-client-springboot in FastCas mode) | √ | x |
| By default, two-way RSA/ECDSA encryption is supported during password login (eg: whether to pass the password field in plain text in the form when logging in and submit) | √√ | x |
| Eco friendly to Spring | √√ | x |
| Support Gateway deployment (eg: one-stop management of authentication/billing/current limiting/routing for open business API services) | 90% | x |
| Support Docker deployment | √√ | √√ |
| Supports Helm deployment | 80% | x | |
| Support Kubernetes deployment | 80% | √ | |
| (High performance) Support distributed deployment based on Spring Native microservices | 50% | x | |
| Support distributed deployment of microservices based on Feign + Spring Boot | √√ | x |
| Support distributed deployment of microservices based on Feign + Spring Boot + Istio | 80% | x |
| Support distributed deployment of microservices based on Feign + Spring Cloud | √√ | x |
| Support distributed deployment based on Feign + Dubbo microservices | 80% | x |
| Support private authentication protocol (eg: Fast Cas) | √√ | x |
| Support real-time alarm analysis based on (Kafka/Pulsar/RocketMQ) + Flink (abnormal) events | 50% | x |
| saml2.0 support | x | √√ |
| ldap support | x | √√ |
| kerberos support | x | √√ |
| Front-end unified entry (that is, when there are multiple external client applications, dynamic menus can be configured to integrate the front-end pages of these systems, breaking IAM as an authorized interface service, which is especially suitable for enterprise-level applications Unified authentication and integration of multiple systems) | √√ | x |

- Description: `√` means support; `√√` means complete support; `x` means no support; `?` means not sure whether it is supported; for example, `80%` means it has been developed to a complete degree.
