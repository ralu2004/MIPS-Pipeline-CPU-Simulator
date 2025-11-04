package simulator;

import model.cpu.CPUState;
import model.pipeline.registers.PipelineRegisters;
import model.pipeline.stages.*;

public class PipelineController {

    private final CPUState cpuState;
    private final PipelineRegisters pipelineRegisters = new PipelineRegisters();

    private final FetchStage fetch = new FetchStage();
    private final DecodeStage decode = new DecodeStage();
    private final ExecuteStage execute = new ExecuteStage();
    private final MemoryStage memory = new MemoryStage();
    private final WriteBackStage writeBack = new WriteBackStage();

    public PipelineController(CPUState state) {
        this.cpuState = state;
    }

    public void runCycle() {
        // Process in reverse order to emulate write-back before read
        writeBack.process(cpuState, pipelineRegisters);
        memory.process(cpuState, pipelineRegisters);
        execute.process(cpuState, pipelineRegisters);
        decode.process(cpuState, pipelineRegisters);
        fetch.process(cpuState, pipelineRegisters);
    }
}
