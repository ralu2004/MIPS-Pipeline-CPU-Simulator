package tests;

import model.cpu.RegisterFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterFileTest {

    private RegisterFile rf;

    @BeforeEach
    void setUp() {
        rf = new RegisterFile();
    }

    @Test
    void testSetAndGetRegister() {
        rf.set(5, 123);
        assertEquals(123, rf.get(5), "Register 5 should store the value 123");
    }

    @Test
    void testZeroRegisterAlwaysZero() {
        rf.set(0, 999);
        assertEquals(0, rf.get(0), "$zero register should always return 0");
    }

    @Test
    void testOverwriteRegister() {
        rf.set(10, 55);
        rf.set(10, 99);
        assertEquals(99, rf.get(10), "Register 10 should reflect the latest value");
    }

    @Test
    void testAllRegistersInitiallyZero() {
        for (int i = 0; i < 32; i++) {
            assertEquals(0, rf.get(i), "All registers should start at 0");
        }
    }

    @Test
    void testSettingZeroRegisterDoesNotAffectOthers() {
        rf.set(1, 100);
        rf.set(0, 999);
        assertEquals(100, rf.get(1), "Other registers should be unaffected by $zero writes");
        assertEquals(0, rf.get(0), "$zero register must remain 0");
    }
}
