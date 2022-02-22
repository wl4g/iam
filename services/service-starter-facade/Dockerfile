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
## 1). Example:
##      export appName=myapp
##      export appActive=pro
##      export appPort=8080
##      export appDataDir="/mnt/disk1/${appName}"
##      export appLogDir="/mnt/disk1/log/${appName}"
##      #export javaToolOptions='-javaagent:/opt/javaagent/jmx_prometheus_javaagent-0.16.1.jar=10105:/opt/javaagent/jmx-metrics.yml'
##      #export javaOpts=""
##      #export javaHeapOpts='-Xms4G -Xmx4G -XX:MaxDirectMemorySize=4G'
##      #export javaDumpOpts='-XX:+HeapDumpOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow -XX:HeapDumpPath=${appLogDir}/jvm_dump.hprof'
##      #export javaGcOpts='-XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:+DisableExplicitGC -Xloggc:${appLogDir}/${appName}-gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M'
##      docker run -d \
##        -e APP_NOHUP=y \
##        -e APP_DEBUG=y \
##        -e APP_ACTIVE=${appActive} \
##        -e APP_PORT=${appPort} \
##        -e APP_DATA=${appDataDir} \
##        -e APP_LOG=${appLogDir} \
##        -e APP_OPTS=${appOpts} \
##        -e JAVA_TOOL_OPTIONS=${javaToolOptions} \
##        -e JAVA_OPTS=${javaOpts} \
##        -e JAVA_HEAP_OPTS=${javaHeapOpts} \
##        -e JAVA_DUMP_OPTS=${javaDumpOpts} \
##        -e JAVA_GC_OPTS=${javaGcOpts} \
##        -p ${appPort}:${appPort} \
##        -v ${appDataDir}:${appDataDir} \
##        -v ${appLogDir}:${appLogDir} \
##        --name=${appName} ${appName}:latest

# see:https://blogs.wl4g.com/archives/2969
FROM openjdk:8u212-jre-alpine3.9 AS wl4g_springboot_base
LABEL maintainer="Wanglsir<983708408@qq.com>, Wanglsir<Wanglsir@gmail.com>"

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
&& echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
&& apk update upgrade \
&& apk add --no-cache procps unzip curl bash tzdata \
&& ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

FROM wl4g_springboot_base
LABEL maintainer="Wanglsir<983708408@qq.com, Wanglsir@gmail.com>"

##
## Define the immutable properties.
##
ARG APP_NAME
ARG APP_VERSION
ARG APP_MAINCLASS
ARG APP_EXT_CLASSPATH # Optional
ENV APP_NAME "${APP_NAME}"
ENV APP_VERSION "${APP_VERSION}"
ENV APP_MAINCLASS "${APP_MAINCLASS}"
ENV APP_EXT_CLASSPATH "${APP_EXT_CLASSPATH}"
ENV APP_BIN_NAME "${APP_NAME}-${APP_VERSION}-bin"
ENV APP_HOME_PARENT "/opt/apps/ecm/${APP_NAME}-package"
ENV APP_HOME "${APP_HOME_PARENT}/${APP_BIN_NAME}"
ENV APP_HOME_LINK "${APP_HOME_PARENT}/${APP_NAME}-current"
ENV APP_DATA "${APP_HOME}/data"
ENV APP_LOG "${APP_HOME}/log"

##
## Installation APP files.
##
COPY target/${APP_BIN_NAME}.tar /tmp/${APP_BIN_NAME}.tar

