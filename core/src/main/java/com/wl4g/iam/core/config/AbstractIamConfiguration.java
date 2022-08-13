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
package com.wl4g.iam.core.config;

import static com.wl4g.iam.common.constant.FastCasIAMConstants.BEAN_SESSION_RESOURCE_MSG_BUNDLER;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.shiro.event.EventBus;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.ShiroEventBusBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.NameableFilter;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.wl4g.iam.common.i18n.SessionResourceMessageBundler;
import com.wl4g.iam.core.annotation.FastCasController;
import com.wl4g.iam.core.annotation.IamFilter;
import com.wl4g.iam.core.authz.EnhancedModularRealmAuthorizer;
import com.wl4g.iam.core.cache.JedisIamCacheManager;
import com.wl4g.iam.core.config.AbstractIamProperties.ParamProperties;
import com.wl4g.iam.core.crypto.AesIamCipherService;
import com.wl4g.iam.core.crypto.BlowfishIamCipherService;
import com.wl4g.iam.core.crypto.Des3IamCipherService;
import com.wl4g.iam.core.crypto.IamCipherService;
import com.wl4g.iam.core.crypto.IamCipherService.CipherCryptKind;
import com.wl4g.iam.core.filter.IamAuthenticationFilter;
import com.wl4g.iam.core.filter.chain.IamFilterChainManager;
import com.wl4g.iam.core.filter.chain.IamShiroFilterFactoryBean;
import com.wl4g.iam.core.mgt.IamSubjectFactory;
import com.wl4g.iam.core.realm.AbstractPermittingAuthorizingRealm;
import com.wl4g.iam.core.security.domain.HstsSecurityFilter;
import com.wl4g.iam.core.security.mitm.CipherRequestSecurityFilter;
import com.wl4g.iam.core.security.mitm.CipherRequestWrapper;
import com.wl4g.iam.core.security.mitm.CipherRequestWrapperFactory;
import com.wl4g.iam.core.session.mgt.IamSessionFactory;
import com.wl4g.iam.core.session.mgt.JedisIamSessionDAO;
import com.wl4g.iam.core.session.mgt.support.IamUidSessionIdGenerator;
import com.wl4g.iam.core.web.error.IamSmartErrorHandler;
import com.wl4g.iam.core.web.servlet.IamCookie;
import com.wl4g.infra.common.eventbus.EventBusSupport;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.core.framework.operator.GenericOperatorAdapter;
import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration.ErrorHandlerProperties;
import com.wl4g.infra.core.web.mapping.PrefixHandlerMappingSupport;
import com.wl4g.infra.support.cache.jedis.JedisClientFactoryBean;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * Abstract IAM common based configuration.
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2018年12月23日
 * @since
 */
public abstract class AbstractIamConfiguration extends PrefixHandlerMappingSupport {

    // ==============================
    // Locale i18n configuration.
    // ==============================

    /**
     * A delegate message resource. Note that this bean can instantiate multiple
     * different 'base-names', so the name must be unique
     *
     * @return
     */
    @Bean(BEAN_SESSION_RESOURCE_MSG_BUNDLER)
    public SessionResourceMessageBundler sessionResourceMessageBundler() {
        return new SessionResourceMessageBundler(AbstractIamConfiguration.class);
    }

    // ==============================
    // S H I R O _ C O N F I G's.
    // ==============================

    @Bean
    public FilterChainManager filterChainManager() {
        return new IamFilterChainManager();
    }

    @Bean
    public EnhancedModularRealmAuthorizer enhancedModularRealmAuthorizer(List<AbstractPermittingAuthorizingRealm> realms) {
        // Register define realm.
        return new EnhancedModularRealmAuthorizer(realms.stream().collect(toList()));
    }

