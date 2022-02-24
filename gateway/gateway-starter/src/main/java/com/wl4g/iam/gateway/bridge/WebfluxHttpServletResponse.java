package com.wl4g.iam.gateway.bridge;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ServerWebExchange;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.valueOf;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link WebfluxHttpServletResponse}
 * 
 * @since
 */
public class WebfluxHttpServletResponse implements HttpServletResponse {

    /** {@link ServerHttpRequest} */
    protected final ServerWebExchange exchange;

    public WebfluxHttpServletResponse(ServerWebExchange exchange) {
        notNullOf(exchange, "exchange");
        this.exchange = exchange;
    }

    @Override
    public String getContentType() {
        return exchange.getResponse().getHeaders().getContentType().toString();
    }

    @Override
    public void setContentLength(int len) {
        exchange.getResponse().getHeaders().setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        exchange.getResponse().getHeaders().setContentLength(len);
    }

    @Override
    public void setContentType(String type) {
        exchange.getResponse().getHeaders().setContentType(MediaType.valueOf(type));
    }

    @Override
    public boolean isCommitted() {
        return exchange.getResponse().isCommitted();
    }

    @Override
    public void setLocale(Locale loc) {
        exchange.getResponse().getHeaders().setAcceptLanguageAsLocales(singletonList(loc));
    }

    @Override
    public Locale getLocale() {
        Optional<Locale> loc = exchange.getResponse().getHeaders().getAcceptLanguageAsLocales().stream().findFirst();
        return loc.isPresent() ? loc.get() : null;
    }

    @Override
    public void addCookie(Cookie c) {
        exchange.getResponse().addCookie(ResponseCookie.from(c.getName(), c.getValue()).domain(c.getDomain())
                .httpOnly(c.isHttpOnly()).maxAge(c.getMaxAge()).path(c.getPath()).secure(c.getSecure()).build());
    }

    @Override
    public boolean containsHeader(String name) {
        return exchange.getResponse().getHeaders().containsKey(name);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(sc));
        // TODO
    }

    @Override
    public void sendError(int sc) throws IOException {
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(sc));
        // TODO
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        exchange.getResponse().getHeaders().setLocation(URI.create(location));
    }

    @Override
    public void setDateHeader(String name, long date) {
        exchange.getResponse().getHeaders().setDate(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        exchange.getResponse().getHeaders().add(name, valueOf(date));
    }

    @Override
    public void setHeader(String name, String value) {
        exchange.getResponse().getHeaders().set(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        exchange.getResponse().getHeaders().add(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        exchange.getResponse().getHeaders().set(name, valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        exchange.getResponse().getHeaders().add(name, valueOf(value));

    }

    @Override
    public void setStatus(int sc) {
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(sc));
    }

    @Override
    public void setStatus(int sc, String sm) {
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(sc));
        // TODO
    }

    @Override
    public int getStatus() {
        return exchange.getResponse().getStatusCode().value();
    }

    @Override
    public String getHeader(String name) {
        return exchange.getResponse().getHeaders().getFirst(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return exchange.getResponse().getHeaders().get(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return exchange.getResponse().getHeaders().keySet();
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBufferSize(int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeURL(String url) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see {@link org.apache.shiro.web.servlet.ShiroHttpServletResponse#encodeRedirectURL(String)}
     */
    @Override
    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public String encodeUrl(String url) {
        return null;
    }

    @Deprecated
    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        throw new UnsupportedOperationException();
    }

}
