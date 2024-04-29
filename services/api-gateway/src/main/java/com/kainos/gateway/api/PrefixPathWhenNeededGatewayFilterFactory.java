package com.kainos.gateway.api;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * This is a variation of PrefixPathGatewayFilterFactory and SetPathGatewayFilterFactory
 * that prevents undesirable behaviour when the URI of route defines a path, but
 * is it omitted (see RouteToRequestUrlFilter::filter) when constructing the URI of
 * final request to the backend.
 * See also https://github.com/spring-cloud/spring-cloud-gateway/issues/881#issuecomment-470182593
 *
 * Example:
 * - given the request to Spring Cloud Gateway is GET /something,
 * - and Spring Cloud Gateway has a route configured with:
 *   - uri: http://localhost/backend
 *   - predicate: GET /something
 *
 * Then final request will be:
 * - without this filter: http://localhost/something (default behaviour)
 * - with this filter: http://localhost/backend/something
 *
 * @see org.springframework.cloud.gateway.filter.factory.PrefixPathGatewayFilterFactory
 * @see org.springframework.cloud.gateway.filter.factory.SetPathGatewayFilterFactory
 * @see org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter
 */

@Component
public class PrefixPathWhenNeededGatewayFilterFactory
    extends AbstractGatewayFilterFactory<PrefixPathWhenNeededGatewayFilterFactory.Config> {

    public PrefixPathWhenNeededGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);

            ServerHttpRequest req = exchange.getRequest();
            String path = req.getURI().getRawPath();
            String newPath = route.getUri().getPath() + path;

            ServerHttpRequest request = req.mutate().path(newPath).build();

            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, request.getURI());

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    public static class Config { }
}
