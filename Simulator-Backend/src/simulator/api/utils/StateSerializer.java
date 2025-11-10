package simulator.api.utils;

import model.cpu.CPUState;
import model.instruction.Instruction;
import model.pipeline.registers.*;
import simulator.PipelineController;

public class StateSerializer {

	public static String serialize(CPUState state, PipelineController controller) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"pc\":").append(state.pc.get()).append(',');
		sb.append("\"registers\":").append(serializeRegisters(state)).append(',');
		sb.append("\"pipeline\":").append(serializePipeline(controller)).append(',');
		sb.append("\"dataMemory\":").append(serializeDataMemory(state));
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

	private static String serializePipeline(PipelineController controller) {
		PipelineRegisters regs = controller.getPipelineRegisters();
		StringBuilder sb = new StringBuilder();
		sb.append('{');

		// IF/ID
		sb.append("\"IF_ID\":{");
		sb.append("\"pc\":").append(regs.IF_ID.getPC());
		sb.append(",\"instruction\":").append(instrToJson(regs.IF_ID.getInstruction()));
		sb.append('}');
		sb.append(',');

		// ID/EX
		sb.append("\"ID_EX\":{");
		sb.append("\"instruction\":").append(instrToJson(regs.ID_EX.getInstruction()));
		sb.append(",\"rs\":").append(regs.ID_EX.getRs());
		sb.append(",\"rt\":").append(regs.ID_EX.getRt());
		sb.append(",\"rd\":").append(regs.ID_EX.getRd());
		sb.append(",\"readData1\":").append(regs.ID_EX.getReadData1());
		sb.append(",\"readData2\":").append(regs.ID_EX.getReadData2());
		sb.append(",\"signExtImm\":").append(regs.ID_EX.getSignExtendedImm());
		sb.append(",\"regWrite\":").append(regs.ID_EX.isRegWrite());
		sb.append('}');
		sb.append(',');

		// EX/MEM
		sb.append("\"EX_MEM\":{");
		sb.append("\"instruction\":").append(instrToJson(regs.EX_MEM.getInstruction()));
		sb.append(",\"aluResult\":").append(regs.EX_MEM.getAluResult());
		sb.append(",\"writeData\":").append(regs.EX_MEM.getWriteData());
		sb.append(",\"destReg\":").append(regs.EX_MEM.getDestReg());
		sb.append(",\"zero\":").append(regs.EX_MEM.isZeroFlag());
		sb.append(",\"regWrite\":").append(regs.EX_MEM.isRegWrite());
		sb.append(",\"memRead\":").append(regs.EX_MEM.isMemRead());
		sb.append(",\"memWrite\":").append(regs.EX_MEM.isMemWrite());
		sb.append(",\"branch\":").append(regs.EX_MEM.isBranch());
		sb.append(",\"branchTaken\":").append(regs.EX_MEM.isBranchTaken());
		sb.append('}');
		sb.append(',');

		// MEM/WB
		sb.append("\"MEM_WB\":{");
		sb.append("\"instruction\":").append(instrToJson(regs.MEM_WB.getInstruction()));
		sb.append(",\"aluResult\":").append(regs.MEM_WB.getAluResult());
		sb.append(",\"memData\":").append(regs.MEM_WB.getMemData());
		sb.append(",\"writeData\":").append(regs.MEM_WB.getWriteData());
		sb.append(",\"destReg\":").append(regs.MEM_WB.getDestReg());
		sb.append(",\"regWrite\":").append(regs.MEM_WB.isRegWrite());
		sb.append(",\"memToReg\":").append(regs.MEM_WB.isMemToReg());
		sb.append('}');

		sb.append('}');
		return sb.toString();
	}

	private static String instrToJson(Instruction instr) {
		if (instr == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append("\"hex\":\"0x").append(String.format("%08X", instr.getBinary())).append('"');
		sb.append(",\"opcode\":").append(instr.getOpcode());
		sb.append('}');
		return sb.toString();
	}

	private static String serializeDataMemory(CPUState state) {
		// serialize first 1KB
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		boolean first = true;
		for (int addr = 0; addr < 1024; addr += 4) {
			int value = state.dataMemory.loadWord(addr);
			if (value != 0) {
				if (!first) sb.append(',');
				sb.append('"').append(addr).append("\":").append(value);
				first = false;
			}
		}
		sb.append('}');
		return sb.toString();
	}
}