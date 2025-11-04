package simulator;

public class Clock {

    private int cycle = 0;
    private final PipelineController pipelineController;

    public Clock(PipelineController pipelineController) {
        this.pipelineController = pipelineController;
    }

    public void tick() {
        cycle++;
        pipelineController.runCycle();
        System.out.println("Cycle " + cycle + " completed.");
    }

    public int getCycle() { return cycle; }

    public void run(int n) {
        for (int i = 0; i < n; i++) {
            tick();
        }
    }
}
