//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
	public static void main(String[] args) {
		try {
			simulator.ApiServer api = new simulator.ApiServer(8081);
			api.start();
		} catch (Exception e) {
			System.err.println("Failed to start API: " + e.getMessage());
			e.printStackTrace();
		}
	}
}