    @Bean
    public IamShiroFilterFactoryBean iamFilterFactoryBean(
            AbstractIamProperties<? extends ParamProperties> config,
            DefaultWebSecurityManager securityManager,
            FilterChainManager chainManager) {
        /*
         * Note: The purpose of using Iam Shiro FilterFactory Bean is to use Iam
         * Path Matching Filter Chain Resolver, while Iam Path Matching Filter
         * Chain Resolver mainly implements the servlet/filter matching
         * specification of getChain () method for default enhancements (because
         * Shiro does not implement it, this causes serious problems)
         */
        IamShiroFilterFactoryBean iamFilter = new IamShiroFilterFactoryBean(chainManager);
        iamFilter.setSecurityManager(securityManager);

        /*
         * IAM server login page.(shiro default by "/login.jsp")
         */
        iamFilter.setLoginUrl(config.getLoginUri());
        // Default login success callback URL.
        iamFilter.setSuccessUrl(config.getSuccessUri());
        // IAM server 403 page URL
        iamFilter.setUnauthorizedUrl(config.getUnauthorizedUri());

        // Register define filters.
        Map<String, Filter> filters = new LinkedHashMap<>();
        // Register define filter mapping.
        Map<String, String> filterChain = new LinkedHashMap<>();
        actx.getBeansWithAnnotation(IamFilter.class).values().stream().forEach(filter -> {
            String filterName = null, uriPertten = null;
            if (filter instanceof NameableFilter) {
                filterName = (String) invokeMethod(findMethod(filter.getClass(), "getName"), filter);
            }
            if (filter instanceof IamAuthenticationFilter) {
                uriPertten = ((IamAuthenticationFilter) filter).getUriMapping();
            }
            notNull(filterName, "'filterName' must not be null");
            notNull(uriPertten, "'uriPertten' must not be null");

            if (filters.putIfAbsent(filterName, (Filter) filter) != null) {
                throw new IllegalStateException(format("Already filter. [%s]", filterName));
            }
            if (filterChain.putIfAbsent(uriPertten, filterName) != null) {
                throw new IllegalStateException(format("Already filter mapping. [%s] = %s", uriPertten, filterName));
            }
        });
        // Filter chain definition register
        iamFilter.setFilters(filters);

        // Add external filter chain configuration
        config.getFilterChain().forEach((uriPertten, filterName) -> {
            if (filterChain.putIfAbsent(uriPertten, filterName) != null) {
                throw new IllegalStateException(format("Already filter mapping. [%s] = %s", uriPertten, filterName));
            }
        });

        // Filter chain mappings register
        iamFilter.setFilterChainDefinitionMap(filterChain);

        return iamFilter;
    }

