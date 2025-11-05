package model.pipeline.registers;

import model.instruction.Instruction;

/**
 * ID/EX Pipeline Register
 * Holds data between Instruction Decode and Execute stages
 */
public class ID_EX_Register {
    // Data from register file
    private int readData1;      // Value from rs register
    private int readData2;      // Value from rt register
    private int signExtendedImm; // Sign-extended immediate value
    private int pcPlus4;        // PC+4 for branch/jump calculations
    
    // Register numbers for hazard detection and forwarding
    private int rs;             // Source register 1
    private int rt;             // Source register 2 / destination for I-type
    private int rd;             // Destination register for R-type
    
    // Control signals (from Control Unit)
    private boolean regWrite;
    private boolean memToReg;
    private boolean branch;
    private boolean memRead;
    private boolean memWrite;
    private boolean regDst;     // 1 = rd, 0 = rt
    private boolean aluSrc;     // 1 = immediate, 0 = readData2
    private int aluOp;          // ALU operation code
    
    // Instruction reference (for debugging/tracking)
    private Instruction instruction;

    public void setReadData1(int value) { this.readData1 = value; }
    public void setReadData2(int value) { this.readData2 = value; }
    public void setSignExtendedImm(int value) { this.signExtendedImm = value; }
    public void setPcPlus4(int value) { this.pcPlus4 = value; }
    public void setRs(int value) { this.rs = value; }
    public void setRt(int value) { this.rt = value; }
    public void setRd(int value) { this.rd = value; }
    public void setRegWrite(boolean value) { this.regWrite = value; }
    public void setMemToReg(boolean value) { this.memToReg = value; }
    public void setBranch(boolean value) { this.branch = value; }
    public void setMemRead(boolean value) { this.memRead = value; }
    public void setMemWrite(boolean value) { this.memWrite = value; }
    public void setRegDst(boolean value) { this.regDst = value; }
    public void setAluSrc(boolean value) { this.aluSrc = value; }
    public void setAluOp(int value) { this.aluOp = value; }
    public void setInstruction(Instruction instr) { this.instruction = instr; }

    public int getReadData1() { return readData1; }
    public int getReadData2() { return readData2; }
    public int getSignExtendedImm() { return signExtendedImm; }
    public int getPcPlus4() { return pcPlus4; }
    public int getRs() { return rs; }
    public int getRt() { return rt; }
    public int getRd() { return rd; }
    public boolean isRegWrite() { return regWrite; }
    public boolean isMemToReg() { return memToReg; }
    public boolean isBranch() { return branch; }
    public boolean isMemRead() { return memRead; }
    public boolean isMemWrite() { return memWrite; }
    public boolean isRegDst() { return regDst; }
    public boolean isAluSrc() { return aluSrc; }
    public int getAluOp() { return aluOp; }
    public Instruction getInstruction() { return instruction; }
}
