package model.pipeline.registers;

public class PipelineRegisters {
    public final IF_ID_Register IF_ID = new IF_ID_Register();
    public final ID_EX_Register ID_EX = new ID_EX_Register();
    public final EX_MEM_Register EX_MEM = new EX_MEM_Register();
    public final MEM_WB_Register MEM_WB = new MEM_WB_Register();
}