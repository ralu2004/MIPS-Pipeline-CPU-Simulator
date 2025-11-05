package model.pipeline.stages;

import model.cpu.CPUState;
import model.instruction.Instruction;
import model.pipeline.registers.EX_MEM_Register;
import model.pipeline.registers.MEM_WB_Register;
import model.pipeline.registers.PipelineRegisters;

/**
 * MEM (Memory Access) Stage
 * Performs memory loads/stores and handles branch/jump PC updates
 */
public class MemoryStage implements PipelineStage {

    @Override
    public void process(CPUState cpuState, PipelineRegisters regs) {
        EX_MEM_Register exMem = regs.EX_MEM;
        MEM_WB_Register memWb = regs.MEM_WB;

        Instruction instr = exMem.getInstruction();
        if (instr == null) {
            // No instruction - clear MEM_WB register (bubble)
            clearMEM_WB(regs);
            return;
        }

        // Handle branch instructions - update PC if branch taken
        // Branch Prediction: "Predict Not Taken"
        // - Branch decision was made in EX stage (previous cycle)
        // - If branch is taken, update PC to branch target
        // - If branch not taken, PC continues sequential (already incremented in IF)
        // - PipelineController will flush incorrectly fetched instructions if taken
        if (exMem.isBranch() && exMem.isBranchTaken()) {
            cpuState.pc.set(exMem.getBranchTarget());
        }

        int aluResult = exMem.getAluResult();
        int writeData = exMem.getWriteData();
        boolean memRead = exMem.isMemRead();
        boolean memWrite = exMem.isMemWrite();
        int destReg = exMem.getDestReg();

        int memData = 0;

        // Perform memory operations
        if (memRead) {
            // Load word from memory
            memData = cpuState.dataMemory.loadWord(aluResult);
        }

        if (memWrite) {
            // Store word to memory
            cpuState.dataMemory.storeWord(aluResult, writeData);
        }

        // Write to MEM/WB register
        memWb.setInstruction(instr);
        memWb.setAluResult(aluResult);
        memWb.setMemData(memData);
        memWb.setDestReg(destReg);
        memWb.setRegWrite(exMem.isRegWrite());
        memWb.setMemToReg(exMem.isMemToReg());
    }
    
    private void clearMEM_WB(PipelineRegisters regs) {
        regs.MEM_WB.setAluResult(0);
        regs.MEM_WB.setMemData(0);
        regs.MEM_WB.setDestReg(0);
        regs.MEM_WB.setRegWrite(false);
        regs.MEM_WB.setMemToReg(false);
        regs.MEM_WB.setInstruction(null);
    }
}
