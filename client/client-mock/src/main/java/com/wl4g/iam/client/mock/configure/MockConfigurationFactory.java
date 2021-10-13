package com.wl4g.iam.client.mock.configure;

import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.isNullOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.wl4g.iam.common.subject.IamPrincipal.PrincipalOrganization;

import static com.google.common.net.InetAddresses.isInetAddress;
import static com.wl4g.component.common.web.WebUtils2.getHttpRemoteAddr;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static com.wl4g.component.common.web.CookieUtils.getCookie;

/**
 * {@link MockConfigurationFactory}
 *
 * @since
 */
public final class MockConfigurationFactory {

	/**
	 * Mock for IAM client authentication userinfo.
	 */
	private final Map<MockFilter, MockUserCredentials> mockRegistry = new ConcurrentHashMap<>(4);

	/**
	 * Gets mock user info by principal
	 * 
	 * @param principal
	 * @return
	 */
	public MockAuthzInfo getMockAuthcInfo(String principal) {
		return mockRegistry.entrySet().stream()
				.filter(e -> StringUtils.equals(principal, e.getValue().getAuthzInfo().getPrincipal()))
				.map(e -> e.getValue().getAuthzInfo()).findFirst().filter(e -> !isNull(e)).orElse(null);
	}

	/**
	 * Mock user authentication info collections
	 * 
	 * @return
	 */
	public Collection<MockUserCredentials> getMockUserCredentials() {
		return mockRegistry.values();
	}

	/**
	 * Matching mock user credentials.
	 * 
	 * @param request
	 * @return
	 */
	public MockUserCredentials matchMockCredentials(HttpServletRequest request) {
		return mockRegistry.entrySet().stream().filter(e -> {
			MockFilter filter = e.getKey();
			return filter.getType().getParser().matchValue(request, filter.getValue());
		}).map(e -> e.getValue()).findFirst().filter(e -> !isNull(e)).orElse(null);
	}

	/**
	 * Registering mock configureation and authz credentials information.
	 * 
	 * @param filter
	 * @param info
	 */
	void register(MockFilter filter, MockAuthzInfo info) {
		isNullOf(mockRegistry.putIfAbsent(filter, new MockUserCredentials(info)),
				"Cannot register mock filter, becasue already exist");
	}

	/**
	 * {@link MockAuthcInfo}
	 *
	 * @since
	 */
	public static class MockAuthcInfo {

		/**
		 * Mock iam client authentication accessToken(credentials) info.
		 */
		private final String accessToken;

		/**
		 * Mock iam client authentication session info.
		 */
		private final String sessionId;

		public MockAuthcInfo(String accessToken, String sessionId) {
			hasTextOf(accessToken, "mockAccessToken");
			hasTextOf(sessionId, "mockSessionId");
			this.accessToken = accessToken;
			this.sessionId = sessionId;
		}

		public String getAccessToken() {
			return accessToken;
		}

		public String getSessionId() {
			return sessionId;
		}

	}

	/**
	 * {@link MockAuthzInfo}
	 *
	 * @since
	 */
	public static class MockAuthzInfo {

		/** Mock principalId */
		final private String principalId;

		/** Mock principal */
		final private String principal;

		/** Mock roles */
		final private String roles;

		/** Mock permissions */
		final private String permissions;

		/** {@link PrincipalOrganization} */
		final private PrincipalOrganization organization;

		public MockAuthzInfo(String principalId, String principal, String roles, String permissions,
				PrincipalOrganization organization) {
			super();
			this.principalId = principalId;
			this.principal = principal;
			this.roles = roles;
			this.permissions = permissions;
			this.organization = organization;
		}

		public String getPrincipalId() {
			return principalId;
		}

		public String getPrincipal() {
			return principal;
		}

		public String getRoles() {
			return roles;
		}

		public String getPermissions() {
			return permissions;
		}

		public PrincipalOrganization getOrganization() {
			return organization;
		}

	}

	/**
	 * {@link MockFilter}
	 *
	 * @since
	 */
	public static class MockFilter {

