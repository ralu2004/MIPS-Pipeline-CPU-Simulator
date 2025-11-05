# Branch Handling and Control Hazards - Current Implementation

## Current Behavior (Implicit "Predict Not Taken")

### How Branches Are Currently Handled:

1. **IF Stage (Fetch):**
   - Always fetches from `PC` (sequential instruction)
   - Increments PC by 4 (PC+4)
   - **No prediction** - always assumes sequential execution

2. **ID Stage (Decode):**
   - Decodes branch instruction
   - Generates branch control signal
   - Continues sequential execution

3. **EX Stage (Execute):**
   - **Branch decision made here** (line 110 in ExecuteStage)
   - Calculates branch target: `PC+4 + (immediate << 2)`
   - Evaluates condition (for beq: checks if zero flag is true)
   - Sets `branchTaken` flag in EX_MEM register

4. **MEM Stage (Memory):**
   - **PC updated if branch taken** (line 31 in MemoryStage)
   - If `branchTaken == true`: PC = branchTarget
   - If `branchTaken == false`: PC continues sequential (already incremented in IF)

5. **Pipeline Flushing:**
   - **Only if branch is taken** (line 67 in PipelineController)
   - Flushes IF_ID and ID_EX registers (2 instructions already fetched)
   - **2-cycle penalty** when branch is taken

### Current Implementation = "Predict Not Taken"

**What "Predict Not Taken" means:**
- ✅ Continue fetching sequential instructions (PC+4, PC+8, PC+12...)
- ✅ If branch NOT taken: No penalty, continue normally
- ❌ If branch TAKEN: Flush pipeline, lose 2 cycles (IF_ID and ID_EX)

**Timeline Example:**
```
Cycle 1: IF fetches instruction at PC (branch instruction)
Cycle 2: IF fetches PC+4 (instruction after branch - might be wrong!)
         ID decodes branch
Cycle 3: IF fetches PC+8 (instruction after PC+4 - might be wrong!)
         ID decodes PC+4
         EX evaluates branch condition
Cycle 4: MEM updates PC if branch taken
         Pipeline flushed (IF_ID and ID_EX cleared)
         IF now fetches from correct PC (branch target or PC+4)
```

---

## Issues with Current Implementation

### 1. **Timing Issue: PC Update Too Late**
- Branch decision made in EX stage
- PC updated in MEM stage (1 cycle delay)
- Pipeline flush happens AFTER MEM stage processes
- This means we're flushing based on EX_MEM state from previous cycle

**Current Flow:**
```
Cycle N: EX stage makes branch decision, writes to EX_MEM
Cycle N+1: MEM stage updates PC based on EX_MEM
         : PipelineController.flush() checks EX_MEM (from cycle N)
         : But we've already fetched more instructions in cycle N+1!
```

**This is actually correct** - we check EX_MEM which contains the branch decision, and flush accordingly.

### 2. **No Explicit Prediction Logic**
- The code doesn't explicitly say "predict not taken"
- It's implicit in the behavior (always fetch sequential)
- Hard to change to other prediction strategies

---

## Options for Improvement

### Option 1: Keep "Predict Not Taken" (Simplest)
**Pros:**
- ✅ Already implemented (implicitly)
- ✅ Simple and correct
- ✅ Good for educational purposes

**Cons:**
- ❌ Always has 2-cycle penalty when branch taken
- ❌ Not explicitly configurable

**What to do:**
- Make it explicit with comments/documentation
- Add a configuration flag if needed
- Current code is fine, just needs clarification

---

### Option 2: Implement "Predict Taken" (More Complex)
**Changes needed:**
1. Detect branch in ID stage (not EX)
2. Calculate branch target in ID stage
3. Start fetching from branch target immediately
4. If prediction wrong: flush pipeline

**Pros:**
- ✅ Better for branches that are usually taken
- ✅ Can reduce penalty in some cases

**Cons:**
- ❌ More complex
- ❌ Still has penalty if prediction wrong
- ❌ Need to calculate target earlier (might need register values)

---

### Option 3: Branch Prediction Table (Most Complex)
**Changes needed:**
1. Track branch history (taken/not taken)
2. Predict based on history in ID stage
3. Update prediction table after branch resolves

**Pros:**
- ✅ Most accurate predictions
- ✅ Best performance for loops

**Cons:**
- ❌ Very complex to implement
- ❌ Probably overkill for educational simulator

---

## Recommendation

### For Your Project:

**Keep "Predict Not Taken"** - it's:
1. ✅ Already working correctly
2. ✅ Simple to understand and explain
3. ✅ Appropriate for educational MIPS simulator
4. ✅ Matches behavior of basic MIPS processors

**Make it explicit:**
- Add comments explaining the prediction strategy
- Document the 2-cycle penalty for taken branches
- Optionally add a configuration flag if you want to experiment later

**If you want to change later:**
- The architecture supports it - just need to:
  - Move branch target calculation to ID stage
  - Add prediction logic
  - Update PC earlier
  - Handle misprediction

---

## Current Code Locations

### Branch Decision:
- **ExecuteStage.java** line 110: `boolean branchTaken = regs.ID_EX.isBranch() && zeroFlag;`

### PC Update:
- **MemoryStage.java** line 31: `cpuState.pc.set(exMem.getBranchTarget());`

### Pipeline Flush:
- **PipelineController.java** line 67: Checks `EX_MEM.isBranchTaken()` and flushes IF_ID and ID_EX

### Sequential Fetch:
- **FetchStage.java** line 16: Always fetches from `cpuState.pc.get()` and increments

---

## Summary

**Current Status:** ✅ Implements "Predict Not Taken" (implicitly)

**Branch Taken Penalty:** 2 cycles (IF_ID and ID_EX flushed)

**Branch Not Taken Penalty:** 0 cycles (no flush needed)

**Recommendation:** Keep current implementation, add explicit documentation

**Future Flexibility:** Architecture supports adding prediction logic if needed

