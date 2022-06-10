#!/bin/bash
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
set -e

## see:https://datav.aliyun.com/portal/school/atlas/area_selector
## e.g:https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json
## e.g:https://geo.datav.aliyun.com/areas_v3/bound/440000_full.json
## e.g:https://geo.datav.aliyun.com/areas_v3/bound/440100_full.json
## e.g:https://geo.datav.aliyun.com/areas_v3/bound/440118.json
export GEO_BASE_URL="https://geo.datav.aliyun.com/areas_v3/bound"

export BASE_DIR=$(echo "$(cd "`dirname "$0"`"/; pwd)")

## Import executor tools.
. ${BASE_DIR}/executor.sh

## Definitions.
export OUTPUT_DIR=$BASE_DIR/output && mkdir -p $OUTPUT_DIR
export DOWNLOAD_LEVEL_LE=3 ## By default: level<=3

function downloadAll() {
    # if [ -n "$(ls $OUTPUT_DIR)" ]; then
    #     echo "ERROR: directory for '$OUTPUT_DIR' not empty." && exit -1
    # fi
    cd $OUTPUT_DIR

    local batchTaskArguments=[]
    local count=0
    local index=0
    for line in `cat $BASE_DIR/../area_cn.csv`; do
      ((count+=1))
      if [[ $count == 1 || -n "$(echo $line|grep -E '^#')" ]]; then
        continue # Skip head or annotation rows.
      fi
      # Extract area info & trim
      local adcode=$(echo $line|awk -F ',' '{print $1}'|sed -e 's/^\s*//' -e 's/\s*$//')
      local parent=$(echo $line|awk -F ',' '{print $2}'|sed -e 's/^\s*//' -e 's/\s*$//')
      local name=$(echo $line|awk -F ',' '{print $3}'|sed -e 's/^\s*//' -e 's/\s*$//')
      local level=$(echo $line|awk -F ',' '{print $4}'|sed -e 's/^\s*//' -e 's/\s*$//')
      if [[ -z "$adcode" || -z "$name" || -z "$level" ]]; then
        echo "ERROR: Failed to download, there is an empty field area record for '$adcode/$name/$level'"; exit -1
      fi
      if [[ $level -le $DOWNLOAD_LEVEL_LE ]]; then
        local suffix="${adcode}_full.json"
        if [[ $level -ge 2 ]]; then
            suffix="${adcode}.json"
        fi
        # echo "Add task for $adcode/$name/$level ..."
        batchTaskArguments[index]="$suffix"
        ((index+=1))

        ## Batch tasks.
        if [[ $(($index % 100)) == 0 ]]; then
          local batchTaskSize="${#batchTaskArguments[@]}"
          echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] [idx-$index] - submit batch task:\n${batchTaskArguments[*]}"
          execute "doDownload" "10" "$batchTaskSize" "$index" "${batchTaskArguments[*]}" &

          ## Clear batch arguments.
          unset batchTaskArguments
        fi
      fi
    done
}

function doDownload() {
    local taskId="$1"
    local index="$2"
    local suffixList="$3"
    for ((i=0;i<${#suffixList[@]};i++)); do
      local suffix=${suffixList[i]}
      sleep $(($RANDOM/9731))  ## Prevent request limiting
      echo "[$(date '+%Y-%m-%d %H:%M:%S')] [tid-$taskId/idx-$index] - downloading for $suffix ..."
      curl -sLO "${GEO_BASE_URL}/${suffix}"
    done
}

## --- Main. ---
downloadAll
