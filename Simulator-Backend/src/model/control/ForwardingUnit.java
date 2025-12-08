package model.control;

import model.pipeline.registers.PipelineRegisters;

public class ForwardingUnit {

    // 00 = no forwarding
    // 10 = forward from EX/MEM
    // 01 = forward from MEM/WB
    private int forwardA = 0;
    private int forwardB = 0;

    public ForwardingResult determineForwarding(PipelineRegisters regs) {
        forwardA = 0;
        forwardB = 0;

        int exRs = regs.ID_EX.getRs();
        int exRt = regs.ID_EX.getRt();

        // System.out.println("DEBUG Forwarding Unit:");
        // System.out.println("  ID/EX Rs=" + exRs + ", Rt=" + exRt);
        // System.out.println("  EX/MEM destReg=" + regs.EX_MEM.getDestReg() +
        //         ", RegWrite=" + regs.EX_MEM.isRegWrite());
        // System.out.println("  MEM/WB destReg=" + regs.MEM_WB.getDestReg() +
        //         ", RegWrite=" + regs.MEM_WB.isRegWrite());

        // EX/MEM
        // consecutive instr, dist = 1
        if (regs.EX_MEM.isRegWrite() && regs.EX_MEM.getDestReg() != 0) {
            int exMemDest = regs.EX_MEM.getDestReg();

            if (exRs == exMemDest) {
                forwardA = 2;
            }

            if (exRt == exMemDest) {
                forwardB = 2;
            }
        }

        // MEM/WB - dist = 2
        if (regs.MEM_WB.isRegWrite() && regs.MEM_WB.getDestReg() != 0) {
            int memWbDest = regs.MEM_WB.getDestReg();

            if (exRs == memWbDest &&
                    (!regs.EX_MEM.isRegWrite() || regs.EX_MEM.getDestReg() != exRs)) {
                forwardA = 1;
            }

            if (exRt == memWbDest &&
                    (!regs.EX_MEM.isRegWrite() || regs.EX_MEM.getDestReg() != exRt)) {
                forwardB = 1;
            }
        }

        return new ForwardingResult(forwardA, forwardB);
    }

    public static class ForwardingResult {
        public final int forwardA;
        public final int forwardB;

        public ForwardingResult(int forwardA, int forwardB) {
            this.forwardA = forwardA;
            this.forwardB = forwardB;
        }
    }
}
