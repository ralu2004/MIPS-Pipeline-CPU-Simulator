package simulator.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import simulator.api.ServerContext;
import simulator.api.utils.HttpUtils;

import java.io.IOException;
import java.util.Map;

/**
 * POST /api/step?cycles=1 -> advances the simulation by N clock cycles
 */
public class StepHandler implements HttpHandler {

    private final ServerContext context;

    public StepHandler(ServerContext context) {
        this.context = context;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handleCors(exchange)) return;

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendError(exchange, 405, "Method not allowed");
            return;
        }

        try {
            Map<String, String> params = HttpUtils.parseQueryParams(exchange.getRequestURI());
            int cycles = Math.max(1, HttpUtils.parseIntOrDefault(params.get("cycles"), 1));

            context.clock.run(cycles);

            String json = String.format("{\"cycles\":%d}", cycles);
            HttpUtils.sendJson(exchange, 200, json);

        } catch (Exception e) {
            HttpUtils.sendError(exchange, 500, e.getMessage());
        }
    }
}