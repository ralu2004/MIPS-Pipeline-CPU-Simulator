package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import simulator.Assembler;

public class AssemblerTest {

    private static final int START_ADDRESS = 0x00400000;

    @Test
    @DisplayName("Test R-type instructions")
    void testRTypeInstructions() {
        String[] assembly = {
                "add $t0, $s1, $s2",      // opcode=0, rs=17, rt=18, rd=8, funct=0x20
                "sub $t1, $t2, $t3",      // opcode=0, rs=10, rt=11, rd=9, funct=0x22
                "and $v0, $a0, $a1",      // opcode=0, rs=4, rt=5, rd=2, funct=0x24
                "or $v1, $a2, $a3",       // opcode=0, rs=6, rt=7, rd=3, funct=0x25
                "xor $s0, $s1, $s2",      // opcode=0, rs=17, rt=18, rd=16, funct=0x26
                "nor $s3, $s4, $s5",      // opcode=0, rs=20, rt=21, rd=19, funct=0x27
                "slt $t4, $t5, $t6",      // opcode=0, rs=13, rt=14, rd=12, funct=0x2A
                "sll $t0, $s1, 5",       // Shift left logical: $t0 = $s1 << 5
                "srl $t1, $s2, 3"        // Shift right logical: $t1 = $s2 >>> 3
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should have no errors: " + result.errors);
        assertEquals(9, result.machineCode.size(), "Should assemble 9 instructions");
        assertEquals(0x02324020, result.machineCode.get(0)); // add $t0, $s1, $s2
        assertEquals(0x014B4822, result.machineCode.get(1)); // sub $t1, $t2, $t3
        assertEquals(0x00851024, result.machineCode.get(2)); // and $v0, $a0, $a1
        assertEquals(0x00C71825, result.machineCode.get(3)); // or $v1, $a2, $a3
        assertEquals(0x02328026, result.machineCode.get(4)); // xor $s0, $s1, $s2
        assertEquals(0x02959827, result.machineCode.get(5)); // nor $s3, $s4, $s5
        assertEquals(0x01AE602A, result.machineCode.get(6)); // slt $t4, $t5, $t6
        assertEquals(0x00114140, result.machineCode.get(7)); // sll $t0, $s1, 5
        assertEquals(0x001248C2, result.machineCode.get(8)); // srl $t1, $s2, 3
    }

    @Test
    @DisplayName("Test I-type arithmetic instructions")
    void testITypeArithmetic() {
        String[] assembly = {
                "addi $t0, $s1, 100",     // opcode=0x08, rs=17, rt=8, imm=100
                "addi $t1, $s2, -50",     // opcode=0x08, rs=18, rt=9, imm=-50
                "andi $t3, $s4, 0xFF",    // opcode=0x0C, rs=20, rt=11, imm=0xFF
                "ori $t4, $s5, 0x55",     // opcode=0x0D, rs=21, rt=12, imm=0x55
                "slti $t5, $s6, 10"       // opcode=0x0A, rs=22, rt=13, imm=10
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should have no errors: " + result.errors);
        assertEquals(5, result.machineCode.size(), "Should assemble 5 instructions");
        // addi $t0, $s1, 100: 0x08 | 17<<21 | 8<<16 | 100 = 0x22280064
        assertEquals(0x22280064, result.machineCode.get(0));
        // addi $t1, $s2, -50: 0x08 | 18<<21 | 9<<16 | 0xFFCE = 0x2249FFCE
        assertEquals(0x2249FFCE, result.machineCode.get(1));
        // andi $t3, $s4, 0xFF: 0x0C | 20<<21 | 11<<16 | 0xFF = 0x328B00FF
        assertEquals(0x328B00FF, result.machineCode.get(2));
        // ori $t4, $s5, 0x55: 0x0D | 21<<21 | 12<<16 | 0x55 = 0x36AC0055
        assertEquals(0x36AC0055, result.machineCode.get(3));
        // slti $t5, $s6, 10: 0x0A | 22<<21 | 13<<16 | 10 = 0x2ACD000A
        assertEquals(0x2ACD000A, result.machineCode.get(4));
    }

    @Test
    @DisplayName("Test I-type load/store instructions")
    void testITypeLoadStore() {
        String[] assembly = {
                "lw $t0, 100($s1)",       // opcode=0x23, rs=17, rt=8, offset=100
                "sw $t1, -50($s2)",       // opcode=0x2B, rs=18, rt=9, offset=-50
                "lw $t2, 0x7F($s3)",      // opcode=0x23, rs=19, rt=10, offset=0x7F
                "sw $t3, 0xFF($s4)"       // opcode=0x2B, rs=20, rt=11, offset=0xFF
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should have no errors: " + result.errors);
        assertEquals(4, result.machineCode.size(), "Should assemble 4 instructions");
        assertEquals(0x8E280064, result.machineCode.get(0)); // lw $t0, 100($s1)
        assertEquals(0xAE49FFCE, result.machineCode.get(1)); // sw $t1, -50($s2)
        assertEquals(0x8E6A007F, result.machineCode.get(2)); // lw $t2, 0x7F($s3)
        assertEquals(0xAE8B00FF, result.machineCode.get(3)); // sw $t3, 0xFF($s4)
    }

    @Test
    @DisplayName("Test I-type branch instructions with labels")
    void testITypeBranchesWithLabels() {
        String[] assembly = {
                "start:",                     // Address: 0x00400000
                "addi $t0, $zero, 10",        // Address: 0x00400000
                "addi $t1, $zero, 0",         // Address: 0x00400004
                "loop:",                      // Address: 0x00400008
                "beq $t0, $zero, end",        // Address: 0x00400008, target: 0x00400014
                "addi $t1, $t1, 1",           // Address: 0x0040000C
                "addi $t0, $t0, -1",          // Address: 0x00400010
                "bne $t0, $zero, loop",       // Address: 0x00400014, target: 0x00400008
                "end:",                       // Address: 0x00400018
                "sw $t1, 0($sp)"              // Address: 0x00400018
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should have no errors: " + result.errors);
        assertEquals(7, result.machineCode.size(), "Should assemble 7 instructions");

        // beq $t0, $zero, end
        // Offset = (0x00400018 - (0x00400008 + 4)) / 4 = 0xC / 4 = 3
        int beqInstruction = result.machineCode.get(2);
        int beqOffset = beqInstruction & 0xFFFF;
        assertEquals(3, beqOffset, "beq offset should be 3");

        // bne $t0, $zero, loop
        // Offset = (0x00400008 - (0x00400014 + 4)) / 4 = -0x10 / 4 = -4 = 0xFFFC
        int bneInstruction = result.machineCode.get(5);
        int bneOffset = bneInstruction & 0xFFFF;
        assertEquals(0xFFFC, bneOffset, "bne offset should be 0xFFFC (-4)");
    }

    @Test
    @DisplayName("Test J-type instructions with labels")
    void testJTypeInstructions() {
        String[] assembly = {
                "j main",                     // Address: 0x00400000, target: main
                "add $zero, $zero, $zero",    // Address: 0x00400004 (nop)
                "main:",                      // Address: 0x00400008
                "jal subroutine",             // Address: 0x00400008, target: subroutine
                "j end",                      // Address: 0x0040000C, target: end
                "subroutine:",                // Address: 0x00400010
                "add $zero, $zero, $zero",    // Address: 0x00400010 (nop placeholder for jr)
                "end:"                        // Address: 0x00400014
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should have no errors: " + result.errors);

        // j main: target = 0x00400008 / 4 = 0x100002
        int jInstruction = result.machineCode.get(0);
        assertEquals(0x02, (jInstruction >> 26) & 0x3F, "j opcode should be 0x02");
        assertEquals(0x100002, jInstruction & 0x3FFFFFF, "j target should be 0x100002");

        // jal subroutine: target = 0x00400010 / 4 = 0x100004
        int jalInstruction = result.machineCode.get(2);
        assertEquals(0x03, (jalInstruction >> 26) & 0x3F, "jal opcode should be 0x03");
        assertEquals(0x100004, jalInstruction & 0x3FFFFFF, "jal target should be 0x100004");
    }

    @Test
    @DisplayName("Test register aliases")
    void testRegisterAliases() {
        String[] assembly = {
                "add $8, $17, $18",
                "add $t0, $17, $s2",
                "addi $8, $17, 100",
                "lw $24, 100($28)"       // $24 = $t8, $28 = $gp
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should have no errors: " + result.errors);
        assertEquals(4, result.machineCode.size(), "Should assemble 4 instructions");
        assertEquals(0x02324020, result.machineCode.get(0));
        assertEquals(0x02324020, result.machineCode.get(1));
    }

    @Test
    @DisplayName("Test immediate parsing in different bases")
    void testImmediateParsing() {
        String[] assembly = {
                "addi $t0, $zero, 100",    // Decimal
                "addi $t1, $zero, 0x64",   // Hex with 0x (100 decimal)
                "addi $t2, $zero, 0XFF",   // Hex with 0X (255 decimal)
                "addi $t3, $zero, -1"      // Negative decimal
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should have no errors: " + result.errors);
        assertEquals(4, result.machineCode.size(), "Should assemble 4 instructions");

        // addi $t0, $zero, 100: imm = 0x0064
        assertEquals(0x20080064, result.machineCode.get(0));
        // addi $t1, $zero, 0x64: imm = 0x0064 (same as 100)
        assertEquals(0x20090064, result.machineCode.get(1));
        // addi $t2, $zero, 0xFF: imm = 0x00FF
        assertEquals(0x200A00FF, result.machineCode.get(2));
        // addi $t3, $zero, -1: imm = 0xFFFF
        assertEquals(0x200BFFFF, result.machineCode.get(3));
    }

    @Test
    @DisplayName("Test comments and whitespace handling")
    void testCommentsAndWhitespace() {
        String[] assembly = {
                "# This is a comment",
                "  add $t0, $s1, $s2   # Another comment",
                "",
                "sub $t1, $t2, $t3",
                "   # Only comment line",
                "and $v0, $a0, $a1  "
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should have no errors: " + result.errors);
        assertEquals(3, result.machineCode.size(), "Should assemble 3 instructions (ignoring comments/empty lines)");

        assertEquals(0x02324020, result.machineCode.get(0)); // add
        assertEquals(0x014B4822, result.machineCode.get(1)); // sub
        assertEquals(0x00851024, result.machineCode.get(2)); // and
    }

    @Test
    @DisplayName("Test case insensitivity for instructions")
    void testCaseInsensitivity() {
        String[] assembly = {
                "ADD $T0, $S1, $S2",
                "Addi $T1, $ZERO, 100",
                "lW $T2, 100($S3)"
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should have no errors: " + result.errors);
        assertEquals(3, result.machineCode.size(), "Should assemble 3 instructions");

        assertEquals(0x02324020, result.machineCode.get(0)); // ADD
        assertEquals(0x20090064, result.machineCode.get(1)); // Addi
        assertEquals(0x8E6A0064, result.machineCode.get(2)); // lW
    }

    @Test
    @DisplayName("Test error conditions - unknown instruction")
    void testUnknownInstruction() {
        String[] assembly = {
                "add $t0, $s1, $s2",
                "unknown $t1, $t2",
                "sub $t3, $t4, $t5"
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertFalse(result.errors.isEmpty(), "Should have errors");
        assertTrue(result.errors.get(0).contains("Unknown instruction"),
                "Error should mention unknown instruction");
        assertEquals(2, result.machineCode.size(), "Should assemble valid instructions");
    }

    @Test
    @DisplayName("Test error conditions - invalid register")
    void testInvalidRegister() {
        String[] assembly = {
                "add $t0, $s1, $s2",
                "add $t99, $t1, $t2",
                "addi $t3, $invalid, 100"
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertFalse(result.errors.isEmpty(), "Should have errors");
        assertEquals(2, result.errors.size(), "Should have 2 errors");
        assertTrue(result.errors.stream().anyMatch(e -> e.contains("Invalid register")),
                "Error should mention invalid register");
    }

    @Test
    @DisplayName("Test error conditions - malformed instruction")
    void testMalformedInstruction() {
        String[] assembly = {
                "add $t0, $s1",           // Missing operand
                "addi $t1, $s2",          // Missing immediate
                "lw $t2, $s3",            // Missing offset format
                "j"                       // Missing address
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertFalse(result.errors.isEmpty(), "Should have errors");
        assertEquals(4, result.errors.size(), "Should have 4 errors");
    }

    @Test
    @DisplayName("Test error conditions - invalid immediate")
    void testInvalidImmediate() {
        String[] assembly = {
                "addi $t0, $s1, not_a_number",
                "lw $t1, invalid($s2)",
                "addi $t2, $s3, 0xGGG"
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertFalse(result.errors.isEmpty(), "Should have errors");
        assertEquals(3, result.errors.size(), "Should have 3 errors");
        assertTrue(result.errors.stream().anyMatch(e -> e.contains("Invalid immediate")),
                "Error should mention invalid immediate");
    }

    @Test
    @DisplayName("Test undefined label treated as immediate")
    void testUndefinedLabel() {
        String[] assembly = {
                "beq $t0, $t1, undefined_label"
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);
        assertFalse(result.errors.isEmpty(), "Should have error for non-existent label treated as immediate");
        assertTrue(result.errors.get(0).contains("Invalid immediate"),
                "Error should indicate invalid immediate value");
    }

    @Test
    @DisplayName("Test empty input")
    void testEmptyInput() {
        String[] empty = {};
        Assembler.AssemblyResult result = Assembler.assemble(empty, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Empty array should have no errors");
        assertTrue(result.machineCode.isEmpty(), "Empty array should produce no code");
    }

    @Test
    @DisplayName("Test simple program with labels")
    void testSimpleProgram() {
        String[] assembly = {
                "# Simple loop program",
                "main:",
                "    addi $t0, $zero, 10",
                "    addi $t1, $zero, 0",
                "loop:",
                "    beq $t0, $zero, end",
                "    addi $t1, $t1, 1",
                "    addi $t0, $t0, -1",
                "    j loop",
                "end:",
                "    sw $t1, 0($sp)"
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);
        assertTrue(result.errors.isEmpty(), "Should have no errors: " + result.errors);
        assertEquals(7, result.machineCode.size(), "Should assemble 7 instructions");
    }

    @Test
    @DisplayName("Test instruction with parentheses variations")
    void testParenthesesVariations() {
        String[] assembly = {
                "lw $t0, 100($s1)",
                "lw $t1,100($s2)",
                "lw $t2, 100 ( $s3 )",
                "sw $t3, -50 ( $s4 )"
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should handle parentheses variations: " + result.errors);
        assertEquals(4, result.machineCode.size(), "Should assemble 4 instructions");
        assertEquals(0x8E280064, result.machineCode.get(0)); // lw $t0, 100($s1)
        assertEquals(0x8E490064, result.machineCode.get(1)); // lw $t1, 100($s2)
        assertEquals(0x8E6A0064, result.machineCode.get(2)); // lw $t2, 100($s3)
        assertEquals(0xAE8BFFCE, result.machineCode.get(3)); // sw $t3, -50($s4)
    }

    @Test
    @DisplayName("Test forward references")
    void testForwardReferences() {
        String[] assembly = {
                "j forward",
                "addi $t0, $zero, 1",
                "forward:",
                "addi $t1, $zero, 2"
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should handle forward references: " + result.errors);
        assertEquals(3, result.machineCode.size(), "Should assemble 3 instructions");
        int jInstruction = result.machineCode.get(0);
        assertEquals(0x100002, jInstruction & 0x3FFFFFF, "Should correctly resolve forward reference");
    }

    @Test
    @DisplayName("Test backward references")
    void testBackwardReferences() {
        String[] assembly = {
                "start:",
                "addi $t0, $zero, 5",
                "j start"
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should handle backward references: " + result.errors);
        assertEquals(2, result.machineCode.size(), "Should assemble 2 instructions");
        int jInstruction = result.machineCode.get(1);
        assertEquals(0x100000, jInstruction & 0x3FFFFFF, "Should correctly resolve backward reference");
    }

    @Test
    @DisplayName("Test large branch offset")
    void testLargeBranchOffset() {
        StringBuilder sb = new StringBuilder();
        sb.append("start:\n");
        sb.append("    beq $zero, $zero, far_label\n");

        for (int i = 0; i < 100; i++) {
            sb.append("    add $zero, $zero, $zero\n");
        }
        sb.append("far_label:\n");
        sb.append("    addi $t0, $zero, 1\n");

        String[] assembly = sb.toString().split("\n");
        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should handle large offsets: " + result.errors);
        int beqInstruction = result.machineCode.get(0);
        int offset = beqInstruction & 0xFFFF;
        assertEquals(100, offset, "Branch offset should be 100");
    }

    @Test
    @DisplayName("Test label on same line as instruction")
    void testLabelOnSameLine() {
        String[] assembly = {
                "start: addi $t0, $zero, 10",
                "j start"
        };

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        assertTrue(result.errors.isEmpty(), "Should handle label on same line: " + result.errors);
        assertEquals(2, result.machineCode.size(), "Should assemble 2 instructions");
        int jInstruction = result.machineCode.get(1);
        assertEquals(0x100000, jInstruction & 0x3FFFFFF, "Should resolve label correctly");
    }

    @Test
    @DisplayName("Debug I-type assembler")
    void debugITypeAssembler() {
        String[] assembly = {"addi $t0, $s1, 100"};

        Assembler.AssemblyResult result = Assembler.assemble(assembly, START_ADDRESS);

        System.out.println("Testing: addi $t0, $s1, 100");
        System.out.println("Errors: " + result.errors);

        if (result.machineCode.size() > 0) {
            int instr = result.machineCode.get(0);
            System.out.println("Generated: 0x" + Integer.toHexString(instr));

            int expected = (0x08 << 26) | (17 << 21) | (8 << 16) | 100;
            System.out.println("Expected: 0x" + Integer.toHexString(expected));

            int opcode = (instr >>> 26) & 0x3F;
            int rs = (instr >>> 21) & 0x1F;
            int rt = (instr >>> 16) & 0x1F;
            int imm = instr & 0xFFFF;

            System.out.println("Actual breakdown:");
            System.out.println("  opcode: 0x" + Integer.toHexString(opcode));
            System.out.println("  rs: " + rs + " ($" + rs + ")");
            System.out.println("  rt: " + rt + " ($" + rt + ")");
            System.out.println("  imm: 0x" + Integer.toHexString(imm) + " (" + imm + ")");

            assertEquals(expected, instr, "Machine code should match expected");
        }
    }
}