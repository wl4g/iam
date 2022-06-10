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
# */
set -e

[ -n "$XDEBUG" ] && export XDEBUG="$(echo $XDEBUG | tr '[a-z]' '[A-Z]')"

## The universal asynchronous concurrency executor based on pure shell edition.
## see:https://blogs.wl4g.com/archives/292
function execute() {
  ## Extract arguments.
  local arguments="$@"
  local taskFunc=$(echo $arguments|awk -F ' ' '{print $1}')
  local concurrency=$(echo $arguments|awk -F ' ' '{print $2}')
  local taskCount=$(echo $arguments|awk -F ' ' '{print $3}') ## Total tasks count.
  local taskFuncArgument=$(echo $arguments|awk -F ' ' '{for(i=1; i<=3; i++){ $i="" }; print $0}')
  taskFuncArgument=$(echo $taskFuncArgument|awk '{gsub(/^\s+|\s+$/, "");print}') ## trim first blank character.

  ## Check options valid.
  if [[ -z "$taskFunc" || -z "$concurrency" || -z "$taskCount" ]]; then
    echo "ERROR: Bad arguments, {taskFunc,concurrency,taskCount} is required!"
    exit -1
  fi
  if [[ "$concurrency" -le 0 ]]; then
    echo "ERROR: Bad arguments, concurrency>=1 is required, but concurrency=$concurrency, taskCount=$taskCount"
    exit -1
  fi

  if [ "$XDEBUG" == "Y" ]; then
    echo "$(date '+%Y-%m-%d %H:%M:%S') [DEBUG] - call '${taskFunc}', concurrency=${concurrency}, argument=\"${taskFuncArgument}\" ..."
  fi

  ## Internal definitions.
  local pfile="/tmp/shell-executor.fifo"
  ## 注:使用函数'$RANDOM'变量当CPU使用高时, 也可能由于随机源不足而生成重复值, 这将引发一系列异步问题.(如,执行过程中意外挂起永不退出)
  local pfileFD=$(($RANDOM/31))

  [ ! -p "$pfile" ] && mkfifo $pfile
  eval "exec $pfileFD<>$pfile"
  rm -f $pfile

  ## Initial concurrency. That add the same number of newlines as concurrency to the FD file,
  ## for subsequent reading of newlines to control the number of concurrencys. (echo defaults
  ## to standard output newlines)
  eval "for ((i=0;i<${concurrency};i++)); do echo ; done >&${pfileFD}"

  ## Start an asynchronous process to execute a task.
  for i in `seq 1 $taskCount`; do
    eval "read -u${pfileFD}" ## Read (take out) one element at a time (fifo queue)
    {
      ## Asynchronously, call the function that actually performs the task.
      [ "$XDEBUG" == "Y" ] && echo "$(date '+%Y-%m-%d %H:%M:%S') [DEBUG] - call:${i} '$taskFunc' ..."
      eval "$taskFunc $i $taskFuncArgument"
      ## Every time a task is executed, a newline is added, the purpose is to keep the concurrency
      ## flags in the fd forever until the task is executed.
      eval "echo >&${pfileFD}"
    } &
  done
#   wait ## Wait for all tasks to complete, otherwise the main process will exit immediately.
  eval "exec ${pfileFD}>&-" ## close fd (pipe file)
}

##
## ------- for Testing --------
##

# ## testing main function.
# function testExecute() {
#   execute "testRun" "5" "20" "$@"
#   exit 0
# }

# ## testing run function.
# function testRun() {
#   local taskId=$1
#   echo "$(date '+%Y-%m-%d %H:%M:%S') - task $taskId is running, input arguments: $@ ..."
#   sleep 2
#   echo "$(date '+%Y-%m-%d %H:%M:%S') - task $taskId completed, input arguments: $@"
# }

# ## --- Main. ---
# testExecute "value1" "value2" "value3" "value4"
