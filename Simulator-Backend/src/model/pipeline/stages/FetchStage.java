package model.pipeline.stages;

import model.cpu.CPUState;
import model.instruction.Instruction;
import model.pipeline.registers.PipelineRegisters;

public class FetchStage implements PipelineStage {

    @Override
    public void process(CPUState cpuState, PipelineRegisters regs) {
        Instruction instr = cpuState.instructionMemory.fetch(cpuState.pc.get());
        regs.IF_ID.set(instr, cpuState.pc.get());
        cpuState.pc.increment();
    }
}
