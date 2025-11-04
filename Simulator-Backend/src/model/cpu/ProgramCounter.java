package model.cpu;

public class ProgramCounter {
    private int pc = 0;

    public int get() { return pc; }
    public void set(int value) { pc = value; }
    public void increment() { pc += 4; }
    public void addOffset(int offset) { pc += offset; }
}

