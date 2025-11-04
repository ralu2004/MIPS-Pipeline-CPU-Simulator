package model.pipeline.stages;
import model.cpu.CPUState;
import model.pipeline.registers.PipelineRegisters;

public interface PipelineStage {
    void process(CPUState state, PipelineRegisters regs);
}
