package com.wl4g.iam.gateway.console;

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;

import com.wl4g.iam.gateway.console.args.UpdatingRefreshDelayArgument;
import com.wl4g.iam.gateway.route.TimingRoutesRefresher;
import com.wl4g.iam.gateway.route.config.RouteProperties;
import com.wl4g.iam.gateway.route.repository.AbstractRouteRepository;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.shell.common.annotation.ShellMethod;
import com.wl4g.shell.core.handler.SimpleShellContext;
import com.wl4g.shell.springboot.annotation.ShellComponent;

import reactor.core.publisher.Flux;

/**
 * {@link IamGatewayConsole}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-23
 * @since
 */
@ShellComponent
public class IamGatewayConsole {

    final protected SmartLogger log = getLogger(getClass());

    @Autowired
    protected RouteProperties config;

    @Autowired
    private AbstractRouteRepository routeRepository;

    @Autowired
    protected TimingRoutesRefresher refresher;

    /**
     * Manual refresh gateway configuration all from cache(redis).
     * 
     * @param context
     */
    @ShellMethod(keys = "refresh", group = DEFAULT_GATEWAY_SHELL_GROUP, help = "Refresh gateway configuration all")

    public void refresh(SimpleShellContext context) {
        try {
            context.printf("Refreshing configuration ...");
            refresher.getRefresher().refreshRoutes();
        } catch (Exception e) {
            log.error("", e);
        }

        Flux<RouteDefinition> routes = routeRepository.getRouteDefinitions();

        // Print result message.
        StringBuilder res = new StringBuilder(100);
        res.append("Refresh succeeded. The current configuration information is: ");
        res.append(LINE_SEPARATOR);
        res.append("\t");
        res.append("----route info----\n"); // TODO

        routes.subscribe(r -> res.append(toJSONString(r)).append("\n"));

        context.printf(res.toString());
        context.completed();
    }

    /**
     * Updating refresh delay time.
     * 
     * @param context
     * @param arg
     * @return
     */
    @ShellMethod(keys = "updateRefreshDelay", group = DEFAULT_GATEWAY_SHELL_GROUP, help = "Update configuration refresh schedule delay(Ms)")
    public void updateRefreshDelay(SimpleShellContext context, UpdatingRefreshDelayArgument arg) {
        try {
            config.setRefreshDelayMs(arg.getRefreshDelayMs());

            // Restart refresher
            refresher.restartRefresher();
        } catch (Exception e) {
            log.error("", e);
        }

        context.printf("Successful updated, The now refreshDelayMs is: ".concat(valueOf(config.getRefreshDelayMs())));
        context.completed();
    }

    /** Gateway shell console group name. */
    final public static String DEFAULT_GATEWAY_SHELL_GROUP = "Gateway server shell commands";

}
