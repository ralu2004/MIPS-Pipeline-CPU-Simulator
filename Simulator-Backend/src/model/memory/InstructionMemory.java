package model.memory;
import model.instruction.Instruction;

public class InstructionMemory {
    private final Instruction[] instructions;

    public InstructionMemory() {
        this.instructions = new Instruction[1024]; // 4KB (1024 words)
    }

    public InstructionMemory(Instruction[] instructions) {
        this.instructions = instructions;
    }

    public Instruction fetch(int address) {
        if (address < 0 || address >= instructions.length * 4) {
            return null; // Out of bounds
        }
        return instructions[address / 4]; // word-aligned
    }

    public void setInstruction(int address, Instruction instruction) {
        if (address >= 0 && address < instructions.length * 4) {
            instructions[address / 4] = instruction;
        }
    }
}

