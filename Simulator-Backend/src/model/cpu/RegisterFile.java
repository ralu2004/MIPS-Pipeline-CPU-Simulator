package model.cpu;

public class RegisterFile {
    private final int[] regs = new int[32];

    public int get(int index) {
        if (index == 0) {
            return 0;
        }
        return regs[index];
    }

    public void set(int index, int value) {
        if (index != 0) {
            regs[index] = value;
        }
    }
}