    /**
     * Using {@link Lazy} loading to solve cycle injection problem.
     * 
     * @param config
     * @param factoryBean
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IamSubjectFactory iamSubjectFactory(
            AbstractIamProperties<? extends ParamProperties> config,
            @Lazy IamShiroFilterFactoryBean factoryBean) {
        return new IamSubjectFactory(config, factoryBean);
    }

    @Bean
    public JedisIamCacheManager jedisIamCacheManager(
            AbstractIamProperties<? extends ParamProperties> config,
            JedisClientFactoryBean factory) throws Exception {
        return new JedisIamCacheManager(config.getCache().getPrefix(), factory.getObject());
    }

    @Bean
    public IamUidSessionIdGenerator iamUidSessionIdGenerator() {
        return new IamUidSessionIdGenerator();
    }

    @Bean
    public JedisIamSessionDAO jedisIamSessionDAO(
            AbstractIamProperties<? extends ParamProperties> config,
            JedisIamCacheManager cacheManager,
            IamUidSessionIdGenerator sessionIdGenerator) {
        JedisIamSessionDAO sessionDAO = new JedisIamSessionDAO(config, cacheManager);
        sessionDAO.setSessionIdGenerator(sessionIdGenerator);
        return sessionDAO;
    }

    @Bean
    public IamSessionFactory iamSessionFactory() {
        return new IamSessionFactory();
    }

    @Bean
    public IamCookie iamCookie(AbstractIamProperties<? extends ParamProperties> config) {
        return new IamCookie(config.getCookie());
    }

    /**
     * Ensuring the execution of beans that implement lifecycle functions within
     * Shiro
     *
     * @return
     */
    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorCreator = new DefaultAdvisorAutoProxyCreator();
        advisorCreator.setProxyTargetClass(true);
        return advisorCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authzAdvisor = new AuthorizationAttributeSourceAdvisor();
        authzAdvisor.setSecurityManager(securityManager);
        return authzAdvisor;
    }

    // ==============================
    // A U T H E N T I C A T I O N _ C O N F I G's.
    // ==============================

    // ==============================
    // A U T H E N T I C A T I O N _ R E G I S T R A T I O N _ C O N F I G's.
    // Reference See: http://www.hillfly.com/2017/179.html
    // org.apache.catalina.core.ApplicationFilterChain#internalDoFilter
    // ==============================

    // ==============================
    // A U T H O R I Z I N G _ R E A L M _ C O N F I G's.
    // ==============================

    // ==============================
    // A U T H E N T I C A T I O N _ H A N D L E R _ C O N F I G's.
    // ==============================

    // ==============================
    // I A M _ C O N T R O L L E R _ C O N F I G's.
    // ==============================

    /**
     * Ne IAM controller prefix request handler mapping.
     * 
     * @param mappingPrefix
     * @return
     */
    protected Object newIamControllerPrefixHandlerMapping(@NotBlank String mappingPrefix) {
        return super.newPrefixHandlerMapping(mappingPrefix, FastCasController.class);
    }

    // ==============================
    // IAM security protection's
    // ==============================

    //
    // C I P H E R _ A N D _ F I L T E R _ C O N F I G's.
    //

    @Bean
    public AesIamCipherService aesIamCipherService() {
        return new AesIamCipherService();
    }

    @Bean
    public BlowfishIamCipherService blowfishIamCipherService() {
        return new BlowfishIamCipherService();
    }

    @Bean
    public Des3IamCipherService des3IamCipherService() {
        return new Des3IamCipherService();
    }

    @Bean
    public GenericOperatorAdapter<CipherCryptKind, IamCipherService> compositeIamCipherServiceAdapter(
            List<IamCipherService> cipherServices) {
        return new GenericOperatorAdapter<CipherCryptKind, IamCipherService>(cipherServices) {
        };
    }

    /**
     * Can be used to extend and create a custom {@link CipherRequestWrapper}
     * instance.
     * 
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public CipherRequestWrapperFactory cipherRequestWrapperFactory() {
        return new CipherRequestWrapperFactory() {
        };
    }

    @Bean
    public CipherRequestSecurityFilter cipherRequestSecurityFilter(
            AbstractIamProperties<? extends ParamProperties> config,
            CipherRequestWrapperFactory factory) {
        return new CipherRequestSecurityFilter(config, factory);
    }

    @Bean
    public FilterRegistrationBean<CipherRequestSecurityFilter> cipherRequestSecurityFilterBean(
            CipherRequestSecurityFilter filter) {
        // Register cipher filter
        FilterRegistrationBean<CipherRequestSecurityFilter> filterBean = new FilterRegistrationBean<>(filter);
        filterBean.setOrder(ORDER_CIPHER_PRECEDENCE);
        // Cannot use '/*' or it will not be added to the container chain (only
        // '/**')
        filterBean.addUrlPatterns("/*");
        return filterBean;
    }

    //
    // D O M A I N _ S E C U I T Y _ F I L T E R _ C O N F I G's.
    //

    @Bean
    public HstsSecurityFilter hstsSecurityFilter(
            AbstractIamProperties<? extends ParamProperties> config,
            Environment environment) {
        return new HstsSecurityFilter(config, environment);
    }

    @Bean
    public FilterRegistrationBean<HstsSecurityFilter> hstsSecurityFilterBean(HstsSecurityFilter filter) {
        // Register cipher filter
        FilterRegistrationBean<HstsSecurityFilter> filterBean = new FilterRegistrationBean<>(filter);
        filterBean.setOrder(ORDER_HSTS_PRECEDENCE);
        // Cannot use '/*' or it will not be added to the container chain (only
        // '/**')
        filterBean.addUrlPatterns("/*");
        return filterBean;
    }

    // ==============================
    // IAM _ EVENT BUS _ C O N F I G's.
    // ==============================

    @Bean(destroyMethod = "close")
    public EventBusSupport iamEventBusSupport() {
        return new EventBusSupport(3);
    }

    @Bean
    public ShiroEventBusBeanPostProcessor shiroEventBusBeanPostProcessor(EventBusSupport eventBus) {
        final com.google.common.eventbus.EventBus bus = eventBus.getBus();
        return new ShiroEventBusBeanPostProcessor(new EventBus() {
            @Override
            public void unregister(Object object) {
                bus.unregister(object);
            }

            @Override
            public void register(Object object) {
                bus.register(object);
            }

            @Override
            public void publish(Object event) {
                bus.post(event);
            }
        });
    }

    // ==============================
    // IAM _ O T H E R _ C O N F I G's.
    // ==============================

    @Bean
    public IamSmartErrorHandler iamSmartErrorHandler(ErrorHandlerProperties config) {
        return new IamSmartErrorHandler(config);
    }

    // ==============================
    // REST T E M P L A T E _ C O N F I G's.
    // ==============================

    @Bean(BEAN_IAM_OKHTTP3_POOL)
    public ConnectionPool iamOkHttp3ConnectionPool(AbstractIamProperties<?> config) {
        return new ConnectionPool(config.getHttp().getMaxIdleConnections(), config.getHttp().getKeepAliveDuration(), MINUTES);
    }

    @Bean(BEAN_IAM_OKHTTP3_PROXY_SELECTOR)
    public IamDynamicProxySelector iamOkHttp3ProxySelector() {
        // see:okhttp3.internal.proxy.NullProxySelector
        return new IamDynamicProxySelector();
    }

    @Bean(BEAN_IAM_OKHTTP3_CLIENT)
    public OkHttpClient iamOkHttp3Client(
            AbstractIamProperties<?> config,
            @Qualifier(BEAN_IAM_OKHTTP3_POOL) ConnectionPool pool,
            @Qualifier(BEAN_IAM_OKHTTP3_PROXY_SELECTOR) IamDynamicProxySelector proxySelector) {
        return new OkHttpClient().newBuilder()
                .connectionPool(pool)
                .connectTimeout(config.getHttp().getConnectTimeout(), MILLISECONDS)
                .readTimeout(config.getHttp().getReadTimeout(), MILLISECONDS)
                .writeTimeout(config.getHttp().getWriteTimeout(), MILLISECONDS)
                .proxySelector(proxySelector)
                .build();
    }

    @Bean(BEAN_IAM_OKHTTP3_CLIENT_FACTORY)
    public OkHttp3ClientHttpRequestFactory iamOkHttp3ClientHttpRequestFactory(
            @Qualifier(BEAN_IAM_OKHTTP3_CLIENT) OkHttpClient client) {
        return new OkHttp3ClientHttpRequestFactory(client);
    }

    @Bean(BEAN_IAM_OKHTTP3_REST_TEMPLATE)
    public RestTemplate iamOkhttp3RestTemplate(
            @Qualifier(BEAN_IAM_OKHTTP3_CLIENT_FACTORY) OkHttp3ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    public static class IamDynamicProxySelector extends ProxySelector {
        private SmartLogger log = getLogger(getClass());

        private final Map<String, Proxy> selectProxyMap = new ConcurrentHashMap<>(16);

        public void register(@NotNull URI uri, @NotNull Proxy proxy) {
            if (nonNull(selectProxyMap.putIfAbsent(toSelectKey(notNullOf(uri, "uri")), notNullOf(proxy, "proxy")))) {
                throw new IllegalArgumentException(format("Register already select proxy for '%'=>'%s'", uri, proxy));
            }
        }

        @Override
        public List<Proxy> select(URI uri) {
            return singletonList(selectProxyMap.getOrDefault(toSelectKey(uri), Proxy.NO_PROXY));
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            log.warn("called:connectFailed ::: uri='{}', address='{}', cause='{}'", uri, sa, ioe.getLocalizedMessage());
        }

        private String toSelectKey(URI uri) {
            return uri.getScheme().concat(uri.getHost()).concat(valueOf(uri.getPort()));
        }
    }

    private static final String BEAN_IAM_OKHTTP3_POOL = "iamOkhttp3ConnectionPool";
    private static final String BEAN_IAM_OKHTTP3_CLIENT = "iamOkhttp3Client";
    private static final String BEAN_IAM_OKHTTP3_CLIENT_FACTORY = "iamOkHttp3ClientHttpRequestFactory";
    public static final String BEAN_IAM_OKHTTP3_PROXY_SELECTOR = "iamOkhttp3ProxySelector";
    public static final String BEAN_IAM_OKHTTP3_REST_TEMPLATE = "iamOkhttp3RestTemplate";

    //
    // Build-in security protection filter order-precedence definitions.
    //

    public static final int ORDER_HSTS_PRECEDENCE = Ordered.HIGHEST_PRECEDENCE + 8;
    public static final int ORDER_CORS_PRECEDENCE = Ordered.HIGHEST_PRECEDENCE + 9;
    public static final int ORDER_XSRF_PRECEDENCE = Ordered.HIGHEST_PRECEDENCE + 10;
    public static final int ORDER_CIPHER_PRECEDENCE = Ordered.HIGHEST_PRECEDENCE + 11;
    public static final int ORDER_REPAY_PRECEDENCE = Ordered.HIGHEST_PRECEDENCE + 12;

}