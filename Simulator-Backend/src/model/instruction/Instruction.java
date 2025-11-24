package model.instruction;

public abstract class Instruction {

    private int opcode;
    private int binary;

    public Instruction(int opcode, int binary) {
        this.opcode = opcode;
        this.binary = binary;
    }

    public abstract void decodeFields();
    public abstract Instruction copy();
    public int getOpcode() { return opcode; }
    public int getBinary() { return binary; }

}
