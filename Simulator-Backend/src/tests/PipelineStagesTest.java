package tests;

import model.cpu.CPUState;
import model.instruction.Instruction;
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

        // Preload some data in register file and memory
        cpu.registerFile.set(1, 5);
        cpu.registerFile.set(2, 10);
        cpu.dataMemory.storeWord(0, 42);
        cpu.dataMemory.storeWord(100, 99);
    }

    @Test
    void testFetchStage() {
        // lw $2, 0($1) - Load word from address in $1 + 0 into $2
        // Using the public constructor: Instruction(int opcode, int binary)
        ITypeInstruction lw = new ITypeInstruction(35, 0x8C220000); // opcode 35 for lw
        lw.decodeFields();
        cpu.instructionMemory.setInstruction(0, lw);

        assertEquals(0, cpu.pc.get(), "PC should start at 0");

        fetch.process(cpu, regs);

        assertNotNull(regs.IF_ID.getInstruction(), "Instruction should be fetched");
        assertEquals(lw, regs.IF_ID.getInstruction());
        assertEquals(4, cpu.pc.get(), "PC should increment by 4");
        assertEquals(4, regs.IF_ID.getPC(), "IF/ID should store PC+4");
    }

    @Test
    void testDecodeStage() {
        // lw $2, 0($1) - rs=1, rt=2, imm=0
        ITypeInstruction lw = new ITypeInstruction(35, 0x8C220000); // opcode 35 for lw
        lw.decodeFields();

        regs.IF_ID.set(lw, 4);

        decode.process(cpu, regs);

        assertEquals(5, regs.ID_EX.getReadData1(), "$1 contains 5");
        assertEquals(10, regs.ID_EX.getReadData2(), "$2 should be read (currently 10)");
        assertEquals(0, regs.ID_EX.getSignExtendedImm(), "Offset is 0");
        assertEquals(1, regs.ID_EX.getRs(), "Source register is $1");
        assertEquals(2, regs.ID_EX.getRt(), "Target register is $2");
        assertTrue(regs.ID_EX.isMemRead(), "lw requires memory read");
        assertTrue(regs.ID_EX.isRegWrite(), "lw writes to register");
    }

    @Test
    void testExecuteStage_Addition() {
        // add $3, $1, $2 - Add $1 (5) and $2 (10) -> $3 (15)
        // R-type instruction has opcode 0
        RTypeInstruction add = new RTypeInstruction(0, 0x00221820);
        add.decodeFields();

        regs.ID_EX.setInstruction(add);
        regs.ID_EX.setRs(1);
        regs.ID_EX.setRt(2);
        regs.ID_EX.setRd(3);
        regs.ID_EX.setRegDst(true);      // Use rd as destination
        regs.ID_EX.setRegWrite(true);    // Enable register write
        regs.ID_EX.setAluOp(2);          // R-type ALU operation
        regs.ID_EX.setReadData1(5);     // Value from $1
        regs.ID_EX.setReadData2(10);    // Value from $2
        regs.ID_EX.setPcPlus4(4);       // PC+4 for branch calculations
        regs.ID_EX.setSignExtendedImm(0);
        regs.ID_EX.setAluSrc(false);    // Use register, not immediate

        execute.process(cpu, regs);

        assertEquals(15, regs.EX_MEM.getAluResult(), "5 + 10 should equal 15");
        assertEquals(3, regs.EX_MEM.getDestReg(), "Destination should be $3");
        assertTrue(regs.EX_MEM.isRegWrite(), "Should enable register write");
    }

    @Test
    void testExecuteStage_LoadAddressCalculation() {
        // lw $2, 100($1) - Calculate address: $1 (5) + 100 = 105
        ITypeInstruction lw = new ITypeInstruction(35, 0x8C220064); // opcode 35 for lw, offset=100
        lw.decodeFields();

        regs.ID_EX.setInstruction(lw);
        regs.ID_EX.setRs(1);
        regs.ID_EX.setRt(2);
        regs.ID_EX.setReadData1(5);     // Base address from $1
        regs.ID_EX.setReadData2(10);    // Value from $2 (not used in lw)
        regs.ID_EX.setSignExtendedImm(100); // Offset
        regs.ID_EX.setAluSrc(true);     // Use immediate for address calculation
        regs.ID_EX.setAluOp(0);         // Load/Store uses ADD
        regs.ID_EX.setMemRead(true);
        regs.ID_EX.setRegWrite(true);
        regs.ID_EX.setRegDst(false);    // Use rt as destination
        regs.ID_EX.setPcPlus4(4);

        execute.process(cpu, regs);

        assertEquals(105, regs.EX_MEM.getAluResult(), "Address should be 5 + 100 = 105");
        assertEquals(2, regs.EX_MEM.getDestReg(), "Destination should be $2 (rt)");
        assertTrue(regs.EX_MEM.isMemRead(), "Should enable memory read");
    }

    @Test
    void testMemoryStage_LoadWord() {
        // Setup: Memory[0] = 42
        cpu.dataMemory.storeWord(0, 42);

        // lw $2, 0($1) where ALU computed address 0
        ITypeInstruction lw = new ITypeInstruction(35, 0x8C220000); // opcode 35 for lw
        lw.decodeFields();

        regs.EX_MEM.setInstruction(lw);
        regs.EX_MEM.setMemRead(true);
        regs.EX_MEM.setAluResult(0);    // Address to load from
        regs.EX_MEM.setDestReg(2);
        regs.EX_MEM.setRegWrite(true);
        regs.EX_MEM.setMemToReg(true);  // Select memory data

        memory.process(cpu, regs);

        assertEquals(42, regs.MEM_WB.getMemData(), "Should load 42 from memory[0]");
        assertEquals(2, regs.MEM_WB.getDestReg(), "Destination is $2");
        assertTrue(regs.MEM_WB.isRegWrite(), "Should write to register");
        assertTrue(regs.MEM_WB.isMemToReg(), "Should select memory data");
    }

    @Test
    void testMemoryStage_StoreWord() {
        // sw $2, 8($0) - Store $2 (value 10) to address 8
        ITypeInstruction sw = new ITypeInstruction(43, 0xAC020008); // opcode 43 for sw
        sw.decodeFields();

        regs.EX_MEM.setInstruction(sw);
        regs.EX_MEM.setMemWrite(true);
        regs.EX_MEM.setAluResult(8);    // Address = 8
        regs.EX_MEM.setWriteData(10);   // Value from $2

        memory.process(cpu, regs);

        assertEquals(10, cpu.dataMemory.loadWord(8), "Should store 10 at address 8");
    }

    @Test
    void testMemoryStage_NoOperation() {
        // R-type instruction doesn't access memory
        RTypeInstruction add = new RTypeInstruction(0, 0x00221820); // opcode 0 for R-type
        add.decodeFields();

        regs.EX_MEM.setInstruction(add);
        regs.EX_MEM.setMemRead(false);
        regs.EX_MEM.setMemWrite(false);
        regs.EX_MEM.setAluResult(15);
        regs.EX_MEM.setDestReg(3);
        regs.EX_MEM.setRegWrite(true);
        regs.EX_MEM.setMemToReg(false); // Use ALU result

        memory.process(cpu, regs);

        assertEquals(15, regs.MEM_WB.getAluResult(), "ALU result should pass through");
        assertEquals(3, regs.MEM_WB.getDestReg());
        assertTrue(regs.MEM_WB.isRegWrite());
        assertFalse(regs.MEM_WB.isMemToReg(), "Should use ALU result, not memory");
    }

    @Test
    void testWriteBackStage_FromALU() {
        // Write ALU result to register
        RTypeInstruction add = new RTypeInstruction(0, 0x00221820); // opcode 0 for R-type
        add.decodeFields();

        regs.MEM_WB.setInstruction(add);
        regs.MEM_WB.setRegWrite(true);
        regs.MEM_WB.setDestReg(4);
        regs.MEM_WB.setAluResult(99);
        regs.MEM_WB.setMemToReg(false); // Select ALU result

        int oldValue = cpu.registerFile.get(4);
        writeback.process(cpu, regs);

        assertEquals(99, cpu.registerFile.get(4), "Should write 99 to $4");
        assertNotEquals(oldValue, cpu.registerFile.get(4), "Register should change");
    }

    @Test
    void testWriteBackStage_FromMemory() {
        // Write memory data to register
        ITypeInstruction lw = new ITypeInstruction(35, 0x8C220000); // opcode 35 for lw
        lw.decodeFields();

        regs.MEM_WB.setInstruction(lw);
        regs.MEM_WB.setRegWrite(true);
        regs.MEM_WB.setDestReg(5);
        regs.MEM_WB.setAluResult(999);  // Should be ignored
        regs.MEM_WB.setMemData(42);     // Data from memory
        regs.MEM_WB.setMemToReg(true);  // Select memory data

        writeback.process(cpu, regs);

        assertEquals(42, cpu.registerFile.get(5), "Should write 42 from memory to $5");
    }

    @Test
    void testWriteBackStage_NoWrite() {
        // RegWrite = false, should not write
        RTypeInstruction add = new RTypeInstruction(0, 0x00221820); // opcode 0 for R-type
        add.decodeFields();

        regs.MEM_WB.setInstruction(add);
        regs.MEM_WB.setRegWrite(false);  // Disable write
        regs.MEM_WB.setDestReg(6);
        regs.MEM_WB.setAluResult(777);

        int oldValue = cpu.registerFile.get(6);
        writeback.process(cpu, regs);

        assertEquals(oldValue, cpu.registerFile.get(6), "Register should not change when RegWrite=false");
    }

    @Test
    void testPipelinePropagation_FullFlow() {
        // Test instruction flowing through entire pipeline
        // add $3, $1, $2 - Add 5 + 10 = 15, store in $3
        RTypeInstruction add = new RTypeInstruction(0, 0x00221820); // opcode 0 for R-type
        add.decodeFields();

        cpu.instructionMemory.setInstruction(0, add);

        // Cycle 1: Fetch
        fetch.process(cpu, regs);
        assertNotNull(regs.IF_ID.getInstruction(), "Instruction fetched");
        assertEquals(4, cpu.pc.get(), "PC incremented");

        // Cycle 2: Decode
        decode.process(cpu, regs);
        assertEquals(5, regs.ID_EX.getReadData1(), "$1 = 5");
        assertEquals(10, regs.ID_EX.getReadData2(), "$2 = 10");

        // Setup for execute (control signals would normally be set by decode)
        regs.ID_EX.setAluOp(2);        // R-type
        regs.ID_EX.setAluSrc(false);   // Use register
        regs.ID_EX.setRegDst(true);    // Use rd
        regs.ID_EX.setRegWrite(true);
        regs.ID_EX.setMemToReg(false);
        regs.ID_EX.setPcPlus4(4);

        // Cycle 3: Execute
        execute.process(cpu, regs);
        assertEquals(15, regs.EX_MEM.getAluResult(), "5 + 10 = 15");
        assertEquals(3, regs.EX_MEM.getDestReg(), "Destination is $3");

        // Cycle 4: Memory (no operation for R-type)
        memory.process(cpu, regs);
        assertEquals(15, regs.MEM_WB.getAluResult(), "Result passed through");

        // Cycle 5: Write Back
        int oldValue = cpu.registerFile.get(3);
        writeback.process(cpu, regs);
        assertEquals(15, cpu.registerFile.get(3), "$3 should contain 15");
        assertNotEquals(oldValue, cpu.registerFile.get(3), "$3 should have changed");
    }

    @Test
    void testRegisterZeroImmutable() {
        // Attempt to write to $zero, should remain 0
        RTypeInstruction add = new RTypeInstruction(0, 0x00221020); // add $0, $1, $2, opcode 0 for R-type
        add.decodeFields();

        regs.MEM_WB.setInstruction(add);
        regs.MEM_WB.setRegWrite(true);
        regs.MEM_WB.setDestReg(0);      // Try to write to $zero
        regs.MEM_WB.setAluResult(999);
        regs.MEM_WB.setMemToReg(false);

        writeback.process(cpu, regs);

        assertEquals(0, cpu.registerFile.get(0), "$zero should always be 0");
    }
}