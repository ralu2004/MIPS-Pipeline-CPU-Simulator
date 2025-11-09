package model.pipeline.registers;

import model.instruction.Instruction;

public class MEM_WB_Register {

    private int aluResult;
    private int memData;
    private int destReg;
    private boolean regWrite;
    private boolean memToReg;
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