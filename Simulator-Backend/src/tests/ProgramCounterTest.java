package tests;

import model.cpu.ProgramCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgramCounterTest {

    private ProgramCounter pc;

    @BeforeEach
    void setUp() {
        pc = new ProgramCounter();
    }

    @Test
    void testInitialPCIsZero() {
        assertEquals(0, pc.get(), "Initial PC should be 0");
    }

    @Test
    void testSetAndGetPC() {
        pc.set(100);
        assertEquals(100, pc.get(), "PC should reflect the value set");
    }

    @Test
    void testIncrement() {
        pc.set(0);
        pc.increment();
        assertEquals(4, pc.get(), "PC should increment by 4");
        pc.increment();
        assertEquals(8, pc.get(), "PC should increment by 4 again");
    }

    @Test
    void testAddOffset() {
        pc.set(50);
        pc.addOffset(20);
        assertEquals(70, pc.get(), "PC should add the offset correctly");
        pc.addOffset(-10);
        assertEquals(60, pc.get(), "PC should handle negative offsets correctly");
    }

    @Test
    void testCombinedOperations() {
        pc.set(0);
        pc.increment();      // PC = 4
        pc.addOffset(12);    // PC = 16
        pc.increment();      // PC = 20
        assertEquals(20, pc.get(), "PC should reflect all operations combined");
    }
}
