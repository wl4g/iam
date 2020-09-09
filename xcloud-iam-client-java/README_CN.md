#### XCloud Iam Client Gateway

临时模块，主要基于 gateway 的 appsecret 机制安全校验，对于外系统调用 Open API 提供安全校验功能
此模块后续会与iam-client整合或迁移，最终目的是： 实现可通过依赖的形式集成到 gateway-server 上来提供认证功能，和以依赖的形式集成到具体 biz-app 上来提供认证功能