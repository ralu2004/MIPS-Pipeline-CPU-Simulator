package model.instruction;

/**
 * Format: opcode(6) | address(26)
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

    @Override
    public String toString() {
        return "JTypeInstruction{" +
                "address=" + address +
                '}';
    }
}
