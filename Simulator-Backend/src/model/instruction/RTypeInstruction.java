package model.instruction;

import model.cpu.CPUState;

public class RTypeInstruction extends Instruction{

    private int rs;
    private int rt;
    private int rd;
    private int shamt;
    private int func;

    @Override
    public void execute(CPUState cpuState) {
        switch (func) {
            case 0x20: // add
                cpuState.registers.set(rd, cpuState.registers.get(rs) + cpuState.registers.get(rt));
                break;
            case 0x22: // sub
                cpuState.registers.set(rd, cpuState.registers.get(rs) - cpuState.registers.get(rt));
                break;
            default:
                throw new UnsupportedOperationException("Unsupported R-type function: " + func);
        }
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
}
