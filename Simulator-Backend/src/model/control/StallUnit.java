package model.control;

import model.pipeline.registers.PipelineRegisters;

/**
 * Stall Unit
 * Determines when pipeline stalls are needed and generates stall control signals
 * Works with HazardDetectionUnit to identify hazards that require stalling
 * Specifically handles load-use hazards where forwarding cannot resolve the hazard
 */
public class StallUnit {
    
    private final HazardDetectionUnit hazardDetectionUnit = new HazardDetectionUnit();
    
    private boolean stall = false;
    private boolean pcWrite = true;   // Enable PC update (disable on stall)
    private boolean ifidWrite = true; // Enable IF/ID write (disable on stall)
    private boolean idExClear = false; // Clear ID/EX (insert bubble)
    
    /**
     * Detect if a stall is needed
     * Uses HazardDetectionUnit to identify load-use hazards
     * @param regs Pipeline registers
     * @return true if stall is needed
     */
    public boolean detectStall(PipelineRegisters regs) {
        stall = false;
        pcWrite = true;
        ifidWrite = true;
        idExClear = false;
        
        HazardDetectionUnit.HazardReport report = hazardDetectionUnit.checkAllHazards(regs);
        
        if (report.hasLoadUseHazard()) {
            //Stall the pipeline
            stall = true;
            pcWrite = false;      
            ifidWrite = false;    
            idExClear = true;    
        }
        
        return stall;
    }
    
    /**
     * Get control signals for pipeline stall
     */
    public StallControl getStallControl() {
        return new StallControl(stall, pcWrite, ifidWrite, idExClear);
    }
    
    /**
     * Stall control signals
     */
    public static class StallControl {
        public final boolean stall;
        public final boolean pcWrite;    
        public final boolean ifidWrite;    
        public final boolean idExClear;   
        
        public StallControl(boolean stall, boolean pcWrite, boolean ifidWrite, boolean idExClear) {
            this.stall = stall;
            this.pcWrite = pcWrite;
            this.ifidWrite = ifidWrite;
            this.idExClear = idExClear;
        }
    }
}
