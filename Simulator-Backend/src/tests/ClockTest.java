package tests;

import model.cpu.CPUState;
import model.instruction.Instruction;
import model.instruction.RTypeInstruction;
import model.memory.InstructionMemory;
import simulator.Clock;
import simulator.PipelineController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClockTest {

    private Clock clock;
    private CPUState cpuState;
    private int runCounter;

    @BeforeEach
    void setUp() {
        Instruction[] instructions = new Instruction[10];
        instructions[0] = new RTypeInstruction(0x00, 0x00422020); // add $4, $2, $2
        InstructionMemory instrMem = new InstructionMemory(instructions);

        cpuState = new CPUState(instrMem);

        runCounter = 0;

        PipelineController controller = new PipelineController(cpuState) {
            @Override
            public void runCycle() {
                runCounter++;
            }
        };

        clock = new Clock(controller);
    }

    @Test
    void testTickIncrementsCycle() {
        assertEquals(0, clock.getCycle(), "Initial cycle should be 0");
        clock.tick();
        assertEquals(1, clock.getCycle(), "Cycle should increment to 1 after tick");
        assertEquals(1, runCounter, "PipelineController.runCycle() should be called once");
    }

    @Test
    void testRunMultipleCycles() {
        clock.run(5);
        assertEquals(5, clock.getCycle(), "Cycle count should match number of ticks");
        assertEquals(5, runCounter, "PipelineController.runCycle() should be called for each cycle");
    }
}
