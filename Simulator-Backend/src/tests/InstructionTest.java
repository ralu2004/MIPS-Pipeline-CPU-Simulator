package tests;
import model.instruction.ITypeInstruction;
import model.instruction.JTypeInstruction;
import model.instruction.RTypeInstruction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class InstructionTest {

    private RTypeInstruction addInstruction;
    private ITypeInstruction addiInstruction;
    private JTypeInstruction jInstruction;

    @BeforeEach
    void setUp() {
        addInstruction = new RTypeInstruction(0x00, 0x012A4020);    // add $t0, $t1, $t2
        addInstruction.decodeFields();
        addiInstruction = new ITypeInstruction(0x08, 0x21280064);    // addi $t0, $t1, 100
        addiInstruction.decodeFields();
        jInstruction = new JTypeInstruction(0x02, 0x08100000);       // j 0x00400000
        jInstruction.decodeFields();
    }

    @AfterEach
    void tearDown() {
        addInstruction = null;
        addiInstruction = null;
        jInstruction = null;
    }

    @Test
    void decodeFields() {
        // R-type: add $t0, $t1, $t2
        assertEquals(0, addInstruction.getOpcode(), "R-type opcode should be 0");
        assertEquals(9, addInstruction.getRs(), "rs should be $t1 (register 9)");
        assertEquals(10, addInstruction.getRt(), "rt should be $t2 (register 10)");
        assertEquals(8, addInstruction.getRd(), "rd should be $t0 (register 8)");
        assertEquals(0, addInstruction.getShamt(), "shamt should be 0 for add");
        assertEquals(32, addInstruction.getFunc(), "funct should be 32 for add");

        // I-type: addi $t0, $t1, 100
        assertEquals(8, addiInstruction.getOpcode(), "addi opcode should be 8");
        assertEquals(9, addiInstruction.getRs(), "rs should be $t1 (register 9)");
        assertEquals(8, addiInstruction.getRt(), "rt should be $t0 (register 8)");
        assertEquals(100, addiInstruction.getImmediate(), "immediate should be 100");

        // J-type: j 0x00400000
        assertEquals(2, jInstruction.getOpcode(), "j opcode should be 2");
        assertEquals(0x00400000 >> 2, jInstruction.getAddress(), "address field should match target address >> 2");
    }

    @Test
    void getOpcode() {
        assertEquals(0, addInstruction.getOpcode());
        assertEquals(8, addiInstruction.getOpcode());
        assertEquals(2, jInstruction.getOpcode());
    }

    @Test
    void getBinary() {
        assertEquals(0x012A4020, addInstruction.getBinary());
        assertEquals(0x21280064, addiInstruction.getBinary());
        assertEquals(0x08100000, jInstruction.getBinary());
    }
}