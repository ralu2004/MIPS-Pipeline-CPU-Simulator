package tests;

import model.pipeline.state.PipelineSnapshot;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import simulator.*;
import model.cpu.CPUState;
import model.memory.InstructionMemory;
import java.util.List;

public class MIPSTest {

    private static final int START_ADDRESS = 0x00000000;
    private CPUState cpuState;
    private PipelineController pipelineController;

    @BeforeEach
    void setUp() {
        cpuState = new CPUState(new InstructionMemory());
        pipelineController = new PipelineController(cpuState);
        ProgramLoader.resetState(cpuState, true, true, START_ADDRESS);
        pipelineController.clearPipeline();
        pipelineController.clearHistory();
    }

    @Test
    @DisplayName("Test 1: Basic Arithmetic Operations")
    void testBasicArithmetic() {
        String[] assembly = {
                "addi $t0, $zero, 10",    // $t0 = 0 + 10 = 10
                "addi $t1, $zero, 5",     // $t1 = 0 + 5 = 5
                "add $t0, $t0, $t1",      // $t0 = 10 + 5 = 15
                "sub $t1, $t0, $t0"       // $t1 = 15 - 15 = 0
        };

        ProgramLoader.ProgramLoadResult loadResult =
                ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        assertTrue(loadResult.warnings.isEmpty(), "No warnings expected");

        runCycles(8);

        assertEquals(15, cpuState.registerFile.get(8), "$t0 should be 15");
        assertEquals(0, cpuState.registerFile.get(9), "$t1 should be 5");
    }

