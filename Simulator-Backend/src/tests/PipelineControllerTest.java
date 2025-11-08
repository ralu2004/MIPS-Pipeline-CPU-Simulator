package tests;

import model.cpu.CPUState;
import model.instruction.RTypeInstruction;
import model.memory.InstructionMemory;
import model.pipeline.registers.PipelineRegisters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulator.PipelineController;

import static org.junit.jupiter.api.Assertions.*;

class PipelineControllerTest {

    private CPUState cpuState;
    private PipelineController controller;

    @BeforeEach
    void setUp() {
        InstructionMemory instrMem = new InstructionMemory();
        // add $4, $2, $2
        RTypeInstruction instr1 = new RTypeInstruction(0x00, 0x00422020);
        instrMem.setInstruction(0, instr1);
        
        cpuState = new CPUState(instrMem);
        cpuState.registerFile.set(2, 5);
        controller = new PipelineController(cpuState);
    }

    @Test
    void testSingleCycleExecution() {
        controller.runCycle();
        PipelineRegisters regs = controller.getPipelineRegisters();
        assertNotNull(regs.IF_ID.getInstruction(),
                "IF/ID should have instruction after first cycle");
    }

    @Test
    void testPipelineRegistersProgression() {
        controller.runCycle();
        controller.runCycle();
        PipelineRegisters regs = controller.getPipelineRegisters();
        assertNotNull(regs.ID_EX.getInstruction(), "ID/EX should hold first instruction after 2 cycles");
    }

    @Test
    void testWriteBackStageWritesToRegisterFile() {
        for (int i = 0; i < 5; i++) {
            controller.runCycle();
        }

        assertEquals(10, cpuState.registerFile.get(4), 
                "Register $4 should contain $2 + $2 = 10 after WB stage");
    }
}
