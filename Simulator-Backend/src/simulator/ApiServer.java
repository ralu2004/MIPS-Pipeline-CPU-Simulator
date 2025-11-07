package simulator;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.cpu.CPUState;
import model.memory.InstructionMemory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal HTTP API using built-in HttpServer.
 * Endpoints:
 *  - POST /api/load?start=0          (body: hex lines separated by \n)
 *  - POST /api/step?cycles=1         (advances N cycles)
 *  - GET  /api/state                 (returns JSON of CPU and pipeline state)
 *  - POST /api/reset?clearRegs=1&clearMem=1&pc=0
 */
public class ApiServer {

	private final HttpServer server;
	private final CPUState cpuState;
	private final PipelineController controller;
	private final Clock clock;

	public ApiServer(int port) throws IOException {
		this.cpuState = new CPUState(new InstructionMemory());
		this.controller = new PipelineController(cpuState);
		this.clock = new Clock(controller);

		this.server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/api/load", new LoadHandler());
		server.createContext("/api/step", new StepHandler());
		server.createContext("/api/state", new StateHandler());
		server.createContext("/api/reset", new ResetHandler());
		server.createContext("/api/health", new HealthHandler());
		server.setExecutor(null);
	}

	public void start() {
		server.start();
		System.out.println("HTTP API started on port " + server.getAddress().getPort());
	}

	private class LoadHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			withCors(exchange);
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { send(exchange, 405, ""); return; }
			Map<String, String> q = queryParams(exchange.getRequestURI());
			int start = parseIntOrDefault(q.get("start"), 0);
			String body = readBody(exchange);
			String[] lines = Arrays.stream(body.split("\n"))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.toArray(String[]::new);
			ProgramLoader.ProgramLoadResult res;
			try {
				res = ProgramLoader.loadFromHexStrings(cpuState, lines, start);
				sendJson(exchange, 200, "{\"loaded\":" + res.loadedCount + ",\"start\":" + res.startAddress + ",\"end\":" + res.endAddress + "}");
			} catch (Exception e) {
				sendJson(exchange, 400, "{\"error\":\"" + jsonEscape(e.getMessage()) + "\"}");
			}
		}
	}

	private class StepHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			withCors(exchange);
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { send(exchange, 405, ""); return; }
			Map<String, String> q = queryParams(exchange.getRequestURI());
			int cycles = Math.max(1, parseIntOrDefault(q.get("cycles"), 1));
			clock.run(cycles);
			sendJson(exchange, 200, "{\"cycles\":" + cycles + "}");
		}
	}

	private class StateHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			withCors(exchange);
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { send(exchange, 405, ""); return; }
			String json = StateSerializer.serialize(cpuState, controller);
			sendJson(exchange, 200, json);
		}
	}

	private class ResetHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			withCors(exchange);
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { send(exchange, 405, ""); return; }
			Map<String, String> q = queryParams(exchange.getRequestURI());
			boolean clearRegs = parseBool(q.get("clearRegs"));
			boolean clearMem = parseBool(q.get("clearMem"));
			int pc = parseIntOrDefault(q.get("pc"), 0);
			ProgramLoader.resetState(cpuState, clearRegs, clearMem, pc);
			sendJson(exchange, 200, "{\"ok\":true}");
		}
	}

	private class HealthHandler implements HttpHandler {
		@Override public void handle(HttpExchange exchange) throws IOException { withCors(exchange); send(exchange, 200, "ok"); }
	}

	private static Map<String, String> queryParams(URI uri) {
		Map<String, String> m = new HashMap<>();
		String q = uri.getQuery();
		if (q == null || q.isEmpty()) return m;
		for (String kv : q.split("&")) {
			int i = kv.indexOf('=');
			if (i > 0) m.put(urlDecode(kv.substring(0, i)), urlDecode(kv.substring(i + 1)));
		}
		return m;
	}

	private static String urlDecode(String s) {
		try { return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8.name()); }
		catch (Exception e) { return s; }
	}

	private static int parseIntOrDefault(String s, int def) {
		try { return s == null ? def : Integer.parseInt(s); } catch (Exception e) { return def; }
	}
	private static boolean parseBool(String s) { return s != null && ("1".equals(s) || "true".equalsIgnoreCase(s)); }

	private static String readBody(HttpExchange exchange) throws IOException {
		try (InputStream is = exchange.getRequestBody()) {
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static String jsonEscape(String s) {
		if (s == null) return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
	}

	private static void send(HttpExchange ex, int status, String body) throws IOException {
		byte[] out = body.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
		ex.sendResponseHeaders(status, out.length);
		try (OutputStream os = ex.getResponseBody()) { os.write(out); }
	}

	private static void sendJson(HttpExchange ex, int status, String body) throws IOException {
		byte[] out = body.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
		ex.sendResponseHeaders(status, out.length);
		try (OutputStream os = ex.getResponseBody()) { os.write(out); }
	}

	private static void withCors(HttpExchange ex) throws IOException {
		Headers h = ex.getResponseHeaders();
		h.add("Access-Control-Allow-Origin", "*");
		h.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
		h.add("Access-Control-Allow-Headers", "Content-Type");
		if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
			ex.sendResponseHeaders(204, -1);
		}
	}
}
