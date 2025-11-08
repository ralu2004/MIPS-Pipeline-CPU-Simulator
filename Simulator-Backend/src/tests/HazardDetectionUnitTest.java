package tests;

import model.control.HazardDetectionUnit;
import model.control.HazardDetectionUnit.HazardReport;
import model.instruction.ITypeInstruction;
import model.instruction.RTypeInstruction;
import model.pipeline.registers.PipelineRegisters;
import model.pipeline.registers.IF_ID_Register;
import model.pipeline.registers.ID_EX_Register;
import model.pipeline.registers.EX_MEM_Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HazardDetectionUnitTest {

    private HazardDetectionUnit hdu;
    private PipelineRegisters regs;

    @BeforeEach
    void setUp() {
        hdu = new HazardDetectionUnit();
        regs = new PipelineRegisters();
    }

    @Test
    void testLoadUseHazardDetection() {
        // ID_EX -> load instruction writing to register 2
        ID_EX_Register idEx = regs.ID_EX;
        idEx.setInstruction(new ITypeInstruction(0x23, 0x8C220000)); // lw $2, 0($1)
        idEx.setMemRead(true);
        idEx.setRt(2);

        // IF_ID -> instruction using register 2
        IF_ID_Register ifId = regs.IF_ID;
        ifId.set(new RTypeInstruction(0x00, 0x00422020), 0); // add $4, $2, $2

        boolean detected = hdu.hasLoadUseHazard(regs);
        assertTrue(detected, "Load-use hazard should be detected");
    }

    @Test
    void testNoLoadUseHazard() {
        ID_EX_Register idEx = regs.ID_EX;
        idEx.setInstruction(new ITypeInstruction(0x23, 0x8C220000)); // lw $2, 0($1)
        idEx.setMemRead(true);
        idEx.setRt(2);

        IF_ID_Register ifId = regs.IF_ID;
        ifId.set(new RTypeInstruction(0x00, 0x00E83820), 0); // add $7, $7, $8

        boolean detected = hdu.hasLoadUseHazard(regs);
        assertFalse(detected, "No load-use hazard for unrelated registers");
    }

    @Test
    void testCheckAllHazards() {
        EX_MEM_Register exMem = regs.EX_MEM;
        exMem.setBranch(true);
        exMem.setBranchTaken(true);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasControlHazard(), "Control hazard should be detected for branch taken");
    }
}
