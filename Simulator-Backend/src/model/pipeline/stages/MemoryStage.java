package model.pipeline.stages;

import model.cpu.CPUState;
import model.instruction.Instruction;
import model.pipeline.registers.EX_MEM_Register;
import model.pipeline.registers.MEM_WB_Register;
import model.pipeline.registers.PipelineRegisters;

public class MemoryStage implements PipelineStage {

    @Override
    public void process(CPUState cpuState, PipelineRegisters regs) {
        EX_MEM_Register exMem = regs.EX_MEM;
        MEM_WB_Register memWb = regs.MEM_WB;

        Instruction instr = exMem.getInstruction();
        if (instr == null) return; // nothing to process

        int aluResult = exMem.getAluResult();
        int rtValue = exMem.getRtValue(); // data to store if SW
        boolean memRead = exMem.isMemRead();
        boolean memWrite = exMem.isMemWrite();
        int destReg = exMem.getDestReg();

        int memData = 0;

        if (memRead) {
            memData = cpuState.dataMemory.loadWord(aluResult);
            System.out.println("MEM: Loaded " + memData + " from address " + aluResult);
        }

        if (memWrite) {
            cpuState.dataMemory.storeWord(aluResult, rtValue);
            System.out.println("MEM: Stored " + rtValue + " to address " + aluResult);
        }

        memWb.setInstruction(instr);
        memWb.setAluResult(aluResult);
        memWb.setMemData(memData);
        memWb.setDestReg(destReg);
        memWb.setRegWrite(exMem.isRegWrite());
    }
}

