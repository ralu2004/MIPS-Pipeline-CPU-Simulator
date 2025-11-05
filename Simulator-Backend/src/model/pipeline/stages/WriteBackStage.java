package model.pipeline.stages;

import model.cpu.CPUState;
import model.pipeline.registers.MEM_WB_Register;
import model.pipeline.registers.PipelineRegisters;

/**
 * WB (Write-Back) Stage
 * Writes result back to register file
 */
public class WriteBackStage implements PipelineStage {

    @Override
    public void process(CPUState cpuState, PipelineRegisters regs) {
        MEM_WB_Register memWb = regs.MEM_WB;

        if (memWb.isRegWrite() && memWb.getInstruction() != null) {
            int destReg = memWb.getDestReg();
            int writeData = memWb.getWriteData(); //memData or aluResult
            
            if (destReg != 0) {
                cpuState.registerFile.set(destReg, writeData);
            }
        }
    }
}
