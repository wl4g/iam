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
package com.wl4g.iam.core.session.mgt;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.CACHE_PREFIX_IAM_SESSION;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.infra.common.jedis.JedisClient;
import com.wl4g.infra.common.jedis.cursor.ScanCursor;
import com.wl4g.infra.common.jedis.cursor.ScanCursor.ClusterScanParams;
import com.wl4g.infra.common.jedis.cursor.ScanCursor.CursorSpec;
import com.wl4g.infra.support.cache.locks.JedisLockManager;
import com.wl4g.iam.core.cache.CacheKey;
import com.wl4g.iam.core.cache.IamCacheManager;
import com.wl4g.iam.core.cache.JedisIamCacheManager;
import com.wl4g.iam.core.config.AbstractIamProperties;
import com.wl4g.iam.core.config.AbstractIamProperties.ParamProperties;
import com.wl4g.iam.core.session.IamSession;

/**
 * Redis shiro session DAO.
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年11月28日
 * @since
 */
public class JedisIamSessionDAO extends RelationAttributesIamSessionDAO {

	/**
	 * Distributed locks.
	 */
	@Autowired
	protected JedisLockManager lockManager;

	public JedisIamSessionDAO(AbstractIamProperties<? extends ParamProperties> config, IamCacheManager cacheManager) {
		super(config, cacheManager);
	}

	@Override
	public ScanCursor<IamSession> getAccessSessions(final int limit) {
		return getAccessSessions(new CursorSpec(), limit);
	}

	@Override
	public ScanCursor<IamSession> getAccessSessions(final CursorSpec cursor, int limit) {
		isTrue(limit > 0, "accessSessions batchSize must >0");
		byte[] match = (cacheManager.getIamCache(CACHE_PREFIX_IAM_SESSION).getCacheName() + "*").getBytes(UTF_8);
		ClusterScanParams params = new ClusterScanParams(limit, match);
		JedisClient jedisClient = ((JedisIamCacheManager) cacheManager).getJedisClient();
		return new ScanCursor<IamSession>(jedisClient, cursor, IamSession.class, new ScanCursor.Deserializer() {
			public Object deserialize(byte[] data, Class<?> clazz) {
				IamSession s = (IamSession) super.deserialize(data, clazz);
				awareRelationCache(s);
				return s;
			}
		}, params) {
		}.open();
	}

	@Override
	public Set<IamSession> getAccessSessions(final CursorSpec cursor, final int limit, final Object principal) {
		Set<IamSession> principalSessions = new HashSet<>(4);
		ScanCursor<IamSession> sc = getAccessSessions(cursor, limit);
		while (sc.hasNext()) {
			IamSession s = sc.next();
			if (nonNull(s)) {
				awareRelationCache(s);
				Object primaryPrincipal = s.getPrimaryPrincipal();
				if (nonNull(primaryPrincipal) && primaryPrincipal.equals(principal)) {
					principalSessions.add(s);
				}
			}
		}
		return principalSessions;
	}

	@Override
	public void removeAccessSession(Object principal) {
		log.debug("removeActiveSession principal: {} ", principal);

		Set<IamSession> sessions = getAccessSessions(principal);
		if (!isEmpty(sessions)) {
			for (IamSession s : sessions) {
				delete(s);
				log.debug("Removed iam session for principal: {}, session: {}", principal, s);
			}
		}
	}

	@Override
	protected Serializable doCreate(Session session) {
		log.debug("doCreate {}", session.getId());
		Serializable sessionId = generateSessionId(session);
		assignSessionId(session, sessionId);
		update(session);
		return sessionId;
	}

	@Override
	public Session readSession(Serializable sessionId) throws UnknownSessionException {
		log.debug("readSession {}", sessionId);
		try {
			return super.readSession(sessionId);
		} catch (UnknownSessionException e) {
			return null;
		}
	}

	@Override
	public void assignSessionId(Session session, Serializable sessionId) {
		((IamSession) session).setId((String) sessionId);
	}

	@Override
	protected void doPutIamSession(Session session) {
		// Update session latest expiration time to timeout.
		cacheManager.getIamCache(CACHE_PREFIX_IAM_SESSION).put(new CacheKey(session.getId(), session.getTimeout()), session);
	}

	@Override
	protected void doDeleteIamSession(Session session) {
		cacheManager.getIamCache(CACHE_PREFIX_IAM_SESSION).remove(new CacheKey(session.getId()));
	}

	@Override
	protected Session doReadIamSession(Serializable sessionId) {
		return (Session) cacheManager.getIamCache(CACHE_PREFIX_IAM_SESSION).get(new CacheKey(sessionId, IamSession.class));
	}

}