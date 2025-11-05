package model.control;

import model.pipeline.registers.PipelineRegisters;
import model.instruction.ITypeInstruction;
import model.instruction.RTypeInstruction;
import java.util.ArrayList;
import java.util.List;

/**
 * Hazard Detection Unit
 * Detects data hazards, control hazards, and structural hazards in the pipeline
 * Works in conjunction with ForwardingUnit and StallUnit to resolve hazards
 */
public class HazardDetectionUnit {
    
    private final List<String> detectedHazards = new ArrayList<>();
    
    /**
     * Check for all types of hazards in the pipeline
     * @param regs Pipeline registers to inspect
     * @return HazardReport containing detected hazards
     */
    public HazardReport checkAllHazards(PipelineRegisters regs) {
        detectedHazards.clear();
        
        checkDataHazards(regs);
        checkControlHazards(regs);
        checkStructuralHazards(regs);
        
        return new HazardReport(new ArrayList<>(detectedHazards));
    }
    
    /**
     * Check for data hazards (RAW - Read After Write)
     */
    private void checkDataHazards(PipelineRegisters regs) {
       
        if (regs.IF_ID.getInstruction() == null) {
            return; 
        }
        
        int idRs = 0, idRt = 0;
        regs.IF_ID.getInstruction().decodeFields();
     
        if (regs.IF_ID.getInstruction() instanceof RTypeInstruction) {
            RTypeInstruction rInstr = (RTypeInstruction) regs.IF_ID.getInstruction();
            idRs = rInstr.getRs();
            idRt = rInstr.getRt();
        } else if (regs.IF_ID.getInstruction() instanceof ITypeInstruction) {
            ITypeInstruction iInstr = (ITypeInstruction) regs.IF_ID.getInstruction();
            idRs = iInstr.getRs();
            idRt = iInstr.getRt();
        }
        
        // Check for load-use hazard: instruction in EX stage (ID_EX) is a load,
        // and instruction in ID stage (IF_ID) needs that register
        if (regs.ID_EX.getInstruction() != null && regs.ID_EX.isMemRead()) {
            int exRt = regs.ID_EX.getRt();
            
            // Check if ID stage instruction uses this register
            if (idRs == exRt || idRt == exRt) {
                // Load-use hazard: must stall (forwarding can't help - data not ready yet)
                detectedHazards.add("LOAD_USE_HAZARD: Instruction in ID needs data from load in EX");
            }
        }
        
        // Check if EX stage instruction writes to a register that ID stage needs
        if (regs.ID_EX.getInstruction() != null && regs.ID_EX.isRegWrite() && !regs.ID_EX.isMemRead()) {
            int exDestReg = regs.ID_EX.isRegDst() ? regs.ID_EX.getRd() : regs.ID_EX.getRt();
            
            // Check if ID stage instruction uses this register
            if (idRs == exDestReg || idRt == exDestReg) {
                // Data hazard that can be resolved by forwarding
                detectedHazards.add("DATA_HAZARD: Can be resolved by forwarding");
            }
        }
        
        // Check MEM/WB stage
        if (regs.EX_MEM.getInstruction() != null && regs.EX_MEM.isRegWrite()) {
            int memDestReg = regs.EX_MEM.getDestReg();
            if (idRs == memDestReg || idRt == memDestReg) {
                // Can be resolved by forwarding from MEM/WB
                detectedHazards.add("DATA_HAZARD: Can be resolved by forwarding from MEM/WB");
            }
        }
    }
    
    /**
     * Check for control hazards (branch/jump mispredictions)
     * Control hazards occur when a branch or jump instruction changes the PC,
     * causing incorrectly fetched instructions to be in the pipeline.
     */
    private void checkControlHazards(PipelineRegisters regs) {
        // Check if a branch is taken in EX stage
        if (regs.EX_MEM.isBranch() && regs.EX_MEM.isBranchTaken()) {
            detectedHazards.add("CONTROL_HAZARD: Branch taken - pipeline needs flushing");
        }
        
        // Check for jump instruction in ID stage (jumps are handled in ID, so we need to flush IF)
        // This is checked in DecodeStage, but we can detect it here too
        if (regs.IF_ID.getInstruction() != null) {
            int opcode = regs.IF_ID.getInstruction().getOpcode();
            if (opcode == 0x02 || opcode == 0x03) { // j or jal
                detectedHazards.add("CONTROL_HAZARD: Jump instruction - pipeline needs flushing");
            }
        }
    }
    
    /**
     * Check for structural hazards (resource conflicts)
     */
    private void checkStructuralHazards(PipelineRegisters regs) {
        
        if (regs.EX_MEM.isRegWrite() && regs.MEM_WB.isRegWrite()) {
            int exMemDest = regs.EX_MEM.getDestReg();
            int memWbDest = regs.MEM_WB.getDestReg();
            
            if (exMemDest != 0 && exMemDest == memWbDest) {
                detectedHazards.add("STRUCTURAL_HAZARD: Multiple writes to same register detected");
            }
        }
    }
    
    /**
     * Check specifically for load-use hazard (requires stalling)
     * Load-use hazard: instruction in EX stage is a load, and instruction in ID stage needs that data
     * @param regs Pipeline registers
     * @return true if load-use hazard detected
     */
    public boolean hasLoadUseHazard(PipelineRegisters regs) {
        // Load-use hazard: instruction in EX stage (ID_EX) is a load, and instruction in ID stage (IF_ID) needs that data
        if (regs.ID_EX.getInstruction() == null || !regs.ID_EX.isMemRead()) {
            return false;
        }
      
        if (regs.IF_ID.getInstruction() == null) {
            return false;
        }
        
        regs.IF_ID.getInstruction().decodeFields();
        int idRs = 0, idRt = 0;
        
        if (regs.IF_ID.getInstruction() instanceof RTypeInstruction) {
            RTypeInstruction rInstr = (RTypeInstruction) regs.IF_ID.getInstruction();
            idRs = rInstr.getRs();
            idRt = rInstr.getRt();
        } else if (regs.IF_ID.getInstruction() instanceof ITypeInstruction) {
            ITypeInstruction iInstr = (ITypeInstruction) regs.IF_ID.getInstruction();
            idRs = iInstr.getRs();
            idRt = iInstr.getRt();
        }
        
        int exRt = regs.ID_EX.getRt();
        return (idRs == exRt || idRt == exRt);
    }
    
    /**
     * Report of detected hazards
     */
    public static class HazardReport {
        private final List<String> hazards;
        
        public HazardReport(List<String> hazards) {
            this.hazards = hazards;
        }
        
        public boolean hasHazards() {
            return !hazards.isEmpty();
        }
        
        public List<String> getHazards() {
            return hazards;
        }
        
        public boolean hasLoadUseHazard() {
            return hazards.stream().anyMatch(h -> h.contains("LOAD_USE_HAZARD"));
        }
        
        public boolean hasControlHazard() {
            return hazards.stream().anyMatch(h -> h.contains("CONTROL_HAZARD"));
        }
    }
}
