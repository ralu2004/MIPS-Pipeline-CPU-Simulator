package simulator.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import simulator.api.ServerContext;
import simulator.api.utils.HttpUtils;
import simulator.api.utils.StateSerializer;

import java.io.IOException;

/**
 * GET /api/state -> get current CPU and pipeline state as JSON
 */
public class StateHandler implements HttpHandler {

    private final ServerContext context;

    public StateHandler(ServerContext context) {
        this.context = context;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handleCors(exchange)) return;

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendError(exchange, 405, "Method not allowed");
            return;
        }

        try {
            String json = StateSerializer.serialize(context.cpuState, context.controller);
            HttpUtils.sendJson(exchange, 200, json);

        } catch (Exception e) {
            HttpUtils.sendError(exchange, 500, e.getMessage());
        }
    }
}