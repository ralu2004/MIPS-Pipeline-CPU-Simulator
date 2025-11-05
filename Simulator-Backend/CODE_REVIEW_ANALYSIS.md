# MIPS 32 Pipeline Simulator - Code Review & Compliance Analysis

## Executive Summary

The codebase implements a solid foundation for a MIPS 32-bit 5-stage pipeline simulator. The architecture correctly follows MIPS pipeline principles, but there are **critical issues** that prevent proper stall and hazard handling from working, and several **architectural improvements** needed for full compliance.

---

## ✅ **What's Working Well**

### 1. **Pipeline Architecture** ✓
- Correct 5-stage pipeline: IF → ID → EX → MEM → WB
- Proper reverse-order processing (WB before IF) to simulate write-before-read
- All pipeline registers properly defined with correct data paths

### 2. **Control Unit** ✓
- Correctly generates control signals for R-type, I-type, load/store, branch, and jump
- Proper ALUOp encoding (0=add, 1=sub, 2=R-type funct)
- Control signals properly propagated through pipeline registers

### 3. **Forwarding Unit** ✓
- Correctly implements EX/MEM and MEM/WB forwarding
- Proper priority (EX/MEM takes precedence over MEM/WB)
- Integrated correctly into ExecuteStage

### 4. **Hazard Detection** ✓
- HazardDetectionUnit properly detects load-use, data, control, and structural hazards
- Correctly decodes instructions to check register dependencies

### 5. **Instruction Decoding** ✓
- R-type, I-type, J-type instructions properly decoded
- Sign extension correctly implemented
- Register number extraction matches MIPS specification

---

## 🚨 **CRITICAL ISSUES**

### 1. **Stall Unit Not Integrated into PipelineController** ❌ **CRITICAL**

**Problem:** `PipelineController.runCycle()` does NOT check for stalls before processing stages. The `StallUnit` exists but is never called.

**Impact:** Load-use hazards will cause incorrect execution - instructions will use stale register values.

**Location:** `src/simulator/PipelineController.java`

**Current Code:**
```java
public void runCycle() {
    writeBack.process(cpuState, pipelineRegisters);
    memory.process(cpuState, pipelineRegisters);
    execute.process(cpuState, pipelineRegisters);
    decode.process(cpuState, pipelineRegisters);
    fetch.process(cpuState, pipelineRegisters);
}
```

**Required Fix:**
- Check for stalls BEFORE processing stages
- Apply stall control signals (freeze PC, freeze IF/ID, clear ID/EX)
- Skip stages if stall is active

---

### 2. **Pipeline Flushing Not Implemented** ❌ **CRITICAL**

**Problem:** When branches/jumps are taken, incorrectly fetched instructions remain in IF_ID and ID_EX registers. These should be flushed (cleared).

**Impact:** 
- After a taken branch, wrong instructions continue through pipeline
- After a jump, wrong instructions execute
- Control hazards not properly handled

**Location:** `src/model/pipeline/stages/DecodeStage.java` and `src/model/pipeline/stages/MemoryStage.java`

**Required Fix:**
- When branch taken in MEM stage: flush IF_ID and ID_EX
- When jump detected in ID stage: flush IF_ID (already updating PC, but need to clear IF_ID)

---

### 3. **DataMemory Bounds Checking Missing** ❌ **HIGH PRIORITY**

**Problem:** `DataMemory.loadWord()` and `storeWord()` have no bounds checking. Out-of-bounds access will cause `ArrayIndexOutOfBoundsException`.

**Location:** `src/model/memory/DataMemory.java`

**Current Code:**
```java
public int loadWord(int address) { return memory[address / 4]; }
public void storeWord(int address, int value) { memory[address / 4] = value; }
```

**Required Fix:**
- Add bounds checking similar to InstructionMemory
- Return 0 for out-of-bounds loads (or throw exception)
- Ignore out-of-bounds stores (or throw exception)

---

## ⚠️ **ARCHITECTURAL ISSUES**

### 4. **Branch Timing - PC Update Delay** ⚠️

**Problem:** Branch PC update happens in MEM stage, but branch decision is made in EX stage. This is acceptable but creates a 1-cycle delay penalty.

**Location:** `src/model/pipeline/stages/MemoryStage.java`

**Current Implementation:**
- Branch decision made in EX stage (line 110 in ExecuteStage)
- PC updated in MEM stage (line 31 in MemoryStage)

**Note:** This is actually correct for a simple pipeline model, but real MIPS tries to update PC earlier. For educational purposes, this is acceptable.

---

