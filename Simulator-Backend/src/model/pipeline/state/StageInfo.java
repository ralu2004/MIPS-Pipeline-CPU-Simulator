package model.pipeline.state;

public class StageInfo {

    private final StageState state;
    private final String instruction;

    public StageInfo(StageState state, String instruction) {
        this.state = state;
        this.instruction = instruction;
    }

    public StageState getState() {
        return state;
    }

    public String getInstruction() {
        return instruction;
    }

    @Override
    public String toString() {
        if (state == StageState.INSTR) {
            return instruction;
        }
        return state.toString();
    }
}
