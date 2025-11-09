package simulator;

import model.control.StallUnit;
import model.cpu.CPUState;
import model.pipeline.registers.MEM_WB_Register;
import model.pipeline.registers.PipelineRegisters;
import model.pipeline.stages.*;

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
        stallUnit.detectStall(pipelineRegisters);
        StallUnit.StallControl stallControl = stallUnit.getStallControl();

        MEM_WB_Register savedMEM_WB = saveMEM_WB(pipelineRegisters.MEM_WB);

        writeBack.process(cpuState, pipelineRegisters);
        memory.process(cpuState, pipelineRegisters);

        MEM_WB_Register newMEM_WB = pipelineRegisters.MEM_WB;
        pipelineRegisters.MEM_WB = savedMEM_WB;
        execute.process(cpuState, pipelineRegisters);
        pipelineRegisters.MEM_WB = newMEM_WB;

        handleControlHazards();

        if (stallControl.idExClear) {
            clearID_EX();
        } else {
            decode.process(cpuState, pipelineRegisters);
        }

        if (stallControl.pcWrite) {
            fetch.process(cpuState, pipelineRegisters);
        }
    }

    private MEM_WB_Register saveMEM_WB(MEM_WB_Register original) {
        MEM_WB_Register copy = new MEM_WB_Register();
        copy.setAluResult(original.getAluResult());
        copy.setMemData(original.getMemData());
        copy.setDestReg(original.getDestReg());
        copy.setRegWrite(original.isRegWrite());
        copy.setMemToReg(original.isMemToReg());
        copy.setInstruction(original.getInstruction());

        System.out.println("SAVED MEM_WB: aluResult=" + copy.getAluResult() +
                ", memData=" + copy.getMemData() +
                ", destReg=" + copy.getDestReg() +
                ", memToReg=" + copy.isMemToReg() +
                ", writeData=" + copy.getWriteData());

        return copy;
    }

    private void handleControlHazards() {
        if (pipelineRegisters.EX_MEM.isBranch() && pipelineRegisters.EX_MEM.isBranchTaken()) {
            pipelineRegisters.IF_ID.set(null, 0);
            clearID_EX();
        }
    }

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

    public PipelineRegisters getPipelineRegisters() {
        return pipelineRegisters;
    }
}
