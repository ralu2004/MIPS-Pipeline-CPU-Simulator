package model.control;

public class ControlUnit {

    private boolean regWrite;
    private boolean memRead;
    private boolean memWrite;
    private boolean memToReg;
    private boolean aluSrc;
    private boolean regDst;
    private int aluOp;
    private boolean jump;
    private boolean branch;

    public ControlUnit() {}

    public void generateSignals(int opcode) {}

    public void propagateThroughStages() {}

    public void resetSignals() {}
}
