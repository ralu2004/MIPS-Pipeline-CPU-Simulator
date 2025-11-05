package model.instruction;

/**
 * J-Type Instruction (Jump-type)
 * Format: opcode(6) | address(26)
 * Execution is handled by DecodeStage (jumps update PC in ID stage)
 */
public class JTypeInstruction extends Instruction {
    private int address;

    public JTypeInstruction(int opcode, int binary) {
        super(opcode, binary);
        decodeFields();
    }

    @Override
    public void decodeFields() {
        address = getBinary() & 0x03FFFFFF;
    }

    public int getAddress() { return address; }
}
