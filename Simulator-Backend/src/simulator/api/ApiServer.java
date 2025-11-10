package simulator.api;

import com.sun.net.httpserver.HttpServer;
import model.cpu.CPUState;
import model.memory.InstructionMemory;
import simulator.Clock;
import simulator.PipelineController;
import simulator.api.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * HTTP API Server for MIPS Simulator -> REST endpoints for loading programs, stepping execution, and viewing state
 */
public class ApiServer {

	private final HttpServer server;
	private final ServerContext context;

	public ApiServer(int port) throws IOException {
		CPUState cpuState = new CPUState(new InstructionMemory());
		PipelineController controller = new PipelineController(cpuState);
		Clock clock = new Clock(controller);
		this.context = new ServerContext(cpuState, controller, clock);

		this.server = HttpServer.create(new InetSocketAddress(port), 0);

		server.createContext("/api/load", new LoadHandler(context));
		server.createContext("/api/step", new StepHandler(context));
		server.createContext("/api/state", new StateHandler(context));
		server.createContext("/api/reset", new ResetHandler(context));
		server.createContext("/api/health", new HealthHandler());

		server.setExecutor(null);
	}

	public void start() {
		server.start();
		System.out.println("MIPS Simulator API started on port " + server.getAddress().getPort());
	}

	public void stop() {
		server.stop(0);
		System.out.println("Server stopped");
	}

	public static void main(String[] args) {
		try {
			int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
			ApiServer server = new ApiServer(port);
			server.start();

			System.out.println("Server running at http://localhost:" + port);
			System.out.println("Endpoints:");
			System.out.println("  POST /api/load?start=0");
			System.out.println("  POST /api/step?cycles=1");
			System.out.println("  GET  /api/state");
			System.out.println("  POST /api/reset?clearRegs=1&clearMem=1&pc=0");
			System.out.println("  GET  /api/health");
			System.out.println("\nPress Ctrl+C to stop");

		} catch (Exception e) {
			System.err.println("Failed to start server: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}