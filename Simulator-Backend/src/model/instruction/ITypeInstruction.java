package model.instruction;

import model.cpu.CPUState;

public class ITypeInstruction extends Instruction {

    private int rs;
    private int rt;
    private int immediate;

    @Override
    public void decodeFields() {
        int binary = getBinary();
        rs = (binary >> 21) & 0x1F;
        rt = (binary >> 16) & 0x1F;
        immediate = binary & 0xFFFF;
        if ((immediate & 0x8000) != 0) immediate |= 0xFFFF0000; // sign-extend
    }

    @Override
    public void execute(CPUState cpuState) {
        switch (getOpcode()) {
            case 0x08: // addi
                cpuState.registers.set(rt, cpuState.registers.get(rs) + immediate);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported I-type opcode: " + getOpcode());
        }
    }

    public int getRs() { return rs; }
    public int getRt() { return rt; }
    public int getImmediate() { return immediate; }
}
