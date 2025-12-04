package model.control;

import model.pipeline.registers.PipelineRegisters;
import model.instruction.ITypeInstruction;
import model.instruction.RTypeInstruction;
import java.util.ArrayList;
import java.util.List;

public class HazardDetectionUnit {

    private final List<String> detectedHazards = new ArrayList<>();

    public HazardReport checkAllHazards(PipelineRegisters regs) {
        detectedHazards.clear();

        checkDataHazards(regs);
        checkControlHazards(regs);
        checkStructuralHazards(regs);

        return new HazardReport(new ArrayList<>(detectedHazards));
    }

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

        if (regs.ID_EX.getInstruction() != null && regs.ID_EX.isMemRead()) {
            int exRt = regs.ID_EX.getRt();

            if (idRs == exRt || idRt == exRt) {
                detectedHazards.add("LOAD_USE_HAZARD: Instruction in ID needs data from load in EX");
            }
        }

        if (regs.ID_EX.getInstruction() != null && regs.ID_EX.isRegWrite() && !regs.ID_EX.isMemRead()) {
            int exDestReg = regs.ID_EX.isRegDst() ? regs.ID_EX.getRd() : regs.ID_EX.getRt();

            if (exDestReg != 0 && (idRs == exDestReg || idRt == exDestReg)) {
                detectedHazards.add("DATA_HAZARD: Can be resolved by forwarding");
            }
        }

        if (regs.EX_MEM.getInstruction() != null && regs.EX_MEM.isRegWrite()) {
            int memDestReg = regs.EX_MEM.getDestReg();
            if (memDestReg != 0 && (idRs == memDestReg || idRt == memDestReg)) {
                detectedHazards.add("DATA_HAZARD: Can be resolved by forwarding from MEM/WB");
            }
        }
    }

    private void checkControlHazards(PipelineRegisters regs) {
        if (regs.EX_MEM.isBranch() && regs.EX_MEM.isBranchTaken()) {
            detectedHazards.add("CONTROL_HAZARD: Branch taken - pipeline needs flushing");
        }

        if (regs.IF_ID.getInstruction() != null) {
            int opcode = regs.IF_ID.getInstruction().getOpcode();
            if (opcode == 0x02 || opcode == 0x03) { // j or jal
                detectedHazards.add("CONTROL_HAZARD: Jump instruction - pipeline needs flushing");
            }
        }
    }

    private void checkStructuralHazards(PipelineRegisters regs) {

        if (regs.EX_MEM.isRegWrite() && regs.MEM_WB.isRegWrite()) {
            int exMemDest = regs.EX_MEM.getDestReg();
            int memWbDest = regs.MEM_WB.getDestReg();

            if (exMemDest != 0 && exMemDest == memWbDest) {
                detectedHazards.add("STRUCTURAL_HAZARD: Multiple writes to same register detected");
            }
        }
    }

    public boolean hasLoadUseHazard(PipelineRegisters regs) {
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