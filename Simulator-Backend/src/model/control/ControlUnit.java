package model.control;

public class ControlUnit {
  
    private boolean regWrite;    // Write to register file
    private boolean memToReg;    // 1 = write mem data, 0 = write ALU result
    private boolean branch;      // Branch instruction
    private boolean memRead;     // Load instruction
    private boolean memWrite;    // Store instruction
    private boolean regDst;      // 1 = rd (R-type), 0 = rt (I-type)
    private boolean aluSrc;      // 1 = immediate, 0 = register
    private int aluOp;           // ALU operation code (0, 1, or 2)
    private boolean jump;        // Jump instruction

    public ControlUnit() {
        resetSignals();
    }

    public void generateSignals(int opcode) {
        resetSignals();
        
        switch (opcode) {
            case 0x00: // R-type instruction
                regWrite = true;
                regDst = true;
                aluSrc = false;
                memToReg = false;
                branch = false;
                memRead = false;
                memWrite = false;
                aluOp = 2;      
                jump = false;
                break;

            case 0x08: // addi
                regWrite = true;
                regDst = false;
                aluSrc = true;
                memToReg = false;
                branch = false;
                memRead = false;
                memWrite = false;
                aluOp = 0;
                jump = false;
                break;
                
            case 0x23: // lw
                regWrite = true;
                regDst = false;
                aluSrc = true;
                memToReg = true;
                branch = false;
                memRead = true;
                memWrite = false;
                aluOp = 0;      // ALU does addition (base + offset)
                jump = false;
                break;
                
            case 0x2B: // sw
                regWrite = false;
                regDst = false;
                aluSrc = true;
                memToReg = false; 
                branch = false;
                memRead = false;
                memWrite = true;
                aluOp = 0;      // ALU does addition (base + offset)
                jump = false;
                break;
                
            case 0x04: // beq
                regWrite = false;
                regDst = false; 
                aluSrc = false;
                memToReg = false; 
                branch = true;
                memRead = false;
                memWrite = false;
                aluOp = 1;      // ALU does subtraction (for comparison)
                jump = false;
                break;

            case 0x05: //bne
                regWrite = false;
                regDst = false;
                aluSrc = false;
                memToReg = false;
                branch = true;
                memRead = false;
                memWrite = false;
                aluOp = 1;
                jump = false;
                break;

            case 0x0D: //ori
                regWrite = true;
                regDst = false;
                aluSrc = true;
                memToReg = false;
                branch = false;
                memRead = false;
                memWrite = false;
                aluOp = 3;
                jump = false;
                break;

            case 0x0C: //andi
                regWrite = true;
                regDst = false;
                aluSrc = true;
                memToReg = false;
                branch = false;
                memRead = false;
                memWrite = false;
                aluOp = 4;
                jump = false;
                break;

            case 0x0A: //slti
                regWrite = true;
                regDst = false;
                aluSrc = true;
                memToReg = false;
                branch = false;
                memRead = false;
                memWrite = false;
                aluOp = 5;
                jump = false;
                break;

            case 0x02: // j (jump)
            case 0x03: // jal (jump and link)
                regWrite = (opcode == 0x03); // jal writes to $ra
                regDst = false; 
                aluSrc = false; 
                memToReg = false; 
                branch = false;
                memRead = false;
                memWrite = false;
                aluOp = 0;      
                jump = true;
                break;
                
            default:
                resetSignals();
                break;
        }
    }

    public void resetSignals() {
        regWrite = false;
        memToReg = false;
        branch = false;
        memRead = false;
        memWrite = false;
        regDst = false;
        aluSrc = false;
        aluOp = 0;
        jump = false;
    }

    public boolean isRegWrite() { return regWrite; }
    public boolean isMemToReg() { return memToReg; }
    public boolean isBranch() { return branch; }
    public boolean isMemRead() { return memRead; }
    public boolean isMemWrite() { return memWrite; }
    public boolean isRegDst() { return regDst; }
    public boolean isAluSrc() { return aluSrc; }
    public int getAluOp() { return aluOp; }
    public boolean isJump() { return jump; }
}
