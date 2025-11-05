package model.pipeline.stages;

import model.control.ForwardingUnit;
import model.instruction.Instruction;
import model.instruction.RTypeInstruction;
import model.cpu.CPUState;
import model.pipeline.registers.PipelineRegisters;

/**
 * EX (Execute) Stage
 * Performs ALU operations, evaluates branches, selects destination register
 * Implements forwarding to resolve data hazards
 */
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
        
        int aluInputA = readData1;
        if (forwarding.forwardA == 2) {
           aluInputA = regs.EX_MEM.getAluResult();
        } else if (forwarding.forwardA == 1) {
            aluInputA = regs.MEM_WB.getWriteData();
        }
        
        int aluInputBValue = readData2;
        if (!regs.ID_EX.isAluSrc() && forwarding.forwardB == 2) {
            aluInputBValue = regs.EX_MEM.getAluResult();
        } else if (!regs.ID_EX.isAluSrc() && forwarding.forwardB == 1) {
            aluInputBValue = regs.MEM_WB.getWriteData();
        }
        
        int aluInputB = regs.ID_EX.isAluSrc() ? signExtendedImm : aluInputBValue;
      
        int aluResult = 0;
        boolean zeroFlag = false;
        int aluOp = regs.ID_EX.getAluOp();
        
        if (aluOp == 0) {
            // Addition (for addi, lw, sw)
            aluResult = aluInputA + aluInputB;
        } else if (aluOp == 1) {
            // Subtraction (for beq comparison)
            aluResult = aluInputA - aluInputB;
            zeroFlag = (aluResult == 0);
        } else if (aluOp == 2) {
            // R-type instruction - use function code
            if (instr instanceof RTypeInstruction) {
                RTypeInstruction rInstr = (RTypeInstruction) instr;
                int func = rInstr.getFunc();
                int rtValue = forwarding.forwardB == 2 ? regs.EX_MEM.getAluResult() :
                             forwarding.forwardB == 1 ? regs.MEM_WB.getWriteData() : readData2;
                
                switch (func) {
                    case 0x20: // add
                        aluResult = aluInputA + rtValue;
                        break;
                    case 0x22: // sub
                        aluResult = aluInputA - rtValue;
                        break;
                    case 0x24: // and
                        aluResult = aluInputA & rtValue;
                        break;
                    case 0x25: // or
                        aluResult = aluInputA | rtValue;
                        break;
                    case 0x2A: // slt (set on less than)
                        aluResult = (aluInputA < rtValue) ? 1 : 0;
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported R-type function: 0x" + 
                            Integer.toHexString(func));
                }
            }
        }
        
        int destReg = regs.ID_EX.isRegDst() ? regs.ID_EX.getRd() : regs.ID_EX.getRt();
        
        int branchTarget = pcPlus4 + (signExtendedImm << 2);
        
        // Evaluate branch condition (for beq: take branch if zero flag is true)
        // Branch Prediction: "Predict Not Taken"
        // - Decision is made here in EX stage
        // - Result propagates to MEM stage where PC is updated if taken
        // - PipelineController will flush IF_ID and ID_EX if branch taken
        boolean branchTaken = regs.ID_EX.isBranch() && zeroFlag;
        
        regs.EX_MEM.setAluResult(aluResult);
        regs.EX_MEM.setZeroFlag(zeroFlag);
        
        int writeDataForStore = readData2;
        if (forwarding.forwardB == 2) {
            writeDataForStore = regs.EX_MEM.getAluResult();
        } else if (forwarding.forwardB == 1) {
            writeDataForStore = regs.MEM_WB.getWriteData();
        }
        
        regs.EX_MEM.setWriteData(writeDataForStore); 
        regs.EX_MEM.setBranchTarget(branchTarget);
        regs.EX_MEM.setBranchTaken(branchTaken);
        regs.EX_MEM.setDestReg(destReg);
        
        regs.EX_MEM.setRegWrite(regs.ID_EX.isRegWrite());
        regs.EX_MEM.setMemToReg(regs.ID_EX.isMemToReg());
        regs.EX_MEM.setBranch(regs.ID_EX.isBranch());
        regs.EX_MEM.setMemRead(regs.ID_EX.isMemRead());
        regs.EX_MEM.setMemWrite(regs.ID_EX.isMemWrite());
        regs.EX_MEM.setInstruction(instr);
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
