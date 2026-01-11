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

        boolean isShift = false;
        int shamt = 0;
        if (instr instanceof RTypeInstruction) {
            RTypeInstruction rInstr = (RTypeInstruction) instr;
            int func = rInstr.getFunc();
            isShift = (func == 0x00 || func == 0x02); // sll, srl
            if (isShift) {
                shamt = rInstr.getShamt();
            }
        }

        // ALU input A
        int aluInputA;
        if (isShift) {
            aluInputA = shamt;
        } else if (forwarding.forwardA == 2) {
            aluInputA = regs.EX_MEM.getAluResult();
        } else if (forwarding.forwardA == 1) {
            aluInputA = regs.MEM_WB.getWriteData();
        } else {
            aluInputA = readData1;
        }

        // ALU input B
        int aluInputB;
        if (regs.ID_EX.isAluSrc()) {
            aluInputB = signExtendedImm;
        } else if (forwarding.forwardB == 2) {
            aluInputB = regs.EX_MEM.getAluResult();
        } else if (forwarding.forwardB == 1) {
            aluInputB = regs.MEM_WB.getWriteData();
        } else {
            aluInputB = readData2;
        }

        // ALU operation
        int aluResult = 0;
        boolean zeroFlag = false;
        int aluOp = regs.ID_EX.getAluOp();

        if (aluOp == 0) {
            aluResult = aluInputA + aluInputB;
        } else if (aluOp == 1) {
            aluResult = aluInputA - aluInputB;
            zeroFlag = (aluResult == 0);
        } else if (aluOp == 2 && instr instanceof RTypeInstruction) {
            RTypeInstruction rInstr = (RTypeInstruction) instr;
            int func = rInstr.getFunc();
            switch (func) {
                case 0x00: aluResult = aluInputB << aluInputA; break; //sll
                case 0x02: aluResult = aluInputB >>> aluInputA; break; //srl
                case 0x20: aluResult = aluInputA + aluInputB; break; //add
                case 0x22: aluResult = aluInputA - aluInputB; break; //sub
                case 0x24: aluResult = aluInputA & aluInputB; break; //and
                case 0x25: aluResult = aluInputA | aluInputB; break; //or
                case 0x26: aluResult = aluInputA ^ aluInputB; break; //xor
                case 0x27: aluResult = ~(aluInputA | aluInputB); break; //nor
                case 0x2A: aluResult = (aluInputA < aluInputB) ? 1 : 0; break; //slt
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
        // branch
        int branchTarget = pcPlus4 + (signExtendedImm << 2);
        boolean branchTaken = false;
        if (regs.ID_EX.isBranch()) {
            switch(instr.getOpcode()) {
                case 0x04:
                    branchTaken = zeroFlag;
                    break;
                case 0x05:
                    branchTaken = !zeroFlag;
                    break;
                default:
                    branchTaken = false;
            }
        }

        if (branchTaken) {
            cpuState.setPC(branchTarget);
        }

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
        regs.EX_MEM.setForwardA(forwarding.forwardA);
        regs.EX_MEM.setForwardB(forwarding.forwardB);
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
        regs.EX_MEM.setForwardA(0);
        regs.EX_MEM.setForwardB(0);
    }
}
