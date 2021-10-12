package com.wl4g.gateway.server.bridge;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.collection.CollectionUtils2.safeSet;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.TypeConverts.safeLongToInt;
import static java.util.Collections.enumeration;
import static java.util.Collections.singletonMap;
import static java.util.Locale.US;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * {@link WebfluxHttpServletRequest}
 * 
 * @since
 */
public class WebfluxHttpServletRequest implements HttpServletRequest {

    /** {@link ServerHttpRequest} */
    protected final ServerWebExchange exchange;

    // --- Temporary fields. ---

    private transient volatile String queryStringCache;
    private transient volatile Map<String, String[]> parameterMapCache;
    private transient Cookie[] cookiesCache;

    public WebfluxHttpServletRequest(ServerWebExchange exchange) {
        notNullOf(exchange, "exchange");
        this.exchange = exchange;
    }

    @Override
    public Object getAttribute(String name) {
        return exchange.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        exchange.getAttributes().put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        exchange.getAttributes().remove(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return enumeration(safeSet(exchange.getAttributes().keySet()));
    }

    @Override
    public int getContentLength() {
        return safeLongToInt(exchange.getRequest().getHeaders().getContentLength());
    }

    @Override
    public long getContentLengthLong() {
        return exchange.getRequest().getHeaders().getContentLength();
    }

    @Override
    public String getContentType() {
        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        // return
        // mediaType.getType().concat("/").concat(mediaType.getSubtype());
        return mediaType.toString();
    }

    @Override
    public String getParameter(String name) {
        return exchange.getRequest().getQueryParams().getFirst(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return enumeration(safeSet(exchange.getRequest().getQueryParams().keySet()));
    }

    @Override
    public String[] getParameterValues(String name) {
        return exchange.getRequest().getQueryParams().get(name).toArray(new String[] {});
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (isNull(this.parameterMapCache)) {
            synchronized (this) {
                if (isNull(this.parameterMapCache)) {
                    this.parameterMapCache = safeMap(exchange.getRequest().getQueryParams()).entrySet().stream()
                            .map(e -> singletonMap(e.getKey(), safeList(e.getValue()).toArray(new String[] {})))
                            .flatMap(e -> e.entrySet().stream()).collect(toMap(e -> e.getKey(), e -> e.getValue()));
                }
            }
        }
        return this.parameterMapCache;
    }

    @Override
    public String getProtocol() {
        // TODO
        return "HTTP/1.1";
    }

    @Override
    public String getScheme() {
        return exchange.getRequest().getURI().getScheme();
    }

    @Override
    public String getServerName() {
        return exchange.getRequest().getHeaders().getHost().getHostName();
    }

    @Override
    public int getServerPort() {
        return exchange.getRequest().getHeaders().getHost().getPort();
    }

    @Override
    public String getRemoteAddr() {
        // return
        // exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        return exchange.getRequest().getHeaders().getFirst("REMOTE_ADDR");
    }

    @Override
    public String getRemoteHost() {
        // return
        // exchange.getRequest().getRemoteAddress().getAddress().getHostName();
        return exchange.getRequest().getHeaders().getFirst("REMOTE_HOST");
    }

    @Override
    public int getRemotePort() {
        return exchange.getRequest().getRemoteAddress().getPort();
    }

    @Override
    public String getLocalName() {
        // return
        // exchange.getRequest().getLocalAddress().getAddress().getLocalHost().getHostAddress();
        return exchange.getRequest().getLocalAddress().getAddress().getHostName();
    }

    @Override
    public String getLocalAddr() {
        return exchange.getRequest().getLocalAddress().getAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return exchange.getRequest().getLocalAddress().getPort();
    }

    @Override
    public Locale getLocale() {
        return exchange.getRequest().getHeaders().getContentLanguage();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return enumeration(safeList(exchange.getRequest().getHeaders().getAcceptLanguageAsLocales()));
    }

    @Override
    public boolean isSecure() {
        return exchange.getRequest().getURI().getScheme().toUpperCase(US).startsWith("HTTPS");
    }

    @Override
    public Cookie[] getCookies() {
        if (isNull(this.cookiesCache)) {
            synchronized (this) {
                if (isNull(this.cookiesCache)) {
                    this.cookiesCache = safeMap(exchange.getRequest().getCookies()).values().stream()
                            .map(cs -> safeList(cs).stream().map(c -> convertHttpCookie(c))).collect(toList())
                            .toArray(new Cookie[] {});
                }
            }
        }
        return this.cookiesCache;
    }

    @Override
    public long getDateHeader(String name) {
        return exchange.getRequest().getHeaders().getFirstDate(name);
    }

    @Override
    public String getHeader(String name) {
        return exchange.getRequest().getHeaders().getFirst(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return enumeration(safeList(exchange.getRequest().getHeaders().get(name)));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return enumeration(safeSet(exchange.getRequest().getHeaders().keySet()));
    }

    @Override
    public int getIntHeader(String name) {
        return Integer.parseInt(exchange.getRequest().getHeaders().getFirst(name));
    }

    @Override
    public String getMethod() {
        return exchange.getRequest().getMethodValue();
    }

    @Override
    public String getContextPath() {
        return exchange.getRequest().getPath().contextPath().value();
    }

    @Override
    public String getQueryString() {
        if (isNull(queryStringCache)) {
            synchronized (this) {
                if (isNull(queryStringCache)) {
                    StringBuffer queryString = new StringBuffer(32);
                    Iterator<Entry<String, List<String>>> its = exchange.getRequest().getQueryParams().entrySet().iterator();
                    while (its.hasNext()) {
                        Entry<String, List<String>> ent = its.next();
                        String name = ent.getKey();
                        List<String> vals = ent.getValue();
                        // keyvalues
                        queryString.append(name);
                        queryString.append("=");
                        Iterator<String> it = vals.iterator();
                        while (it.hasNext()) {
                            queryString.append(it.next());
                            if (it.hasNext()) {
                                queryString.append(",");
                            }
                        }
                        // keyvalues part
                        if (its.hasNext()) {
                            queryString.append("&");
                        }
                    }
                    this.queryStringCache = queryString.toString();
                }
            }
        }
        return queryStringCache;
    }

    @Override
    public String getRemoteUser() {
        return exchange.getRequest().getURI().getUserInfo();
    }

    @Override
    public String getRequestedSessionId() {
        return null; // @see shiro
    }

    @Override
    public String getRequestURI() {
        // @see shiro
        return exchange.getRequest().getURI().getPath();
    }

    @Override
    public StringBuffer getRequestURL() {
        // @see shiro
        return new StringBuffer(exchange.getRequest().getURI().toString());
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return true; // @see shiro
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return true; // @see shiro
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false; // @see shiro
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false; // @see shiro
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRealPath(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServletPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getSession(boolean create) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * Converting tangible cookie to {@link HttpCookie}
     * 
     * @param c
     * @return
     */
    private final Cookie convertHttpCookie(org.springframework.http.HttpCookie c) {
        return new Cookie(c.getName(), c.getValue());
    }

}