		final private MockFilterType type;
		final private String value;

		public MockFilter(MockFilterType type, String value) {
			notNullOf(type, "mockFilterType");
			// hasTextOf(value, "mockFilterValue"); // type=All value is null
			this.type = type;
			this.value = value;
		}

		public MockFilterType getType() {
			return type;
		}

		public String getValue() {
			return value;
		}

		/**
		 * {@link MockFilterType}
		 *
		 * @since
		 */
		public static enum MockFilterType {

			All((request, nameValue) -> true),

			Ip((request, nameValue) -> {
				String clientAddr = getHttpRemoteAddr(request);
				// If the proxy is obtained from the request header, it may not
				// be the IP format string.
				if (!isInetAddress(clientAddr)) { // not Ip?
					try {
						clientAddr = InetAddress.getByName(clientAddr).getHostAddress();
					} catch (UnknownHostException e) {
						throw new IllegalStateException(e);
					}
				}
				return StringUtils.equals(clientAddr, nameValue);
			}),

			Host((request, nameValue) -> {
				String clientHost = ((HttpServletRequest) request).getHeader("Host");
				clientHost = isBlank(clientHost) ? request.getServerName() : clientHost;
				return StringUtils.equals(clientHost, nameValue);
			}),

			Query((request, nameValue) -> {
				String[] parts = split(nameValue, "=");
				return StringUtils.equals(request.getParameter(parts[0]), parts[1]);
			}),

			Header((request, nameValue) -> {
				String[] parts = split(nameValue, "=");
				return StringUtils.equals(request.getHeader(parts[0]), parts[1]);
			}),

			Cookie((request, nameValue) -> {
				String[] parts = split(nameValue, "=");
				return StringUtils.equals(getCookie(request, parts[0]), parts[1]);
			}),

			Request((request, nameValue) -> {
				boolean matched = Query.getParser().matchValue(request, nameValue);
				matched = matched ? matched : Header.getParser().matchValue(request, nameValue);
				matched = matched ? matched : Cookie.getParser().matchValue(request, nameValue);
				return matched;
			});

			/** {@link MockFilterParser} */
			private final MockFilterParser parser;

			private MockFilterType(MockFilterParser parser) {
				notNullOf(parser, "parser");
				this.parser = parser;
			}

			public MockFilterParser getParser() {
				return parser;
			}

			/**
			 * Parse mock filter real value by type
			 * 
			 * @param request
			 * @param type
			 * @return
			 */
			public static MockFilterType safeOf(String filterType) {
				for (MockFilterType type : values()) {
					if (type.name().equalsIgnoreCase(filterType)) {
						return type;
					}
				}
				return null;
			}

			/**
			 * Parse mock filter real value by type
			 * 
			 * @param request
			 * @param type
			 * @return
			 */
			public static MockFilterType of(String filterType) {
				MockFilterType type = safeOf(filterType);
				if (isNull(type)) {
					throw new UnsupportedOperationException(format("Not supported mock filterType: %s", filterType));
				}
				return type;
			}

			/**
			 * {@link MockFilterParser}
			 *
			 * @since
			 */
			static interface MockFilterParser {
				boolean matchValue(HttpServletRequest request, String nameValue);
			}

		}

	}

	/**
	 * {@link MockUserCredentials}
	 * 
	 * @sine v1.0.0
	 * @see
	 */
	public static class MockUserCredentials {

		private final MockAuthzInfo authzInfo;
		private MockAuthcInfo authcInfo;

		public MockUserCredentials(MockAuthzInfo authzInfo) {
			notNullOf(authzInfo, "authcInfo");
			this.authzInfo = authzInfo;
		}

		public MockAuthzInfo getAuthzInfo() {
			return authzInfo;
		}

		public MockAuthcInfo getAuthcInfo() {
			return authcInfo;
		}

		public void setAuthcInfo(MockAuthcInfo authcInfo) {
			this.authcInfo = authcInfo;
		}

	}

}
