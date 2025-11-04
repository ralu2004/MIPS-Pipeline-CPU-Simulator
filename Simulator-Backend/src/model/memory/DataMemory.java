package model.memory;

public class DataMemory {
    private final int[] memory = new int[1024];

    public int loadWord(int address) { return memory[address / 4]; }
    public void storeWord(int address, int value) { memory[address / 4] = value; }
}
