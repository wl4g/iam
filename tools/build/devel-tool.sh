#!/bin/bash
# Copyright 2017 ~ 2025 the original authors <jameswong1376@gmail.com, 983708408@qq.com>. 
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

export BASE_DIR=$(cd "`dirname $0`"/../../; pwd)

function print_help() {
  echo $"
Usage: ./$(basename $0) [OPTIONS] [arg1] [arg2] ...
    build                                            Build of projects.
          parent                                     Build from the parent module.
                 --with-runtime-feign-istio          Build packages that only support the fegin-istio runtime mode.
                 --with-runtime-feign-springcloud    Build packages that only support the fegin-springcloud runtime mode.
                 --with-runtime-feign-dubbo          Build packages that only support the fegin-dubbo runtime mode.
          images                                     Build from the server starters images.
"
}

function build_parent() {
  echo "Building of $BASE_DIR/"
  local runtimeMode="build:framework:feign-istio"
  case $1 in
    --with-runtime-feign-istio)
      runtimeMode="build:framework:feign-istio"
    ;;
    --with-runtime-feign-springcloud)
      runtimeMode="build:framework:feign-springcloud"
    ;;
    --with-runtime-feign-dubbo)
      runtimeMode="build:framework:feign-dubbo"
    ;;
  esac
  mvn clean install -DskipTests -T 2C -P ${runtimeMode} -f $BASE_DIR/
}

function push_images() {
  local appVersion=$(cat $BASE_DIR/pom.xml | grep -E '<version>(v|V)*([0-9]+)\.([0-9]+)\.([0-9]+)(-[a-zA-Z0-9])*</version>')

  echo "Tagging images ..."
  docker tag wl4g/iam-web:${appVersion} wl4g/iam-web
  docker tag wl4g/iam-facade:${appVersion} wl4g/iam-facade
  docker tag wl4g/iam-data:${appVersion} wl4g/iam-data
  docker tag wl4g/iam-gateway:${appVersion} wl4g/iam-gateway

  echo "Pushing images of ${appVersion} ..."
  docker push wl4g/iam-web:${appVersion} &
  docker push wl4g/iam-facade:${appVersion} &
  docker push wl4g/iam-data:${appVersion} &
  docker push wl4g/iam-gateway:${appVersion} &

  echo "Pushing images of latest ..."
  docker push wl4g/iam-web &
  docker push wl4g/iam-facade &
  docker push wl4g/iam-data &
  docker push wl4g/iam-gateway &
  wait
}

function build_images() {
  build_parent

  echo "Docker building of $BASE_DIR/server/server-start-web/"
  mvn clean install -DskipTests -Pbuild:tar:docker -f $BASE_DIR/server/server-starter-web/ &

  echo "Docker building of $BASE_DIR/server/server-start-facade/"
  mvn clean install -DskipTests -Pbuild:tar:docker -f $BASE_DIR/server/server-starter-facade/ &

  echo "Docker building of $BASE_DIR/server/server-start-data/"
  mvn clean install -DskipTests -Pbuild:tar:docker -f $BASE_DIR/server/server-starter-data/ &

  echo "Docker building of $BASE_DIR/server/server-start-gateway/"
  mvn clean install -DskipTests -Pbuild:tar:docker -f $BASE_DIR/server/server-starter-gateway/ &
  wait
}

# --- Main. ---
case $1 in
  build)
    case $2 in
      --parent|-p)
        build_parent "$3"
      ;;
      --servers|-s)
        build_images
      ;;
      *)
        print_help
    esac
    ;;
  *)
    print_help
  exit 2
esac
