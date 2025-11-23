package model.control;

import model.pipeline.registers.PipelineRegisters;

public class StallUnit {

    private final HazardDetectionUnit hazardDetectionUnit = new HazardDetectionUnit();

    private boolean stall = false;
    private boolean pcWrite = true;
    private boolean ifidWrite = true;
    private boolean idExClear = false;

    public boolean detectStall(PipelineRegisters regs) {
        stall = false;
        pcWrite = true;
        ifidWrite = true;
        idExClear = false;

        HazardDetectionUnit.HazardReport report = hazardDetectionUnit.checkAllHazards(regs);

        if (report.hasLoadUseHazard()) {
            stall = true;
            pcWrite = false;
            ifidWrite = false;
            idExClear = true;
        }
        return stall;
    }

    public StallControl getStallControl() {
        return new StallControl(stall, pcWrite, ifidWrite, idExClear);
    }

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