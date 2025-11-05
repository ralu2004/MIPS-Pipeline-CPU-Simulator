package model.memory;

/**
 * Data Memory
 * Implements MIPS data memory with word-aligned access
 */
public class DataMemory {
    private final int[] memory = new int[1024]; // 4KB

    /**
     * Load a word from memory
     * @param address Word-aligned address
     * @return Word value at address, or 0 if out of bounds
     */
    public int loadWord(int address) {
        if (address < 0 || address >= memory.length * 4) {
            return 0; 
        }
        return memory[address / 4];
    }
    
    /**
     * Store a word to memory
     * @param address Word-aligned address 
     * @param value Word value to store
     */
    public void storeWord(int address, int value) {
        if (address >= 0 && address < memory.length * 4) {
            memory[address / 4] = value;
        }
    }
}
