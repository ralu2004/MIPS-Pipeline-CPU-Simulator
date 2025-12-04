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
        ITypeInstruction lwInstr = new ITypeInstruction(0x23, 0x8C080000); // lw $t0, 0($zero)
        lwInstr.decodeFields();
        regs.ID_EX.setInstruction(lwInstr);
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRt(lwInstr.getRt());
        regs.ID_EX.setRegWrite(true);

        RTypeInstruction addInstr = new RTypeInstruction(0x00, 0x01095020); // add $t2, $t0, $t1
        addInstr.decodeFields();
        regs.IF_ID.set(addInstr, currentPC);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasLoadUseHazard(), "Load-use hazard should be detected");
    }

    @Test
    void testDataHazardForwardableFromEX() {
        RTypeInstruction addInstr = new RTypeInstruction(0x00, 0x00224020); // add $t0, $at, $v0
        addInstr.decodeFields();
        regs.ID_EX.setInstruction(addInstr);
        regs.ID_EX.setRegWrite(true);
        regs.ID_EX.setRegDst(true);
        regs.ID_EX.setRd(addInstr.getRd());
        regs.ID_EX.setMemRead(false);

        RTypeInstruction subInstr = new RTypeInstruction(0x00, 0x01053022); // sub $a2, $t0, $a1
        subInstr.decodeFields();
        regs.IF_ID.set(subInstr, currentPC);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasHazards(), "Data hazard should be detected");
    }

    @Test
    void testDataHazardForwardableFromMEM() {
        RTypeInstruction orInstr = new RTypeInstruction(0x00, 0x00E84825); // or $t1, $a3, $t0
        orInstr.decodeFields();
        regs.EX_MEM.setInstruction(orInstr);
        regs.EX_MEM.setRegWrite(true);
        regs.EX_MEM.setDestReg(orInstr.getRd());

        ITypeInstruction addiInstr = new ITypeInstruction(0x08, 0x212A0064); // addi $t2, $t1, 100
        addiInstr.decodeFields();
        regs.IF_ID.set(addiInstr, currentPC);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasHazards(), "Data hazard from MEM should be detected");
    }

    @Test
    void testControlHazardBranchTaken() {
        regs.EX_MEM.setBranch(true);
        regs.EX_MEM.setBranchTaken(true);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasControlHazard(), "Control hazard should be detected for branch taken");
    }

    @Test
    void testControlHazardJumpInstruction() {
        ITypeInstruction jInstr = new ITypeInstruction(0x02, 0x08000001); // j 0x04000000
        jInstr.decodeFields();
        regs.IF_ID.set(jInstr, currentPC);

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasControlHazard(), "Control hazard should be detected for jump");
    }

    @Test
    void testStructuralHazardMultipleWrites() {
        regs.EX_MEM.setRegWrite(true);
        regs.EX_MEM.setDestReg(8); // $t0

        regs.MEM_WB.setRegWrite(true);
        regs.MEM_WB.setDestReg(8); // $t0

        HazardReport report = hdu.checkAllHazards(regs);
        assertTrue(report.hasHazards(), "Structural hazard should be detected");
    }

    @Test
    void testHasLoadUseHazardMethod() {
        ITypeInstruction lwInstr = new ITypeInstruction(0x23, 0x8C080000); // lw $t0, 0($zero)
        lwInstr.decodeFields();
        regs.ID_EX.setInstruction(lwInstr);
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRt(lwInstr.getRt());

        RTypeInstruction andInstr = new RTypeInstruction(0x00, 0x01095024); // and $t2, $t0, $t1
        andInstr.decodeFields();
        regs.IF_ID.set(andInstr, currentPC);

        assertTrue(hdu.hasLoadUseHazard(regs), "hasLoadUseHazard should return true");
    }

    @Test
    void testNoHazardDifferentRegisters() {
        RTypeInstruction addInstr = new RTypeInstruction(0x00, 0x00224020); // add $t0, $at, $v0
        addInstr.decodeFields();
        regs.ID_EX.setInstruction(addInstr);
        regs.ID_EX.setRegWrite(true);
        regs.ID_EX.setRegDst(true);
        regs.ID_EX.setRd(addInstr.getRd());

        RTypeInstruction subInstr = new RTypeInstruction(0x00, 0x01253022); // sub $a2, $t1, $a1
        subInstr.decodeFields();
        regs.IF_ID.set(subInstr, currentPC);

        HazardReport report = hdu.checkAllHazards(regs);
        assertFalse(report.hasHazards(), "No hazard should be detected for different registers");
    }

    @Test
    void testZeroRegisterNoHazard() {
        RTypeInstruction instr = new RTypeInstruction(0x00, 0x00200020); // add $zero, $at, $v0
        instr.decodeFields();
        regs.ID_EX.setInstruction(instr);
        regs.ID_EX.setRegWrite(true);
        regs.ID_EX.setRegDst(true);
        regs.ID_EX.setRd(instr.getRd()); // should be 0

        RTypeInstruction readInstr = new RTypeInstruction(0x00, 0x00053022); // sub $a2, $zero, $a1
        readInstr.decodeFields();
        regs.IF_ID.set(readInstr, currentPC);

        HazardReport report = hdu.checkAllHazards(regs);
        assertFalse(report.hasHazards(), "No hazard should be detected for $zero register");
    }

    @Test
    void testLoadUseWithITypeInstruction() {
        ITypeInstruction lwInstr = new ITypeInstruction(0x23, 0x8E080004); // lw $t0, 4($s0)
        lwInstr.decodeFields();
        regs.ID_EX.setInstruction(lwInstr);
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRt(lwInstr.getRt());

        ITypeInstruction addiInstr = new ITypeInstruction(0x08, 0x2109000A); // addi $t1, $t0, 10
        addiInstr.decodeFields();
        regs.IF_ID.set(addiInstr, currentPC);

        assertTrue(hdu.hasLoadUseHazard(regs), "Load-use hazard with I-type should be detected");
    }

    @Test
    void testZeroRegisterHazardIgnored() {
        regs.EX_MEM.setRegWrite(true);
        regs.EX_MEM.setDestReg(0); // $zero

        HazardReport report = hdu.checkAllHazards(regs);
        assertFalse(report.hasHazards(), "Hazard on $zero should be ignored");
    }

    @Test
    void testNoHazardWhenIDStageEmpty() {
        RTypeInstruction instr = new RTypeInstruction(0x00, 0x00224020); // add $t0, $at, $v0
        instr.decodeFields();
        regs.ID_EX.setInstruction(instr);
        regs.ID_EX.setRegWrite(true);
        regs.ID_EX.setRd(instr.getRd());
        regs.IF_ID.set(null, 0);

        HazardReport report = hdu.checkAllHazards(regs);
        assertFalse(report.hasHazards(), "No hazard when ID stage is empty");
    }
}