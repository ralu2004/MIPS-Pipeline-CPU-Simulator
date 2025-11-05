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

    public void runCycle() {
        // Check for stalls BEFORE processing stages
        // This detects hazards based on current pipeline state
        stallUnit.detectStall(pipelineRegisters);
        StallUnit.StallControl stallControl = stallUnit.getStallControl();
        
        // Process in reverse order to emulate write-back before read
        writeBack.process(cpuState, pipelineRegisters);
        memory.process(cpuState, pipelineRegisters);
        execute.process(cpuState, pipelineRegisters);
        
        // Handle control hazards (pipeline flushing)
        handleControlHazards();
        
        // Decode stage - check for stall before processing
        if (stallControl.idExClear) {
            // Insert bubble: clear ID/EX register
            clearID_EX();
        } else {
            decode.process(cpuState, pipelineRegisters);
        }
        
        // Fetch stage - only if not stalled
        if (stallControl.pcWrite && stallControl.ifidWrite) {
            fetch.process(cpuState, pipelineRegisters);
        } else if (stallControl.pcWrite) {
            // PC can update but IF/ID is frozen (partial stall)
            // This case shouldn't normally occur, but handle it
            fetch.process(cpuState, pipelineRegisters);
        }
        // If both pcWrite and ifidWrite are false, fetch is completely frozen
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
        // Check if branch was taken (decision made in EX stage, result in EX_MEM)
        // Branch prediction: "predict not taken" - we always fetch sequential
        // If branch is taken, we need to flush the incorrectly fetched instructions
        if (pipelineRegisters.EX_MEM.isBranch() && pipelineRegisters.EX_MEM.isBranchTaken()) {
            // Branch was taken - flush IF_ID and ID_EX (bubble them)
            // These contain instructions that were fetched assuming branch not taken
            pipelineRegisters.IF_ID.set(null, 0);
            clearID_EX();
        }
        
        // Jumps are handled in DecodeStage (they update PC immediately)
        // DecodeStage already flushes IF_ID when jump is executed
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
