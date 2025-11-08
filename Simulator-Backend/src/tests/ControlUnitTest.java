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
        cu.generateSignals(0x00); // R-type
        assertTrue(cu.isRegWrite(), "R-type should write to register");
        assertTrue(cu.isRegDst(), "R-type should use rd");
        assertFalse(cu.isAluSrc(), "R-type uses register as ALU source");
        assertFalse(cu.isMemToReg());
        assertFalse(cu.isBranch());
        assertFalse(cu.isMemRead());
        assertFalse(cu.isMemWrite());
        assertEquals(2, cu.getAluOp());
        assertFalse(cu.isJump());
    }

    @Test
    void testLoadWordSignals() {
        cu.generateSignals(0x23); // lw
        assertTrue(cu.isRegWrite());
        assertFalse(cu.isRegDst());
        assertTrue(cu.isAluSrc());
        assertTrue(cu.isMemToReg());
        assertTrue(cu.isMemRead());
        assertFalse(cu.isMemWrite());
        assertEquals(0, cu.getAluOp());
    }

    @Test
    void testStoreWordSignals() {
        cu.generateSignals(0x2B); // sw
        assertFalse(cu.isRegWrite());
        assertTrue(cu.isAluSrc());
        assertFalse(cu.isMemToReg());
        assertTrue(cu.isMemWrite());
    }

    @Test
    void testBranchSignals() {
        cu.generateSignals(0x04); // beq
        assertFalse(cu.isRegWrite());
        assertTrue(cu.isBranch());
        assertEquals(1, cu.getAluOp());
    }

    @Test
    void testAddiSignals() {
        cu.generateSignals(0x08); // addi
        assertTrue(cu.isRegWrite());
        assertTrue(cu.isAluSrc());
        assertFalse(cu.isMemToReg());
    }

    @Test
    void testJumpAndJalSignals() {
        cu.generateSignals(0x02); // j
        assertTrue(cu.isJump());
        assertFalse(cu.isRegWrite());

        cu.generateSignals(0x03); // jal
        assertTrue(cu.isJump());
        assertTrue(cu.isRegWrite()); // writes to $ra
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
}
