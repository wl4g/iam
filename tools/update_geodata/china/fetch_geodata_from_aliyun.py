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

import urllib.request
from gevent import monkey
import gevent
from re import S
import csv
import os
import sys
import signal
monkey.patch_all()  # Required for time-consuming operations

# see:https://datav.aliyun.com/portal/school/atlas/area_selector
# e.g:https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json
# e.g:https://geo.datav.aliyun.com/areas_v3/bound/440000_full.json
# e.g:https://geo.datav.aliyun.com/areas_v3/bound/440100_full.json
# e.g:https://geo.datav.aliyun.com/areas_v3/bound/440118.json
GEO_BASE_URI = "https://geo.datav.aliyun.com/areas_v3/bound/"
DOWNLOAD_LEVEL_LE = 3  # By default: level<=3
CURRDIR = os.getcwd()
OUTPUT_DIR = CURRDIR + "/output/"
os.makedirs(OUTPUT_DIR, exist_ok=True)


def downLoad(base_uri, adcode, name, level):
    suffix = adcode + "_full.json"
    if int(level) >= 2:
        suffix = adcode + ".json"
    url = base_uri + suffix
    print('Downlaoding for %s/%s/%s' % (adcode, name, level))
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3941.4 Safari/537.36'
        }
        req = urllib.request.Request(url, headers=headers)
        resp = urllib.request.urlopen(req, timeout=5)
        if resp.status == 200:
            data = resp.read()
            with open(OUTPUT_DIR + suffix, "w", encoding="utf-8") as geofile:
                geofile.write(data.decode('UTF-8'))
                geofile.close
        else:
            raise RuntimeError("Response status %s is invalid" % resp.status)
    except Exception as e:
        print("Failed to fetch for %s. reason: %s" % (suffix, e))
        with open(OUTPUT_DIR + suffix + ".err", "w", encoding="utf-8") as geofile:
            geofile.write(
                "ERROR: Failed to fetch for %s, caused by: %s" % (url, e))
            geofile.close


def download_all(base_uri, download_level):
    with open(CURRDIR + "/area_cn.csv", "r", encoding="utf-8") as csvfile:
        greenlets = []
        batchs = 0
        reader = csv.reader(csvfile)
        headers = next(reader)
        for row in reader:
            adcode = row[0]
            parent = row[1]
            name = row[2]
            level = row[3]
            # print("add task for '%s/%s/%s/%s'" % (adcode, parent, name, level))
            if int(level) <= download_level:
                greenlets.append(gevent.spawn(
                    downLoad, base_uri, adcode, name, level))
            if len(greenlets) % 50 == 0:
                batchs += 1
                print("[%d] Submit batch tasks ..." % (batchs))
                gevent.joinall(greenlets, timeout=30)


if __name__ == "__main__":
    print('Starting AliyunGeoDataFetcher ...')
    try:
        os.nice(5)
        download_all(GEO_BASE_URI, DOWNLOAD_LEVEL_LE)
    except KeyboardInterrupt:
        print("Cancel fetch tasks ...")
        # gevent.killall()
