/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.iam.core.web;

import static com.wl4g.infra.common.lang.DateUtils2.formatDate;
import static com.wl4g.infra.common.jedis.cursor.ScanCursor.CursorSpec.parse;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.BEAN_SESSION_RESOURCE_MSG_BUNDLER;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.CACHE_PREFIX_IAM_SESSION_REFATTRS;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_API_V2_SESSION;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.shiro.subject.support.DefaultSubjectContext.AUTHENTICATED_SESSION_KEY;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.annotations.Beta;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.context.web.BaseController;
import com.wl4g.infra.common.jedis.cursor.ScanCursor;
import com.wl4g.iam.common.i18n.SessionResourceMessageBundler;
import com.wl4g.iam.core.cache.IamCache;
import com.wl4g.iam.core.cache.IamCacheManager;
import com.wl4g.iam.core.config.AbstractIamProperties;
import com.wl4g.iam.core.session.IamSession;
import com.wl4g.iam.core.session.mgt.IamSessionDAO;
import com.wl4g.iam.core.web.model.SessionAttributeModel;
import com.wl4g.iam.core.web.model.SessionAttributeModel.CursorIndex;
import com.wl4g.iam.core.web.model.SessionAttributeModel.IamSessionInfo;
import com.wl4g.iam.core.web.model.SessionDestroyModel;
import com.wl4g.iam.core.web.model.SessionQueryModel;

/**
 * Generic abstract API controller.
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2019年10月31日
 * @since
 */
@Beta
@ResponseBody
public abstract class BaseApiController extends BaseController implements InitializingBean {
    public static final String DEFAULT_DATE_PATTERN = "yy/MM/dd HH:mm:ss";

    /**
     * IAM properties configuration.
     */
    @Autowired
    protected AbstractIamProperties<?> config;

    /**
     * Session delegate message source bundle.
     */
    @javax.annotation.Resource(name = BEAN_SESSION_RESOURCE_MSG_BUNDLER)
    protected SessionResourceMessageBundler bundle;

    /**
     * IAM session DAO.
     */
    @Autowired
    protected IamSessionDAO sessionDAO;

    /**
     * Enhanced cache manager.
     */
    @Autowired
    protected IamCacheManager cacheManager;

