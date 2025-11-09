package simulator;

import model.cpu.CPUState;

public class StateSerializer {

	public static String serialize(CPUState state, PipelineController controller) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"pc\":").append(state.pc.get()).append(',');
		sb.append("\"registers\":").append(serializeRegisters(state)).append(',');
		sb.append("\"pipeline\":").append(serializePipeline(controller));
		sb.append("}");
		return sb.toString();
	}

	private static String serializeRegisters(CPUState state) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < 32; i++) {
			if (i > 0) sb.append(',');
			sb.append(state.registerFile.get(i));
		}
		sb.append(']');
		return sb.toString();
	}

	// TODO
	private static String serializePipeline(PipelineController controller) {
		return "{}";
	}
}
