package com.wl4g.iam.gateway.console;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;

import com.wl4g.iam.gateway.console.args.UpdatingRefreshDelayArgument;
import com.wl4g.iam.gateway.route.TimeBasedRouteRefresher;
import com.wl4g.iam.gateway.route.config.RouteProperties;
import com.wl4g.shell.common.annotation.ShellMethod;
import com.wl4g.shell.core.handler.SimpleShellContext;
import com.wl4g.shell.springboot.annotation.ShellComponent;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link IamGatewayConsole}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-23
 * @since
 */
@Slf4j
@ShellComponent
public class IamGatewayConsole {

    private @Autowired RouteProperties config;
    private @Autowired TimeBasedRouteRefresher refresher;
    // private @Autowired AbstractRouteRepository routeRepository;
    private @Autowired RouteLocator routeLocator;

    /**
     * Print routes configuration.
     * 
     * @param context
     */
    @ShellMethod(keys = "routes", group = DEFAULT_SHELL_GROUP, help = "Print routes configuration")
    public void routes(SimpleShellContext context) {
        // Print result message.
        StringBuilder res = new StringBuilder(100);
        res.append("Routes configuration:");
        res.append(LINE_SEPARATOR);
        res.append("\t");

        routeLocator.getRoutes().subscribe(r -> res.append(toJSONString(r)).append("\n"));

        context.printf(res.toString());
        context.completed();
    }

    /**
     * Manual refresh routes configuration.
     * 
     * @param context
     */
    @ShellMethod(keys = "refresh", group = DEFAULT_SHELL_GROUP, help = "Refresh routes configuration")
    public void refresh(SimpleShellContext context) {
        try {
            context.printf("Refreshing routes ...");
            refresher.getRefresher().refreshRoutes();
        } catch (Exception e) {
            log.error("", e);
        }

        // Print result message.
        StringBuilder res = new StringBuilder(100);
        res.append("Refreshed completed, Routes configuration:");
        res.append(LINE_SEPARATOR);
        res.append("\t");

        // Flux<RouteDefinition> routes = routeRepository.getRouteDefinitions();
        routeLocator.getRoutes().subscribe(r -> res.append(toJSONString(r)).append("\n"));

        context.printf(res.toString());
        context.completed();
    }

    /**
     * Update refresher configuration.
     * 
     * @param context
     * @param arg
     * @return
     */
    @ShellMethod(keys = "updateRefresh", group = DEFAULT_SHELL_GROUP, help = "Update configuration refresh schedule delay(Ms)")
    public void updateRefresh(SimpleShellContext context, UpdatingRefreshDelayArgument arg) {
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
    public static final String DEFAULT_SHELL_GROUP = "Iam Gateway Shell Console";

}
