package model.memory;

/**
 * Data Memory
 * Implements MIPS data memory with word-aligned access
 */
public class DataMemory {
	private final int[] memory = new int[1024]; // 4KB data memory (1024 words)

	/**
	 * Load a word from memory
	 * @param address Word-aligned address (must be multiple of 4)
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
	 * @param address Word-aligned address (must be multiple of 4)
	 * @param value Word value to store
	 */
	public void storeWord(int address, int value) {
		if (address >= 0 && address < memory.length * 4) {
			memory[address / 4] = value;
		}
	}

	/**
	 * @return size of data memory in bytes
	 */
	public int sizeBytes() {
		return memory.length * 4;
	}
}
