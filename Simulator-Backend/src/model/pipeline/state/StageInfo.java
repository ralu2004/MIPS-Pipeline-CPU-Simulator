package model.pipeline.state;

import model.instruction.Instruction;

public class StageInfo {

    private final StageState state;
    private final Instruction instruction;

    public StageInfo(StageState state, Instruction instruction) {
        this.state = state;
        this.instruction = instruction;
    }

    public StageState getState() {
        return state;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    @Override
    public String toString() {
        if (state == StageState.INSTR) {
            return instruction.toString();
        }
        return state.toString();
    }
}
