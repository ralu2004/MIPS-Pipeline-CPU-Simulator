package model.pipeline.state;

public class PipelineSnapshot {

    private final StageInfo ifStage;
    private final StageInfo idStage;
    private final StageInfo exStage;
    private final StageInfo memStage;
    private final StageInfo wbStage;

    public PipelineSnapshot(StageInfo ifStage,
                            StageInfo idStage,
                            StageInfo exStage,
                            StageInfo memStage,
                            StageInfo wbStage) {
        this.ifStage = ifStage;
        this.idStage = idStage;
        this.exStage = exStage;
        this.memStage = memStage;
        this.wbStage = wbStage;
    }

    public StageInfo getIfStage() { return ifStage; }
    public StageInfo getIdStage() { return idStage; }
    public StageInfo getExStage() { return exStage; }
    public StageInfo getMemStage() { return memStage; }
    public StageInfo getWbStage() { return wbStage; }

    @Override
    public String toString() {
        return "IF="  + ifStage +
                ", ID="  + idStage +
                ", EX="  + exStage +
                ", MEM=" + memStage +
                ", WB="  + wbStage;
    }
}

