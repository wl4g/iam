/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.iam.client.session.mgt;

import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.iam.client.filter.AbstractClientIamAuthenticationFilter.SAVE_GRANT_TICKET;
import static com.wl4g.iam.common.constant.ServiceIAMConstants.CACHE_TICKET_C;
import static com.wl4g.iam.common.constant.ServiceIAMConstants.LOCK_SESSION_VALIDATING;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.component.support.cache.jedis.ScanCursor;
import com.wl4g.component.support.cache.locks.JedisLockManager;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.client.validation.IamValidator;
import com.wl4g.iam.common.constant.ServiceIAMConstants;
import com.wl4g.iam.core.authc.model.SessionValidateModel;
import com.wl4g.iam.core.cache.IamCacheManager;
import com.wl4g.iam.core.session.IamSession;
import com.wl4g.iam.core.session.mgt.AbstractIamSessionManager;

/**
 * IAM client session manager
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月29日
 * @since
 */
public class IamClientSessionManager extends AbstractIamSessionManager<IamClientProperties> {

    /**
     * Expire session validator
     */
    final protected IamValidator<SessionValidateModel, SessionValidateModel> validator;

    @Autowired
    protected JedisLockManager lockManager;

    public IamClientSessionManager(IamClientProperties config, IamCacheManager cacheManager,
            IamValidator<SessionValidateModel, SessionValidateModel> validator) {
        super(config, cacheManager, CACHE_TICKET_C);
        this.validator = validator;
    }

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        return super.getSessionId(request, response);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void validateSessions() {
        Lock lock = lockManager.getLock(LOCK_SESSION_VALIDATING);

        try {
            if (lock.tryLock()) {
                log.info("Validating all active sessions...");

                ScanCursor<IamSession> cursor = sessionDAO.getAccessSessions(ServiceIAMConstants.DEFAULT_SESSION_SCAN_BATCHS);
                while (cursor.hasNext()) {
                    List<IamSession> activeSessions = cursor.toValues();
                    log.debug("Activity client sessions: ", () -> toJSONString(activeSessions));

                    // Grant ticket of local sessions.
                    Map<String, Session> clientSessions = new HashMap<>(activeSessions.size());

                    // Make to validation request
                    SessionValidateModel request = new SessionValidateModel(config.getServiceName());
                    for (IamSession session : activeSessions) {
                        String grantTicket = (String) session.getAttribute(SAVE_GRANT_TICKET);
                        if (!isBlank(grantTicket)) {
                            request.getTickets().add(grantTicket);
                            clientSessions.put(grantTicket, session);
                        }
                    }

                    // Validation expires sessions.
                    SessionValidateModel assertion = validator.validate(request);
                    for (String deadTicket : assertion.getTickets()) {
                        Session session = clientSessions.get(deadTicket);
                        try {
                            if (nonNull(session)) {
                                sessionDAO.delete(session);
                                log.info("Cleauping expired sessionId: {}", session.getId());
                            }
                        } catch (Exception e) {
                            log.warn("Failed to cleaup expired sessions. sessionId: {}, deadTicket: {}", session.getId(),
                                    deadTicket);
                        }
                    }

                }
            } else {
                log.info("Skip validating all active sessions.");
            }

        } catch (Exception e) {
            log.error("Validating expire sessions failed", e);
        } finally {
            lock.unlock();
        }

    }

}