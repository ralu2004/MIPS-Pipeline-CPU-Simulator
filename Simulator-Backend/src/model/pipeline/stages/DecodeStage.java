package model.pipeline.stages;

import model.control.ControlUnit;
import model.instruction.Instruction;
import model.instruction.ITypeInstruction;
import model.instruction.JTypeInstruction;
import model.instruction.RTypeInstruction;
import model.cpu.CPUState;
import model.pipeline.registers.PipelineRegisters;

public class DecodeStage implements PipelineStage {
    
    private final ControlUnit controlUnit = new ControlUnit();

    @Override
    public void process(CPUState cpuState, PipelineRegisters regs) {
        Instruction instr = regs.IF_ID.getInstruction();
        if (instr == null) {
            clearID_EX(regs);
            return;
        }
        
        instr.decodeFields();
        
        int rs = 0, rt = 0, rd = 0;
        int signExtendedImm = 0;
        
        if (instr instanceof RTypeInstruction) {
            RTypeInstruction rInstr = (RTypeInstruction) instr;
            rs = rInstr.getRs();
            rt = rInstr.getRt();
            rd = rInstr.getRd();
        } else if (instr instanceof ITypeInstruction) {
            ITypeInstruction iInstr = (ITypeInstruction) instr;
            rs = iInstr.getRs();
            rt = iInstr.getRt();
            signExtendedImm = iInstr.getImmediate();
        }
    
        int readData1 = cpuState.registerFile.get(rs);
        int readData2 = cpuState.registerFile.get(rt);
        
        controlUnit.generateSignals(instr.getOpcode());
        
        if (controlUnit.isJump() && instr instanceof JTypeInstruction) {
            JTypeInstruction jInstr = (JTypeInstruction) instr;
            int pcUpper = cpuState.pc.get() & 0xF0000000; 
            int targetAddress = (jInstr.getAddress() << 2) | pcUpper;
            
            if (instr.getOpcode() == 0x02) { // j
                cpuState.pc.set(targetAddress);
                regs.IF_ID.set(null, 0);
            } else if (instr.getOpcode() == 0x03) { // jal
                cpuState.registerFile.set(31, regs.IF_ID.getPC()); 
                cpuState.pc.set(targetAddress);
                regs.IF_ID.set(null, 0);
            }
        }
        
        regs.ID_EX.setReadData1(readData1);
        regs.ID_EX.setReadData2(readData2);
        regs.ID_EX.setSignExtendedImm(signExtendedImm);
        regs.ID_EX.setPcPlus4(regs.IF_ID.getPC());
        regs.ID_EX.setRs(rs);
        regs.ID_EX.setRt(rt);
        regs.ID_EX.setRd(rd);
        
        regs.ID_EX.setRegWrite(controlUnit.isRegWrite());
        regs.ID_EX.setMemToReg(controlUnit.isMemToReg());
        regs.ID_EX.setBranch(controlUnit.isBranch());
        regs.ID_EX.setMemRead(controlUnit.isMemRead());
        regs.ID_EX.setMemWrite(controlUnit.isMemWrite());
        regs.ID_EX.setRegDst(controlUnit.isRegDst());
        regs.ID_EX.setAluSrc(controlUnit.isAluSrc());
        regs.ID_EX.setAluOp(controlUnit.getAluOp());
        regs.ID_EX.setInstruction(instr);
    }
    
    private void clearID_EX(PipelineRegisters regs) {
        regs.ID_EX.setReadData1(0);
        regs.ID_EX.setReadData2(0);
        regs.ID_EX.setSignExtendedImm(0);
        regs.ID_EX.setPcPlus4(0);
        regs.ID_EX.setRs(0);
        regs.ID_EX.setRt(0);
        regs.ID_EX.setRd(0);
        regs.ID_EX.setRegWrite(false);
        regs.ID_EX.setMemToReg(false);
        regs.ID_EX.setBranch(false);
        regs.ID_EX.setMemRead(false);
        regs.ID_EX.setMemWrite(false);
        regs.ID_EX.setRegDst(false);
        regs.ID_EX.setAluSrc(false);
        regs.ID_EX.setAluOp(0);
        regs.ID_EX.setInstruction(null);
    }
}
