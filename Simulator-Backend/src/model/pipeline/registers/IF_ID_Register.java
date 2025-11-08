package model.pipeline.registers;

import model.instruction.Instruction;

public class IF_ID_Register {

    private Instruction instruction;
    private int pc;

    public void set(Instruction instr, int pc) {
        this.instruction = instr;
        this.pc = pc;
    }

    public Instruction getInstruction() { return instruction; }

    public int getPC() { return pc; }


}
