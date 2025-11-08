package tests;

import model.memory.InstructionMemory;
import model.instruction.Instruction;
import model.instruction.RTypeInstruction;
import model.instruction.ITypeInstruction;
import model.instruction.JTypeInstruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstructionMemoryTest {

    private InstructionMemory memory;

    @BeforeEach
    void setUp() {
        memory = new InstructionMemory();
    }

    @Test
    void testSetAndFetchRTypeInstruction() {
        int address = 0;
        Instruction instr = new RTypeInstruction(0, 0x00A63820); // add $7, $5, $6
        memory.setInstruction(address, instr);

        Instruction fetched = memory.fetch(address);
        assertNotNull(fetched, "Fetched instruction should not be null");
        assertEquals(instr, fetched, "Fetched instruction should match the one set");
        assertEquals(0, fetched.getOpcode());
        assertEquals(0x00A63820, fetched.getBinary());
    }

    @Test
    void testSetAndFetchITypeInstruction() {
        int address = 4;
        Instruction instr = new ITypeInstruction(8, 0x20840004); // addi $4, $4, 4
        memory.setInstruction(address, instr);

        Instruction fetched = memory.fetch(address);
        assertNotNull(fetched);
        assertEquals(instr, fetched);
        assertEquals(8, fetched.getOpcode());
        assertEquals(0x20840004, fetched.getBinary());
    }

    @Test
    void testSetAndFetchJTypeInstruction() {
        int address = 8;
        Instruction instr = new JTypeInstruction(2, 0x0800000A); // j 0x0000000A
        memory.setInstruction(address, instr);

        Instruction fetched = memory.fetch(address);
        assertNotNull(fetched);
        assertEquals(instr, fetched);
        assertEquals(2, fetched.getOpcode());
        assertEquals(0x0800000A, fetched.getBinary());
    }

    @Test
    void testFetchInvalidAddressTooHigh() {
        int invalidAddress = 5000; // > 1024 * 4
        assertNull(memory.fetch(invalidAddress), "Should return null for out-of-range address");
    }

    @Test
    void testFetchInvalidAddressNegative() {
        assertNull(memory.fetch(-4), "Should return null for negative address");
    }

    @Test
    void testSetInstructionOutOfRangeDoesNotThrow() {
        Instruction instr = new RTypeInstruction(0, 0x00A63820);
        assertDoesNotThrow(() -> memory.setInstruction(99999, instr),
                "Setting instruction out of range should not throw");
    }

    @Test
    void testWordAlignment() {
        Instruction rInstr = new RTypeInstruction(0, 0x00A63820);
        Instruction iInstr = new ITypeInstruction(8, 0x20840004);

        memory.setInstruction(4, rInstr);
        memory.setInstruction(8, iInstr);

        assertEquals(rInstr, memory.fetch(4));
        assertEquals(iInstr, memory.fetch(8));
    }

    @Test
    void testConstructorWithInstructionArray() {
        Instruction[] arr = new Instruction[4];
        arr[0] = new RTypeInstruction(0, 0x00A63820);
        arr[1] = new ITypeInstruction(8, 0x20840004);
        arr[2] = new JTypeInstruction(2, 0x0800000A);

        InstructionMemory memory2 = new InstructionMemory(arr);

        assertEquals(arr[0], memory2.fetch(0));
        assertEquals(arr[1], memory2.fetch(4));
        assertEquals(arr[2], memory2.fetch(8));
    }
}
