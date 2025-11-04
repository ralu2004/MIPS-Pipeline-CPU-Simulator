package model.memory;
import model.instruction.Instruction;

public class InstructionMemory {
    private final Instruction[] instructions;

    public InstructionMemory() {
        this.instructions = instructions;
    }

    public Instruction fetch(int address) {
        return instructions[address / 4]; // word-aligned
    }
}

