# Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
## 1). Run container for example:
##      export APP_NAME=myapp
##      export APP_ACTIVE=pro
##      export APP_PORT=8080
##      export APP_DATA=/mnt/disk1/${APP_NAME}
##      export APP_LOG=/mnt/disk1/log/${APP_NAME}
##      export JAVA_OPTS='-Xms1G -Xmx1G'
##      #export JAVA_TOOL_OPTIONS='-javaagent:/opt/apps/ecm/javaagent/jmx_prometheus_javaagent-0.16.1.jar=10105:/opt/apps/ecm/javaagent/jmx-metrics.yml'
##      docker run -d \
##        -e=APP_NOHUP=y \
##        -e=APP_DEBUG=y \
##        -e=APP_PORT=${APP_PORT} \
##        -e=APP_DATA=${APP_DATA} \
##        -e=APP_LOG=${APP_LOG} \
##        -p=${APP_PORT}:${APP_PORT} \
##        -v=${APP_DATA}:${APP_DATA} \
##        -v=${APP_LOG}:${APP_LOG} \
##        --name=${APP_NAME} ${APP_NAME}:latest /bin/sh

FROM openjdk:8-jre-alpine
LABEL maintainer="Wanglsir<983708408@qq.com>, Wanglsir<Wanglsir@gmail.com>"

ARG APP_NAME
ARG APP_VERSION
ARG APP_MAINCLASS
ARG APP_CLASSPATH # Optional
ENV APP_BIN_NAME "${APP_NAME}-${APP_VERSION}-bin"
ENV APP_HOME_PARENT "/opt/apps/ecm/${APP_NAME}-package/"
ENV APP_HOME "${APP_HOME_PARENT}/${APP_BIN_NAME}"

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
&& echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
&& apk update upgrade \
&& apk add --no-cache procps unzip curl bash tzdata \
&& ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

COPY target/${APP_BIN_NAME}.tar /tmp/${APP_BIN_NAME}.tar

RUN mkdir -p ${APP_HOME_PARENT}
RUN tar -xf /tmp/${APP_BIN_NAME}.tar -C ${APP_HOME_PARENT} \
&& rm -rf /tmp/${APP_BIN_NAME}.tar

# Make script.
RUN touch /start.sh && chmod +x /start.sh
RUN echo -e '#!/bin/bash' >>/start.sh
RUN echo -e "export APP_NAME=${APP_NAME}" >>/start.sh
# Support configurable environments.
RUN echo -e "export APP_ACTIVE=\${APP_ACTIVE:-pro}" >>/start.sh
RUN echo -e "export APP_DATA=\${APP_DATA:-/mnt/disk1/\$APP_NAME}" >>/start.sh
RUN echo -e "export APP_LOG=\${APP_LOG:-/mnt/disk1/log/\$APP_NAME}" >>/start.sh
RUN echo -e "export APP_PORT=\${APP_PORT:-}" >>/start.sh
# Standard JVM options, for production environment.
RUN echo -e "export JAVA_OPTS=\${JAVA_OPTS:-'-Xms256M -Xmx1G'}" >>/start.sh
RUN echo -e "export JAVA_TOOL_OPTIONS=\${JAVA_TOOL_OPTIONS:-}" >>/start.sh
RUN echo -e "[[ -n \"\$APP_PORT\" && \"\$APP_PORT\" != \"defaultPort\" ]] && export APP_PORT_OPTS=\${APP_PORT}" >>/start.sh
RUN echo -e "[ -z \"\$APP_NOHUP\" ] && export CMD_NOHUP=\"nohup\"" >>/start.sh
RUN echo -e "[ -n \"\$APP_DEBUG\" ] && export DEBUG_OPTS=\"-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n\"" >>/start.sh
RUN echo -e "echo \"Starting \${APP_NAME}:${APP_VERSION} on \${APP_PORT}:\${APP_ACTIVE} ... \"\n" >>/start.sh
RUN echo -e "mkdir -p \${APP_DATA} && mkdir -p \${APP_LOG}\n" >>/start.sh

# Startup commands.
RUN echo -e "cmdRun=\"\${CMD_NOHUP} java -server \${DEBUG_OPTS} \${JAVA_OPTS} -XX:HeapDumpPath=\${APP_LOG}/jvm_dump.hprof \
-XX:InitiatingHeapOccupancyPercent=35 -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError \
-XX:-OmitStackTraceInFastThrow -XX:+UseG1GC -XX:MaxGCPauseMillis=20 \
-Xloggc:\${APP_LOG}/\${APP_NAME}-gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps \
-XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M \
-Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -cp \
.:${APP_HOME}/conf:${APP_CLASSPATH}:${APP_HOME}/ext-lib/*:${APP_HOME}/lib/* ${APP_MAINCLASS} \
\${APP_PORT_OPTS} \
--server.tomcat.basedir=\${APP_DATA} \
--logging.file.name=\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.log \
--spring.application.name=\${APP_NAME} \
--spring.profiles.active=\${APP_ACTIVE} \
1>\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.stdout \
2>\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.stderr\"" >>/start.sh

# Print. (if debug mode)
RUN echo -e "\n[ -n \"\$APP_DEBUG\" ] && echo \$cmdRun\n" >>/start.sh

# Execute.
RUN echo -e "exec \${cmdRun}" >>/start.sh
RUN echo -e "\necho \"Started \${APP_NAME}:${APP_VERSION} on \${APP_PORT}:\${APP_ACTIVE} \"\n" >>/start.sh

#EXPOSE ${APP_PORT} # Mapping ports when delegating to a start container.
CMD ["/bin/bash", "-c", "/start.sh"]
