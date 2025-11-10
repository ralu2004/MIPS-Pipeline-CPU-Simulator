package simulator.api;

import model.cpu.CPUState;
import simulator.Clock;
import simulator.PipelineController;

public class ServerContext {
    public final CPUState cpuState;
    public final PipelineController controller;
    public final Clock clock;

    public ServerContext(CPUState cpuState, PipelineController controller, Clock clock) {
        this.cpuState = cpuState;
        this.controller = controller;
        this.clock = clock;
    }
}