package model.pipeline.registers;

import model.instruction.Instruction;

/**
 * EX/MEM Pipeline Register
 * Holds data between Execute and Memory stages
 */
public class EX_MEM_Register {
    // ALU results
    private int aluResult;      // ALU computation result
    private boolean zeroFlag;   // ALU zero flag (for branch decisions)
    
    // Data for memory operations
    private int writeData;      // Data to write to memory (rt value for stores)
    
    // Branch information
    private int branchTarget;   // Calculated branch target address
    private boolean branchTaken; // Whether branch condition is met
    
    // Destination register (selected by RegDst mux in EX stage)
    private int destReg;
    
    // Control signals (passed from ID/EX)
    private boolean regWrite;
    private boolean memToReg;
    private boolean branch;
    private boolean memRead;
    private boolean memWrite;
    
    // Instruction reference (for debugging/tracking)
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
    
    // Legacy method name for compatibility
    public int getRtValue() { return writeData; }
}
