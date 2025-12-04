package tests;

import model.control.HazardDetectionUnit;
import model.control.HazardDetectionUnit.HazardReport;
import model.instruction.ITypeInstruction;
import model.instruction.RTypeInstruction;
import model.pipeline.registers.PipelineRegisters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HazardDetectionUnitTest {

    private HazardDetectionUnit hdu;
    private PipelineRegisters regs;
    private int currentPC = 0x00000000;

    @BeforeEach
    void setUp() {
        hdu = new HazardDetectionUnit();
        regs = new PipelineRegisters();
    }

    @Test
    void testNoHazardsWhenPipelineEmpty() {
        HazardReport report = hdu.checkAllHazards(regs);
        assertFalse(report.hasHazards(), "Empty pipeline should have no hazards");
    }

    @Test
    void testLoadUseHazardDetection() {
        // Create a load in EX stage (lw $t0, 0($zero))
        ITypeInstruction lwInstr = new ITypeInstruction(0x23, 0x8C080000); // lw $t0, 0($zero)
        lwInstr.decodeFields(); // CRITICAL: Must decode fields!
        regs.ID_EX.setInstruction(lwInstr);
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRt(lwInstr.getRt()); // Now returns 8
        regs.ID_EX.setRegWrite(true);

        // Create instruction in ID that uses $t0 (add $t2, $t0, $t1)
        RTypeInstruction addInstr = new RTypeInstruction(0x00, 0x01095020); // add $t2, $t0, $t1
        addInstr.decodeFields(); // CRITICAL: Must decode fields!
        regs.IF_ID.set(addInstr, currentPC + 4);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasLoadUseHazard(), "Load-use hazard should be detected");
    }

    @Test
    void testDataHazardForwardableFromEX() {
        // Instruction in EX writes to $t0 (add $t0, $at, $v0)
        RTypeInstruction addInstr = new RTypeInstruction(0x00, 0x00224020); // add $t0, $at, $v0
        addInstr.decodeFields();
        regs.ID_EX.setInstruction(addInstr);
        regs.ID_EX.setRegWrite(true);
        regs.ID_EX.setRegDst(true);
        regs.ID_EX.setRd(addInstr.getRd()); // Should be 8

        // Instruction in ID reads $t0 (sub $a2, $t0, $a1)
        RTypeInstruction subInstr = new RTypeInstruction(0x00, 0x01053022); // sub $a2, $t0, $a1
        subInstr.decodeFields();
        regs.IF_ID.set(subInstr, currentPC + 4);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasHazards(), "Data hazard should be detected");
    }

    @Test
    void testDataHazardForwardableFromMEM() {
        // Instruction in MEM writes to $t1 (or $t1, $a3, $t0)
        RTypeInstruction orInstr = new RTypeInstruction(0x00, 0x00E84825); // or $t1, $a3, $t0
        orInstr.decodeFields();
        regs.EX_MEM.setInstruction(orInstr);
        regs.EX_MEM.setRegWrite(true);
        regs.EX_MEM.setDestReg(orInstr.getRd()); // Should be 9

        // Instruction in ID reads $t1 (addi $t2, $t1, 100)
        ITypeInstruction addiInstr = new ITypeInstruction(0x08, 0x212A0064); // addi $t2, $t1, 100
        addiInstr.decodeFields();
        regs.IF_ID.set(addiInstr, currentPC + 4);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasHazards(), "Data hazard from MEM should be detected");
    }

    @Test
    void testControlHazardBranchTaken() {
        // Branch taken in EX/MEM
        regs.EX_MEM.setBranch(true);
        regs.EX_MEM.setBranchTaken(true);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasControlHazard(), "Control hazard should be detected for branch taken");
    }

    @Test
    void testControlHazardJumpInstruction() {
        // Jump instruction in IF/ID (j 0x04000000)
        ITypeInstruction jInstr = new ITypeInstruction(0x02, 0x08000001); // j 0x04000000
        jInstr.decodeFields();
        regs.IF_ID.set(jInstr, currentPC);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasControlHazard(), "Control hazard should be detected for jump");
    }

    @Test
    void testStructuralHazardMultipleWrites() {
        // Two instructions writing to same register $t0
        regs.EX_MEM.setRegWrite(true);
        regs.EX_MEM.setDestReg(8); // $t0

        regs.MEM_WB.setRegWrite(true);
        regs.MEM_WB.setDestReg(8); // Also $t0

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasHazards(), "Structural hazard should be detected");
    }

    @Test
    void testHasLoadUseHazardMethod() {
        // Load in EX (lw $t0, 0($zero))
        ITypeInstruction lwInstr = new ITypeInstruction(0x23, 0x8C080000); // lw $t0, 0($zero)
        lwInstr.decodeFields();
        regs.ID_EX.setInstruction(lwInstr);
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRt(lwInstr.getRt()); // Should be 8

        // Instruction using $t0 in ID (and $t2, $t0, $t1)
        RTypeInstruction andInstr = new RTypeInstruction(0x00, 0x01095024); // and $t2, $t0, $t1
        andInstr.decodeFields();
        regs.IF_ID.set(andInstr, currentPC + 4);

        assertTrue(hdu.hasLoadUseHazard(regs), "hasLoadUseHazard should return true");
    }

    @Test
    void testNoHazardDifferentRegisters() {
        // Write to $t0 in EX (add $t0, $at, $v0)
        RTypeInstruction addInstr = new RTypeInstruction(0x00, 0x00224020); // add $t0, $at, $v0
        addInstr.decodeFields();
        regs.ID_EX.setInstruction(addInstr);
        regs.ID_EX.setRegWrite(true);
        regs.ID_EX.setRegDst(true);
        regs.ID_EX.setRd(addInstr.getRd()); // Should be 8

        // Read from $t1 in ID (different register) (sub $a2, $t1, $a1)
        RTypeInstruction subInstr = new RTypeInstruction(0x00, 0x01253022); // sub $a2, $t1, $a1
        subInstr.decodeFields();
        regs.IF_ID.set(subInstr, currentPC + 4);

        HazardReport report = hdu.checkAllHazards(regs);
        assertFalse(report.hasHazards(), "No hazard should be detected for different registers");
    }

    @Test
    void testZeroRegisterNoHazard() {
        // Writing to $zero in EX (add $zero, $at, $v0)
        RTypeInstruction instr = new RTypeInstruction(0x00, 0x00200020); // add $zero, $at, $v0
        instr.decodeFields();
        regs.ID_EX.setInstruction(instr);
        regs.ID_EX.setRegWrite(true);
        regs.ID_EX.setRegDst(true);
        regs.ID_EX.setRd(instr.getRd()); // This should be 0

        // Reading from $zero in ID (sub $a2, $zero, $a1)
        RTypeInstruction readInstr = new RTypeInstruction(0x00, 0x00053022); // sub $a2, $zero, $a1
        readInstr.decodeFields();
        regs.IF_ID.set(readInstr, currentPC + 4);

        HazardReport report = hdu.checkAllHazards(regs);
        assertFalse(report.hasHazards(), "No hazard should be detected for $zero register");
    }

    @Test
    void testLoadUseWithITypeInstruction() {
        // Load in EX (lw $t0, 4($s0))
        ITypeInstruction lwInstr = new ITypeInstruction(0x23, 0x8E080004); // lw $t0, 4($s0)
        lwInstr.decodeFields();
        regs.ID_EX.setInstruction(lwInstr);
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRt(lwInstr.getRt()); // Should be 8

        // I-type instruction in ID using $t0 (addi $t1, $t0, 10)
        ITypeInstruction addiInstr = new ITypeInstruction(0x08, 0x2109000A); // addi $t1, $t0, 10
        addiInstr.decodeFields();
        regs.IF_ID.set(addiInstr, currentPC + 4);

        assertTrue(hdu.hasLoadUseHazard(regs), "Load-use hazard with I-type should be detected");
    }

    // Debug test to check what the HDU is actually seeing
    @Test
    void debugHazardDetection() {
        // Simple test with known values
        ITypeInstruction lwInstr = new ITypeInstruction(0x23, 0x8C080000); // lw $t0, 0($zero)
        lwInstr.decodeFields();
        System.out.println("lwInstr rt: " + lwInstr.getRt()); // Should print 8

        RTypeInstruction addInstr = new RTypeInstruction(0x00, 0x01095020); // add $t2, $t0, $t1
        addInstr.decodeFields();
        System.out.println("addInstr rs: " + addInstr.getRs() + ", rt: " + addInstr.getRt()); // Should print rs=8

        regs.ID_EX.setInstruction(lwInstr);
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRt(lwInstr.getRt());

        regs.IF_ID.set(addInstr, currentPC + 4);

        HazardReport report = hdu.checkAllHazards(regs);
        System.out.println("Hazards detected: " + report.getHazards());
        assertTrue(report.hasLoadUseHazard());
    }
}