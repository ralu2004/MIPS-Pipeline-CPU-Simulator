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
        rs = (binary >> 21) & 0x1F;
        rt = (binary >> 16) & 0x1F;
        immediate = binary & 0xFFFF;
        if ((immediate & 0x8000) != 0) immediate |= 0xFFFF0000; // sign-extend
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