    /**
     * Relations attributes {@link IamCache}
     */
    protected IamCache relationAttrsCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.relationAttrsCache = cacheManager.getIamCache(CACHE_PREFIX_IAM_SESSION_REFATTRS);
    }

    /**
     * Iterative scan gets the list of access sessions (including all clients
     * and authenticated and uncertified sessions).</br>
     * <p>
     * For example response:
     *
     * <pre>
     * {
     *   "code": 200,
     *   "status": "Normal",
     *   "message": "Ok",
     *   "data": {
     *     "index": {
     *       "cursorString": "0@5",
     *       "hasNext": false
     *     },
     *     "sessions": [
     *       {
     *         "id": "sidad6d3cbae8e24b1488f439845b1c3540",
     *         "startTimestamp": 1573200604837,
     *         "stopTimestamp": null,
     *         "lastAccessTime": 1573200612065,
     *         "timeout": 2592000000,
     *         "expired": false,
     *         "authenticated": true,
     *         "host": "127.0.0.1",
     *         "principal": "admin2",
     *         "grants": [
     *           "portal",
     *           "base"
     *         ],
     *         "clientRef": "WINDOWS",
     *         "oauth2Provider": null
     *       },
     *       {
     *         "id": "sidc14af9cc44374d0c810d5a3d948766fb",
     *         "startTimestamp": 1573200412703,
     *         "stopTimestamp": null,
     *         "lastAccessTime": 1573200412703,
     *         "timeout": 2592000000,
     *         "expired": false,
     *         "authenticated": false,
     *         "host": "127.0.0.1",
     *         "principal": null,
     *         "grants": [
     *           
     *         ],
     *         "clientRef": "WINDOWS",
     *         "oauth2Provider": null
     *       ]
     *     }
     *  }
     * </pre>
     */
    @GetMapping(path = URI_IAM_SERVER_API_V2_SESSION)
    public RespBase<?> getSessions(@Validated SessionQueryModel query, HttpServletRequest request) throws Exception {
        log.info("called:getSessions '{}' from '{}', query={}", URI_IAM_SERVER_API_V2_SESSION, request.getRemoteHost(), query);

        RespBase<Object> resp = RespBase.create();
        // Priority search principal.
        if (!isBlank(query.getPrincipal())) {
            Collection<IamSession> ss = sessionDAO.getAccessSessions(query.getPrincipal());
            List<IamSessionInfo> sas = ss.stream().map(s -> convertIamSessionInfo(s)).collect(toList());
            resp.setData(new SessionAttributeModel(new CursorIndex(), sas));
        } else { // Scan sessions all
            ScanCursor<IamSession> sc = sessionDAO.getAccessSessions(parse(query.getCursor()), query.getLimit());
            // Convert to SessionAttribute.
            List<IamSessionInfo> sas = sc.toValues().stream().map(s -> convertIamSessionInfo(s)).collect(toList());
            // Setup response attributes.
            CursorIndex index = new CursorIndex(sc.getCursor().getCursorFullyString(), sc.getCursor().getHasNext());
            resp.setData(new SessionAttributeModel(index, sas));
        }
        log.info("resp:getSessions {}", resp.asJson());
        return resp;
    }

    /**
     * Destroy cleanup session.
     *
     * @param destroy
     * @return
     * @throws Exception
     */
    @PostMapping(path = URI_IAM_SERVER_API_V2_SESSION)
    public RespBase<?> destroySessions(@Validated @RequestBody SessionDestroyModel destroy, HttpServletRequest request)
            throws Exception {
        log.info("called:destroySessions '{}' from '{}', destroy={}", URI_IAM_SERVER_API_V2_SESSION, request.getRemoteHost(),
                destroy);

        RespBase<String> resp = RespBase.create();
        // Destroy with sessionIds.
        if (!isBlank(destroy.getSessionId())) {
            sessionDAO.delete(new IamSession((Serializable) destroy.getSessionId()));
        }
        // Destroy with principal.
        if (!isBlank(destroy.getPrincipal())) {
            sessionDAO.removeAccessSession(destroy.getPrincipal());
        }
        log.info("resp:DestroySessions {}", resp);
        return resp;
    }

    /**
     * Convert wrap {@link IamSession} to {@link SessionAttributeModel}. </br>
     * </br>
     *
     * <b>Origin {@link IamSession} json string example:</b>
     *
     * <pre>
     *    {
     * 	  "code": 200,
     * 	  "status": "normal",
     * 	  "message": "ok",
     * 	  "data": {
     * 	    "sessions": [
     *          {
     * 	        "id": "sid4c034ff4e95741dcb3b20f687c952cd4",
     * 	        "startTimestamp": 1572593959441,
     * 	        "stopTimestamp": null,
     * 	        "lastAccessTime": 1572593993963,
     * 	        "timeout": 1800000,
     * 	        "expired": false,
     * 	        "host": "0:0:0:0:0:0:0:1",
     * 	        "attributes": {
     * 	          "org.apache.shiro.subject.support.DefaultSubjectContext_AUTHENTICATED_SESSION_KEY": true,
     * 	          "authcTokenAttributeKey": {
     * 	            "principals": {
     * 	              "empty": false,
     * 	              "primaryPrincipal": "root",
     * 	              "realmNames": [
     * 	                "com.wl4g.devops.iam.realm.GeneralAuthorizingRealm_0"
     * 	              ]
     *                },
     * 	            "credentials": "911ef082b5de81151ba25d8442efb6e77bb380fd36ac349ee737ee5461ae6d3e8a13e4366a20e6dd71f95e8939fe375e203577568297cdbc34d598dd47475a7c",
     * 	            "credentialsSalt": null,
     * 	            "accountInfo": {
     * 	              "principal": "root",
     * 	              "storedCredentials": "911ef082b5de81151ba25d8442efb6e77bb380fd36ac349ee737ee5461ae6d3e8a13e4366a20e6dd71f95e8939fe375e203577568297cdbc34d598dd47475a7c"
     *                }
     *              },
     * 	          "CentralAuthenticationHandler.GRANT_TICKET": {
     * 	            "applications": {
     * 	              "umc-manager": "stzgotzYWGdweoBGgEOtDKpXwJsxyEaqCrttfMSgFMYkZuIWrDWNpzPYWFa"
     *                }
     *              },
     * 	          "org.apache.shiro.subject.support.DefaultSubjectContext_PRINCIPALS_SESSION_KEY": {
     * 	            "empty": false,
     * 	            "primaryPrincipal": "root",
     * 	            "realmNames": [
     * 	              "com.wl4g.devops.iam.realm.GeneralAuthorizingRealm_0"
     * 	            ]
     *              }
     *            }
     *          }
     * 	    ]
     *      }
     *    }
     * </pre>
     *
     * @param session
     * @return
     */
    protected IamSessionInfo convertIamSessionInfo(IamSession session) {
        IamSessionInfo sa = new IamSessionInfo();
        sa.setId(String.valueOf(session.getId()));
        if (nonNull(session.getLastAccessTime())) {
            sa.setLastAccessTime(formatDate(session.getLastAccessTime(), DEFAULT_DATE_PATTERN));
        }
        if (nonNull(session.getStartTimestamp())) {
            sa.setStartTime(formatDate(session.getStartTimestamp(), DEFAULT_DATE_PATTERN));
        }
        if (nonNull(session.getStopTimestamp())) {
            sa.setStopTime(formatDate(session.getStopTimestamp(), DEFAULT_DATE_PATTERN));
        }
        sa.setHost(session.getHost());
        sa.setExpired(session.isExpired());

        // Authentication status.
        Object authenticated = session.getAttribute(AUTHENTICATED_SESSION_KEY);
        sa.setAuthenticated(false);
        if (nonNull(authenticated)) {
            if (authenticated instanceof Boolean || authenticated.getClass() == boolean.class) {
                sa.setAuthenticated((Boolean) authenticated);
            } else {
                sa.setAuthenticated(Boolean.parseBoolean((String) authenticated));
            }
        }

        // Authentication principal.
        sa.setPrincipal(session.getPrimaryPrincipal());
        return sa;
    }

}