package model.instruction;

/**
 * Format: opcode(6) | rs(5) | rt(5) | immediate(16)
 */
public class ITypeInstruction extends Instruction {

    private int rs;
    private int rt;
    private int immediate;

    public ITypeInstruction(int opcode, int binary) {
        super(opcode, binary);
    }

    @Override
    public void decodeFields() {
        int binary = getBinary();
        int opcode = getOpcode();
        rs = (binary >> 21) & 0x1F;
        rt = (binary >> 16) & 0x1F;
        immediate = binary & 0xFFFF;

        boolean zeroExtend = (opcode == 0x0D || opcode == 0x0C);
        if (!zeroExtend && (immediate & 0x8000) != 0) {
            immediate |= 0xFFFF0000; // sign-extend
        }
    }

    @Override
    public Instruction copy() {
        ITypeInstruction c = new ITypeInstruction(getOpcode(), getBinary());
        c.rs = this.rs;
        c.rt = this.rt;
        c.immediate = this.immediate;
        return c;
    }

    public int getRs() { return rs; }
    public int getRt() { return rt; }
    public int getImmediate() { return immediate; }

    @Override
    public String toString() {
        return "ITypeInstruction{" +
                "rs=" + rs +
                ", rt=" + rt +
                ", immediate=" + immediate +
                '}';
    }
}
