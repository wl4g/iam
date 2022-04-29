package com.wl4g.iam.gateway.security.oauth2;
//package com.wl4g.iam.gateway.auth;
//
//import java.time.Duration;
//
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
//import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
//import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
//import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
//import org.springframework.security.oauth2.core.OAuth2AccessToken;
//import org.springframework.web.server.ServerWebExchange;
//
//import reactor.core.publisher.Mono;
//
///**
// * Token Relay Gateway Filter with Token Refresh. This can be removed when issue
// * see: https://github.com/spring-cloud/spring-cloud-security/issues/175 is
// * closed. Implementierung in Anlehnung an
// * {@link ServerOAuth2AuthorizedClientExchangeFilterFunction}
// * 
// * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
// * @version 2022-03-25 v3.0.0
// * @since v3.0.0
// */
//public class TokenRelayRefreshGatewayFilter extends AbstractGatewayFilterFactory<Object> {
//
//    private static final Duration accessTokenExpiresSkew = Duration.ofSeconds(3);
//    private final ReactiveOAuth2AuthorizedClientManager oauth2ClientManager;
//
//    public TokenRelayRefreshGatewayFilter(ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
//            ReactiveClientRegistrationRepository clientRegistrationRepository) {
//        super(Object.class);
//        this.oauth2ClientManager = createDefaultAuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
//    }
//
//    private static ReactiveOAuth2AuthorizedClientManager createDefaultAuthorizedClientManager(
//            ReactiveClientRegistrationRepository clientRegistrationRepository,
//            ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
//
//        final ReactiveOAuth2AuthorizedClientProvider clientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
//                .authorizationCode()
//                .refreshToken(configurer -> configurer.clockSkew(accessTokenExpiresSkew))
//                .clientCredentials(configurer -> configurer.clockSkew(accessTokenExpiresSkew))
//                .password(configurer -> configurer.clockSkew(accessTokenExpiresSkew))
//                .build();
//        final DefaultReactiveOAuth2AuthorizedClientManager clientManager = new DefaultReactiveOAuth2AuthorizedClientManager(
//                clientRegistrationRepository, authorizedClientRepository);
//        clientManager.setAuthorizedClientProvider(clientProvider);
//
//        return clientManager;
//    }
//
//    @Override
//    public GatewayFilter apply(Object config) {
//        return (exchange, chain) -> exchange.getPrincipal()
//                // .log("token-relay-filter")
//                .filter(principal -> principal instanceof OAuth2AuthenticationToken)
//                .cast(OAuth2AuthenticationToken.class)
//                .flatMap(this::getOauth2Client)
//                .map(OAuth2AuthorizedClient::getAccessToken)
//                .map(token -> withBearerAuth(exchange, token))
//                // TODO: adjustable behavior if empty
//                .defaultIfEmpty(exchange)
//                .flatMap(chain::filter);
//    }
//
//    private ServerWebExchange withBearerAuth(ServerWebExchange exchange, OAuth2AccessToken token) {
//        return exchange.mutate().request(r -> r.headers(headers -> headers.setBearerAuth(token.getTokenValue()))).build();
//    }
//
//    private Mono<OAuth2AuthorizedClient> getOauth2Client(OAuth2AuthenticationToken token) {
//        final String clientRegistrationId = token.getAuthorizedClientRegistrationId();
//        return Mono.defer(() -> oauth2ClientManager.authorize(createOAuth2AuthorizeRequest(clientRegistrationId, token)));
//    }
//
//    private OAuth2AuthorizeRequest createOAuth2AuthorizeRequest(String clientRegistrationId, Authentication principal) {
//        return OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId).principal(principal).build();
//    }
//
//}