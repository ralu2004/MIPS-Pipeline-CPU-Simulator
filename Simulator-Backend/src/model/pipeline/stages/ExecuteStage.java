package model.pipeline.stages;

import model.instruction.Instruction;
import model.cpu.CPUState;
import model.pipeline.registers.PipelineRegisters;

public class ExecuteStage implements PipelineStage {
    @Override
    public void process(CPUState cpuState, PipelineRegisters regs) {
        Instruction instr = regs.IF_ID.getInstruction();
        if (instr != null) {
            instr.execute(cpuState);
            System.out.println("Executed instruction at PC=" + regs.IF_ID.getPC());
        }
    }
}
