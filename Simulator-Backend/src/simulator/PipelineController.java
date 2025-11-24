package simulator;

import model.control.StallUnit;
import model.cpu.CPUState;
import model.instruction.Instruction;
import model.pipeline.registers.MEM_WB_Register;
import model.pipeline.registers.PipelineRegisters;
import model.pipeline.stages.*;
import model.pipeline.state.*;

import java.util.ArrayList;
import java.util.List;

public class PipelineController {

    private final CPUState cpuState;
    private final PipelineRegisters pipelineRegisters = new PipelineRegisters();
    private final StallUnit stallUnit = new StallUnit();

    private final FetchStage fetch = new FetchStage();
    private final DecodeStage decode = new DecodeStage();
    private final ExecuteStage execute = new ExecuteStage();
    private final MemoryStage memory = new MemoryStage();
    private final WriteBackStage writeBack = new WriteBackStage();

    private final List<PipelineSnapshot> history = new ArrayList<>();
    private boolean branchFlushedThisCycle = false;
    private Instruction lastWbInstr = null;

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

        saveSnapshot();
    }

    private MEM_WB_Register saveMEM_WB(MEM_WB_Register original) {
        MEM_WB_Register copy = new MEM_WB_Register();
        copy.setAluResult(original.getAluResult());
        copy.setMemData(original.getMemData());
        copy.setDestReg(original.getDestReg());
        copy.setRegWrite(original.isRegWrite());
        copy.setMemToReg(original.isMemToReg());
        copy.setInstruction(original.getInstruction() == null ? null : original.getInstruction().copy());

        return copy;
    }

    private void handleControlHazards() {
        if (pipelineRegisters.EX_MEM.isBranch() && pipelineRegisters.EX_MEM.isBranchTaken()) {
            branchFlushedThisCycle = true;
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

    public void clearPipeline() {
        pipelineRegisters.IF_ID.set(null, 0);
        clearID_EX();

        pipelineRegisters.EX_MEM.setAluResult(0);
        pipelineRegisters.EX_MEM.setZeroFlag(false);
        pipelineRegisters.EX_MEM.setWriteData(0);
        pipelineRegisters.EX_MEM.setBranchTarget(0);
        pipelineRegisters.EX_MEM.setBranchTaken(false);
        pipelineRegisters.EX_MEM.setDestReg(-1);
        pipelineRegisters.EX_MEM.setRegWrite(false);
        pipelineRegisters.EX_MEM.setMemToReg(false);
        pipelineRegisters.EX_MEM.setBranch(false);
        pipelineRegisters.EX_MEM.setMemRead(false);
        pipelineRegisters.EX_MEM.setMemWrite(false);
        pipelineRegisters.EX_MEM.setInstruction(null);

        pipelineRegisters.MEM_WB.setAluResult(0);
        pipelineRegisters.MEM_WB.setMemData(0);
        pipelineRegisters.MEM_WB.setDestReg(-1);
        pipelineRegisters.MEM_WB.setRegWrite(false);
        pipelineRegisters.MEM_WB.setMemToReg(false);
        pipelineRegisters.MEM_WB.setInstruction(null);
    }

    public void clearHistory() {
        history.clear();
        branchFlushedThisCycle = false;
        lastWbInstr = null;
    }

    private void saveSnapshot() {

        StallUnit.StallControl stall = stallUnit.getStallControl();

        StageInfo ifInfo;
        StageInfo idInfo;
        StageInfo exInfo;
        StageInfo memInfo;
        StageInfo wbInfo;

        if (branchFlushedThisCycle) {
            ifInfo = new StageInfo(StageState.FLUSH, null);
        } else if (!stall.pcWrite) {
            ifInfo = new StageInfo(StageState.STALL, null);
        } else if (pipelineRegisters.IF_ID.getInstruction() == null) {
            ifInfo = new StageInfo(StageState.EMPTY, null);
        } else {
            ifInfo = new StageInfo(StageState.INSTR, pipelineRegisters.IF_ID.getInstruction());
        }

        if (branchFlushedThisCycle) {
            idInfo = new StageInfo(StageState.FLUSH, null);
        } else if (!stall.ifidWrite) {
            idInfo = new StageInfo(StageState.STALL, null);
        } else if (pipelineRegisters.ID_EX.getInstruction() == null) {
            idInfo = new StageInfo(StageState.EMPTY, null);
        } else {
            idInfo = new StageInfo(StageState.INSTR, pipelineRegisters.ID_EX.getInstruction());
        }

        if (stall.idExClear) {
            exInfo = new StageInfo(StageState.BUBBLE, null);
        } else if (pipelineRegisters.EX_MEM.getInstruction() == null) {
            exInfo = new StageInfo(StageState.EMPTY, null);
        } else {
            exInfo = new StageInfo(StageState.INSTR, pipelineRegisters.EX_MEM.getInstruction());
        }

        if (pipelineRegisters.MEM_WB.getInstruction() == null) {
            memInfo = new StageInfo(StageState.EMPTY, null);
        } else {
            memInfo = new StageInfo(StageState.INSTR, pipelineRegisters.MEM_WB.getInstruction());
        }

        if (lastWbInstr == null) {
            wbInfo = new StageInfo(StageState.EMPTY, null);
        } else {
            wbInfo = new StageInfo(StageState.INSTR, lastWbInstr);
        }

        if (pipelineRegisters.MEM_WB.getInstruction() != null) {
            lastWbInstr = pipelineRegisters.MEM_WB.getInstruction();
        } else {
            lastWbInstr = null;
        }

        PipelineSnapshot snapshot = new PipelineSnapshot(ifInfo, idInfo, exInfo, memInfo, wbInfo);

        history.add(snapshot);
        branchFlushedThisCycle = false;
    }

    public PipelineRegisters getPipelineRegisters() {
        return pipelineRegisters;
    }

    public List<PipelineSnapshot> getHistory() {
        return history;
    }

}