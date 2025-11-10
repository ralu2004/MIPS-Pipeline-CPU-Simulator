package simulator.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import simulator.api.utils.HttpUtils;

import java.io.IOException;

/**
 * GET /api/health -> health check endpoint for the api
 */
public class HealthHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handleCors(exchange)) return;
        HttpUtils.sendText(exchange, 200, "OK");
    }
}