    @Test
    @DisplayName("Test 2: RAW Data Hazard")
    void testRAWHazard() {
        String[] assembly = {
                "addi $t1, $zero, 10",    // $t1 = 0 + 10 = 10
                "addi $t2, $zero, 10",    // $t2 = 0 + 10 = 10
                "add $t0, $t1, $t2",      // $t0 = 10 + 10 = 20
                "add $t3, $t0, $zero"     // uses $t0 (RAW hazard)
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(8);

        assertEquals(20, cpuState.registerFile.get(8), "$t0 should be 20");
        assertEquals(10, cpuState.registerFile.get(9), "$t1 should be 10");
        assertEquals(10, cpuState.registerFile.get(10), "$t2 should be 10");
        assertEquals(20, cpuState.registerFile.get(11), "$t3 should be 20");
    }

    @Test
    @DisplayName("Test 3: Load-Use Hazard")
    void testLoadUseHazard() {
        String[] assembly = {
                "addi $t0, $zero, 42",    // $t0 = 42
                "sw $t0, 0($zero)",       // Store 42 to memory[0]
                "lw $t1, 0($zero)",       // Load from memory[0]
                "add $t2, $t1, $zero"     // Use loaded value (load-use hazard)
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(10);

        assertEquals(42, cpuState.registerFile.get(10), "$t2 should be 42");
        assertEquals(42, cpuState.dataMemory.loadWord(0), "Memory[0] should be 42");
    }

    @Test
    @DisplayName("Test 4: Memory Operations")
    void testMemoryOperations() {
        String[] assembly = {
                "addi $t0, $zero, 100",   // $t0 = 100
                "addi $t1, $zero, 200",   // $t1 = 200
                "sw $t0, 0($zero)",       // Store 100 at address 0
                "sw $t1, 4($zero)",       // Store 200 at address 4
                "lw $t2, 0($zero)",       // Load from address 0
                "lw $t3, 4($zero)"        // Load from address 4
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(10);

        assertEquals(100, cpuState.dataMemory.loadWord(0), "Memory[0] should be 100");
        assertEquals(200, cpuState.dataMemory.loadWord(4), "Memory[4] should be 200");
        assertEquals(100, cpuState.registerFile.get(10), "$t2 should be 100");
        assertEquals(200, cpuState.registerFile.get(11), "$t3 should be 200");
    }

    @Test
    @DisplayName("Test 5: Branch Equal (Taken)")
    void testBranchEqualTaken() {
        String[] assembly = {
                "addi $t0, $zero, 5",
                "addi $t1, $zero, 5",
                "beq $t0, $t1, target",
                "addi $t0, $zero, 88",    // Should be skipped
                "target:",
                "addi $t0, $zero, 99"     // Should execute
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(10);

        assertEquals(99, cpuState.registerFile.get(8), "$t0 should be 99 (branch taken)");
    }

    @Test
    @DisplayName("Test 6: Branch Equal (Not Taken)")
    void testBranchEqualNotTaken() {
        String[] assembly = {
                "addi $t0, $zero, 5",
                "addi $t1, $zero, 6",
                "beq $t0, $t1, target",
                "addi $t2, $zero, 77",    // Should execute
                "target:",
                "addi $t2, $zero, 88"     // Should also execute
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(10);

        assertEquals(88, cpuState.registerFile.get(10), "$t2 should be 88");
    }

    @Test
    @DisplayName("Test 7: Branch Not Equal")
    void testBranchNotEqual() {
        String[] assembly = {
                "addi $t0, $zero, 5",
                "addi $t1, $zero, 6",
                "bne $t0, $t1, target",
                "addi $t3, $zero, 99",    // Should be skipped
                "target:",
                "addi $t3, $zero, 111"    // Should execute
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(10);

        assertEquals(111, cpuState.registerFile.get(11), "$t3 should be 111");
    }

    @Test
    @DisplayName("Test 8: Logical Operations")
    void testLogicalOperations() {
        String[] assembly = {
                "addi $t0, $zero, 15",    // 00001111
                "addi $t1, $zero, 240",   // 11110000
                "and $t2, $t0, $t1",      // 15 & 240 = 0
                "or $t3, $t0, $t1",       // 15 | 240 = 255
                "xor $t4, $t0, $t0",      // 15 ^ 15 = 0
                "nor $t5, $zero, $zero"   // ~(0 | 0) = -1
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(10);

        assertEquals(0, cpuState.registerFile.get(10), "$t2 should be 0 (AND)");
        assertEquals(255, cpuState.registerFile.get(11), "$t3 should be 255 (OR)");
        assertEquals(0, cpuState.registerFile.get(12), "$t4 should be 0 (XOR)");
        assertEquals(-1, cpuState.registerFile.get(13), "$t5 should be -1 (NOR)");
    }

    @Test
    @DisplayName("Test 9: Set Less Than")
    void testSetLessThan() {
        String[] assembly = {
                "addi $t0, $zero, 5",     // $t0 = 5
                "addi $t1, $zero, 10",    // $t1 = 10
                "slt $t2, $t0, $t1",      // 5 < 10 = 1
                "slt $t3, $t1, $t0"       // 10 < 5 = 0
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(8);

        assertEquals(1, cpuState.registerFile.get(10), "$t2 should be 1");
        assertEquals(0, cpuState.registerFile.get(11), "$t3 should be 0");
    }

    @Test
    @DisplayName("Test 10: Zero Register Immutability")
    void testZeroRegister() {
        String[] assembly = {
                "addi $zero, $zero, 100", // attempt to modify $zero
                "add $t0, $zero, $zero"   // $t0 should still be 0
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(6);

        assertEquals(0, cpuState.registerFile.get(0), "$zero should always be 0");
        assertEquals(0, cpuState.registerFile.get(8), "$t0 should be 0");
    }

    @Test
    @DisplayName("Test 11: Negative Numbers")
    void testNegativeNumbers() {
        String[] assembly = {
                "addi $t0, $zero, -1",
                "addi $t1, $zero, -10",
                "add $t2, $t0, $t1"       // -1 + (-10) = -11
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(7);

        assertEquals(-1, cpuState.registerFile.get(8), "$t0 should be -1");
        assertEquals(-10, cpuState.registerFile.get(9), "$t1 should be -10");
        assertEquals(-11, cpuState.registerFile.get(10), "$t2 should be -11");
    }

    @Test
    @DisplayName("Test 12: Overflow Test")
    void testOverflow() {
        String[] assembly = {
                "addi $t0, $zero, 32767",     // max positive 16-bit signed
                "addi $t0, $t0, 32767",
                "addi $t0, $t0, 2"            // should overflow/wrap
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(7);

        int expected = 32767 + 32767 + 2;
        assertEquals(expected, cpuState.registerFile.get(8), "Overflow test");
    }

    @Test
    @DisplayName("Test 13: Sequential Memory Access")
    void testSequentialMemory() {
        String[] assembly = {
                "addi $t0, $zero, 10",
                "addi $t1, $zero, 20",
                "addi $t2, $zero, 30",
                "addi $t3, $zero, 40",
                "addi $t4, $zero, 50",
                "sw $t0, 0($zero)",
                "sw $t1, 4($zero)",
                "sw $t2, 8($zero)",
                "sw $t3, 12($zero)",
                "sw $t4, 16($zero)"
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(14);

        assertEquals(10, cpuState.dataMemory.loadWord(0), "Memory[0] should be 10");
        assertEquals(20, cpuState.dataMemory.loadWord(4), "Memory[4] should be 20");
        assertEquals(30, cpuState.dataMemory.loadWord(8), "Memory[8] should be 30");
        assertEquals(40, cpuState.dataMemory.loadWord(12), "Memory[12] should be 40");
        assertEquals(50, cpuState.dataMemory.loadWord(16), "Memory[16] should be 50");
    }

    @Test
    @DisplayName("Test 14: Loop Counter (0 to 5)")
    void testLoopCounter() {
        String[] assembly = {
                "addi $t0, $zero, 0",     // counter = 0
                "addi $t1, $zero, 5",     // limit = 5
                "loop_start:",
                "addi $t0, $t0, 1",       // counter++
                "slt $t2, $t0, $t1",      // check if counter < limit
                "bne $t2, $zero, loop_start"
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(30);

        assertEquals(5, cpuState.registerFile.get(8), "$t0 should be 5 after loop");
    }

    @Test
    @DisplayName("Test 15: Fibonacci (First 5 numbers)")
    void testFibonacci() {
        String[] assembly = {
                "addi $t0, $zero, 0",     // fib(0) = 0
                "addi $t1, $zero, 1",     // fib(1) = 1
                "sw $t0, 100($zero)",     // Store fib(0)
                "sw $t1, 104($zero)",     // Store fib(1)
                "add $t2, $t0, $t1",      // fib(2) = 1
                "sw $t2, 108($zero)",     // Store fib(2)
                "add $t0, $t1, $t2",      // fib(3) = 2
                "sw $t0, 112($zero)",     // Store fib(3)
                "add $t1, $t2, $t0",      // fib(4) = 3
                "sw $t1, 116($zero)"      // Store fib(4)
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(15);

        assertEquals(0, cpuState.dataMemory.loadWord(100), "fib(0) should be 0");
        assertEquals(1, cpuState.dataMemory.loadWord(104), "fib(1) should be 1");
        assertEquals(1, cpuState.dataMemory.loadWord(108), "fib(2) should be 1");
        assertEquals(2, cpuState.dataMemory.loadWord(112), "fib(3) should be 2");
        assertEquals(3, cpuState.dataMemory.loadWord(116), "fib(4) should be 3");
    }

    @Test
    @DisplayName("Test 16: Offset Addressing")
    void testOffsetAddressing() {
        String[] assembly = {
                "addi $t0, $zero, 100",   // base address
                "addi $t1, $zero, 77",    // value
                "sw $t1, 0($t0)",         // Store at 100
                "sw $t1, 4($t0)",         // Store at 104
                "sw $t1, -4($t0)"         // Store at 96 (negative offset)
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(9);

        assertEquals(77, cpuState.dataMemory.loadWord(100), "Memory[100] should be 77");
        assertEquals(77, cpuState.dataMemory.loadWord(104), "Memory[104] should be 77");
        assertEquals(77, cpuState.dataMemory.loadWord(96), "Memory[96] should be 77");
    }

    @Test
    @DisplayName("Test 17: Multiple Hazards")
    void testMultipleHazards() {
        String[] assembly = {
                "addi $t0, $zero, 10",
                "add $t1, $t0, $t0",      // RAW on $t0
                "add $t2, $t1, $t1",      // RAW on $t1
                "add $t3, $t2, $t2"       // RAW on $t2
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(10);

        assertEquals(20, cpuState.registerFile.get(9), "$t1 should be 20");
        assertEquals(40, cpuState.registerFile.get(10), "$t2 should be 40");
        assertEquals(80, cpuState.registerFile.get(11), "$t3 should be 80");
    }

    @Test
    @DisplayName("Test 18: Branch with Hazard")
    void testBranchWithHazard() {
        String[] assembly = {
                "addi $t0, $zero, 5",
                "addi $t1, $zero, 5",
                "add $t2, $t0, $t1",      // $t2 = 10
                "beq $t2, $t1, skip",     // Use $t2 immediately (10 != 5, branch not taken)
                "addi $t3, $zero, 99",
                "skip:",
                "addi $t4, $zero, 100"
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(12);

        assertEquals(99, cpuState.registerFile.get(11), "$t3 should be 99 (branch not taken)");
        assertEquals(100, cpuState.registerFile.get(12), "$t4 should be 100");
    }

    @Test
    @DisplayName("Test 19: Complex Control Flow")
    void testComplexControlFlow() {
        String[] assembly = {
                "addi $t0, $zero, 3",
                "addi $t1, $zero, 3",
                "beq $t0, $t1, path1",    // Branch taken
                "addi $t2, $zero, 1",
                "j end_test19",
                "path1:",
                "addi $t2, $zero, 2",
                "end_test19:",
                "addi $t3, $zero, 99"
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(12);

        assertEquals(2, cpuState.registerFile.get(10), "$t2 should be 2 (branch taken)");
        assertEquals(99, cpuState.registerFile.get(11), "$t3 should be 99");
    }

    @Test
    @DisplayName("Test 20: Stress Test - Many Instructions")
    void testStressTest() {
        String[] assembly = {
                "addi $s0, $zero, 1",
                "addi $s1, $zero, 2",
                "addi $s2, $zero, 3",
                "addi $s3, $zero, 4",
                "addi $s4, $zero, 5",
                "add $s5, $s0, $s1",
                "add $s6, $s2, $s3",
                "add $s7, $s4, $s5",
                "sub $t0, $s6, $s7",
                "and $t1, $s0, $s1",
                "or $t2, $s2, $s3",
                "xor $t3, $s4, $s5"
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(20);

        // $s0-$s7 = 16-23, $t0-$t7 = 8-15
        assertEquals(3, cpuState.registerFile.get(21), "$s5 should be 3 (1+2)");
        assertEquals(7, cpuState.registerFile.get(22), "$s6 should be 7 (3+4)");
        assertEquals(8, cpuState.registerFile.get(23), "$s7 should be 8 (5+3)");
        assertEquals(-1, cpuState.registerFile.get(8), "$t0 should be -1 (7-8)");
        assertEquals(0, cpuState.registerFile.get(9), "$t1 should be 0 (1&2)");
        assertEquals(7, cpuState.registerFile.get(10), "$t2 should be 7 (3|4)");
        assertEquals(6, cpuState.registerFile.get(11), "$t3 should be 6 (5^3)");
    }

    @Test
    @DisplayName("Test 21: Shift Left Logical")
    void testShiftLeftLogical() {
        String[] assembly = {
                "addi $t0, $zero, 1",     // $t0 = 1 (binary: 000...0001)
                "sll $t1, $t0, 3",        // $t1 = 1 << 3 = 8 (binary: 000...1000)
                "sll $t2, $t0, 0",        // $t2 = 1 << 0 = 1
                "addi $t3, $zero, -1",    // $t3 = -1 (all 1s in two's complement)
                "sll $t4, $t3, 16"        // $t4 = -1 << 16 = 0xFFFF0000
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(10);

        assertEquals(8, cpuState.registerFile.get(9), "$t1 should be 8 (1 << 3)");
        assertEquals(1, cpuState.registerFile.get(10), "$t2 should be 1 (1 << 0)");
        assertEquals(0xFFFF0000, cpuState.registerFile.get(12), "$t4 should be 0xFFFF0000");
    }

    @Test
    @DisplayName("Test 22: Shift Right Logical")
    void testShiftRightLogical() {
        String[] assembly = {
                "addi $t0, $zero, 8",     // $t0 = 8 (binary: 000...1000)
                "srl $t1, $t0, 3",        // $t1 = 8 >>> 3 = 1
                "addi $t2, $zero, -1",    // $t2 = -1 (all 1s)
                "srl $t3, $t2, 16"        // $t3 = -1 >>> 16 = 0x0000FFFF
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(8);

        assertEquals(1, cpuState.registerFile.get(9), "$t1 should be 1 (8 >>> 3)");
        assertEquals(0x0000FFFF, cpuState.registerFile.get(11), "$t3 should be 0x0000FFFF");
    }

    @Test
    @DisplayName("Test 23: Jump and Link")
    void testJumpAndLink() {
        String[] assembly = {
                "jal function",
                "j end",
                "function:",
                "addi $t1, $zero, 42",
                "j end",
                "addi $t0, $zero, 99",
                "end:",
                "addi $t2, $zero, 77"
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(12);

        int expectedRA = 0x00000004; // jal is at address 0, so PC+4 = 4
        assertEquals(expectedRA, cpuState.registerFile.get(31), "$ra should contain return address");
        assertEquals(42, cpuState.registerFile.get(9), "$t1 should be 42");
        assertEquals(77, cpuState.registerFile.get(10), "$t2 should be 77");
    }

    @Test
    @DisplayName("Test 24: Immediate Logical Operations")
    void testImmediateLogical() {
        String[] assembly = {
                "addi $t0, $zero, 0xFF",  // $t0 = 255
                "ori $t1, $t0, 0x0F0",    // $t1 = 255 | 240 = 255
                "andi $t2, $t0, 0x0F",    // $t2 = 255 & 15 = 15
                "addi $t3, $zero, 10",
                "slti $t4, $t3, 20",      // $t4 = 10 < 20 = 1
                "slti $t5, $t3, 5"        // $t5 = 10 < 5 = 0
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(12);

        assertEquals(255, cpuState.registerFile.get(9), "$t1 should be 255 (ORI)");
        assertEquals(15, cpuState.registerFile.get(10), "$t2 should be 15 (ANDI)");
        assertEquals(1, cpuState.registerFile.get(12), "$t4 should be 1 (SLTI true)");
        assertEquals(0, cpuState.registerFile.get(13), "$t5 should be 0 (SLTI false)");
    }

    @Test
    @DisplayName("Test 25: Memory Alignment")
    void testMemoryAlignment() {
        String[] assembly = {
                "addi $t0, $zero, 0x5678",
                "sw $t0, 0($zero)",
                "sw $t0, 4($zero)",
                "lw $t1, 0($zero)",
                "lw $t2, 4($zero)",
                "addi $t3, $zero, 100",
                "sw $t0, -4($t3)",
                "lw $t4, -4($t3)"
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(14);

        // 0x5678 = 22136
        assertEquals(0x5678, cpuState.registerFile.get(9), "$t1 should be 0x5678");
        assertEquals(0x5678, cpuState.registerFile.get(10), "$t2 should be 0x5678");
        assertEquals(0x5678, cpuState.registerFile.get(12), "$t4 should be 0x5678");
        assertEquals(0x5678, cpuState.dataMemory.loadWord(0), "Memory[0] should be 0x5678");
        assertEquals(0x5678, cpuState.dataMemory.loadWord(96), "Memory[96] should be 0x5678");
    }

    @Test
    @DisplayName("Test 26: Complex Pipeline Hazards")
    void testComplexPipelineHazards() {
        String[] assembly = {
                "addi $t0, $zero, 1",
                "add $t1, $t0, $t0",      // depends on $t0
                "add $t2, $t1, $t1",      // depends on $t1
                "add $t3, $t2, $t2",      // depends on $t2
                "add $t4, $t3, $t3",      // depends on $t3
                "addi $t5, $zero, 2",
                "addi $t6, $zero, 3",
                "add $t7, $t5, $t6",      // depends on $t5, $t6
                "sub $t8, $t7, $t5",      // depends on $t7, $t5
                "and $t9, $t8, $t6"       // depends on $t8, $t6
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(20);

        assertEquals(2, cpuState.registerFile.get(9), "$t1 should be 2");
        assertEquals(4, cpuState.registerFile.get(10), "$t2 should be 4");
        assertEquals(8, cpuState.registerFile.get(11), "$t3 should be 8");
        assertEquals(16, cpuState.registerFile.get(12), "$t4 should be 16");
        assertEquals(5, cpuState.registerFile.get(15), "$t7 should be 5");
        assertEquals(3, cpuState.registerFile.get(24), "$t8 should be 3");
        assertEquals(3 & 3, cpuState.registerFile.get(25), "$t9 should be 3 & 3 = 3");
    }

    @Test
    @DisplayName("Test 27: Control Hazard Variations")
    void testControlHazardVariations() {
        String[] assembly = {
                "addi $t0, $zero, 1",
                "addi $t1, $zero, 1",
                "beq $t0, $t1, equal",
                "addi $t2, $zero, 99",    // Should be skipped
                "j end",
                "equal:",
                "addi $t2, $zero, 42",    // Should execute
                "end:",
                "addi $t3, $zero, 5",
                "addi $t4, $zero, 10",
                "slt $t5, $t3, $t4",      // $t5 = 1
                "bne $t5, $zero, outer",
                "addi $t6, $zero, 11",
                "j finish",
                "outer:",
                "addi $t6, $zero, 22",
                "beq $t3, $t4, inner",    // Should not be taken
                "addi $t7, $zero, 33",
                "j finish",
                "inner:",
                "addi $t7, $zero, 44",
                "finish:"
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(20);

        assertEquals(42, cpuState.registerFile.get(10), "$t2 should be 42");
        assertEquals(22, cpuState.registerFile.get(14), "$t6 should be 22");
        assertEquals(33, cpuState.registerFile.get(15), "$t7 should be 33");
    }

    @Test
    @DisplayName("Test 28: Zero Register Corner Cases")
    void testZeroRegisterCornerCases() {
        String[] assembly = {
                "add $zero, $zero, $zero",
                "sub $zero, $zero, $zero",
                "and $zero, $zero, $zero",
                "or $zero, $zero, $zero",
                "xor $zero, $zero, $zero",
                "nor $zero, $zero, $zero",
                "slt $zero, $zero, $zero",
                "sll $zero, $zero, 5",
                "srl $zero, $zero, 5",
                "add $t0, $zero, $zero",      // $t0 = 0
                "addi $t1, $zero, 100",       // $t1 = 100
                "add $t2, $t1, $zero",        // $t2 = 100
                "sub $t3, $t1, $zero",        // $t3 = 100
                "and $t4, $t1, $zero",        // $t4 = 0
                "or $t5, $t1, $zero",         // $t5 = 100
                "slt $t6, $zero, $t1"         // $t6 = 1 (0 < 100)
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(25);

        assertEquals(0, cpuState.registerFile.get(0), "$zero should still be 0");
        assertEquals(0, cpuState.registerFile.get(8), "$t0 should be 0");
        assertEquals(100, cpuState.registerFile.get(9), "$t1 should be 100");
        assertEquals(100, cpuState.registerFile.get(10), "$t2 should be 100");
        assertEquals(100, cpuState.registerFile.get(11), "$t3 should be 100");
        assertEquals(0, cpuState.registerFile.get(12), "$t4 should be 0");
        assertEquals(100, cpuState.registerFile.get(13), "$t5 should be 100");
        assertEquals(1, cpuState.registerFile.get(14), "$t6 should be 1");
    }

    @Test
    @DisplayName("Test 29: Data Memory Edge Cases")
    void testDataMemoryEdgeCases() {
        String[] assembly = {
                "addi $t0, $zero, 0xBEEF",    // 0xBEEF = -16657 in signed 16-bit
                "sw $t0, 0($zero)",           // store at address 0
                "sw $t0, 1020($zero)",
                "lw $t1, 0($zero)",
                "lw $t2, 1020($zero)",
                "addi $t3, $zero, 1",
                "sw $t3, 100($zero)",
                "addi $t3, $zero, 2",
                "sw $t3, 100($zero)",
                "lw $t4, 100($zero)",
                "addi $t5, $zero, 200",
                "sw $t0, 0($t5)",
                "lw $t6, 0($t5)",
                "add $t7, $t6, $zero"
        };

        ProgramLoader.loadFromAssembly(cpuState, assembly, START_ADDRESS);
        runCycles(20);

        int expected = 0xFFFFBEEF;
        assertEquals(expected, cpuState.registerFile.get(9), "$t1 should be 0xFFFFBEEF (sign-extended)");
        assertEquals(expected, cpuState.registerFile.get(10), "$t2 should be 0xFFFFBEEF");
        assertEquals(2, cpuState.registerFile.get(12), "$t4 should be 2");
        assertEquals(expected, cpuState.registerFile.get(15), "$t7 should be 0xFFFFBEEF");
    }

    @Test
    @DisplayName("Complete Test Suite Run")
    void runCompleteTestSuite() {
        System.out.println("Starting complete MIPS test suite...");

        int totalTests = 29;
        int passedTests = 0;

        try { setUp(); testBasicArithmetic(); passedTests++; System.out.println("Test 1 passed"); } catch (Exception e) { System.out.println("Test 1 failed: " + e.getMessage()); }
        try { setUp(); testRAWHazard(); passedTests++; System.out.println("Test 2 passed"); } catch (Exception e) { System.out.println("Test 2 failed: " + e.getMessage()); }
        try { setUp(); testLoadUseHazard(); passedTests++; System.out.println("Test 3 passed"); } catch (Exception e) { System.out.println("Test 3 failed: " + e.getMessage()); }
        try { setUp(); testMemoryOperations(); passedTests++; System.out.println("Test 4 passed"); } catch (Exception e) { System.out.println("Test 4 failed: " + e.getMessage()); }
        try { setUp(); testBranchEqualTaken(); passedTests++; System.out.println("Test 5 passed"); } catch (Exception e) { System.out.println("Test 5 failed: " + e.getMessage()); }
        try { setUp(); testBranchEqualNotTaken(); passedTests++; System.out.println("Test 6 passed"); } catch (Exception e) { System.out.println("Test 6 failed: " + e.getMessage()); }
        try { setUp(); testBranchNotEqual(); passedTests++; System.out.println("Test 7 passed"); } catch (Exception e) { System.out.println("Test 7 failed: " + e.getMessage()); }
        try { setUp(); testLogicalOperations(); passedTests++; System.out.println("Test 8 passed"); } catch (Exception e) { System.out.println("Test 8 failed: " + e.getMessage()); }
        try { setUp(); testSetLessThan(); passedTests++; System.out.println("Test 9 passed"); } catch (Exception e) { System.out.println("Test 9 failed: " + e.getMessage()); }
        try { setUp(); testZeroRegister(); passedTests++; System.out.println("Test 10 passed"); } catch (Exception e) { System.out.println("Test 10 failed: " + e.getMessage()); }
        try { setUp(); testNegativeNumbers(); passedTests++; System.out.println("Test 11 passed"); } catch (Exception e) { System.out.println("Test 11 failed: " + e.getMessage()); }
        try { setUp(); testOverflow(); passedTests++; System.out.println("Test 12 passed"); } catch (Exception e) { System.out.println("Test 12 failed: " + e.getMessage()); }
        try { setUp(); testSequentialMemory(); passedTests++; System.out.println("Test 13 passed"); } catch (Exception e) { System.out.println("Test 13 failed: " + e.getMessage()); }
        try { setUp(); testLoopCounter(); passedTests++; System.out.println("Test 14 passed"); } catch (Exception e) { System.out.println("Test 14 failed: " + e.getMessage()); }
        try { setUp(); testFibonacci(); passedTests++; System.out.println("Test 15 passed"); } catch (Exception e) { System.out.println("Test 15 failed: " + e.getMessage()); }
        try { setUp(); testOffsetAddressing(); passedTests++; System.out.println("Test 16 passed"); } catch (Exception e) { System.out.println("Test 16 failed: " + e.getMessage()); }
        try { setUp(); testMultipleHazards(); passedTests++; System.out.println("Test 17 passed"); } catch (Exception e) { System.out.println("Test 17 failed: " + e.getMessage()); }
        try { setUp(); testBranchWithHazard(); passedTests++; System.out.println("Test 18 passed"); } catch (Exception e) { System.out.println("Test 18 failed: " + e.getMessage()); }
        try { setUp(); testComplexControlFlow(); passedTests++; System.out.println("Test 19 passed"); } catch (Exception e) { System.out.println("Test 19 failed: " + e.getMessage()); }
        try { setUp(); testStressTest(); passedTests++; System.out.println("Test 20 passed"); } catch (Exception e) { System.out.println("Test 20 failed: " + e.getMessage()); }
        try { setUp(); testShiftLeftLogical(); passedTests++; System.out.println("Test 21 passed"); } catch (Exception e) { System.out.println("Test 21 failed: " + e.getMessage()); }
        try { setUp(); testShiftRightLogical(); passedTests++; System.out.println("Test 22 passed"); } catch (Exception e) { System.out.println("Test 22 failed: " + e.getMessage()); }
        try { setUp(); testJumpAndLink(); passedTests++; System.out.println("Test 23 passed"); } catch (Exception e) { System.out.println("Test 23 failed: " + e.getMessage()); }
        try { setUp(); testImmediateLogical(); passedTests++; System.out.println("Test 24 passed"); } catch (Exception e) { System.out.println("Test 24 failed: " + e.getMessage()); }
        try { setUp(); testMemoryAlignment(); passedTests++; System.out.println("Test 25 passed"); } catch (Exception e) { System.out.println("Test 25 failed: " + e.getMessage()); }
        try { setUp(); testComplexPipelineHazards(); passedTests++; System.out.println("Test 26 passed"); } catch (Exception e) { System.out.println("Test 26 failed: " + e.getMessage()); }
        try { setUp(); testControlHazardVariations(); passedTests++; System.out.println("Test 27 passed"); } catch (Exception e) { System.out.println("Test 27 failed: " + e.getMessage()); }
        try { setUp(); testZeroRegisterCornerCases(); passedTests++; System.out.println("Test 28 passed"); } catch (Exception e) { System.out.println("Test 28 failed: " + e.getMessage()); }
        try { setUp(); testDataMemoryEdgeCases(); passedTests++; System.out.println("Test 29 passed"); } catch (Exception e) { System.out.println("Test 29 failed: " + e.getMessage()); }

        System.out.println("Test Suite Complete: " + passedTests + "/" + totalTests + " tests passed");

        assertEquals(totalTests, passedTests, "All tests should pass");
    }

    private void runCycles(int cycles) {
        for (int i = 0; i < cycles; i++) {
            pipelineController.runCycle();
        }
    }
}