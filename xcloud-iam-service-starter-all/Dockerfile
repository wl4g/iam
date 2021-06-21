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
### 1). Manual copy target/${APP_NAME}-${APP_VERSION}.tar to current directory.
### 2). Manual build example:
###      docker build -t com.wl4g/${APP_NAME}:${APP_VERSION} .
### 3). Manual run example:
###      docker run -d -p=${APP_PORT}:${APP_PORT} \
###        -e=APP_DEBUG=y
###        -e=APP_DATA=/mnt/disk1/${APP_NAME}
###        -e=APP_LOG=/mnt/disk1/log/${APP_NAME}
###        -v=/mnt/disk1/log/${APP_NAME}:/mnt/disk1/log/${APP_NAME} \
###        -v=/mnt/disk1/${APP_NAME}:/mnt/disk1/${APP_NAME} \
###        --add-host="peer1 peer2 peer3:10.0.0.114" \
###        --name=${APP_NAME} ${APP_NAME}:${APP_VERSION}

FROM openjdk:8-jre-alpine
LABEL maintainer="Wanglsir<983708408@qq.com>, Wanglsir<Wanglsir@gmail.com>"

ARG APP_NAME
ARG APP_VERSION
ARG APP_MAINCLASS
ARG APP_PORT
ENV APP_NAME "${APP_NAME}"
ENV APP_BIN_NAME "${APP_NAME}-${APP_VERSION}-bin"
ENV APP_HOME_PARENT "/opt/apps/ecm/${APP_NAME}-package/"
ENV APP_HOME "${APP_HOME_PARENT}/${APP_BIN_NAME}"
ENV APP_DATA "/mnt/disk1/${APP_NAME}"
ENV APP_LOG "/mnt/disk1/log/${APP_NAME}"

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
&& echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
&& apk update upgrade \
&& apk add --no-cache procps unzip curl bash tzdata ttf-dejavu fontconfig \
&& ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

COPY target/${APP_BIN_NAME}.tar /tmp/${APP_BIN_NAME}.tar

RUN mkdir -p ${APP_HOME_PARENT}
RUN tar -xf /tmp/${APP_BIN_NAME}.tar -C ${APP_HOME_PARENT} \
&& rm -rf /tmp/${APP_BIN_NAME}.tar

# Make startup script.
RUN touch /start.sh && chmod +x /start.sh
RUN echo -e '#!/bin/bash' >>/start.sh
RUN echo -e "[ -z \$APP_ACTIVE ] && export APP_ACTIVE=pro" >>/start.sh
RUN echo -e "[ -z \$APP_PORT ] && export APP_PORT=${APP_PORT}" >>/start.sh
RUN echo -e "[ -z \$APP_DATA ] && export APP_DATA=${APP_DATA}" >>/start.sh
RUN echo -e "[ -z \$APP_LOG ] && export APP_LOG=${APP_LOG}" >>/start.sh
RUN echo -e "[ -z \$APP_DEBUG ] && export DEBUG_OPTS=\"-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n\"" >>/start.sh
RUN echo -e "[ -z \$APP_DEBUG ] && export NOHUP=\"nohup\"" >>/start.sh
RUN echo -e "echo \"Starting ${APP_NAME}:${APP_VERSION} on \${APP_PORT}:\${APP_ACTIVE} ... \"\n" >>/start.sh
RUN echo -e "mkdir -p \${APP_DATA} && mkdir -p \${APP_LOG}\n" >>/start.sh

# Make startup commands.
RUN echo -e "cmdRun=\"\${NOHUP} java -server \${DEBUG_OPTS} -Xms256M -Xmx1G -XX:+UseG1GC -XX:MaxGCPauseMillis=20 \
-XX:InitiatingHeapOccupancyPercent=35 -XX:+DisableExplicitGC -Djava.awt.headless=true -XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=\${APP_LOG}/jvm_dump.hprof -XX:-OmitStackTraceInFastThrow \
-Xloggc:\${APP_LOG}/${APP_NAME}-gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps \
-XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M \
-Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -cp \
.:${APP_HOME}/conf:${APP_HOME}/ext-lib/*:${APP_HOME}/lib/* ${APP_MAINCLASS} \
--logging.file.name=\${APP_LOG}/${APP_NAME}_\${APP_ACTIVE}.log \
--server.port=\${APP_PORT} \
--server.tomcat.basedir=\${APP_DATA} \
--spring.application.name=${APP_NAME} \
--spring.profiles.active=\${APP_ACTIVE} \
1>\${APP_LOG}/${APP_NAME}_\${APP_ACTIVE}.stdout \
2>\${APP_LOG}/${APP_NAME}_\${APP_ACTIVE}.stderr\"" >>/start.sh

# Print startup info. (if debug mode)
RUN echo -e "\n[ -n \$APP_DEBUG ] && echo \$cmdRun\n" >>/start.sh

# Execute startup.
RUN echo -e "exec \${cmdRun}" >>/start.sh

RUN echo -e "\necho \"Started ${APP_NAME}:${APP_VERSION} on \${APP_PORT}:\${APP_ACTIVE} \"\n" >>/start.sh

EXPOSE ${APP_PORT}

CMD ["/bin/bash", "-c", "/start.sh"]
