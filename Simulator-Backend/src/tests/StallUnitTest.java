package tests;

import model.control.StallUnit;
import model.control.StallUnit.StallControl;
import model.instruction.ITypeInstruction;
import model.instruction.RTypeInstruction;
import model.pipeline.registers.PipelineRegisters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StallUnitTest {

    private StallUnit stallUnit;
    private PipelineRegisters regs;

    @BeforeEach
    void setUp() {
        stallUnit = new StallUnit();
        regs = new PipelineRegisters();
    }

    @Test
    void testDetectsLoadUseHazard() {
        ITypeInstruction lwInstr = new ITypeInstruction(0x23, 0x8C220000); // lw $2, 0($1)
        regs.ID_EX.setInstruction(lwInstr);
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRt(2);

        RTypeInstruction addInstr = new RTypeInstruction(0x00, 0x00422020); // add $4, $2, $2
        regs.IF_ID.set(addInstr, 0);

        boolean stallDetected = stallUnit.detectStall(regs);
        assertTrue(stallDetected, "Pipeline should stall for load-use hazard");

        StallControl control = stallUnit.getStallControl();
        assertFalse(control.pcWrite, "PC should not update on stall");
        assertFalse(control.ifidWrite, "IF/ID should not write on stall");
        assertTrue(control.idExClear, "ID/EX should be cleared (bubble inserted)");
    }

    @Test
    void testNoStallWhenNoHazard() {
        ITypeInstruction lwInstr = new ITypeInstruction(0x23, 0x8C220000); // lw $2, 0($1)
        regs.ID_EX.setInstruction(lwInstr);
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRt(2);

        RTypeInstruction addInstr = new RTypeInstruction(0x00, 0x00843020); // add $6, $4, $3
        regs.IF_ID.set(addInstr, 0);

        boolean stallDetected = stallUnit.detectStall(regs);
        assertFalse(stallDetected, "No stall expected for independent instructions");

        StallControl control = stallUnit.getStallControl();
        assertTrue(control.pcWrite);
        assertTrue(control.ifidWrite);
        assertFalse(control.idExClear);
    }

    @Test
    void testControlSignalsResetAfterNoHazard() {
        ITypeInstruction lwInstr = new ITypeInstruction(0x23, 0x8C220000);
        regs.ID_EX.setInstruction(lwInstr);
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRt(2);
        regs.IF_ID.set(new RTypeInstruction(0x00, 0x00422020), 0);
        stallUnit.detectStall(regs);

        regs.ID_EX.setMemRead(false);
        regs.IF_ID.set(null, 0);

        stallUnit.detectStall(regs);
        StallControl control = stallUnit.getStallControl();

        assertTrue(control.pcWrite, "PC write should be re-enabled");
        assertTrue(control.ifidWrite, "IF/ID write should be re-enabled");
        assertFalse(control.idExClear, "No bubble should be inserted");
    }
}
