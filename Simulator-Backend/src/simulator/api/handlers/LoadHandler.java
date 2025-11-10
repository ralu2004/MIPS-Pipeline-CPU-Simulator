package simulator.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import simulator.ProgramLoader;
import simulator.api.ServerContext;
import simulator.api.utils.HttpUtils;

import java.io.IOException;
import java.util.Map;

/**
 * POST /api/load?start=0 -> load hex instructions into memory
 */
public class LoadHandler implements HttpHandler {

    private final ServerContext context;

    public LoadHandler(ServerContext context) {
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
            int startAddress = HttpUtils.parseIntOrDefault(params.get("start"), 0);

            String body = HttpUtils.readBody(exchange);
            String[] lines = body.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);

            ProgramLoader.ProgramLoadResult result =
                    ProgramLoader.loadFromHexStrings(context.cpuState, lines, startAddress);

            String json = String.format(
                    "{\"loaded\":%d,\"start\":%d,\"end\":%d}",
                    result.loadedCount, result.startAddress, result.endAddress
            );
            HttpUtils.sendJson(exchange, 200, json);

        } catch (Exception e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        }
    }
}