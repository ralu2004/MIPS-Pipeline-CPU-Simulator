package simulator.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import simulator.ProgramLoader;
import simulator.api.ServerContext;
import simulator.api.utils.HttpUtils;

import java.io.IOException;
import java.util.Map;

/**
 * POST /api/reset?clearRegs=1&clearMem=1&pc=0 -> resets CPU state (registers, memory, PC)
 */
public class ResetHandler implements HttpHandler {

    private final ServerContext context;

    public ResetHandler(ServerContext context) {
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
            boolean clearRegs = HttpUtils.parseBoolean(params.get("clearRegs"));
            boolean clearMem = HttpUtils.parseBoolean(params.get("clearMem"));
            int pc = HttpUtils.parseIntOrDefault(params.get("pc"), 0);

            ProgramLoader.resetState(context.cpuState, clearRegs, clearMem, pc);
            context.controller.clearPipeline();

            HttpUtils.sendJson(exchange, 200, "{\"ok\":true}");

        } catch (Exception e) {
            HttpUtils.sendError(exchange, 500, e.getMessage());
        }
    }
}