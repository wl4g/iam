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

# see:https://gadm.org/download_country_v3.html
# see:https://gadm.org/download_country.html
# e.g:https://geodata.ucdavis.edu/gadm/gadm4.0/shp/gadm40_USA_shp.zip
# e.g:https://geodata.ucdavis.edu/gadm/gadm4.0/shp/gadm40_CHN_shp.zip
# e.g:https://geodata.ucdavis.edu/gadm/gadm4.0/shp/gadm40_JPN_shp.zip
# e.g:https://geodata.ucdavis.edu/gadm/gadm4.0/shp/gadm40_GBR_shp.zip
GEO_BASE_URI = "https://geodata.ucdavis.edu/gadm/gadm4.0/shp/"
DOWNLOAD_LEVEL_LE = 3  # By default: level<=3
CURRDIR = os.getcwd()
OUTPUT_DIR = CURRDIR + "/output/"
os.makedirs(OUTPUT_DIR, exist_ok=True)


def downLoad(base_uri, adcode, name):
    suffix = "gadm40_" + name[0:3].upper() + "_shp.zip"
    url = base_uri + suffix
    saveFilename = OUTPUT_DIR + suffix
    print('Fetching for %s/%s' % (adcode, name))
    try:
        urllib.request.urlretrieve(url, saveFilename)
    except Exception as e:
        print("Failed to fetch for %s. reason: %s" % (suffix, e))
        with open(saveFilename + ".err", "w", encoding="utf-8") as geofile:
            geofile.write(
                "ERROR: Failed to fetch for %s, caused by: %s" % (url, e))
            geofile.close


def download_all(base_uri, download_level):
    with open(CURRDIR + "/area_global.csv", "r", encoding="utf-8") as csvfile:
        greenlets = []
        batchs = 0
        reader = csv.reader(csvfile)
        headers = next(reader)
        for row in reader:
            adcode = row[0]
            name = row[1]
            # for testing
            # print("add task for '%s/%s'" % (adcode, name))
            # downLoad(base_uri, adcode, name)
            greenlets.append(gevent.spawn(downLoad, base_uri, adcode, name))
            if len(greenlets) % 10 == 0:
                batchs += 1
                print("[%d] Submit batch tasks ..." % (batchs))
                gevent.joinall(greenlets, timeout=30)


if __name__ == "__main__":
    print('Starting GADMGeoDataFetcher ...')
    try:
        os.nice(5)
        download_all(GEO_BASE_URI, DOWNLOAD_LEVEL_LE)
    except KeyboardInterrupt:
        print("Cancel fetch tasks ...")
        # gevent.killall()