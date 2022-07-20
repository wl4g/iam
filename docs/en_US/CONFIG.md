# IAM Security Configuration

## 支持完整配置项

### iam-web full configuration

```yaml
spring:
  cloud:
    devops:
      iam: # IAM configuration.
        #login-uri: /view/login.html
        login-uri: http://localhost:8080/#/login # Default, see:super-devops-view
        unauthorized-uri: /view/403.html
        success-endpoint: ${spring.application.name}@/view/index.html # Default
        acl:
          secure: false # Turn off protection will trust any same intranet IP.
          allowIpRange: ${DEVOPS_IAM_ACL_ALLOW:127.0.0.1}
          denyIpRange: ${DEVOPS_IAM_ACL_DENY}
        param: # Must be consistent with the client, otherwise authentication will never succeed
          sid: __sid
          sid-save-cookie: __cookie
          logout-forced: forced
          application: service
          grant-ticket: st
          response-type: response_type
          redirect-url: redirect_url
          which: which
          state: state
          refreshUrl: refresh_url
          agent: agent
          authorizers: authorizers
          second-auth-code: secondAuthCode
          funcId: function
          i18n-lang: lang
        matcher:
          fail-fast-match-max-attempts: 10
          fail-fast-match-delay: 3600000
          enabled-captcha-max-attempts: 3
          fail-fast-captcha-max-attempts: 20
          fail-fast-captcha-delay: 600000
          captcha-expire-ms: 60000
          fail-fast-sms-max-attempts: 3
          fail-fast-sms-max-delay: 1800000
          fail-fast-sms-delay: 90000
          sms-expire-ms: 300000
        cache:
          prefix: iam_
        session:
          global-session-timeout: 1500000
          session-validation-interval: 1500000
        cookie:
          name: IAMTOKEN_TGC
        captcha:
          #jigsaw:
            #source-dir: ${server.tomcat.basedir}/data/jigsaw-maternal
        sns: # SNS configuration.
          oauth2-connect-expire-ms: 60_000 # oauth2 connect processing expire time
          wechat-mp:
            app-id: yourappid
            app-secret: yoursecret
            redirect-url: https://iam.${DEVOPS_SERVICE_ZONE:wl4g.com}${server.contextPath}/sns/wechatmp/callback
          wechat:
            app-id: yourappid
            app-secret: yoursecret
            redirect-url: http://passport.wl4g.com${server.contextPath}/sns/wechat/callback
            #href: https://cdn.wl4g.com/static/css/wx.css
          qq:
            app-id: 101542056
            app-secret: yoursecret
            redirect-url: http://passport.wl4g.com${server.contextPath}/sns/qq/callback
```

### iam-client full configuration

```yaml
spring:
  cloud:
    devops:
      iam: # IAM client configuration.
        acl:
          secure: false # Turn off protection will trust any same intranet IP.
          allowIpRange: 127.0.0.1
          denyIpRange: 
        client: # IAM client configuration.
          service-name: ${spring.application.name}
          # Authentication center api base uri
          base-uri: http://localhost:14040/devops-iam
          login-uri: ${spring.cloud.devops.iam.client.base-uri}/view/login.html
          success-uri: http://localhost:${server.port}${server.contextPath}/index.html
          unauthorized-uri: ${spring.cloud.devops.iam.client.base-uri}/view/403.html
          use-remember-redirect: false
          filter-chain:
            /public/**: anon # Public rule release
          param:
            # Must be consistent with the server, otherwise authentication will never succeed
            sid: __sid
            sid-save-cookie: __cookie
            logout-forced: forced
            application: service
            grant-ticket: st
            response-type: response_type
            redirect-url: redirect_url
            which: which
            state: state
            refreshUrl: refresh_url
            agent: agent
            authorizers: authorizers
            second-auth-code: secondAuthCode
            funcId: function
          cache:
            prefix: ${spring.application.name}
          session:
            global-session-timeout: 1500000
            session-validation-interval: 1500000
          cookie:
            name: IAMTOKEN_${spring.application.name}
```
