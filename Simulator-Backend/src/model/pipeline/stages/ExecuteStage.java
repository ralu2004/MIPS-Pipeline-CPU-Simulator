package model.pipeline.stages;

import model.control.ForwardingUnit;
import model.instruction.Instruction;
import model.instruction.RTypeInstruction;
import model.cpu.CPUState;
import model.pipeline.registers.PipelineRegisters;

public class ExecuteStage implements PipelineStage {

    private final ForwardingUnit forwardingUnit = new ForwardingUnit();

    @Override
    public void process(CPUState cpuState, PipelineRegisters regs) {
        Instruction instr = regs.ID_EX.getInstruction();
        if (instr == null) {
            clearEX_MEM(regs);
            return;
        }

        int readData1 = regs.ID_EX.getReadData1();
        int readData2 = regs.ID_EX.getReadData2();
        int signExtendedImm = regs.ID_EX.getSignExtendedImm();
        int pcPlus4 = regs.ID_EX.getPcPlus4();

        ForwardingUnit.ForwardingResult forwarding = forwardingUnit.determineForwarding(regs);

        System.out.println("Forwarding: forwardA=" + forwarding.forwardA + ", forwardB=" + forwarding.forwardB);
        System.out.println("MEM_WB.writeData=" + regs.MEM_WB.getWriteData());
        System.out.println("EX_MEM.aluResult=" + regs.EX_MEM.getAluResult());

        // ALU input A
        int aluInputA;
        if (forwarding.forwardA == 2) {
            aluInputA = regs.EX_MEM.getAluResult();
        } else if (forwarding.forwardA == 1) {
            aluInputA = regs.MEM_WB.getWriteData();
        } else {
            aluInputA = readData1;
        }

        // ALU input B
        int aluInputBValue;
        if (!regs.ID_EX.isAluSrc()) {
            if (forwarding.forwardB == 2) {
                aluInputBValue = regs.EX_MEM.getAluResult();
            } else if (forwarding.forwardB == 1) {
                aluInputBValue = regs.MEM_WB.getWriteData();
            } else {
                aluInputBValue = readData2;
            }
        } else {
            aluInputBValue = signExtendedImm;
        }
        int aluInputB = regs.ID_EX.isAluSrc() ? signExtendedImm : aluInputBValue;

        // ALU operation
        int aluResult = 0;
        boolean zeroFlag = false;
        int aluOp = regs.ID_EX.getAluOp();

        System.out.println("A= " + aluInputA + " B= " + aluInputB);

        if (aluOp == 0) {
            aluResult = aluInputA + aluInputB;
        } else if (aluOp == 1) {
            aluResult = aluInputA - aluInputB;
            zeroFlag = (aluResult == 0);
        } else if (aluOp == 2 && instr instanceof RTypeInstruction) {
            RTypeInstruction rInstr = (RTypeInstruction) instr;
            int func = rInstr.getFunc();
            switch (func) {
                case 0x20: aluResult = aluInputA + aluInputB; break;
                case 0x22: aluResult = aluInputA - aluInputB; break;
                case 0x24: aluResult = aluInputA & aluInputB; break;
                case 0x25: aluResult = aluInputA | aluInputB; break;
                case 0x2A: aluResult = (aluInputA < aluInputB) ? 1 : 0; break;
                default: throw new UnsupportedOperationException(
                        "Unsupported R-type function: 0x" + Integer.toHexString(func));
            }
        } else if (aluOp == 3) {
            aluResult = aluInputA | aluInputB;
        } else if (aluOp == 4) {
            aluResult = aluInputA & aluInputB;
        } else if (aluOp == 5) {
            aluResult = (aluInputA < aluInputB) ? 1 : 0;
        }

        int destReg = regs.ID_EX.isRegDst() ? regs.ID_EX.getRd() : regs.ID_EX.getRt();
        System.out.println("EX Stage: opcode=" + instr.getOpcode() + ", RegDst=" + regs.ID_EX.isRegDst() + ", destReg=" + destReg);

        // branch
        int branchTarget = pcPlus4 + (signExtendedImm << 2);
        boolean branchTaken = regs.ID_EX.isBranch() && zeroFlag;

        // store - write data
        int writeDataForStore;
        if (forwarding.forwardB == 2) {
            writeDataForStore = regs.EX_MEM.getAluResult();
        } else if (forwarding.forwardB == 1) {
            writeDataForStore = regs.MEM_WB.getWriteData();
        } else {
            writeDataForStore = readData2;
        }

        regs.EX_MEM.setAluResult(aluResult);
        regs.EX_MEM.setZeroFlag(zeroFlag);
        regs.EX_MEM.setWriteData(writeDataForStore);
        regs.EX_MEM.setBranchTarget(branchTarget);
        regs.EX_MEM.setBranchTaken(branchTaken);
        regs.EX_MEM.setDestReg(destReg);
        regs.EX_MEM.setRegWrite(regs.ID_EX.isRegWrite());
        regs.EX_MEM.setMemToReg(regs.ID_EX.isMemToReg());
        regs.EX_MEM.setBranch(regs.ID_EX.isBranch());
        regs.EX_MEM.setMemRead(regs.ID_EX.isMemRead());
        regs.EX_MEM.setMemWrite(regs.ID_EX.isMemWrite());
        regs.EX_MEM.setInstruction(instr.copy());
    }

    private void clearEX_MEM(PipelineRegisters regs) {
        regs.EX_MEM.setAluResult(0);
        regs.EX_MEM.setZeroFlag(false);
        regs.EX_MEM.setWriteData(0);
        regs.EX_MEM.setBranchTarget(0);
        regs.EX_MEM.setBranchTaken(false);
        regs.EX_MEM.setDestReg(0);
        regs.EX_MEM.setRegWrite(false);
        regs.EX_MEM.setMemToReg(false);
        regs.EX_MEM.setBranch(false);
        regs.EX_MEM.setMemRead(false);
        regs.EX_MEM.setMemWrite(false);
        regs.EX_MEM.setInstruction(null);
    }
}
