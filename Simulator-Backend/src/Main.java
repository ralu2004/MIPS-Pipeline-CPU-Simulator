import simulator.api.ApiServer;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
	public static void main(String[] args) {
		/*try {
			simulator.api.ApiServer api = new simulator.api.ApiServer(8081);
			api.start();
		} catch (Exception e) {
			System.err.println("Failed to start API: " + e.getMessage());
			e.printStackTrace();
		}*/
		try {
			int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
			ApiServer server = new ApiServer(port);
			server.start();
			System.out.println("Server running on http://localhost:" + port);
			System.out.println("Press Ctrl+C to stop");
		} catch (Exception e) {
			System.err.println("Failed to start server: " + e.getMessage());
			e.printStackTrace();
		}

	}
}