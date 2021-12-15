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
## 1). for example:
##      export appName=myapp
##      export appActive=pro
##      export appPort=8080
##      export appDataDir="/mnt/disk1/${APP_NAME}"
##      export appLogDir="/mnt/disk1/log/${APP_NAME}""
##      #export appOpts='--server.servlet.contextPath=/myapp'
##      #export javaOpts='-Xms1G -Xmx1G'
##      #export javaToolOptions='-javaagent:/opt/javaagent/jmx_prometheus_javaagent-0.16.1.jar=10105:/opt/javaagent/jmx-metrics.yml'
##      docker run -d \
##        -e APP_NOHUP=y \
##        -e APP_DEBUG=y \
##        -e APP_ACTIVE=${appActive} \
##        -e APP_PORT=${appPort} \
##        -e APP_DATA=${appDataDir} \
##        -e APP_LOG=${appLogDir} \
##        -e APP_OPTS=${appOpts} \
##        -e JAVA_OPTS=${javaOpts} \
##        -e JAVA_TOOL_OPTIONS=${javaToolOptions} \
##        -p ${appPort}:${appPort} \
##        -v ${appDataDir}:${appDataDir} \
##        -v ${appLogDir}:${appLogDir} \
##        --name=${appName} ${appName}:latest

FROM openjdk:8-jre-alpine AS wl4g_base
LABEL maintainer="Wanglsir<983708408@qq.com>, Wanglsir<Wanglsir@gmail.com>"

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
&& echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
&& apk update upgrade \
&& apk add --no-cache procps unzip curl bash tzdata ttf-dejavu fontconfig \
&& ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

FROM wl4g_base
LABEL maintainer="Wanglsir<983708408@qq.com>, Wanglsir<Wanglsir@gmail.com>"

ARG APP_NAME
ARG APP_VERSION
ARG APP_MAINCLASS
ARG APP_EXT_CLASSPATH # Optional
ENV APP_BIN_NAME "${APP_NAME}-${APP_VERSION}-bin"
ENV APP_HOME_PARENT "/opt/apps/ecm/${APP_NAME}-package/"
ENV APP_HOME "${APP_HOME_PARENT}/${APP_BIN_NAME}"

# Installcation app files.
COPY target/${APP_BIN_NAME}.tar /tmp/${APP_BIN_NAME}.tar

RUN mkdir -p ${APP_HOME_PARENT} \
&& tar -xf /tmp/${APP_BIN_NAME}.tar -C ${APP_HOME_PARENT} \
&& rm -rf /tmp/${APP_BIN_NAME}.tar \
# Make control script.
&& touch /startup.sh && chmod +x /startup.sh \
&& echo -e "#!/bin/bash" >>/startup.sh \
&& echo -e "\
# Define app basic the immutable properties.\n\
export APP_NAME=${APP_NAME:-}\n\
export APP_VERSION=${APP_VERSION:-}\n\
export APP_MAINCLASS=${APP_MAINCLASS:-}\n\
export APP_EXT_CLASSPATH=${APP_EXT_CLASSPATH:-}\n\
# Define app basic the mutable properties.\n\
export APP_NOHUP=\$(echo \$APP_NOHUP|tr '[a-z]' '[A-Z]')\n\
export APP_DEBUG=\$(echo \$APP_DEBUG|tr '[a-z]' '[A-Z]')\n\
export APP_PORT=\${APP_PORT:-'<defaultPort>'}\n\
export APP_ACTIVE=\${APP_ACTIVE:-pro}\n\
export APP_DATA=\${APP_DATA:-/mnt/disk1/\$APP_NAME}\n\
export APP_LOG=\${APP_LOG:-/mnt/disk1/log/\$APP_NAME}\n\
export APP_OPTS=\${APP_OPTS:-}\n\
# Define JVM options.\n\
export JAVA_OPTS=\${JAVA_OPTS:-'-Xms256M -Xmx1G'}\n\
export JAVA_TOOL_OPTIONS=\${JAVA_TOOL_OPTIONS:-}\n\
[[ \$APP_NOHUP == 'TRUE' || \$APP_NOHUP == 'YES' || \$APP_NOHUP == 'Y' || \$APP_NOHUP == 'ON' ]] && export CMD_NOHUP='nohup'\n\
[[ \$APP_DEBUG == 'TRUE' || \$APP_DEBUG == 'YES' || \$APP_DEBUG == 'Y' || \$APP_DEBUG == 'ON' ]] && export APP_DEBUG_OPTS='-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n'\n\
[[ \$APP_PORT != '' && \$APP_PORT != '<defaultPort>' ]] && export APP_PORT_OPTS='--server.port=\${APP_PORT}'\n\
# Initialization.\n\
mkdir -p \${APP_DATA} && mkdir -p \${APP_LOG}\n\
# Make run commands.\n\
export CMD_EXEC=\"\${CMD_NOHUP} java -server \${APP_DEBUG_OPTS} \${JAVA_OPTS} -XX:HeapDumpPath=\${APP_LOG}/jvm_dump.hprof \
-XX:InitiatingHeapOccupancyPercent=35 -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError \
-XX:-OmitStackTraceInFastThrow -XX:+UseG1GC -XX:MaxGCPauseMillis=20 \
-Xloggc:\${APP_LOG}/\${APP_NAME}-gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps \
-XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M \
-Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -cp \
.:\${APP_HOME}/conf:\${APP_EXT_CLASSPATH}:\${APP_HOME}/ext-lib/*:\${APP_HOME}/lib/* \${APP_MAINCLASS} \
--spring.application.name=\${APP_NAME} \
--spring.profiles.active=\${APP_ACTIVE} \
\${APP_PORT_OPTS} \
--server.tomcat.basedir=\${APP_DATA} \
--logging.file.name=\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.log \${APP_OPTS} \
1>\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.stdout \
2>\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.stderr\"\n\
# Print run commands. (if debugging enabled)\n\
[ -n \$APP_DEBUG ] && echo \$CMD_EXEC\n\
# Execution.\n\
echo \"Starting \${APP_NAME}:\${APP_VERSION}(\${APP_ACTIVE}) on \${APP_PORT} ...\"\n\
exec \${CMD_EXEC}\n\
echo \"Started \${APP_NAME}:\${APP_VERSION}(\${APP_ACTIVE}) on \${APP_PORT}\"\n" >>/startup.sh

#EXPOSE ${APP_PORT} # Mapping ports when delegating to a start container.

# [FIX]: Notice that if you use bash or sh to start the program, graceful termination cannot be achieved under kubernetes.
#CMD ["/bin/bash", "-c", "/startup.sh"]
CMD ["/startup.sh"]