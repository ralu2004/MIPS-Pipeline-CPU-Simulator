package tests;

import model.control.ForwardingUnit;
import model.control.ForwardingUnit.ForwardingResult;
import model.pipeline.registers.PipelineRegisters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForwardingUnitTest {

    private ForwardingUnit fu;
    private PipelineRegisters regs;

    @BeforeEach
    void setUp() {
        fu = new ForwardingUnit();
        regs = new PipelineRegisters();
    }

    @Test
    void testForwardFromEXMEM() {
        // EX stage: instruction uses registers $2, $3
        regs.ID_EX.setRs(2);
        regs.ID_EX.setRt(3);

        // EX/MEM stage: instruction writes to $2
        regs.EX_MEM.setRegWrite(true);
        regs.EX_MEM.setDestReg(2);

        ForwardingResult result = fu.determineForwarding(regs);
        assertEquals(2, result.forwardA, "Should forward A from EX/MEM");
        assertEquals(0, result.forwardB, "No forwarding for B");
    }

    @Test
    void testForwardFromMEMWB() {
        // EX stage: instruction uses $5, $6
        regs.ID_EX.setRs(5);
        regs.ID_EX.setRt(6);

        // MEM/WB stage writes to $6
        regs.MEM_WB.setRegWrite(true);
        regs.MEM_WB.setDestReg(6);

        ForwardingResult result = fu.determineForwarding(regs);
        assertEquals(0, result.forwardA, "No forwarding for A");
        assertEquals(1, result.forwardB, "Should forward B from MEM/WB");
    }

    @Test
    void testNoForwardingNeeded() {
        regs.ID_EX.setRs(1);
        regs.ID_EX.setRt(2);
        // No RegWrite signals active in EX/MEM or MEM/WB

        ForwardingResult result = fu.determineForwarding(regs);
        assertEquals(0, result.forwardA);
        assertEquals(0, result.forwardB);
    }
}
