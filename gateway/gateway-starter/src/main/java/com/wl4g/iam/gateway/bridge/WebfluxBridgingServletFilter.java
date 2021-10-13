package com.wl4g.iam.gateway.bridge;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import static reactor.core.publisher.Flux.just;
import static org.springframework.http.HttpStatus.OK;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.web.rest.RespBase;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.google.common.base.Charsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isAnyEmpty;

/**
 * {@link WebfluxBridgingServletFilter}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @author vjay
 * @version v1.0 2020-07-04
 * @since
 */
public class WebfluxBridgingServletFilter implements GlobalFilter, Ordered {

    protected final SmartLogger log = getLogger(getClass());

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * for example: </br>
     * 
     * <pre>
     * storedAppSecret=5aUpyX5X7wzC8iLgFNJuxqj3xJdNQw8yS
     * 
     * curl http://wl4g.debug:14085/openapi/v2/test?appId=oi554a94bc416e4edd9ff963ed0e9e25e6c10545&nonce=0L9GyULPfwsD3Swg&timestamp=1599637679878&signature=5ac8747ccc2b1b332e8445b496d0c38529b38fba2c1b8ca8490cbf2932e06943
     * 
     * </pre>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
        String appId = params.getFirst("appId");
        String nonce = params.getFirst("nonce");
        String timestamp = params.getFirst("timestamp");
        String signature = params.getFirst("signature");
        if (isAnyEmpty(appId, nonce, timestamp, signature)) {
            log.warn("appId/nonce/timestamp/signature is requires");
            return writeResponse(4000, "Invalid parameters", exchange);
        }

        return chain.filter(exchange);
    }

    /**
     * Write http restful response
     * 
     * @param errcode
     * @param errmsg
     * @param exchange
     * @return
     */
    protected Mono<Void> writeResponse(int errcode, String errmsg, ServerWebExchange exchange) {
        RespBase<?> resp = RespBase.create().withCode(errcode).withMessage(errmsg);

        ServerHttpResponse response = exchange.getResponse();
        DataBuffer buffer = response.bufferFactory().wrap(resp.asJson().getBytes(UTF_8));
        response.setStatusCode(OK);
        return response.writeWith(just(buffer));
    }

}