RUN mkdir -p ${APP_HOME} ${APP_DATA} ${APP_LOG} && ln -snf ${APP_HOME} ${APP_HOME_LINK} \
&& tar -xf /tmp/${APP_BIN_NAME}.tar --strip-components=1 -C ${APP_HOME} \
&& rm -rf /tmp/${APP_BIN_NAME}.tar \
\
## Make control script.
&& touch /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh \
&& echo -e "#!/bin/bash" >>/docker-entrypoint.sh \
&& echo -e "\
\
## Define the mutable APP properties.\n\
export APP_NOHUP=\$(echo \$APP_NOHUP|tr '[a-z]' '[A-Z]')\n\
export APP_DEBUG=\$(echo \$APP_DEBUG|tr '[a-z]' '[A-Z]')\n\
export APP_NAME=\${APP_NAME:-${APP_NAME}}\n\
export APP_PORT=\${APP_PORT:-'<default>'}\n\
export APP_ACTIVE=\${APP_ACTIVE:-pro}\n\
export APP_OPTS=\${APP_OPTS:-}\n\
\
## Define the mutable JVM options.\n\
export JAVA_OPTS=\${JAVA_OPTS:-}\n\
\
export DEFAULT_JAVA_HEAP_OPTS=\"-XX:InitialRAMPercentage=80.0 -XX:MinRAMPercentage=80.0 -XX:MaxRAMPercentage=80.0\" \n\
export JAVA_HEAP_OPTS=\${JAVA_HEAP_OPTS:-\${DEFAULT_JAVA_HEAP_OPTS}}\n\
\
export DEFAULT_JAVA_DUMP_OPTS=\"-XX:+HeapDumpOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow -XX:HeapDumpPath=\${APP_LOG}/jvm_dump.hprof\" \n\
export JAVA_DUMP_OPTS=\${JAVA_DUMP_OPTS:-\${DEFAULT_JAVA_DUMP_OPTS}}\n\
\
export DEFAULT_JAVA_GC_OPTS=\"-XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:+DisableExplicitGC -Xloggc:\${APP_LOG}/\${APP_NAME}-gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M\" \n\
export JAVA_GC_OPTS=\${JAVA_GC_OPTS:-\${DEFAULT_JAVA_GC_OPTS}}\n\
\
export JAVA_TOOL_OPTIONS=\${JAVA_TOOL_OPTIONS:-}\n\
\
[ -n \"\$APP_NOHUP\" ] && export CMD_NOHUP='nohup' \n\
[ -n \"\$APP_DEBUG\" ] && export JVM_DEBUG_OPTS='-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n' \n\
\
[[ \$APP_PORT != '' && \$APP_PORT != '<default>' ]] && export APP_PORT_OPTS='--server.port=\${APP_PORT}'\n\
\
## Make run commands.\n\
export CMD_EXEC=\"\${CMD_NOHUP} java -server \${JAVA_OPTS} \${JAVA_HEAP_OPTS} \${JAVA_DUMP_OPTS} \${JAVA_GC_OPTS} \${JVM_DEBUG_OPTS} \
-Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -cp \
.:\${APP_HOME_LINK}/conf:\${APP_EXT_CLASSPATH}:\${APP_HOME_LINK}/ext-lib/*:\${APP_HOME_LINK}/lib/* \${APP_MAINCLASS} \
--spring.application.name=\${APP_NAME} \
--spring.profiles.active=\${APP_ACTIVE} \
\${APP_PORT_OPTS} \
--server.tomcat.basedir=\${APP_DATA} \
--logging.file.name=\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.log \${APP_OPTS} \
1>\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.stdout \
2>\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.stderr\"\n\
\
## Print run commands.\n\
[ -n \"\$APP_DEBUG\" ] && echo \$CMD_EXEC\n\
\
## Execution.\n\
echo \"Starting \${APP_NAME}:\${APP_VERSION}(\${APP_ACTIVE}) on \${APP_PORT} ...\"\n\
exec \${CMD_EXEC}\n\
echo \"Started \${APP_NAME}:\${APP_VERSION}(\${APP_ACTIVE}) on \${APP_PORT}\"\n" >>/docker-entrypoint.sh

##
## Using root-less run container. see:https://docs.docker.com/engine/security/rootless/
## see:https://stackoverflow.com/questions/49955097/how-do-i-add-a-user-when-im-using-alpine-as-a-base-image
##
RUN adduser ${APP_NAME} --disabled-password \
&& chown -R ${APP_NAME}:${APP_NAME} ${APP_HOME_PARENT} ${APP_DATA} ${APP_LOG} /docker-entrypoint.sh \
&& chmod -R 755 ${APP_HOME_PARENT} /docker-entrypoint.sh
USER ${APP_NAME}

##
## [BUGFIX]: Notice that if you use bash or sh to start the program, graceful termination cannot be achieved under kubernetes.
##
#CMD ["/bin/bash", "-c", "/docker-entrypoint.sh"]
CMD ["/docker-entrypoint.sh"]