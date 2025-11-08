package tests;

import model.memory.DataMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataMemoryTest {

    private DataMemory memory;

    @BeforeEach
    void setUp() {
        memory = new DataMemory();
    }

    @Test
    void testStoreAndLoadWordValidAddress() {
        int address = 0;
        int value = 12345;

        memory.storeWord(address, value);
        int loaded = memory.loadWord(address);

        assertEquals(value, loaded, "Loaded value should match the stored value");
    }

    @Test
    void testStoreAndLoadMultipleAddresses() {
        memory.storeWord(0, 10);
        memory.storeWord(4, 20);
        memory.storeWord(8, 30);

        assertEquals(10, memory.loadWord(0));
        assertEquals(20, memory.loadWord(4));
        assertEquals(30, memory.loadWord(8));
    }

    @Test
    void testLoadWordOutOfRangeReturnsZero() {
        assertEquals(0, memory.loadWord(-4), "Negative address should return 0");
        assertEquals(0, memory.loadWord(5000), "Out-of-range address should return 0");
    }

    @Test
    void testStoreWordOutOfRangeDoesNotThrow() {
        assertDoesNotThrow(() -> memory.storeWord(-4, 999),
                "Storing with negative address should not throw");
        assertDoesNotThrow(() -> memory.storeWord(99999, 999),
                "Storing with out-of-range address should not throw");
    }

    @Test
    void testWordAlignmentLogic() {
        memory.storeWord(8, 123);
        memory.storeWord(12, 456);

        assertEquals(123, memory.loadWord(8));
        assertEquals(456, memory.loadWord(12));
    }

    @Test
    void testSizeBytesReturnsExpectedValue() {
        assertEquals(4096, memory.sizeBytes(),
                "Data memory should be 4KB (1024 words * 4 bytes)");
    }

    @Test
    void testDefaultValuesAreZero() {
        assertEquals(0, memory.loadWord(0));
        assertEquals(0, memory.loadWord(100));
        assertEquals(0, memory.loadWord(4092));
    }
}
