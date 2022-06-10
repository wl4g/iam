#!/bin/python3
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

import subprocess
import json
import random
import time
import urllib.request
from gevent import monkey
import gevent
from re import S
import csv
import os
import sys
import signal
import os
import sys


# see:https://datav.aliyun.com/portal/school/atlas/area_selector
# e.g:https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json
# e.g:https://geo.datav.aliyun.com/areas_v3/bound/440000_full.json
# e.g:https://geo.datav.aliyun.com/areas_v3/bound/440100_full.json
# e.g:https://geo.datav.aliyun.com/areas_v3/bound/440118.json
GEO_BASE_URI = "https://geo.datav.aliyun.com/areas_v3/bound/"
FOR_LEVEL_LE = 3  # By default: level<=3
INIT_DELAY = 3  # Initial delay seconds.
MAX_RETRIES = 3  # Max attempts with 403
BATCH_TASKS = 50  # Add batch tasks count.


os.environ['GEVENT_SUPPORT'] = 'True'
monkey.patch_all()  # Required for time-consuming operations


# current_dir = os.getcwd()
entrypoint_dir, entrypoint_file = os.path.split(
    os.path.abspath(sys.argv[0]))
output_dir = entrypoint_dir + "/output/"
os.makedirs(output_dir, exist_ok=True)


def do_fetch(url, outputFile, adcode, name, level):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3941.4 Safari/537.36'
    }
    req = urllib.request.Request(url, headers=headers)
    resp = urllib.request.urlopen(req, timeout=5)
    if resp.status == 200:
        data = resp.read()
        with open(outputFile, "w", encoding="utf-8") as geofile:
            geofile.write(data.decode('UTF-8'))
            geofile.close
    else:
        raise RuntimeError("Response status %s is invalid" % resp.status)
    return resp


def do_retries_fetch(adcode, name, level):
    suffix = adcode + "_full.json"
    if int(level) >= 2:
        suffix = adcode + ".json"
    url = GEO_BASE_URI + suffix
    outputFile = output_dir + suffix
    try:
        retries = 0
        while retries <= MAX_RETRIES:
            retries += 1
            backoff = random.random() * INIT_DELAY * retries
            time.sleep(backoff)  # Prevent request limiting.
            print("Fetching of delay %ds for %s/%s/%s ..." %
                  (backoff, adcode, name, level))
            resp = do_fetch(url, outputFile, adcode, name, level)
            if resp.status == 200:
                break
            elif resp.status == 403:  # Has been limiting?
                time.sleep(backoff)
    except Exception as e:
        print("Failed to fetch for %s/%s. reason: %s" % (name, suffix, e))
        with open(outputFile + ".err", "w", encoding="utf-8") as geofile:
            errjson = {
                "adcode": adcode,
                "name": name,
                "level": level,
                "errmsg": ("ERROR: Failed to fetch for %s, caused by: %s" % (url, e))
            }
            geofile.write(json.dumps(errjson))
            geofile.close


def fetch_all():
    files = os.listdir(output_dir)
    if len(files) <= 0:
        print("Full new fetching ...")
        with open(entrypoint_dir + "/area_cn.csv", "r", encoding="utf-8") as csvfile:
            greenlets = []
            batchs = 0
            reader = csv.reader(csvfile)
            headers = next(reader)
            for row in reader:
                adcode = row[0]
                # parent = row[1]
                name = row[2]
                level = row[3]

                # for testing.
                # print("add task for '%s/%s/%s/%s'" % (adcode, parent, name, level))
                # do_retries_fetch(adcode, name, level)

                if int(level) <= FOR_LEVEL_LE:
                    greenlets.append(gevent.spawn(
                        do_retries_fetch, adcode, name, level))
                if len(greenlets) % BATCH_TASKS == 0:
                    batchs += 1
                    print("[%d] Submit batch tasks ..." % (batchs))
                    gevent.joinall(greenlets, timeout=30)
    else:
        print("Continue last uncompleted or failed fetch tasks from '%s'" %
              (output_dir))
        greenlets = []
        batchs = 0
        for f in files:
            if f.endswith(".err"):
                errjsonStr = ""
                errfileStr = output_dir + "/" + f
                with open(errfileStr, "r", encoding="utf-8") as errfile:
                    while True:
                        line = errfile.readline(1024)
                        if len(line) <= 0:
                            break
                        errjsonStr += line

                errjson = json.loads(errjsonStr)
                adcode = errjson['adcode']
                name = errjson['name']
                level = errjson['level']

                # for testing.
                # print("add task for '%s/%s/%s/%s'" % (adcode, parent, name, level))
                # do_retries_fetch(adcode, name, level)

                # Continue add batch fetch tasks.
                greenlets.append(gevent.spawn(
                    do_retries_fetch, adcode, name, level))
                if len(greenlets) % BATCH_TASKS == 0:
                    batchs += 1
                    print("[%d] Submit batch tasks ..." % (batchs))
                    gevent.joinall(greenlets, timeout=30)
                # Remove older err json file.
                os.remove(errfileStr)


def statistics():
    success = 0
    failure = 0
    for f in os.listdir(output_dir):
        if f.endswith(".err"):
            failure += 1
        else:
            success += 1
    print("-----------------------------------------------")
    print("FETCHED Completed of SUCCESS: %d / FAILURE: %d" % (success, failure))
    print("-----------------------------------------------")


if __name__ == "__main__":
    print('Starting Aliyun GeoData Fetcher ...')
    try:
        os.nice(5)
        fetch_all()
        statistics()
    except KeyboardInterrupt:
        print("Cancel fetch tasks ...")
        # gevent.killall()
