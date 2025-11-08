package model.instruction;

/**
 * R-Type Instruction (Register-type)
 * Format: opcode(6) | rs(5) | rt(5) | rd(5) | shamt(5) | funct(6)
 * Execution is handled by ExecuteStage using ALU operations
 */
public class RTypeInstruction extends Instruction{

    private int rs;
    private int rt;
    private int rd;
    private int shamt;
    private int func;

    public RTypeInstruction(int opcode, int binary) {
        super(opcode, binary);
    }

    @Override
    public void decodeFields() {
        int binary = super.getBinary();

        int rs = (binary >> 21) & 0x1F;      // bits 25-21
        int rt = (binary >> 16) & 0x1F;      // bits 20-16
        int rd = (binary >> 11) & 0x1F;      // bits 15-11
        int shamt = (binary >> 6) & 0x1F;    // bits 10-6
        int func = binary & 0x3F;            // bits 5-0

        this.rs = rs;
        this.rt = rt;
        this.rd = rd;
        this.shamt = shamt;
        this.func = func;
    }

    public int getRs() { return rs; }
    public int getRt() { return rt; }
    public int getRd() { return rd; }
    public int getShamt() { return shamt; }
    public int getFunc() { return func; }

    @Override
    public String toString() {
        return "RTypeInstruction{" +
                "rs=" + rs +
                ", rt=" + rt +
                ", rd=" + rd +
                ", shamt=" + shamt +
                ", func=" + func +
                '}';
    }
}
