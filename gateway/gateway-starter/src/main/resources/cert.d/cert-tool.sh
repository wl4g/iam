#!/bin/bash
#/*
# * Copyright 2017 ~ 2025 the original author or authors. <Wanglsir@gmail.com, 983708408@qq.com>
# *
# * Licensed under the Apache License, Version 2.0 (the "License");
# * you may not use this file except in compliance with the License.
# * You may obtain a copy of the License at
# *
# *      http://www.apache.org/licenses/LICENSE-2.0
# *
# * Unless required by applicable law or agreed to in writing, software
# * distributed under the License is distributed on an "AS IS" BASIS,
# * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# * See the License for the specific language governing permissions and
# * limitations under the License.
# * 
# * see: https://blogs.wl4g.com/archives/953
# * see: https://blogs.wl4g.com/archives/948
# */
set -e

export baseDir=$(cd "`dirname $0`"/ ; pwd)
export outputDir="${baseDir}"
export tmpDir="/tmp/cfssl_$(date +%s)" && mkdir -p ${tmpDir}

function makeConfig() {
  local certCommonName="$1"
cat <<-EOF > ${tmpDir}/config.json
{
  "signing": {
    "default": {
      "expiry": "8760h"
    },
    "profiles": {
      "${certCommonName}": {
        "usages": [
            "signing",
            "key encipherment",
            "server auth",
            "client auth"
        ],
        "expiry": "8760h"
      }
    }
  }
}
EOF
}

function makeCaCsr() {
cat <<-EOF > ${tmpDir}/ca-csr.json
{
    "CN": "WL4G Root Certificate Authority",
    "CA": {
        "expiry": "87600h",
        "pathlen": 0
    },
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
        {
            "C":  "US",
            "L":  "San Francisco 12th street",
            "O":  "WL4G Company, Inc.",
            "OU": "WWW Dept",
            "ST": "California"
        }
    ]
}
EOF
}

function makeCertCsr() {
  local certCommonName="$1"
  local certCommonNameUpper="$(echo $certCommonName | tr -t [a-z] [A-Z)"
  local certHosts="$2"
  local certHostsArr=(${certHosts//,/ })
  local hostArrStr="\"localhost\",\"127.0.0.1\",\"https://localhost\",\"https://127.0.0.1\""
  for host in ${certHostsArr[@]}
  do
    IFS="\:" # Set shell string separators.
    hostArrStr="${hostArrStr},\"${host}\",\"https://${host}\""
  done
cat <<-EOF > ${tmpDir}/${certCommonName}-csr.json
{
    "hosts": [
        ${hostArrStr}
    ],
    "CN": "${certCommonName}",
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
        {
            "C":  "CN",
            "L":  "GuangZhou TianHe 6th street",
            "O":  "${certCommonNameUpper} Company, Inc.",
            "OU": "WWW Dept",
            "ST": "GuangDong"
        }
    ]
}
EOF
}

function generateCA() {
  local certName="$1"
  makeConfig "$certName"
  makeCaCsr
  cfssl genkey -initca ${tmpDir}/ca-csr.json | cfssljson -bare ca
}

function generateCert() {
  local certCommonName="$1"
  local certHost="$2"
  # Check CA
  local caCert="${outputDir}/ca.pem"
  local caKey="${outputDir}/ca-key.pem"
  if [[ ! -f "$caCert" || ! -f "$caKey" ]]; then
    echo -e "ERROR: No found CA certificate: '${caCert}'\nCA certificate key: '${caKey}'" && exit 1
  fi
  # Generation
  makeConfig "$certCommonName"
  makeCertCsr "$certCommonName" "$certHost"
  echo ${tmpDir}/${certCommonName}-csr.json
  cfssl gencert \
    -loglevel=0 \
    -ca=${caCert} \
    -ca-key=${caKey} \
    -config=${tmpDir}/config.json \
    -profile=${certCommonName} ${tmpDir}/${certCommonName}-csr.json | cfssljson -bare ${certCommonName}
}

function usage() {
  echo "Simple Certificate Tools
By default generate to the output directory: ${outputDir}

Usage: ./$(basename $0) [OPTIONS] [arg1] [arg2]
       ca                                Generate the CA certificate. (E.g: ./$(basename $0) ca)
       cert      <commonName> <hosts>    Generate the certificate. (E.g: ./$(basename $0) cert example.io 192.168.88.2,10.88.8.5)
       describe  <certFile>              Print details certificate information. (E.g: ./$(basename $0) describe example.io.pem)
       pem-p12   <certFile>   <keyFile>  Transform PEM certificate to P12(PKCS12) format. (E.g: ./$(basename $0) pem-p12 example.io.pem example.io-key.pem)
       pem-cer   <certFile>              Transform PEM certificate to CER format. (E.g: ./$(basename $0) pem-cer example.io.pem)
       clean                             Clean up the works site(Remove all generated certificates and temporary files). (E.g: ./$(basename $0) clean)
"
exit 1
}

# ----- Main call. -----
arg1=$1
arg2=$2
arg3=$3
case $arg1 in
  help|-help|--help|-h)
    usage
  ;;
  ca)
    generateCA
  ;;
  cert)
    [ -z "$arg2" ] &&  echo "Bad arguments, missing client certificate common name" && usage
    [ -z "$arg3" ] &&  echo "Bad arguments, missing client certificate hosts" && usage
    generateCert "$arg2" "$arg3"
  ;;
  describe)
    [ -z "$arg2" ] &&  echo "Bad arguments, missing client certificate file" && usage
    openssl x509 -in ${arg2} -noout -text
  ;;
  pem-p12)
    [ -z "$arg2" ] &&  echo "Bad arguments, missing client PEM certificate file" && usage
    [ -z "$arg3" ] &&  echo "Bad arguments, missing client PEM certificate key file" && usage
    openssl pkcs12 -export -in ${arg2} -inkey ${arg3} -out "${arg2%.*}".p12
  ;;
  pem-cer)
    [ -z "$arg2" ] &&  echo "Bad arguments, missing client PEM certificate file" && usage
    openssl x509 -outform der -in ${arg2} -out "${arg2%.*}".cer
  ;;
  clean)
    rm -rf ${outputDir}/*.csr
    rm -rf ${outputDir}/*.pem
    rm -rf ${outputDir}/*.p12
    rm -rf ${outputDir}/*.cer
    rm -rf /tmp/cfssl_*
  ;;
  *)
    usage
  ;;
esac
