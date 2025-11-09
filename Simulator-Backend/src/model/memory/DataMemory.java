package model.memory;

public class DataMemory {
	private final int[] memory = new int[1024]; // 4KB data memory (1024 words)

	public int loadWord(int address) {
		if (address < 0 || address >= memory.length * 4) {
			return 0;
		}
		return memory[address / 4];
	}

	public void storeWord(int address, int value) {
		if (address >= 0 && address < memory.length * 4) {
			memory[address / 4] = value;
		}
	}

	public int sizeBytes() {
		return memory.length * 4;
	}
}
