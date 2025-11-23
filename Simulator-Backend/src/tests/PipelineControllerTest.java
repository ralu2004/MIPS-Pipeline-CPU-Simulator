package tests;

import model.cpu.CPUState;
import model.instruction.ITypeInstruction;
import model.instruction.RTypeInstruction;
import model.memory.InstructionMemory;
import simulator.PipelineController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PipelineControllerTest {

    private CPUState cpu;
    private PipelineController controller;

    @BeforeEach
    void setUp() {
        InstructionMemory instrMem = new InstructionMemory();
        cpu = new CPUState(instrMem);
        controller = new PipelineController(cpu);

        // Initialize some register values
        cpu.registerFile.set(1, 5);
        cpu.registerFile.set(2, 10);
        cpu.registerFile.set(3, 15);
    }

    @Test
    void testLoadUseHazardProducesStallAndBubble() {
        // Set up memory for load instruction
        cpu.dataMemory.storeWord(5, 25);

        // Load-use hazard: lw followed by dependent instruction
        ITypeInstruction lw = new ITypeInstruction(35, 0x8C230000); // lw $3, 0($1)
        RTypeInstruction add = new RTypeInstruction(0, 0x00622020); // add $4, $3, $2
        lw.decodeFields();
        add.decodeFields();

        cpu.instructionMemory.setInstruction(0, lw);
        cpu.instructionMemory.setInstruction(4, add);

        boolean foundStall = false;
        boolean foundBubble = false;

        for (int i = 0; i < 6; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);

                if (snapshot.getIfStage().getState().toString().equals("STALL")) {
                    foundStall = true;
                }
                if (snapshot.getExStage().getState().toString().equals("BUBBLE")) {
                    foundBubble = true;
                }
            }

            if (foundStall && foundBubble) break;
        }

        assertTrue(foundStall, "Should detect stall due to load-use hazard");
        assertTrue(foundBubble, "Should insert bubble in pipeline");
    }

    @Test
    void testBranchFlush() {
        ITypeInstruction beq = new ITypeInstruction(4, 0x10220008); // beq $1, $2, 8
        RTypeInstruction nop = new RTypeInstruction(0, 0x00000000); // nop
        beq.decodeFields();
        nop.decodeFields();

        cpu.instructionMemory.setInstruction(0, beq);
        cpu.instructionMemory.setInstruction(4, nop);

        // force branch taken
        cpu.registerFile.set(1, 10);
        cpu.registerFile.set(2, 10);

        boolean foundFlush = false;

        for (int i = 0; i < 5; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);

                if (snapshot.getIfStage().getState().toString().equals("FLUSH") ||
                        snapshot.getIdStage().getState().toString().equals("FLUSH")) {
                    foundFlush = true;
                    break;
                }
            }
        }

        assertTrue(foundFlush, "Pipeline should flush stages after taken branch");
    }

    @Test
    void testNoStallWhenNoHazard() {
        RTypeInstruction add1 = new RTypeInstruction(0, 0x00221820); // add $3, $1, $2
        RTypeInstruction add2 = new RTypeInstruction(0, 0x00622020); // add $4, $3, $2
        add1.decodeFields();
        add2.decodeFields();

        cpu.instructionMemory.setInstruction(0, add1);
        cpu.instructionMemory.setInstruction(4, add2);

        boolean foundStall = false;

        for (int i = 0; i < 5; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);

                if (snapshot.getIfStage().getState().toString().equals("STALL")) {
                    foundStall = true;
                    break;
                }
            }
        }

        assertFalse(foundStall, "Should not stall for independent instructions");
    }

    @Test
    void testForwardingPreventsStall() {
        RTypeInstruction add1 = new RTypeInstruction(0, 0x00221820); // add $3, $1, $2
        RTypeInstruction add2 = new RTypeInstruction(0, 0x00622020); // add $4, $3, $2
        add1.decodeFields();
        add2.decodeFields();

        cpu.instructionMemory.setInstruction(0, add1);
        cpu.instructionMemory.setInstruction(4, add2);

        boolean foundStall = false;

        for (int i = 0; i < 6; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);

                if (snapshot.getIfStage().getState().toString().equals("STALL")) {
                    foundStall = true;
                    break;
                }
            }
        }

        assertFalse(foundStall, "Forwarding should prevent stall for RAW hazard");
    }

    @Test
    void testPipelineProgressesNormally() {
        RTypeInstruction add = new RTypeInstruction(0, 0x00221820); // add $3, $1, $2
        add.decodeFields();

        cpu.instructionMemory.setInstruction(0, add);

        for (int i = 0; i < 3; i++) {
            controller.runCycle();
        }

        var history = controller.getHistory();
        assertTrue(history.size() >= 3, "Should have history entries");

        var latest = history.get(history.size() - 1);
        assertNotNull(latest, "Should have latest snapshot");
    }

    @Test
    void testDataHazardWithStoreWord() {
        cpu.dataMemory.storeWord(5, 30);

        ITypeInstruction lw = new ITypeInstruction(35, 0x8C230000); // lw $3, 0($1)
        ITypeInstruction sw = new ITypeInstruction(43, 0xAC230004); // sw $3, 4($1)
        lw.decodeFields();
        sw.decodeFields();

        cpu.instructionMemory.setInstruction(0, lw);
        cpu.instructionMemory.setInstruction(4, sw);

        boolean foundStall = false;

        for (int i = 0; i < 6; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);

                if (snapshot.getIfStage().getState().toString().equals("STALL") ||
                        snapshot.getExStage().getState().toString().equals("BUBBLE")) {
                    foundStall = true;
                    break;
                }
            }
        }

        assertTrue(foundStall, "Should stall for load-store data hazard");
    }

    @Test
    void testMultipleConsecutiveHazards() {
        cpu.dataMemory.storeWord(5, 20);

        ITypeInstruction lw = new ITypeInstruction(35, 0x8C230000); // lw $3, 0($1)
        RTypeInstruction add1 = new RTypeInstruction(0, 0x00622020); // add $4, $3, $2
        RTypeInstruction add2 = new RTypeInstruction(0, 0x00822820); // add $5, $4, $2
        lw.decodeFields();
        add1.decodeFields();
        add2.decodeFields();

        cpu.instructionMemory.setInstruction(0, lw);
        cpu.instructionMemory.setInstruction(4, add1);
        cpu.instructionMemory.setInstruction(8, add2);

        int stallCount = 0;

        for (int i = 0; i < 8; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);

                if (snapshot.getIfStage().getState().toString().equals("STALL")) {
                    stallCount++;
                }
            }
        }

        assertTrue(stallCount >= 1, "Should handle multiple consecutive hazards");
        assertEquals(20, cpu.registerFile.get(3), "Load should get value from memory");
        assertEquals(30, cpu.registerFile.get(4), "First add should compute correctly");
        assertEquals(40, cpu.registerFile.get(5), "Second add should compute correctly with forwarding");
    }

    @Test
    void testBranchWithDataHazard() {
        cpu.dataMemory.storeWord(5, 10);

        ITypeInstruction lw = new ITypeInstruction(35, 0x8C230000); // lw $3, 0($1)
        ITypeInstruction beq = new ITypeInstruction(4, 0x10600008); // beq $3, $0, 8
        lw.decodeFields();
        beq.decodeFields();

        cpu.instructionMemory.setInstruction(0, lw);
        cpu.instructionMemory.setInstruction(4, beq);

        boolean foundStall = false;
        boolean foundFlush = false;

        for (int i = 0; i < 8; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);

                if (snapshot.getIfStage().getState().toString().equals("STALL")) {
                    foundStall = true;
                }
                if (snapshot.getIfStage().getState().toString().equals("FLUSH") ||
                        snapshot.getIdStage().getState().toString().equals("FLUSH")) {
                    foundFlush = true;
                }
            }
        }

        assertTrue(foundStall, "Should stall for branch data hazard");
        assertFalse(foundFlush, "Branch should not be taken with non-zero value");
    }

    @Test
    void testMemoryAccessHazards() {
        ITypeInstruction sw = new ITypeInstruction(43, 0xAC220000); // sw $2, 0($1)
        ITypeInstruction lw = new ITypeInstruction(35, 0x8C230000); // lw $3, 0($1)
        sw.decodeFields();
        lw.decodeFields();

        cpu.instructionMemory.setInstruction(0, sw);
        cpu.instructionMemory.setInstruction(4, lw);

        for (int i = 0; i < 6; i++) {
            controller.runCycle();
        }

        assertEquals(10, cpu.dataMemory.loadWord(5), "Memory should contain stored value");
        assertEquals(10, cpu.registerFile.get(3), "Should load correct value from memory");
    }

    @Test
    void testComplexForwardingScenarios() {
        RTypeInstruction add1 = new RTypeInstruction(0, 0x00221820); // add $3, $1, $2
        RTypeInstruction add2 = new RTypeInstruction(0, 0x00622020); // add $4, $3, $2
        RTypeInstruction add3 = new RTypeInstruction(0, 0x00642820); // add $5, $3, $4
        add1.decodeFields();
        add2.decodeFields();
        add3.decodeFields();

        cpu.instructionMemory.setInstruction(0, add1);
        cpu.instructionMemory.setInstruction(4, add2);
        cpu.instructionMemory.setInstruction(8, add3);

        boolean foundStall = false;

        for (int i = 0; i < 8; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);

                if (snapshot.getIfStage().getState().toString().equals("STALL")) {
                    foundStall = true;
                    break;
                }
            }
        }

        assertFalse(foundStall, "Forwarding should handle complex dependency chains without stalls");
        assertEquals(15, cpu.registerFile.get(3), "First add result");
        assertEquals(25, cpu.registerFile.get(4), "Second add result");
        assertEquals(40, cpu.registerFile.get(5), "Third add result with complex forwarding");
    }

    @Test
    void testPipelineFullThroughput() {
        RTypeInstruction add1 = new RTypeInstruction(0, 0x00221820); // add $3, $1, $2
        RTypeInstruction add2 = new RTypeInstruction(0, 0x00222020); // add $4, $1, $2
        RTypeInstruction add3 = new RTypeInstruction(0, 0x00222820); // add $5, $1, $2
        add1.decodeFields();
        add2.decodeFields();
        add3.decodeFields();

        cpu.instructionMemory.setInstruction(0, add1);
        cpu.instructionMemory.setInstruction(4, add2);
        cpu.instructionMemory.setInstruction(8, add3);

        int instructionCount = 0;
        int cycleCount = 8;

        for (int i = 0; i < cycleCount; i++) {
            controller.runCycle();
        }

        if (cpu.registerFile.get(3) == 15) instructionCount++;
        if (cpu.registerFile.get(4) == 15) instructionCount++;
        if (cpu.registerFile.get(5) == 15) instructionCount++;

        assertEquals(3, instructionCount, "Should achieve high throughput with independent instructions");
    }

    @Test
    void testZeroRegisterImmutable() {
        RTypeInstruction addToZero = new RTypeInstruction(0, 0x00220020); // add $0, $1, $2
        addToZero.decodeFields();

        cpu.instructionMemory.setInstruction(0, addToZero);

        for (int i = 0; i < 5; i++) {
            controller.runCycle();
        }

        assertEquals(0, cpu.registerFile.get(0), "$zero register should always remain 0");
    }
}