/**
 * Common Util v2.0.0 | (c) 2017 ~ 2050 wl4g Foundation, Inc.
 * Copyright 2017-2032 <wangsir@gmail.com, 983708408@qq.com>, Inc. x
 * Licensed under Apache2.0 (https://github.com/wl4g/dopaas-iam/blob/master/LICENSE)
 */
(function(window, document) {
	'use strict';

	// Exposing the API to the outside.
	if(!window.Common){ window.Common = {}; }
	if(!window.Common.Constants){ window.Common.Constants = {}; }

	// Common util.
	window.Common.Util = {
		printSafeWarn: function(msg){
			console.log("%cSECURITY WARNING!","font-size:50px;color:red;-webkit-text-fill-color:red;-webkit-text-stroke:1px black;");
		 	if(!Common.Util.isEmpty(msg)){
				console.log("%c"+msg,"font-size:14px;color:#303d65;");
		 	}
		},
		getEleValue: function(name, obj){
			return getEleValue(name, obj, true);
		},
		getEleValue: function(name, obj, assertion){
			if(!assertion) {
				return $(obj).val();
			}
			return Common.Util.checkEmpty(name, $(obj).val());
		},
		checkEmpty: function(name, param){
			if(Common.Util.isEmpty(param)){
				throw "Argument '" + name + "' must not be empty";
			}
			return param;
		},
		empty: function(param){
			if(Common.Util.isEmpty(param)){
				return "";
			}
			return param;
		},
		isEmpty: function(param){
			if(param == null || param == undefined || param == '')
				return true;
			else if(Common.Util.isFunction(param))
				return false;
			else
				return param.length == 0;
		},
		isAnyEmpty: function(params){
			for(var i = 0; i < arguments.length; i++){
				if(Common.Util.isEmpty(arguments[i]))
					return true;
			}
			return false;
		},
		isAnyContains: function(params, search){
			if(!Common.Util.isArray(Common.Util.checkEmpty("Arguments", params))){
				throw "Argument must is type of Array";
			}
			for(var i = 0; i < params.length; i++){
				if(params[i] == search)
					return true;
			}
			return false;
		},
		isArray : function (obj) {
		    return Object.prototype.toString.call(obj) === '[object Array]';
		},
		isMap : function (obj) {
		    return Object.prototype.toString.call(obj) === '[object Map]';
		},
		isObject : function (obj) {
		    return Object.prototype.toString.call(obj) === '[object Object]';
		},
		isFunction : function (obj) {
		    return Object.prototype.toString.call(obj) === '[object Function]';
		},
		isString : function (obj) {
		    return Object.prototype.toString.call(obj) === '[object String]';
		},
		sortWithAscii : function(str) { // 按ASCII排序
			return Array.prototype.sort.call(Array.from(str), function(a, b) {
			    return a.charCodeAt(0) - b.charCodeAt(0); // (a,b)=>(a.charCodeAt(0) - b.charCodeAt(0))
			}).join('');
		},
		extTopDomainString: function(hostOrUri) {
			var domain = hostOrUri; // Is host?
			if (hostOrUri.indexOf('/') > 0) { // Is URI?
				domain = new URL(hostOrUri).host;
			}
			// Check domain available?
			if (Common.Util.isEmpty(domain)) {
				return "";
			}
			var topDomainName = domain.split('.').slice(-2).join('.');
        	if(domain.indexOf("com.") > 0) { // e.g: com.cn/com.sg
        		topDomainName = domain.split('.').slice(-3).join('.');
        	}
        	return topDomainName;
		},
		getCookie: function(cookieName, cookies) {
			if (!cookies) {
				cookies = document.cookie;
			}
			var cookiesArr = cookies.split(";");
			for(var i = 0; i < cookiesArr.length; i++){
				var cookie = cookiesArr[i].split("=");
				var value = cookie[1];
				if(cookie[0].trim() == cookieName.trim()){
					return value;
				}
			}
			return null;
		},
		// 获取最顶层window对象(对于嵌套iframe刷新页面跳转非常有用)
		getRootWindow: function(currentWindow) {
			var _window = currentWindow;
			while (_window.self.frameElement && _window.self.frameElement.tagName == "IFRAME"
				|| _window.self != _window.top) {
			  _window = _window.parent;
			}
			return _window;
		},
		isEnabled: function(value){
			if(!Common.Util.isEmpty(value)){
				value = value.toLowerCase();
				return  (value == "y" || value == "on" || value == "yes" || value == "1" || value == "enabled" || value == "true" || value == "t");
			}
			return false;
		},
		mergeMap: function(dst, src) {
		    dst = dst || new Map();
		    src = src || new Map();
		    dst.forEach((v, k) => { src.set(k, v); });
		    return src;
		},
		clone: function(obj) { // 或使用JSON.parse(JSON.stringify(oldObj))实现深度拷贝，注意：Object.assign(oldObj,newObj)只能浅层拷贝
			if(!Common.Util.isEmpty(obj)){
				var newobj = obj.constructor === Array ? [] : {};
				if (typeof obj !== "object") {
					return obj;
				} else {
					for (var i in obj) {
						newobj[i] = typeof obj[i] === "object" ? Common.Util.clone(obj[i]) : obj[i];
					}
				}
				return newobj;
			}
			return null;
		},
		Http: {
			createXMLHttpRequest: function() {      
				if (window.ActiveXObject) {      
					var ieArr = ["Msxml2.XMLHTTP.6.0", "Msxml2.XMLHTTP.3.0", "Msxml2.XMLHTTP", "Microsoft.XMLHTTP"];
					for (var i = 0; i < ieArr.length; i++) {
						try {
							var xmlhttp = new ActiveXObject(ieArr[i]);
							if (xmlhttp) {
								return xmlhttp;
							}
						} catch (e) {}
					}
				} else if (window.XMLHttpRequest) {
					return new XMLHttpRequest();
				}
			},
			/**
			 * e.g:
			 * <pre>
			 * Common.Util.Http.request({
			 *	    url: "http://my.domain.com/myapp/list", 
			 *	    type: "post",
			 *	    timeout: 1000,
			 *	    //async: false,
			 *	    xhrFields: {withCredentials: true},
			 *	    success: function(data, textStatus, xhr) {
			 *	        console.log("Response data:", data)
			 *	    },
			 *	    error: function(xhr, textStatus, errmsg) {
			 *	        console.log("Request error:", errmsg)
			 *	    }
			 *	})
			 * </pre>
			 */
			request: function(options) {
				var url = options.url,
				method = options.method || "GET",
				type = options.type || method, // for jquery compatible
				async = options.async,
				xhrFields = options.xhrFields || {},
				headers = options.headers || {},
				data = options.data || null,
				timeout = options.timeout || 30000,
				success = options.success || function(data, textStatus, xhr) {},
				error = options.error || function(xhr, textStatus, errmsg) { console.error(errmsg); },
				complete = options.complete || function(status, statusText, response, responseHeaders) {};
				try {
					// Check arguments requires.
					Common.Util.checkEmpty("url", url);

					// Create XMLHttpRequest
					var _xhr = null;
					if (!_xhr) {
						_xhr = Common.Util.Http.createXMLHttpRequest();
					}

					// Init XMLHttpRequest
					_xhr.open(type.toUpperCase(), url, async);

					// Apply custom fields if provided
					if (xhrFields) {
						for (var i in xhrFields) {
							// e.g: _xhr.withCredentials = withCredentials;
							_xhr[i] = xhrFields[i];
						}
					}

					// Override mime type if needed
					if (options.mimeType && xhr.overrideMimeType) {
						_xhr.overrideMimeType(options.mimeType);
					}

					// X-Requested-With header
					// For cross-domain requests, seeing as conditions for a preflight are
					// akin to a jigsaw puzzle, we simply never set it to be sure.
					// (it can always be set on a per-request basis or even using ajaxSetup)
					// For same-domain requests, won't change header if already provided.
					if (!options.crossDomain && !headers["X-Requested-With"]) {
						headers["X-Requested-With"] = "XMLHttpRequest";
					}

					// Set headers
					for (var i in headers) {
						_xhr.setRequestHeader(i, headers[i]);
					}

					// Synchronous requests must not set a timeout.
					// @see https://chromium.googlesource.com/chromium/blink.git/+/refs/heads/master/Source/core/xmlhttprequest/XMLHttpRequest.cpp#606
					if (async) {
						_xhr.timeout = timeout;
					}

					// 设置超时检查函数
					//var _responsedMark = false;
					//var _timeoutTimer = window.setTimeout(function() {
					//	if (!_responsedMark) {
					//		error(_xhr, null, "Timeout waiting for response, " + timeout);
					//	}
					//}, timeout);

					// 设置回调函数
					_xhr.onreadystatechange = function() {
						if (_xhr.readyState == 4) {
							//_responsedMark = true;
							//window.clearTimeout(_timeoutTimer);
							// 3.1获取返回数据
							var res = _xhr.responseText;
							if (_xhr.status == 200) {
								success(res, _xhr.textStatus, _xhr);
							} else {
								error(_xhr, _xhr.textStatus, "Error status " + _xhr.status);
							}
						}
					};

					// Callback
					var callback = function(type) {
						return function() {
							if (callback) {
								callback = _xhr.onload = _xhr.onerror = _xhr.onabort = _xhr.ontimeout = null;
								if (type === "abort") {
									_xhr.abort();
								} else if (type === "error") {
									complete(
										// File: protocol always yields status 0; see #8605, #14207
										_xhr.status,
										_xhr.statusText
									);
								} else {
									complete(
										_xhr.status,
										_xhr.statusText,
										// For XHR2 non-text, let the caller handle it (gh-2498)
										(_xhr.responseType || "text") === "text" ? {text: _xhr.responseText} : {binary: _xhr.response},
										_xhr.getAllResponseHeaders()
									);
								}
							}
						};
					};

					// Listen to events
					_xhr.onload = callback();
					_xhr.onabort = _xhr.onerror = _xhr.ontimeout = callback("error");

					// Create the abort callback
					callback = callback("abort");

					// Do send the request (this may raise an exception)
					_xhr.send(data);
				} catch(e) {
					error(_xhr, null, e);
				}
			}
		},
		PlatformType: (function() {
		    var ua = navigator.userAgent.toLowerCase();
		    var mua = {
		        MicroMessenger: /micromessenger/.test(ua), //WeChat MicroMessenger
		        iOS: /ipod|iphone|ipad/.test(ua), //iOS
		       	Mac: /macintosh|mac os|mac/.test(ua), // Mac
		        iPhone: /iphone/.test(ua), // iPhone
		        iPad: /ipad/.test(ua), // iPad
		        Android: /android/.test(ua), // Android Device
		        Windows: /windows/.test(ua), // Windows Device
		        Linux: /linux/.test(ua), // Linux Device
		        FreeBSD: /freebsd/.test(ua), // FreeBSD Device
		        OpenBSD: /openbsd/.test(ua), // OpenBSD Device
		        SunOS: /sunos/.test(ua), // SunOS Device
		        AIX: /aix/.test(ua), // AIX Device
		        Irix: /irix/.test(ua), // Irix Device
		        Solaris: /solaris/.test(ua), // Solaris Device
		        TouchDevice: ('ontouchstart' in window) || /touch/.test(ua), // Touch Device
		        Mobile: /mobile/.test(ua), // Mobile Device (iPad)
		        AndroidTablet: false, // Android Tablet
		        WINDOWS_TABLET: false, // Windows Tablet
		        Tablet: false, // Tablet (iPad, Android, Windows)
		        SmartPhone: false // Smart Phone (iPhone, Android)
		    };
		    mua.AndroidTablet = mua.Android && !mua.Mobile;
		    mua.WindowsTablet = mua.Windows && /tablet/.test(ua);
		    mua.Tablet = mua.iPad || mua.AndroidTablet || mua.WindowsTablet;
		    mua.SmartPhone = mua.Mobile && !mua.Tablet;
		    return mua;
		})(),
		int2char: function(n) {
		    return "0123456789abcdefghijklmnopqrstuvwxyz".charAt(n);
		},
		language: function() {
		    return (navigator.language || navigator.browserLanguage || navigator.systemLanguage).toLowerCase();
		},
		isZhCN: function() {
		    return Common.Util.language().indexOf('zh') >= 0;
		},
		// Convertion paramMap to formData url
		toUrl: function(templateMap, paramMap) {
			Common.Util.checkEmpty("templateMap", templateMap);
			Common.Util.checkEmpty("paramMap", paramMap);
			var formData = "";
			paramMap.forEach((value, key) => {
				var paramName = key;
				// Check template placeholder key.
				if(!Common.Util.isEmpty(key)) {
					if(key.startsWith("{") && key.endsWith("}")) {
						var realKey = key.substr(1, key.length - 2);
						paramName = Common.Util.checkEmpty("templateMap." + realKey, templateMap[realKey]);
					}
				} else {
					console.warn("Null param.key of parameters: "+ JSON.stringify(paramMap));
				}
				formData += (paramName + "=" + value + "&");
			});
			if(formData.endsWith("&")){
				formData = formData.substr(0, formData.lastIndexOf('&'));
			}
			return formData;
		},
		toUrlQueryParam: function(url) {
		    if(!url) {
		        return null;
		    }
		    var index = url.lastIndexOf("?");
		    if(index >= 0) {
		        url = url.substring(index+1);
		    }
		    var paramPairs = url.split("&");
		    var paramsMap = new Map();
		    for (var i=0; i<paramPairs.length; i++) {
		        var parts = paramPairs[i].split("=");
		        if (parts.length >= 2) {
		            paramsMap.set(parts[0], parts[1]);
		        }
		    }
		    return paramsMap;
		},
		Codec: {
			encodeBase58: function(plaintext){ // 明文字符串base58编码
				if(!plaintext) { return null; }
				var ALPHABET = '123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz';
				var ALPHABET_MAP = {};
				var BASE = 58;
				for (var i = 0; i < ALPHABET.length; i++) {
					ALPHABET_MAP[ALPHABET.charAt(i)] = i;
				}
				// 内部明文字符串转字节函数(toUTF8())
				var plainBuffer = (function(str){
				    var result = new Array();
				    var k = 0;
				    for (var i = 0; i < str.length; i++) {
				        var j = encodeURI(str[i]);
				        if (j.length==1) {
				            // 未转换的字符
				            result[k++] = j.charCodeAt(0);
				        } else {
				            // 转换成%XX形式的字符
				            var bytes = j.split("%");
				            for (var l = 1; l < bytes.length; l++) {
				                result[k++] = parseInt("0x" + bytes[l]);
				            }
				        }
				    }
				    return result;
				})(plaintext);
				// 编码base58字符串
				if (plainBuffer.length === 0) return '';
				var i, j, digits = [0];
				for (i = 0; i < plainBuffer.length; i++) {
					for (j = 0; j < digits.length; j++){
			            // 将数据转为二进制，再位运算右边添8个0，得到的数转二进制
			            // 位运算-->相当于 digits[j].toString(2);parseInt(10011100000000,2)
			            digits[j] <<= 8;
			        }
					digits[0] += plainBuffer[i];
					var carry = 0;
					for (j = 0; j < digits.length; ++j) {
						digits[j] += carry;
						carry = (digits[j] / BASE) | 0;
						digits[j] %= BASE;
					}
					while (carry) {
						digits.push(carry % BASE);
						carry = (carry / BASE) | 0;
					}
				}
				// Deal with leading zeros
				for (i = 0; plainBuffer[i] === 0 && i < plainBuffer.length - 1; i++) digits.push(0);
				return digits.reverse().map(function(digit) { return ALPHABET[digit]; }).join('');
			},
			decodeBase58: function(base58) { // base58字符串解码
				if(!base58) { return null; }
				var ALPHABET = '123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz';
				var ALPHABET_MAP = {};
				var BASE = 58;
				for (var i = 0; i < ALPHABET.length; i++) {
					ALPHABET_MAP[ALPHABET.charAt(i)] = i;
				}
				if (base58.length === 0) return [];
				var i, j, bytes = [0];
				for (i = 0; i < base58.length; i++) {
					var c = base58[i];
			        // c是不是ALPHABET_MAP的key 
					if (!(c in ALPHABET_MAP)) throw new Error('Non-base58 character');
					for (j = 0; j < bytes.length; j++) bytes[j] *= BASE;
					bytes[0] += ALPHABET_MAP[c];
					var carry = 0;
					for (j = 0; j < bytes.length; ++j) {
						bytes[j] += carry;
						carry = bytes[j] >> 8;
			            // 0xff --> 11111111
						bytes[j] &= 0xff;
					}
					while (carry) {
						bytes.push(carry & 0xff);
						carry >>= 8;
					}
				}
				// Deal with leading zeros
				for (i=0; base58[i] === '1' && i < base58.length - 1; i++) bytes.push(0);
				// Ascii to string.
				var plainBuffer = bytes.reverse();
				var plaintext = "";
				for (i=0; i<plainBuffer.length; i++) {
					plaintext += String.fromCharCode(plainBuffer[i]);
				}
				return plaintext;
			},
			encodeBase64: function(str) {
			    var encode = encodeURI(str);
			    var base64Str = btoa(encode);
			    return base64Str;
			},
			decodeBase64: function(base64Str) {
				var decode = atob(base64Str);
				var str = decodeURI(decode);
				return str;
			},
			toHex: function(str) {
				return Common.Util.Codec.base64ToHex(Common.Util.Codec.encodeBase64(str));
			},
			// Base64 and Hex functions.
            hexToBase64: function(str) {
            	var tableStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        		var table = tableStr.split("");
				var btoa = function(bin) {
					for (var i = 0, j = 0, len = bin.length / 3, base64 = []; i < len; ++i) {
	                    var a = bin.charCodeAt(j++), b = bin.charCodeAt(j++), c = bin.charCodeAt(j++);
	                    if ((a | b | c) > 255) throw new Error("String contains an invalid character");
	                    base64[base64.length] = table[a >> 2] + table[((a << 4) & 63) | (b >> 4)] +
	                        (isNaN(b) ? "=" : table[((b << 2) & 63) | (c >> 6)]) +
	                        (isNaN(b + c) ? "=" : table[c & 63]);
	                }
	                return base64.join("");
				};
	            return btoa(String.fromCharCode.apply(null,
	                str.replace(/\r|\n/g, "").replace(/([\da-fA-F]{2}) ?/g, "0x$1 ").replace(/ +$/, "").split(" "))
	            );
	        },
	        base64ToHex: function(str) { // convert a base64 string to hex
	        	if(str == null || !str) { return null; }
			    var ret = "";
			    var k = 0; // b64 state, 0-3
			    var slop = 0;
			    for (var i = 0; i < str.length; ++i) {
			        if (str.charAt(i) == "=") {
			            break;
			        }
			        var v = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(str.charAt(i));
			        if (v < 0) {
			            continue;
			        }
			        if (k == 0) {
			            ret += Common.Util.int2char(v >> 2);
			            slop = v & 3;
			            k = 1;
			        }
			        else if (k == 1) {
			            ret += Common.Util.int2char((slop << 2) | (v >> 4));
			            slop = v & 0xf;
			            k = 2;
			        }
			        else if (k == 2) {
			            ret += Common.Util.int2char(slop);
			            ret += Common.Util.int2char(v >> 2);
			            slop = v & 3;
			            k = 3;
			        }
			        else {
			            ret += Common.Util.int2char((slop << 2) | (v >> 4));
			            ret += Common.Util.int2char(v & 0xf);
			            k = 0;
			        }
			    }
			    if (k == 1) {
			        ret += Common.Util.int2char(slop << 2);
			    }
			    return ret;
			}
		},
		Crc16CheckSum: {
			_auchCRCHi : [
				0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
				0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
				0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
				0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
				0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
				0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
				0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
				0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
				0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
				0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
				0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
				0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
				0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
				0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
				0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
				0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
				0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
				0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
				0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
				0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
				0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
				0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
				0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
				0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
				0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
				0x80, 0x41, 0x00, 0xC1, 0x81, 0x40
			],
			_auchCRCLo : [
				0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2, 0xC6, 0x06,
				0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04, 0xCC, 0x0C, 0x0D, 0xCD,
				0x0F, 0xCF, 0xCE, 0x0E, 0x0A, 0xCA, 0xCB, 0x0B, 0xC9, 0x09,
				0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA, 0x1A,
				0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC, 0x14, 0xD4,
				0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12, 0x13, 0xD3,
				0x11, 0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3,
				0xF2, 0x32, 0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4,
				0x3C, 0xFC, 0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE, 0xFA, 0x3A,
				0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38, 0x28, 0xE8, 0xE9, 0x29,
				0xEB, 0x2B, 0x2A, 0xEA, 0xEE, 0x2E, 0x2F, 0xEF, 0x2D, 0xED,
				0xEC, 0x2C, 0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7, 0xE6, 0x26,
				0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0, 0xA0, 0x60,
				0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62, 0x66, 0xA6, 0xA7, 0x67,
				0xA5, 0x65, 0x64, 0xA4, 0x6C, 0xAC, 0xAD, 0x6D, 0xAF, 0x6F,
				0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68,
				0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA, 0xBE, 0x7E,
				0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C, 0xB4, 0x74, 0x75, 0xB5,
				0x77, 0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71,
				0x70, 0xB0, 0x50, 0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92,
				0x96, 0x56, 0x57, 0x97, 0x55, 0x95, 0x94, 0x54, 0x9C, 0x5C,
				0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E, 0x5A, 0x9A, 0x9B, 0x5B,
				0x99, 0x59, 0x58, 0x98, 0x88, 0x48, 0x49, 0x89, 0x4B, 0x8B,
				0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D, 0x4C, 0x8C,
				0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86, 0x82, 0x42,
				0x43, 0x83, 0x41, 0x81, 0x80, 0x40
			],
			crc16Modbus : function (obj) {
				var buffer = Object.prototype.toString.call(obj) === '[object Array]' ? obj : Common.Util.Crc16CheckSum.strToByte(obj);
				var ucCRCHi = (0xffff & 0xff00) >> 8;
				var ucCRCLo = 0xffff & 0x00ff;
				var iIndex;
				for (var i = 0; i < buffer.length; ++i) {
					iIndex = (ucCRCLo ^ buffer[i]) & 0x00ff;
					ucCRCLo = ucCRCHi ^ Common.Util.Crc16CheckSum._auchCRCHi[iIndex];
					ucCRCHi = Common.Util.Crc16CheckSum._auchCRCLo[iIndex];
				}
				return ((ucCRCHi & 0x00ff) << 8) | (ucCRCLo & 0x00ff) & 0xffff;
			},
			strToByte : function (str) {
				var tmp = str.split(''), arr = [];
				for (var i = 0, c = tmp.length; i < c; i++) {
					var j = encodeURI(tmp[i]);
					if (j.length == 1) {
						arr.push(j.charCodeAt());
					} else {
						var b = j.split('%');
						for (var m = 1; m < b.length; m++) {
							arr.push(parseInt('0x' + b[m]));
						}
					}
				}
				return arr;
			}
		},
		FastMap: function() {
			var length = 0;
			var obj = new Object();
			this.asJsonString = function(){
				return JSON.stringify(obj);
			};
			this.isEmpty = function(){
				return length == 0;
			};
			this.containsKey = function(key){
				return (key in obj);
			};
			this.containsValue = function(value){
				for(var key in obj){
					if(obj[key] == value){
						return true;
					}
				}
				return false;
			};
			this.put = function(key,value){
				if(!this.containsKey(key)){
					length++;
				}
				obj[key] = value;
			};
			this.get = function(key){
				return this.containsKey(key) ? obj[key] : null;
			};
			this.remove = function(key){
				if(this.containsKey(key) && (delete obj[key])){
					length--;
				}
			};
			this.values = function(){
				var _values= new Array();
				for(var key in obj){
					_values.push(obj[key]);
				}
				return _values;
			};
			this.keySet = function(){
				var _keys = new Array();
				for(var key in obj){
					_keys.push(key);
				}
				return _keys;
			};
			this.size = function(){
				return length;
			};
			this.clear = function(){
				length = 0;
				obj = new Object();
			};
		},
		HashMap: function(){
			// See: https://yq.aliyun.com/articles/245764?do=login&accounttraceid=2d2d9331-a204-4c8a-aaf4-a6d3ebd3be6e
			//初始大小
			var size = 0;
			//数组
			var table = [];
			//初始数组长度为16
			var length = 2 << 3;
			//数组扩容临界值为12
			var threshold = 0.75 * length;
			// hash值计算
			this.hash = function(h) {
				h ^= (h >>> 20) ^ (h >>> 12);
				return h ^ (h >>> 7) ^ (h >>> 4);
			};

			//返回HashMap的size
			this.size = function(){
				return size;
			};

			//是否包含某个key
			this.containsKey = function(key) {
				if(key == null || key == undefined)
					return false;
				else {
					var hashCode = this.hashCode(key);
					var hash = this.hash(hashCode);
					var index = this.indexFor(hash, length);
					for(var e = table[index]; e != null && e != undefined; e = e.next){
						if(e.key === key){
							return true;
						}
					}
					return false;
				}
			};

			//是否包含某个value
			this.containsValue = function(value) {
				for(var index = 0; index < table.length; index++) {
					for (var e = table[index]; e != null && e != undefined; e = e.next) {
						if (JSON.stringify(e.value) === JSON.stringify(value)) {
							return true;
						}
					}
				}
				return false;
			};

			//HashMap是否为空
			this.isEmpty = function(){
				return size === 0;
			};

			//计算HashCode值，不同的key有不同的HashCode，这里使用字符串转ASCII码并拼接的方式
			this.hashCode = function(key){
				var hashcode = '';
				for(var i=0 ;i< key.length; i++){
					hashcode += key.charCodeAt(i);
				}
				return hashcode;
			};

			//向HashMap中存放值
			this.put = function(key, value){
				if(key == null || key == undefined){
					return;
				}
				var hashCode = this.hashCode(key);
				var hash = this.hash(hashCode);
				var index = this.indexFor(hash, length);
				for(var e = table[index]; e != null && e != undefined; e = e.next){
					if(e.key === key){
						var oldValue = e.value;
						e.value = value;
						return oldValue;
					}
				}
				this.addEntry(key, value, index)
			};

			//从HashMap中获取值
			this.get = function(key){
				if(key == null || key == undefined)
					return undefined;
				var hashCode = this.hashCode(key);
				var hash = this.hash(hashCode);
				var index = this.indexFor(hash, length);
				for(var e = table[index]; e != null && e != undefined; e = e.next){
					if(e.key === key){
						return e.value;
					}
				}
				return undefined;
			};

			//从HashMap中删除值
			this.remove = function(key){
				if(key == null || key == undefined)
					return undefined;
				var hashCode = this.hashCode(key);
				var hash = this.hash(hashCode);
				var index = this.indexFor(hash, length);
				var prev = table[index];
				var e = prev;
				while(e != null && e!= undefined){
					var next = e.next;
					if(e.key === key){
						size--;
						if(prev == e){
							table[index] = next;
						} else{
							prev.next = next;
						}
						return e;
					}
					prev = e;
					e = next;
				}
				return e == null||e == undefined? undefined: e.value;
			};

			//清空HashMap
			this.clear = function() {
				table = [];
				// 设置size为0
				size = 0;
				length = 2 << 3;
				threshold = 0.75 * length;
			};

			//根据hash值获取数据应该存放到数组的哪个桶(下标)中
			this.indexFor = function(h, length) {
				return h & (length-1);
			};

			//添加一个新的桶来保存key和value
			this.addEntry = function(key, value, bucketIndex) {
				// 保存对应table的值
				var e = table[bucketIndex];
				// 然后用新的桶套住旧的桶，链表
				table[bucketIndex] = { key: key, value: value, next: e};
				// 如果当前size大于等于阈值
				if (size++ >= threshold) { // 调整容量
					length = length << 1;
					threshold = 0.75 * length;
				}
			};

			//获取HashMap中所有的键值对
			this.getEntries = function(){
				var entries = [];
				for(var index = 0; index < table.length; index++) {
					for (var e = table[index]; e != null && e != undefined; e = e.next) {
						entries.push({key: e.key, value: e.value});
					}
				}
				return entries;
			};
        },
        isIpv4 : function(ip) {
            var pattern = /^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$/;
            return pattern.test(ip);
        },
        isIpv6 : function(ip) {
            var pattern = /^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/;
            return pattern.test(ip);
        },
        isIp : function(ip) {
            return Common.Util.isIpv4(ip) || Common.Util.isIpv6(ip);
        }
	},
	// 对Date的扩展，将 Date 转化为指定格式的String 
	// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符， 
	// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) 
	// 例子： 
	// (new Date()).format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423 
	// (new Date()).format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18 
	Date.prototype.format = function(fmt) {
	  var o = { 
	    "M+" : this.getMonth()+1,                 //月份 
	    "d+" : this.getDate(),                    //日 
	    "h+" : this.getHours(),                   //小时 
	    "m+" : this.getMinutes(),                 //分 
	    "s+" : this.getSeconds(),                 //秒 
	    "q+" : Math.floor((this.getMonth()+3)/3), //季度 
	    "S"  : this.getMilliseconds()             //毫秒 
	  }; 
	  if(/(y+)/.test(fmt)) {
		  fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
	  }
	  for(var k in o) {
		  if(new RegExp("("+ k +")").test(fmt)) {
			  fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length))); 
		  }
	  }
	  return fmt; 
	},
	// Map to json object.
	JSON.fromMap = function(map) {
		let json = Object.create(null);
		for (let[k,v] of map) {
			json[k] = v;
		}
		return json;
	},
	// JSON to map object.
	JSON.toMap = function(json) {
		let map = new Map();
		for (let k of Object.keys(json)) {
			map.set(k, json[k]);
		}
		return map;
	}
})(window, document);
