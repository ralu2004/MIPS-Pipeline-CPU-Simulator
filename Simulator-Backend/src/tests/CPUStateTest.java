package tests;

import model.cpu.CPUState;
import model.cpu.RegisterFile;
import model.cpu.ProgramCounter;
import model.memory.DataMemory;
import model.memory.InstructionMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CPUStateTest {

    private CPUState cpuState;
    private InstructionMemory instrMem;

    @BeforeEach
    void setUp() {
        instrMem = new InstructionMemory();
        cpuState = new CPUState(instrMem);
    }

    @Test
    void testInstructionMemoryInitialization() {
        assertNotNull(cpuState.instructionMemory, "InstructionMemory should be initialized");
        assertEquals(instrMem, cpuState.instructionMemory, "InstructionMemory should match constructor argument");
    }

    @Test
    void testDataMemoryInitialization() {
        assertNotNull(cpuState.dataMemory, "DataMemory should be initialized");
    }

    @Test
    void testRegisterFileInitialization() {
        assertNotNull(cpuState.registerFile, "RegisterFile should be initialized");
    }

    @Test
    void testProgramCounterInitialization() {
        assertNotNull(cpuState.pc, "ProgramCounter should be initialized");
        assertEquals(0, cpuState.pc.get(), "ProgramCounter should start at 0");
    }
}
