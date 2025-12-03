package model.pipeline.registers;

import model.instruction.Instruction;

public class EX_MEM_Register {

    private int aluResult;
    private boolean zeroFlag;
    private int writeData;
    private int branchTarget;
    private boolean branchTaken;
    private int destReg;
    private boolean regWrite;
    private boolean memToReg;
    private boolean branch;
    private boolean memRead;
    private boolean memWrite;
    private int forwardA;
    private int forwardB;

    private Instruction instruction;

    public void setAluResult(int value) { this.aluResult = value; }
    public void setZeroFlag(boolean value) { this.zeroFlag = value; }
    public void setWriteData(int value) { this.writeData = value; }
    public void setBranchTarget(int value) { this.branchTarget = value; }
    public void setBranchTaken(boolean value) { this.branchTaken = value; }
    public void setDestReg(int value) { this.destReg = value; }
    public void setRegWrite(boolean value) { this.regWrite = value; }
    public void setMemToReg(boolean value) { this.memToReg = value; }
    public void setBranch(boolean value) { this.branch = value; }
    public void setMemRead(boolean value) { this.memRead = value; }
    public void setMemWrite(boolean value) { this.memWrite = value; }
    public void setInstruction(Instruction instr) { this.instruction = instr; }
    public void setForwardA(int forwardA) { this.forwardA = forwardA;}
    public void setForwardB(int forwardB) { this.forwardB = forwardB;}

    public int getAluResult() { return aluResult; }
    public boolean isZeroFlag() { return zeroFlag; }
    public int getWriteData() { return writeData; }
    public int getBranchTarget() { return branchTarget; }
    public boolean isBranchTaken() { return branchTaken; }
    public int getDestReg() { return destReg; }
    public boolean isRegWrite() { return regWrite; }
    public boolean isMemToReg() { return memToReg; }
    public boolean isBranch() { return branch; }
    public boolean isMemRead() { return memRead; }
    public boolean isMemWrite() { return memWrite; }
    public Instruction getInstruction() { return instruction; }
    public int getForwardA() { return forwardA;}
    public int getForwardB() { return forwardB; }

    public int getRtValue() { return writeData; }
}
