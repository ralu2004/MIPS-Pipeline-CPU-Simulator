package model.pipeline.stages;

import model.cpu.CPUState;
import model.instruction.Instruction;
import model.pipeline.registers.PipelineRegisters;

/**
 * IF (Instruction Fetch) Stage
 * Fetches nextinstruction from instruction memory at PC address
 */
public class FetchStage implements PipelineStage {

    @Override
    public void process(CPUState cpuState, PipelineRegisters regs) {
        
        Instruction instr = cpuState.instructionMemory.fetch(cpuState.pc.get());
        int pcPlus4 = cpuState.pc.get() + 4;
        regs.IF_ID.set(instr, pcPlus4);
        cpuState.pc.increment();
    }
}
