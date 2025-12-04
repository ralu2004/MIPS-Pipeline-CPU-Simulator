package tests;

import model.instruction.ITypeInstruction;
import model.instruction.JTypeInstruction;
import model.instruction.RTypeInstruction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InstructionTest {

    @Test
    void debugRTypeDecoding() {
        RTypeInstruction instr = new RTypeInstruction(0x00, 0x012A4020);
        instr.decodeFields();

        System.out.println("Testing add $t0, $t1, $t2 (0x012A4020):");
        System.out.println("  Opcode: " + instr.getOpcode());
        System.out.println("  Rs: " + instr.getRs());
        System.out.println("  Rt: " + instr.getRt());
        System.out.println("  Rd: " + instr.getRd());
        System.out.println("  Shamt: " + instr.getShamt());
        System.out.println("  Func: " + instr.getFunc());

        assertEquals(9, instr.getRs());
        assertEquals(10, instr.getRt());
        assertEquals(8, instr.getRd());
        assertEquals(0x20, instr.getFunc());
    }

    @Test
    void debugITypeDecoding() {
        ITypeInstruction instr = new ITypeInstruction(0x08, 0x21280064);
        instr.decodeFields();

        System.out.println("Testing addi $t0, $t1, 100 (0x21280064):");
        System.out.println("  Opcode: " + instr.getOpcode());
        System.out.println("  Rs: " + instr.getRs());
        System.out.println("  Rt: " + instr.getRt());
        System.out.println("  Immediate: " + instr.getImmediate());

        assertEquals(9, instr.getRs());
        assertEquals(8, instr.getRt());
        assertEquals(100, instr.getImmediate());
    }

    @Test
    void testSimpleRType() {
        RTypeInstruction add = new RTypeInstruction(0x00, 0x012A4020);
        add.decodeFields();
        assertEquals(9, add.getRs());
        assertEquals(10, add.getRt());
        assertEquals(8, add.getRd());
        assertEquals(0x20, add.getFunc());

        RTypeInstruction sub = new RTypeInstruction(0x00, 0x012A4022);
        sub.decodeFields();
        assertEquals(9, sub.getRs());
        assertEquals(10, sub.getRt());
        assertEquals(8, sub.getRd());
        assertEquals(0x22, sub.getFunc());

        RTypeInstruction and = new RTypeInstruction(0x00, 0x012A4024);
        and.decodeFields();
        assertEquals(9, and.getRs());
        assertEquals(10, and.getRt());
        assertEquals(8, and.getRd());
        assertEquals(0x24, and.getFunc());
    }

    @Test
    void testSimpleIType() {
        ITypeInstruction addi = new ITypeInstruction(0x08, 0x21280064);
        addi.decodeFields();
        assertEquals(9, addi.getRs());
        assertEquals(8, addi.getRt());
        assertEquals(100, addi.getImmediate());

        ITypeInstruction lw = new ITypeInstruction(0x23, 0x8C080064);
        lw.decodeFields();
        assertEquals(0, lw.getRs());
        assertEquals(8, lw.getRt());
        assertEquals(100, lw.getImmediate());

        ITypeInstruction sw = new ITypeInstruction(0x2B, 0xAC080064);
        sw.decodeFields();
        assertEquals(0, sw.getRs());
        assertEquals(8, sw.getRt());
        assertEquals(100, sw.getImmediate());
    }

    @Test
    void testShiftInstructions() {
        // sll $t0, $t1, 5
        // sll rd=8($t0), rt=9($t1), shamt=5
        // 000000 00000 01001 01000 00101 000000
        // 0000 0000 0000 1001 0100 0001 0100 0000
        // = 0x00094140
        RTypeInstruction sll = new RTypeInstruction(0x00, 0x00094140);
        sll.decodeFields();

        assertEquals(0, sll.getRs());
        assertEquals(9, sll.getRt());
        assertEquals(8, sll.getRd());
        assertEquals(5, sll.getShamt());
        assertEquals(0x00, sll.getFunc());

        // srl $t0, $t1, 5
        // srl rd=8($t0), rt=9($t1), shamt=5, func=2
        // 000000 00000 01001 01000 00101 000010
        // 0000 0000 0000 1001 0100 0001 0100 0010
        // = 0x00094142
        RTypeInstruction srl = new RTypeInstruction(0x00, 0x00094142);
        srl.decodeFields();

        assertEquals(0, srl.getRs());
        assertEquals(9, srl.getRt());
        assertEquals(8, srl.getRd());
        assertEquals(5, srl.getShamt());
        assertEquals(0x02, srl.getFunc());
    }

    @Test
    void testAllRTypeFields() {
        RTypeInstruction instr = new RTypeInstruction(0x00, 0x012A4020);
        instr.decodeFields();

        System.out.println("=== R-Type Field Debug ===");
        System.out.println("Binary: 0x" + Integer.toHexString(instr.getBinary()));
        System.out.println("Binary (32-bit): " + String.format("%32s",
                Integer.toBinaryString(instr.getBinary())).replace(' ', '0'));
        System.out.println("Fields from decoder:");
        System.out.println("  Opcode: " + instr.getOpcode());
        System.out.println("  Rs: " + instr.getRs());
        System.out.println("  Rt: " + instr.getRt());
        System.out.println("  Rd: " + instr.getRd());
        System.out.println("  Shamt: " + instr.getShamt());
        System.out.println("  Func: " + instr.getFunc());

        System.out.println("\n=== Shift Instruction Debug ===");
        RTypeInstruction sll = new RTypeInstruction(0x00, 0x00094280);
        sll.decodeFields();

        System.out.println("Binary: 0x" + Integer.toHexString(sll.getBinary()));
        System.out.println("Binary (32-bit): " + String.format("%32s",
                Integer.toBinaryString(sll.getBinary())).replace(' ', '0'));
        System.out.println("Fields from decoder:");
        System.out.println("  Opcode: " + sll.getOpcode());
        System.out.println("  Rs: " + sll.getRs());
        System.out.println("  Rt: " + sll.getRt());
        System.out.println("  Rd: " + sll.getRd());
        System.out.println("  Shamt: " + sll.getShamt());
        System.out.println("  Func: " + sll.getFunc());

        int binary = 0x00094280;
        int shamt = (binary >> 6) & 0x1F;
        System.out.println("Manual shamt calculation: (" + binary + " >> 6) & 0x1F = " + shamt);
    }

    @Test
    void testBranchInstructions() {
        ITypeInstruction beq = new ITypeInstruction(0x04, 0x11090010);
        beq.decodeFields();
        assertEquals(8, beq.getRs());
        assertEquals(9, beq.getRt());
        assertEquals(16, beq.getImmediate());

        ITypeInstruction bne = new ITypeInstruction(0x05, 0x1509FFFC);
        bne.decodeFields();
        assertEquals(8, bne.getRs());
        assertEquals(9, bne.getRt());
        assertEquals(-4, bne.getImmediate());
    }

    @Test
    void testImmediateLogical() {
        ITypeInstruction andi = new ITypeInstruction(0x0C, 0x312800FF);
        andi.decodeFields();
        assertEquals(9, andi.getRs());
        assertEquals(8, andi.getRt());
        assertEquals(0xFF, andi.getImmediate());

        ITypeInstruction ori = new ITypeInstruction(0x0D, 0x352800FF);
        ori.decodeFields();
        assertEquals(9, ori.getRs());
        assertEquals(8, ori.getRt());
        assertEquals(0xFF, ori.getImmediate());

        ITypeInstruction slti = new ITypeInstruction(0x0A, 0x29280064);
        slti.decodeFields();
        assertEquals(9, slti.getRs());
        assertEquals(8, slti.getRt());
        assertEquals(100, slti.getImmediate());
    }

    @Test
    void testJTypeInstructions() {
        JTypeInstruction j = new JTypeInstruction(0x02, 0x08100000);
        j.decodeFields();
        assertEquals(0x02, j.getOpcode());

        JTypeInstruction jal = new JTypeInstruction(0x03, 0x0C100000);
        jal.decodeFields();
        assertEquals(0x03, jal.getOpcode());
    }

    @Test
    void testSignExtension() {
        ITypeInstruction negative = new ITypeInstruction(0x08, 0x2108FFCE);
        negative.decodeFields();
        assertEquals(-50, negative.getImmediate());

        ITypeInstruction positive = new ITypeInstruction(0x08, 0x21080064);
        positive.decodeFields();
        assertEquals(100, positive.getImmediate());
    }

    @Test
    void testAllRTypeFunctions() {
        testRTypeInstruction(0x012A4020, 9, 10, 8, 0x20, "add");
        testRTypeInstruction(0x012A4022, 9, 10, 8, 0x22, "sub");
        testRTypeInstruction(0x012A4024, 9, 10, 8, 0x24, "and");
        testRTypeInstruction(0x012A4025, 9, 10, 8, 0x25, "or");
        testRTypeInstruction(0x012A4026, 9, 10, 8, 0x26, "xor");
        testRTypeInstruction(0x012A4027, 9, 10, 8, 0x27, "nor");
        testRTypeInstruction(0x012A402A, 9, 10, 8, 0x2A, "slt");
        testRTypeInstruction(0x00094280, 0, 9, 8, 0x00, "sll");
        testRTypeInstruction(0x00094282, 0, 9, 8, 0x02, "srl");
    }

    @Test
    void testAllITypeOpcodes() {
        testITypeInstruction(0x21280064, 8, 9, 8, 100, "addi");
        testITypeInstruction(0x312800FF, 0x0C, 9, 8, 0xFF, "andi");
        testITypeInstruction(0x352800FF, 0x0D, 9, 8, 0xFF, "ori");
        testITypeInstruction(0x29280064, 0x0A, 9, 8, 100, "slti");
        testITypeInstruction(0x8C080064, 0x23, 0, 8, 100, "lw");
        testITypeInstruction(0xAC080064, 0x2B, 0, 8, 100, "sw");
        testITypeInstruction(0x11090010, 0x04, 8, 9, 16, "beq");
        testITypeInstruction(0x15090010, 0x05, 8, 9, 16, "bne");
    }

    private void testRTypeInstruction(int binary, int expectedRs, int expectedRt,
                                      int expectedRd, int expectedFunc, String name) {
        RTypeInstruction instr = new RTypeInstruction(0x00, binary);
        instr.decodeFields();
        assertEquals(expectedRs, instr.getRs(), name + " rs");
        assertEquals(expectedRt, instr.getRt(), name + " rt");
        assertEquals(expectedRd, instr.getRd(), name + " rd");
        assertEquals(expectedFunc, instr.getFunc(), name + " func");
    }

    private void testITypeInstruction(int binary, int expectedOpcode, int expectedRs,
                                      int expectedRt, int expectedImm, String name) {
        ITypeInstruction instr = new ITypeInstruction(expectedOpcode, binary);
        instr.decodeFields();
        assertEquals(expectedRs, instr.getRs(), name + " rs");
        assertEquals(expectedRt, instr.getRt(), name + " rt");
        assertEquals(expectedImm, instr.getImmediate(), name + " immediate");
    }
}