package tests;

import model.cpu.CPUState;
import model.instruction.ITypeInstruction;
import model.instruction.RTypeInstruction;
import model.memory.InstructionMemory;
import model.pipeline.registers.PipelineRegisters;
import model.pipeline.stages.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for MIPS pipeline stages.
 * Verifies instruction propagation and correctness of stage behavior.
 */
class PipelineStagesTest {

    private CPUState cpu;
    private PipelineRegisters regs;

    private FetchStage fetch;
    private DecodeStage decode;
    private ExecuteStage execute;
    private MemoryStage memory;
    private WriteBackStage writeback;

    @BeforeEach
    void setUp() {
        InstructionMemory instrMem = new InstructionMemory();
        cpu = new CPUState(instrMem);
        regs = new PipelineRegisters();

        fetch = new FetchStage();
        decode = new DecodeStage();
        execute = new ExecuteStage();
        memory = new MemoryStage();
        writeback = new WriteBackStage();

        // preload some data in register file and memory
        cpu.registerFile.set(1, 5);
        cpu.registerFile.set(2, 10);
        cpu.dataMemory.storeWord(100, 42);
    }

    @Test
    void testFetchStage() {
        // load instruction into memory and fetch it
        ITypeInstruction lw = new ITypeInstruction(0x23, 0x8C220000); // lw $2, 0($1)
        cpu.instructionMemory.setInstruction(0, lw);

        fetch.process(cpu, regs);

        assertNotNull(regs.IF_ID.getInstruction());
        assertEquals(lw, regs.IF_ID.getInstruction());
        assertEquals(4, cpu.pc.get());
    }

    @Test
    void testDecodeStage() {
        ITypeInstruction lw = new ITypeInstruction(0x23, 0x8C220000); // lw $2, 0($1)
        regs.IF_ID.set(lw, 4);
        decode.process(cpu, regs);

        assertEquals(5, regs.ID_EX.getReadData1());
        assertEquals(0, regs.ID_EX.getSignExtendedImm());
        assertTrue(regs.ID_EX.isMemRead());
    }

    @Test
    void testExecuteStage_Addition() {
        RTypeInstruction add = new RTypeInstruction(0x00, 0x00221820); // add $3, $1, $2
        add.decodeFields(); // Decode fields to populate rs, rt, rd, func

        regs.ID_EX.setInstruction(add);
        regs.ID_EX.setRs(1);
        regs.ID_EX.setRt(2);
        regs.ID_EX.setRd(3);
        regs.ID_EX.setRegDst(true);
        regs.ID_EX.setRegWrite(true);
        regs.ID_EX.setAluOp(2);
        regs.ID_EX.setReadData1(cpu.registerFile.get(1)); // 5
        regs.ID_EX.setReadData2(cpu.registerFile.get(2)); // 10
        regs.ID_EX.setPcPlus4(4); // Required by ExecuteStage
        regs.ID_EX.setSignExtendedImm(0);
        regs.ID_EX.setAluSrc(false); // Use register, not immediate

        execute.process(cpu, regs);

        assertEquals(15, regs.EX_MEM.getAluResult()); // 5 + 10 = 15
        assertEquals(3, regs.EX_MEM.getDestReg());
    }

    @Test
    void testMemoryStage_LoadWord() {
        cpu.dataMemory.storeWord(0, 42); // aligned address fix
        ITypeInstruction lw = new ITypeInstruction(0x23, 0x8C220000); // lw $2, 0($1)
        regs.EX_MEM.setInstruction(lw);
        regs.EX_MEM.setMemRead(true);
        regs.EX_MEM.setAluResult(0);
        regs.EX_MEM.setDestReg(2);
        regs.EX_MEM.setRegWrite(true);
        regs.EX_MEM.setMemToReg(true);

        memory.process(cpu, regs);

        assertEquals(42, regs.MEM_WB.getMemData());
    }

    @Test
    void testWriteBackStage() {
        RTypeInstruction add = new RTypeInstruction(0x00, 0x00221820); // add $3, $1, $2
        regs.MEM_WB.setInstruction(add);
        regs.MEM_WB.setRegWrite(true);
        regs.MEM_WB.setDestReg(4);
        regs.MEM_WB.setAluResult(99);
        regs.MEM_WB.setMemToReg(false);

        writeback.process(cpu, regs);

        assertEquals(99, cpu.registerFile.get(4));
    }
}
