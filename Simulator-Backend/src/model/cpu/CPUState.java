package model.cpu;

import model.memory.DataMemory;
import model.memory.InstructionMemory;

public class CPUState {
    public RegisterFile registers = new RegisterFile();
    public InstructionMemory instructionMemory;
    public DataMemory dataMemory = new DataMemory();
    public ProgramCounter pc = new ProgramCounter();

    public CPUState(InstructionMemory instrMem) {
        this.instructionMemory = instrMem;
    }
}