### 5. **Jump Instruction Execute() Method Unused** ⚠️ **MINOR**

**Problem:** `JTypeInstruction.execute()` exists but is never called in the pipeline. Jumps are handled directly in DecodeStage.

**Location:** `src/model/instruction/JTypeInstruction.java`

**Note:** This is legacy code. The `execute()` method in Instruction classes is not used in pipeline stages (everything happens in stages). This is acceptable but could be cleaned up.

---

### 6. **Missing Instruction Factory/Parser** ⚠️ **HIGH PRIORITY**

**Problem:** No way to create `Instruction` objects from:
- Binary instruction words (32-bit integers)
- Assembly text
- Hex strings

**Impact:** Cannot load programs into InstructionMemory

**Required:** 
- Instruction factory to parse binary → Instruction subclass
- Or assembler to parse assembly → binary → Instruction

---

### 7. **FetchStage Always Updates PC** ⚠️

**Problem:** `FetchStage` always increments PC, even when a branch/jump was just taken. This should be conditional based on control hazards.

**Location:** `src/model/pipeline/stages/FetchStage.java`

**Current Code:**
```java
cpuState.pc.increment(); // Always increments
```

**Note:** With proper stall/flush handling, this should work, but needs to be coordinated with control hazard detection.

---

## 📋 **MIPS COMPLIANCE CHECKLIST**

### ✅ **Compliant:**
- [x] 5-stage pipeline structure
- [x] Control signal generation
- [x] Forwarding paths (EX/MEM, MEM/WB)
- [x] Register file ($zero always 0)
- [x] Instruction field decoding (opcode, rs, rt, rd, immediate, funct)
- [x] Sign extension for immediate values
- [x] Branch target calculation (PC+4 + offset<<2)
- [x] Jump target calculation (upper 4 bits + address<<2)

### ❌ **Non-Compliant / Missing:**
- [ ] Stall handling integration (unit exists but not used)
- [ ] Pipeline flushing for control hazards
- [ ] Memory bounds checking
- [ ] Instruction loading mechanism
- [ ] Branch prediction (not required for basic simulator)

---

## 🔧 **RECOMMENDED FIXES (Priority Order)**

### **Priority 1 (Critical - Must Fix):**
1. **Integrate StallUnit into PipelineController**
   - Check for stalls before processing stages
   - Apply stall control signals
   - Insert bubbles when needed

2. **Implement Pipeline Flushing**
   - Flush IF_ID and ID_EX when branch taken
   - Flush IF_ID when jump executed

3. **Add DataMemory Bounds Checking**
   - Prevent array out-of-bounds exceptions

### **Priority 2 (High - Should Fix):**
4. **Create Instruction Factory**
   - Parse 32-bit binary → Instruction object
   - Support all instruction types

5. **Improve FetchStage PC Handling**
   - Only update PC if not stalled
   - Coordinate with control hazards

### **Priority 3 (Nice to Have):**
6. **Remove Unused execute() Methods**
   - Clean up legacy code in Instruction classes

7. **Add More Instruction Support**
   - Expand beyond current basic set (add, sub, addi, lw, sw, beq, j, jal)

---

## 📊 **Architecture Compliance Score**

| Component | Status | Score |
|-----------|--------|-------|
| Pipeline Structure | ✅ Excellent | 95% |
| Control Signals | ✅ Excellent | 95% |
| Forwarding | ✅ Excellent | 90% |
| Hazard Detection | ✅ Good | 85% |
| Stall Handling | ❌ Not Integrated | 20% |
| Pipeline Flushing | ❌ Missing | 0% |
| Memory Safety | ⚠️ Partial | 50% |
| Instruction Loading | ❌ Missing | 0% |
| **Overall** | **⚠️ Needs Work** | **~60%** |

---

## 🎯 **Next Steps**

1. **Fix Critical Issues First:**
   - Integrate StallUnit into PipelineController
   - Implement pipeline flushing
   - Add memory bounds checking

2. **Then Add Missing Features:**
   - Instruction factory/parser
   - Program loader
   - HTTP API layer

3. **Finally:**
   - Testing with sample programs
   - Documentation
   - Frontend integration

---

## 📚 **MIPS Architecture References**

- MIPS uses 5-stage pipeline: IF, ID, EX, MEM, WB ✓
- Forwarding resolves most data hazards ✓
- Load-use hazards require 1-cycle stall ✓ (detected but not applied)
- Branches cause control hazards requiring flush ✓ (detected but not applied)
- Register $zero (register 0) always reads as 0 ✓

---

**Generated:** 2024
**Status:** Requires critical fixes before production use

