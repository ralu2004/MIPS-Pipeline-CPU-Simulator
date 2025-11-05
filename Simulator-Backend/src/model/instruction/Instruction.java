package model.instruction;

/**
 * Base class for MIPS instructions
 * Instructions are executed by pipeline stages, not by calling execute()
 */
public abstract class Instruction {

    private int opcode;
    private int binary;

    public Instruction(int opcode, int binary) {
        this.opcode = opcode;
        this.binary = binary;
    }

    /**
     * Decode instruction fields from binary representation
     * Must be called before accessing instruction-specific fields
     */
    public abstract void decodeFields();

    public int getOpcode() { return opcode; }
    public int getBinary() { return binary; }
}
