package simulator;

import model.cpu.CPUState;
import model.instruction.Instruction;
import model.instruction.ITypeInstruction;
import model.instruction.JTypeInstruction;
import model.instruction.RTypeInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProgramLoader {

	public static final int INSTRUCTION_MEMORY_WORDS = 1024;
	public static final int INSTRUCTION_MEMORY_BYTES = INSTRUCTION_MEMORY_WORDS * 4;

	public static ProgramLoadResult loadFromAssembly(CPUState state, String[] assemblyLines, int startAddress) {
		validateStartAddress(startAddress);

		if (assemblyLines == null || assemblyLines.length == 0)
			throw new IllegalArgumentException("No assembly instructions to load");

		Assembler.AssemblyResult asmResult = Assembler.assemble(assemblyLines, startAddress);

		if (!asmResult.errors.isEmpty()) {
			throw new IllegalArgumentException("Assembly errors: " + String.join("; ", asmResult.errors));
		}

		int[] words = asmResult.machineCode.stream().mapToInt(Integer::intValue).toArray();

		return loadFromIntArray(state, words, startAddress);
	}

	public static ProgramLoadResult loadFromHexStrings(CPUState state, String[] hexLines, int startAddress) {
		validateStartAddress(startAddress);

		if (hexLines == null || hexLines.length == 0)
			throw new IllegalArgumentException("No hex instructions to load");

		String[] cleanLines = Arrays.stream(hexLines)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toArray(String[]::new);

		if (cleanLines.length == 0)
			throw new IllegalArgumentException("No valid hex instructions after filtering");

		int[] words = new int[cleanLines.length];
		for (int i = 0; i < cleanLines.length; i++) {
			words[i] = parseHexToInt(cleanLines[i]);
		}

		return loadFromIntArray(state, words, startAddress);
	}

	public static ProgramLoadResult loadFromIntArray(CPUState state, int[] words, int startAddress) {
		validateStartAddress(startAddress);

		if (words == null || words.length == 0)
			throw new IllegalArgumentException("No instructions to load");

		clearInstructionMemory(state);

		List<String> warnings = new ArrayList<>();
		int loaded = 0;

		for (int i = 0; i < words.length; i++) {
			int address = startAddress + (i * 4);

			if (address < 0 || address >= INSTRUCTION_MEMORY_BYTES) {
				warnings.add("Skipping out-of-bounds instruction at address " + address);
				continue;
			}

			Instruction instr = parseInstruction(words[i]);
			state.instructionMemory.setInstruction(address, instr);
			loaded++;
		}

		state.pc.set(startAddress);

		int endAddress = startAddress + ((loaded > 0) ? (loaded - 1) * 4 : 0);
		return new ProgramLoadResult(loaded, startAddress, endAddress, warnings);
	}

	public static void resetState(CPUState state, boolean clearRegisters, boolean clearDataMem, int pcStart) {
		if (clearRegisters) {
			for (int i = 0; i < 32; i++) {
				state.registerFile.set(i, 0);
			}
		}

		if (clearDataMem) {
			for (int addr = 0; addr < state.dataMemory.sizeBytes(); addr += 4) {
				state.dataMemory.storeWord(addr, 0);
			}
		}

		clearInstructionMemory(state);
		state.pc.set(pcStart);
	}

	private static void clearInstructionMemory(CPUState state) {
		for (int addr = 0; addr < INSTRUCTION_MEMORY_BYTES; addr += 4) {
			state.instructionMemory.setInstruction(addr, null);
		}
	}

	private static void validateStartAddress(int startAddress) {
		if ((startAddress & 0x3) != 0)
			throw new IllegalArgumentException("Start address must be word-aligned (multiple of 4)");

		if (startAddress < 0 || startAddress >= INSTRUCTION_MEMORY_BYTES)
			throw new IllegalArgumentException("Start address out of instruction memory bounds");
	}

	private static int parseHexToInt(String hex) {
		if (hex == null)
			throw new IllegalArgumentException("Null hex string");

		String s = hex.trim();
		if (s.startsWith("0x") || s.startsWith("0X"))
			s = s.substring(2);

		if (s.isEmpty())
			throw new IllegalArgumentException("Empty hex string");

		return (int) Long.parseLong(s, 16);
	}

	private static Instruction parseInstruction(int binaryWord) {
		int opcode = (binaryWord >>> 26) & 0x3F;

		if (opcode == 0x00)
			return new RTypeInstruction(opcode, binaryWord);
		else if (opcode == 0x02 || opcode == 0x03)
			return new JTypeInstruction(opcode, binaryWord);
		else
			return new ITypeInstruction(opcode, binaryWord);
	}

	public static class ProgramLoadResult {
		public final int loadedCount;
		public final int startAddress;
		public final int endAddress;
		public final List<String> warnings;

		public ProgramLoadResult(int loadedCount, int startAddress, int endAddress, List<String> warnings) {
			this.loadedCount = loadedCount;
			this.startAddress = startAddress;
			this.endAddress = endAddress;
			this.warnings = warnings;
		}
	}
}
