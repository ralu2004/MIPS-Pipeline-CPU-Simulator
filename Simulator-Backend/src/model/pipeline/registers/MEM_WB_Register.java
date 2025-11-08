package model.pipeline.registers;

import model.instruction.Instruction;

/**
 * MEM/WB Pipeline Register
 * Holds data between Memory and Write-Back stages
 */
public class MEM_WB_Register {

    private int aluResult;
    private int memData;
    private int destReg;
    private boolean regWrite;
    private boolean memToReg;   // 1 = write memData, 0 = write aluResult
    private Instruction instruction;

    public void setAluResult(int value) { this.aluResult = value; }
    public void setMemData(int value) { this.memData = value; }
    public void setDestReg(int value) { this.destReg = value; }
    public void setRegWrite(boolean value) { this.regWrite = value; }
    public void setMemToReg(boolean value) { this.memToReg = value; }
    public void setInstruction(Instruction instr) { this.instruction = instr; }

    public int getAluResult() { return aluResult; }
    public int getMemData() { return memData; }
    public int getDestReg() { return destReg; }
    public boolean isRegWrite() { return regWrite; }
    public boolean isMemToReg() { return memToReg; }
    public Instruction getInstruction() { return instruction; }

    public int getWriteData() {
        return memToReg ? memData : aluResult;
    }
}
