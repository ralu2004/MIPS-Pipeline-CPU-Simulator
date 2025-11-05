package model.control;

import model.pipeline.registers.PipelineRegisters;

/**
 * Forwarding Unit
 * Implements forwarding (data hazard resolution) for MIPS pipeline
 * Detects when data from EX/MEM or MEM/WB stages should be forwarded to EX stage
 */
public class ForwardingUnit {
    
    // 00 = no forwarding (use register file)
    // 10 = forward from EX/MEM
    // 01 = forward from MEM/WB
    private int forwardA = 0;
  
    private int forwardB = 0;
    
    /**
     * Determine forwarding signals for current instruction in EX stage
     * @param regs Pipeline registers
     * @return ForwardingResult containing forwardA and forwardB signals
     */
    public ForwardingResult determineForwarding(PipelineRegisters regs) {
        forwardA = 0;
        forwardB = 0;
        
        int exRs = regs.ID_EX.getRs();
        int exRt = regs.ID_EX.getRt();
        
        if (regs.EX_MEM.isRegWrite() && regs.EX_MEM.getDestReg() != 0) {
            int exMemDestReg = regs.EX_MEM.getDestReg();
    
            if (exRs == exMemDestReg) {
                forwardA = 2; // Forward from EX/MEM
            }
            
            if (exRt == exMemDestReg) {
                forwardB = 2; // Forward from EX/MEM
            }
        }
        
        if (regs.MEM_WB.isRegWrite() && regs.MEM_WB.getDestReg() != 0) {
            int memWbDestReg = regs.MEM_WB.getDestReg();
            
            if (exRs == memWbDestReg && forwardA == 0) {
                forwardA = 1; // Forward from MEM/WB
            }
            
            if (exRt == memWbDestReg && forwardB == 0) {
                forwardB = 1; // Forward from MEM/WB
            }
        }
        
        return new ForwardingResult(forwardA, forwardB);
    }
    
    /**
     * Result of forwarding determination
     */
    public static class ForwardingResult {
        public final int forwardA; 
        public final int forwardB; 
        
        public ForwardingResult(int forwardA, int forwardB) {
            this.forwardA = forwardA;
            this.forwardB = forwardB;
        }
    }
}
