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

        cpu.registerFile.set(1, 100);  // $1 = 100 (base address)
        cpu.registerFile.set(2, 200);  // $2 = 200
        cpu.registerFile.set(3, 300);  // $3 = 300
        cpu.registerFile.set(4, 400);  // $4 = 400
        cpu.registerFile.set(5, 500);  // $5 = 500

        cpu.dataMemory.storeWord(100, 12345);  // Mem[100] = 12345
        cpu.dataMemory.storeWord(104, 54321);  // Mem[104] = 54321
    }

    @Test
    void testSimpleNoHazardPipeline() {
        // add $6, $1, $2  (100 + 200 = 300)
        // add $7, $3, $4  (300 + 400 = 700)
        RTypeInstruction add1 = new RTypeInstruction(0, encodeRType(32, 1, 2, 6, 0)); // add $6, $1, $2
        RTypeInstruction add2 = new RTypeInstruction(0, encodeRType(32, 3, 4, 7, 0)); // add $7, $3, $4

        cpu.instructionMemory.setInstruction(0, add1);
        cpu.instructionMemory.setInstruction(4, add2);

        // Run enough cycles for both instructions to complete
        for (int i = 0; i < 7; i++) {
            controller.runCycle();
        }

        assertEquals(300, cpu.registerFile.get(6), "First add result");
        assertEquals(700, cpu.registerFile.get(7), "Second add result");

        // Check pipeline progression
        var history = controller.getHistory();
        assertFalse(history.isEmpty(), "Should have pipeline history");

        // Should have no stalls
        boolean hadStall = history.stream().anyMatch(snapshot ->
                snapshot.getIfStage().getState().toString().equals("STALL") ||
                        snapshot.getIdStage().getState().toString().equals("STALL")
        );
        assertFalse(hadStall, "Should have no stalls for independent instructions");
    }

    @Test
    void testLoadUseHazardDetection() {
        // lw $6, 0($1)      // Load from Mem[100] = 12345
        // add $7, $6, $2    // 12345 + 200 = 12545 (RAW on $6)

        ITypeInstruction lw = new ITypeInstruction(35, encodeIType(1, 6, 0)); // lw $6, 0($1)
        RTypeInstruction add = new RTypeInstruction(0, encodeRType(32, 6, 2, 7, 0)); // add $7, $6, $2

        cpu.instructionMemory.setInstruction(0, lw);
        cpu.instructionMemory.setInstruction(4, add);

        // Track pipeline states
        int stallCount = 0;
        int bubbleCount = 0;

        for (int i = 0; i < 8; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);

                if (snapshot.getIfStage().getState().toString().equals("STALL")) {
                    stallCount++;
                }
                if (snapshot.getExStage().getState().toString().equals("BUBBLE")) {
                    bubbleCount++;
                }
            }
        }

        // Should have exactly 1 stall and 1 bubble for load-use hazard
        assertEquals(1, stallCount, "Should stall for 1 cycle due to load-use hazard");
        assertEquals(1, bubbleCount, "Should insert 1 bubble in EX stage");

        // Final results
        assertEquals(12345, cpu.registerFile.get(6), "Loaded value");
        assertEquals(12545, cpu.registerFile.get(7), "Computed value after stall");
    }

    @Test
    void testForwardingEXtoEX() {
        // add $6, $1, $2    // 100 + 200 = 300
        // add $7, $6, $3    // 300 + 300 = 600 (forwarding from EX/MEM)
        // add $8, $7, $4    // 600 + 400 = 1000 (forwarding from EX/MEM)

        RTypeInstruction add1 = new RTypeInstruction(0, encodeRType(32, 1, 2, 6, 0));
        RTypeInstruction add2 = new RTypeInstruction(0, encodeRType(32, 6, 3, 7, 0));
        RTypeInstruction add3 = new RTypeInstruction(0, encodeRType(32, 7, 4, 8, 0));

        cpu.instructionMemory.setInstruction(0, add1);
        cpu.instructionMemory.setInstruction(4, add2);
        cpu.instructionMemory.setInstruction(8, add3);

        int stallCount = 0;

        for (int i = 0; i < 9; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);
                if (snapshot.getIfStage().getState().toString().equals("STALL")) {
                    stallCount++;
                }
            }
        }

        assertEquals(0, stallCount, "Forwarding should prevent stalls for EX→EX dependencies");
        assertEquals(300, cpu.registerFile.get(6), "First add");
        assertEquals(600, cpu.registerFile.get(7), "Second add with forwarding");
        assertEquals(1000, cpu.registerFile.get(8), "Third add with forwarding");
    }

    @Test
    void testForwardingMEMtoEX() {
        // lw $6, 0($1)      // Load 12345
        // nop               // Bubble
        // add $7, $6, $2    // Forward from MEM/WB

        ITypeInstruction lw = new ITypeInstruction(35, encodeIType(1, 6, 0));
        RTypeInstruction nop = new RTypeInstruction(0, 0x00000000); // nop
        RTypeInstruction add = new RTypeInstruction(0, encodeRType(32, 6, 2, 7, 0));

        cpu.instructionMemory.setInstruction(0, lw);
        cpu.instructionMemory.setInstruction(4, nop);
        cpu.instructionMemory.setInstruction(8, add);

        for (int i = 0; i < 9; i++) {
            controller.runCycle();
        }

        assertEquals(12345, cpu.registerFile.get(6), "Loaded value");
        assertEquals(12545, cpu.registerFile.get(7), "Add with forwarding from MEM stage");
    }

    @Test
    void testBranchFlush() {
        cpu.registerFile.set(1, 100);
        cpu.registerFile.set(2, 100);

        // 0: beq $1, $2, 2
        // 4: add $3, $1, $2
        // 12: add $4, $1, $2
        ITypeInstruction beq = new ITypeInstruction(4, encodeBranch(1, 2, 2));
        RTypeInstruction addDelay = new RTypeInstruction(0, encodeRType(32, 1, 2, 3, 0));
        RTypeInstruction addTarget = new RTypeInstruction(0, encodeRType(32, 1, 2, 4, 0));

        cpu.instructionMemory.setInstruction(0, beq);
        cpu.instructionMemory.setInstruction(4, addDelay);
        cpu.instructionMemory.setInstruction(12, addTarget);

        int flushCount = 0;

        for (int i = 0; i < 10; i++) {
            controller.runCycle();

            var history = controller.getHistory();
            if (!history.isEmpty()) {
                var snapshot = history.get(history.size() - 1);
                if (snapshot.getIfStage().getState().toString().equals("FLUSH") ||
                        snapshot.getIdStage().getState().toString().equals("FLUSH")) {
                    flushCount++;
                }
            }
        }

        assertTrue(flushCount > 0, "Pipeline should flush after a taken branch");
        assertEquals(100 + 100, cpu.registerFile.get(4), "Branch target instruction executed correctly");
    }


    @Test
    void testStoreWordNoHazard() {
        // lw $6, 0($1)      // Load 12345
        // nop               // Bubble
        // sw $6, 4($1)      // Store to Mem[104]

        ITypeInstruction lw = new ITypeInstruction(35, encodeIType(1, 6, 0));
        RTypeInstruction nop = new RTypeInstruction(0, 0x00000000);
        ITypeInstruction sw = new ITypeInstruction(43, encodeStore(1, 6, 4));

        cpu.instructionMemory.setInstruction(0, lw);
        cpu.instructionMemory.setInstruction(4, nop);
        cpu.instructionMemory.setInstruction(8, sw);

        for (int i = 0; i < 10; i++) {
            controller.runCycle();
        }

        assertEquals(12345, cpu.dataMemory.loadWord(104), "Should store loaded value");
    }

    @Test
    void testStoreWordWithHazard() {
        // lw $6, 0($1)      // Load 12345
        // sw $6, 4($1)      // Store immediately (HAZARD!)

        ITypeInstruction lw = new ITypeInstruction(35, encodeIType(1, 6, 0));
        ITypeInstruction sw = new ITypeInstruction(43, encodeStore(1, 6, 4));

        cpu.instructionMemory.setInstruction(0, lw);
        cpu.instructionMemory.setInstruction(4, sw);

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

        assertEquals(1, stallCount, "Should stall for load-store hazard");
        assertEquals(12345, cpu.dataMemory.loadWord(104), "Should store after stall");
    }

    private int encodeRType(int func, int rs, int rt, int rd, int shamt) {
        return (func & 0x3F) |
                ((shamt & 0x1F) << 6) |
                ((rd & 0x1F) << 11) |
                ((rt & 0x1F) << 16) |
                ((rs & 0x1F) << 21);
    }

    private int encodeIType(int rs, int rt, int immediate) {
        return (immediate & 0xFFFF) |
                ((rt & 0x1F) << 16) |
                ((rs & 0x1F) << 21);
    }

    private int encodeStore(int rs, int rt, int offset) {
        return (offset & 0xFFFF) |
                ((rt & 0x1F) << 16) |
                ((rs & 0x1F) << 21) |
                (43 << 26);  // sw opcode
    }

    private int encodeBranch(int rs, int rt, int offset) {
        return (offset & 0xFFFF) |
                ((rt & 0x1F) << 16) |
                ((rs & 0x1F) << 21) |
                (4 << 26);   // beq opcode
    }

    @Test
    void testSimpleBranchFlush() {
        // beq $1, $1, 8
        cpu.registerFile.set(1, 100);

        ITypeInstruction beq = new ITypeInstruction(4, encodeBranch(1, 1, 8));
        cpu.instructionMemory.setInstruction(0, beq);

        for (int i = 0; i < 6; i++) {
            controller.runCycle();
        }

        var history = controller.getHistory();
        boolean hadFlush = history.stream().anyMatch(snapshot ->
                snapshot.getIfStage().getState().toString().equals("FLUSH") ||
                        snapshot.getIdStage().getState().toString().equals("FLUSH")
        );

        assertTrue(hadFlush, "Should have pipeline flush for taken branch");
        System.out.println("Flush detected: " + hadFlush);
    }
}