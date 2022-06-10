# Update GeoData Tools

## Directories

```txt
├── china
│   ├── area_cn.csv  ## Fetch metadata of China geo data from Aliyun.
│   ├── fetch_geodata_from_aliyun.py  ## 
│   └── legacy  ## Legacy outdated fetch scripts.
│       ├── executor.sh  ## The concurrent executor tool implemented in pure shell.
│       └── legacy_fetch_geodata_from_aliyun.sh  ## Pure shell implements of concurrently fetch China geodata from Alibyun (only China is supported)
├── area_global.csv  ## Fetch metadata of global geo data from GADM.
├── fetch_geodata_from_gadm.py  ## Fetich global geographic data from GADM.
```

## Quick start

- Fetch `China` geo data from Aliyun (Only supports China)

```bash
./fetch_geodata_from_aliyun.py
```

- Fetch `Any Countries` geo data from GADM (Supports Global), but the latest version may only support the [shapefile](https://gadm.org/formats.html) format, and you need to use tools such as [mapshaper](https://github.com/mbloch/mapshaper) to convert to [geojson](https://gadm.org/formats.html) format.

```bash
./fetch_geodata_from_gadm.py
```
