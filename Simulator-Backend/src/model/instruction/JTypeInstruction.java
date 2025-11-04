package model.instruction;

import model.cpu.CPUState;

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

    @Override
    public void execute(CPUState cpuState) {
        int pcUpper = cpuState.pc.get() & 0xF0000000;
        int targetAddress = (address << 2) | pcUpper;

        switch (getOpcode()) {
            case 0x02: // j
                cpuState.pc.set(targetAddress);
                break;
            case 0x03: // jal
                cpuState.registers.set(31, cpuState.pc.get() + 4);
                cpuState.pc.set(targetAddress);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported J-type opcode: " + getOpcode());
        }
    }

    public int getAddress() { return address; }
}
