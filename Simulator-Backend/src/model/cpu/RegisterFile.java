package model.cpu;

public class RegisterFile {
    private final int[] regs = new int[32];

    public int get(int index) { return regs[index]; }
    public void set(int index, int value) {
        if (index != 0) regs[index] = value; // $zero is always 0
    }
}
