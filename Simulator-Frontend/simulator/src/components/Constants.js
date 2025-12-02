/*export const SAMPLE_PROGRAMS = {
  'Simple ADD': {
    code: '20080005\n21090003\n01095020',
    assembly: 'addi $t0, $zero, 5\naddi $t1, $t0, 3\nadd $t2, $t0, $t1',
    description: 'Basic arithmetic operations'
  },
  'Memory Operations': {
    code: 'ac010000\nac020004\n8c010000\n8c020004\n00221820\nac030008',
    assembly: 'sw $1, 0($0)\nsw $2, 4($0)\nlw $1, 0($0)\nlw $2, 4($0)\nadd $3, $1, $2\nsw $3, 8($0)',
    description: 'Store values, then load, compute, and store result'
  },
  'Branch Test': {
    code: '20010005\n2002000a\n00221820\n10400002\n00832022\n00a42824',
    assembly: 'addi $1, $0, 5\naddi $2, $0, 10\nadd $3, $1, $2\nbeq $2, $0, 2\nsub $4, $4, $3\nand $5, $5, $4',
    description: 'Conditional branching with initialized values'
  },
  'Data Hazard Demo': {
    code: '20010003\n20020007\n00221820\n00622020\n00832820\n00a33020',
    assembly: 'addi $1, $0, 3\naddi $2, $0, 7\nadd $3, $1, $2\nadd $4, $3, $2\nadd $5, $4, $3\nadd $6, $5, $3',
    description: 'Back-to-back dependencies (3+7=10, cascading adds)'
  }
};*/

export const SAMPLE_PROGRAMS = {
  'Simple ADD': {
    code: 'addi $t0, $zero, 5\naddi $t1, $t0, 3\nadd $t2, $t0, $t1',
    assembly: 'addi $t0, $zero, 5\naddi $t1, $t0, 3\nadd $t2, $t0, $t1',
    description: 'Basic arithmetic operations'
  },
  'Arithmetic Demo': {
    code: '2008000a\n20090014\n01095020\n01095822\n012a6024\n012b6825',
    assembly: 'addi $t0, $zero, 10\naddi $t1, $zero, 20\nadd $t2, $t0, $t1\nsub $t3, $t0, $t1\nand $t4, $t1, $t2\nor $t5, $t1, $t3',
    description: 'Complete arithmetic operations',
    expectedResults: 'Final: $t2=30, $t3=-10, $t4=20, $t5=20'
  },
  'Memory Operations': {
    code: '20080064\n200900c8\nac080000\nac090004\n8c0a0000\n8c0b0004\n014b6020\nac0c0008',
    assembly: 'addi $t0, $zero, 100\naddi $t1, $zero, 200\nsw $t0, 0($zero)\nsw $t1, 4($zero)\nlw $t2, 0($zero)\nlw $t3, 4($zero)\nadd $t4, $t2, $t3\nsw $t4, 8($zero)',
    description: 'Load/store operations with computation',
    expectedResults: 'Final: Memory[0]=100, Memory[4]=200, Memory[8]=300, $t4=300'
  },
  'Branch Test': {
    code: '2008000f\n2009000f\n01095020\n11000002\n01095822\n012a6824',
    assembly: 'addi $t0, $zero, 15\naddi $t1, $zero, 15\nadd $t2, $t0, $t1\nbeq $t0, $t1, 2\nsub $t3, $t0, $t2\nand $t5, $t1, $t2',
    description: 'Conditional branching with arithmetic',
    expectedResults: 'Final: Branch TAKEN, $t2=30, $t3=-15, $t5=14'
  }/*,
  'Data Hazard Demo': {
    code: '20080007\n20090009\n01095020\n00625820\n014b6020\n018c7020',
    assembly: 'addi $t0, $zero, 7\naddi $t1, $zero, 9\nadd $t2, $t0, $t1\nadd $t3, $t3, $t2\nadd $t4, $t2, $t3\nadd $t6, $t4, $t4',
    description: 'Back-to-back dependencies showing forwarding',
    expectedResults: 'Final: $t2=16, $t3=16, $t4=32, $t6=64'
  },
  'Comparison Demo': {
    code: '2008001e\n20090014\n010a502a\n200b0001\n11400002\n016c5820\n010d6822',
    assembly: 'addi $t0, $zero, 30\naddi $t1, $zero, 20\nslt $t2, $t0, $t1\naddi $t3, $zero, 1\nbeq $t2, $t3, 2\nadd $t3, $t3, $t4\nsub $t5, $t0, $t1',
    description: 'Set Less Than with conditional branch',
    expectedResults: 'Final: SLT=0 (30>20), Branch NOT taken, $t5=10'
  },
  'Complex Calculation': {
    code: '20080028\n2009001e\n01095020\n01095822\n012a6824\n016b7025\n01ac782a',
    assembly: 'addi $t0, $zero, 40\naddi $t1, $zero, 30\nadd $t2, $t0, $t1\nsub $t3, $t0, $t1\nand $t5, $t1, $t2\nor $t6, $t3, $t5\nslt $t7, $t6, $t2',
    description: 'Multiple operations with mixed results',
    expectedResults: 'Final: $t2=70, $t3=10, $t5=30, $t6=30, SLT=1 (30<70)'
  }*/
};

export const REGISTER_INFO = {
  '$zero': 'Constant value 0 (read-only)',
  '$at': 'Assembler temporary',
  '$v0': 'Function return value 0',
  '$v1': 'Function return value 1',
  '$a0': 'Function argument 0',
  '$a1': 'Function argument 1',
  '$a2': 'Function argument 2',
  '$a3': 'Function argument 3',
  '$t0': 'Temporary register 0',
  '$t1': 'Temporary register 1',
  '$t2': 'Temporary register 2',
  '$t3': 'Temporary register 3',
  '$t4': 'Temporary register 4',
  '$t5': 'Temporary register 5',
  '$t6': 'Temporary register 6',
  '$t7': 'Temporary register 7',
  '$s0': 'Saved register 0 (preserved)',
  '$s1': 'Saved register 1 (preserved)',
  '$s2': 'Saved register 2 (preserved)',
  '$s3': 'Saved register 3 (preserved)',
  '$s4': 'Saved register 4 (preserved)',
  '$s5': 'Saved register 5 (preserved)',
  '$s6': 'Saved register 6 (preserved)',
  '$s7': 'Saved register 7 (preserved)',
  '$t8': 'Temporary register 8',
  '$t9': 'Temporary register 9',
  '$k0': 'Kernel/OS reserved 0',
  '$k1': 'Kernel/OS reserved 1',
  '$gp': 'Global pointer',
  '$sp': 'Stack pointer',
  '$fp': 'Frame pointer',
  '$ra': 'Return address'
};

export const STAGE_INFO = {
  'IF': 'Instruction Fetch: Fetches the next instruction from memory using the Program Counter',
  'ID': 'Instruction Decode: Decodes the instruction and reads register operands',
  'EX': 'Execute: Performs ALU operations or calculates memory addresses',
  'MEM': 'Memory Access: Reads from or writes to data memory for load/store instructions',
  'WB': 'Write Back: Writes the result back to the destination register'
};

export const REGISTER_NAMES = [
  '$zero', '$at', '$v0', '$v1', '$a0', '$a1', '$a2', '$a3',
  '$t0', '$t1', '$t2', '$t3', '$t4', '$t5', '$t6', '$t7',
  '$s0', '$s1', '$s2', '$s3', '$s4', '$s5', '$s6', '$s7',
  '$t8', '$t9', '$k0', '$k1', '$gp', '$sp', '$fp', '$ra'
];