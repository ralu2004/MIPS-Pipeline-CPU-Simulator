package simulator;

import model.control.StallUnit;
import model.cpu.CPUState;
import model.pipeline.registers.PipelineRegisters;
import model.pipeline.stages.*;

/**
 * Pipeline Controller
 * Orchestrates the MIPS 5-stage pipeline execution
 * Handles stalls and pipeline flushing for hazards
 */
public class PipelineController {

    private final CPUState cpuState;
    private final PipelineRegisters pipelineRegisters = new PipelineRegisters();
    private final StallUnit stallUnit = new StallUnit();

    private final FetchStage fetch = new FetchStage();
    private final DecodeStage decode = new DecodeStage();
    private final ExecuteStage execute = new ExecuteStage();
    private final MemoryStage memory = new MemoryStage();
    private final WriteBackStage writeBack = new WriteBackStage();

    public PipelineController(CPUState state) {
        this.cpuState = state;
    }
    
    /**
     * Get pipeline registers for testing/inspection
     */
    public PipelineRegisters getPipelineRegisters() {
        return pipelineRegisters;
    }

    public void runCycle() {
        stallUnit.detectStall(pipelineRegisters);
        StallUnit.StallControl stallControl = stallUnit.getStallControl();

        writeBack.process(cpuState, pipelineRegisters);
        memory.process(cpuState, pipelineRegisters);
        execute.process(cpuState, pipelineRegisters);

        handleControlHazards();

        if (stallControl.idExClear) {
            clearID_EX();
        } else {
            decode.process(cpuState, pipelineRegisters);
        }

        if (stallControl.pcWrite && stallControl.ifidWrite) {
            fetch.process(cpuState, pipelineRegisters);
        } else if (stallControl.pcWrite) {
            fetch.process(cpuState, pipelineRegisters);
        }
    }
    
    /**
     * Handle control hazards by flushing pipeline registers
     * 
     * Branch Prediction Strategy: "Predict Not Taken"
     * - When branch is taken: flush IF_ID and ID_EX (2 instructions)
     * - This results in 2-cycle penalty for taken branches
     * - No penalty for branches that are not taken
     */
    private void handleControlHazards() {
        if (pipelineRegisters.EX_MEM.isBranch() && pipelineRegisters.EX_MEM.isBranchTaken()) {
            pipelineRegisters.IF_ID.set(null, 0);
            clearID_EX();
        }
    }
    
    /**
     * Clear ID/EX register (insert bubble/NOP)
     */
    private void clearID_EX() {
        pipelineRegisters.ID_EX.setReadData1(0);
        pipelineRegisters.ID_EX.setReadData2(0);
        pipelineRegisters.ID_EX.setSignExtendedImm(0);
        pipelineRegisters.ID_EX.setPcPlus4(0);
        pipelineRegisters.ID_EX.setRs(0);
        pipelineRegisters.ID_EX.setRt(0);
        pipelineRegisters.ID_EX.setRd(0);
        pipelineRegisters.ID_EX.setRegWrite(false);
        pipelineRegisters.ID_EX.setMemToReg(false);
        pipelineRegisters.ID_EX.setBranch(false);
        pipelineRegisters.ID_EX.setMemRead(false);
        pipelineRegisters.ID_EX.setMemWrite(false);
        pipelineRegisters.ID_EX.setRegDst(false);
        pipelineRegisters.ID_EX.setAluSrc(false);
        pipelineRegisters.ID_EX.setAluOp(0);
        pipelineRegisters.ID_EX.setInstruction(null);
    }
}
