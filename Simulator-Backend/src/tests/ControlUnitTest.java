package tests;

import model.control.ControlUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControlUnitTest {

    private ControlUnit cu;

    @BeforeEach
    void setUp() {
        cu = new ControlUnit();
    }

    @Test
    void testRTypeSignals() {
        cu.generateSignals(0x00);
        assertTrue(cu.isRegWrite(), "R-type should write to register");
        assertTrue(cu.isRegDst(), "R-type should use rd as destination");
        assertFalse(cu.isAluSrc(), "R-type uses register as ALU source");
        assertFalse(cu.isMemToReg());
        assertFalse(cu.isBranch());
        assertFalse(cu.isMemRead());
        assertFalse(cu.isMemWrite());
        assertEquals(2, cu.getAluOp(), "R-type should use ALU op 2");
        assertFalse(cu.isJump());
    }

    @Test
    void testLoadWordSignals() {
        cu.generateSignals(0x23);
        assertTrue(cu.isRegWrite());
        assertFalse(cu.isRegDst());
        assertTrue(cu.isAluSrc());
        assertTrue(cu.isMemToReg());
        assertTrue(cu.isMemRead());
        assertFalse(cu.isMemWrite());
        assertEquals(0, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testStoreWordSignals() {
        cu.generateSignals(0x2B);
        assertFalse(cu.isRegWrite());
        assertFalse(cu.isRegDst());
        assertTrue(cu.isAluSrc());
        assertFalse(cu.isMemToReg());
        assertFalse(cu.isMemRead());
        assertTrue(cu.isMemWrite());
        assertEquals(0, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testBranchEqualSignals() {
        cu.generateSignals(0x04);
        assertFalse(cu.isRegWrite());
        assertFalse(cu.isRegDst());
        assertFalse(cu.isAluSrc());
        assertTrue(cu.isBranch());
        assertEquals(1, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testBranchNotEqualSignals() {
        cu.generateSignals(0x05);
        assertFalse(cu.isRegWrite());
        assertFalse(cu.isRegDst());
        assertFalse(cu.isAluSrc());
        assertTrue(cu.isBranch());
        assertEquals(1, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testAddiSignals() {
        cu.generateSignals(0x08);
        assertTrue(cu.isRegWrite());
        assertFalse(cu.isRegDst());
        assertTrue(cu.isAluSrc());
        assertFalse(cu.isMemToReg());
        assertFalse(cu.isBranch());
        assertEquals(0, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testOrImmediateSignals() {
        cu.generateSignals(0x0D);
        assertTrue(cu.isRegWrite());
        assertFalse(cu.isRegDst());
        assertTrue(cu.isAluSrc());
        assertFalse(cu.isMemToReg());
        assertFalse(cu.isBranch());
        assertEquals(3, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testAndImmediateSignals() {
        cu.generateSignals(0x0C);
        assertTrue(cu.isRegWrite());
        assertFalse(cu.isRegDst());
        assertTrue(cu.isAluSrc());
        assertFalse(cu.isMemToReg());
        assertFalse(cu.isBranch());
        assertEquals(4, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testSetLessThanImmediateSignals() {
        cu.generateSignals(0x0A);
        assertTrue(cu.isRegWrite());
        assertFalse(cu.isRegDst());
        assertTrue(cu.isAluSrc());
        assertFalse(cu.isMemToReg());
        assertFalse(cu.isBranch());
        assertEquals(5, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testJumpSignals() {
        cu.generateSignals(0x02);
        assertFalse(cu.isRegWrite());
        assertTrue(cu.isJump());
        assertFalse(cu.isBranch());
    }

    @Test
    void testJumpAndLinkSignals() {
        cu.generateSignals(0x03);
        assertTrue(cu.isRegWrite());
        assertTrue(cu.isJump());
        assertFalse(cu.isBranch());
    }

    @Test
    void testUnknownOpcodeResetsSignals() {
        cu.generateSignals(0x00);
        assertTrue(cu.isRegWrite());
        cu.generateSignals(0xFF);
        assertFalse(cu.isRegWrite());
        assertFalse(cu.isMemToReg());
        assertFalse(cu.isBranch());
        assertFalse(cu.isMemRead());
        assertFalse(cu.isMemWrite());
        assertFalse(cu.isRegDst());
        assertFalse(cu.isAluSrc());
        assertEquals(0, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testResetSignalsClearsAll() {
        cu.generateSignals(0x00);
        cu.resetSignals();
        assertFalse(cu.isRegWrite());
        assertFalse(cu.isMemToReg());
        assertFalse(cu.isBranch());
        assertFalse(cu.isMemRead());
        assertFalse(cu.isMemWrite());
        assertFalse(cu.isRegDst());
        assertFalse(cu.isAluSrc());
        assertEquals(0, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testImmediateOperationsHaveAluSrcTrue() {
        int[] immediateOps = {0x08, 0x0C, 0x0D, 0x0A, 0x23, 0x2B}; // addi, andi, ori, slti, lw, sw

        for (int opcode : immediateOps) {
            cu.generateSignals(opcode);
            assertTrue(cu.isAluSrc(), "Opcode 0x" + Integer.toHexString(opcode) + " should have aluSrc = true");
        }
    }

    @Test
    void testMemoryOperationsHaveProperAluOp() {
        cu.generateSignals(0x23); // lw
        assertEquals(0, cu.getAluOp(), "lw should use ALU op 0 (addition)");

        cu.generateSignals(0x2B); // sw
        assertEquals(0, cu.getAluOp(), "sw should use ALU op 0 (addition)");
    }

    @Test
    void testBranchInstructionsHaveAluOp1() {
        cu.generateSignals(0x04); // beq
        assertEquals(1, cu.getAluOp(), "beq should use ALU op 1 (subtraction)");

        cu.generateSignals(0x05); // bne
        assertEquals(1, cu.getAluOp(), "bne should use ALU op 1 (subtraction)");
    }
}