export const SAMPLE_PROGRAMS = {
  'Simple ADD': {
    code: 'addi $t0, $zero, 5\naddi $t1, $t0, 3\nadd $t2, $t0, $t1',
    description: 'Basic arithmetic operations',
    expectedResults: 'Expected: $t0=5, $t1=8, $t2=13'
  },
  'Arithmetic Demo': {
    code: 'addi $t0, $zero, 10\naddi $t1, $zero, 20\nadd $t2, $t0, $t1\nsub $t3, $t0, $t1\nand $t4, $t1, $t2\nor $t5, $t1, $t3',
    description: 'Complete arithmetic operations',
    expectedResults: 'Expected: $t2=30, $t3=-10, $t4=20, $t5=-10'
  },
  'Memory Operations': {
    code: 'addi $t0, $zero, 100\naddi $t1, $zero, 200\nsw $t0, 0($zero)\nsw $t1, 4($zero)\nlw $t2, 0($zero)\nlw $t3, 4($zero)\nadd $t4, $t2, $t3\nsw $t4, 8($zero)',
    description: 'Load/store operations with computation',
    expectedResults: 'Expected: Memory[0]=100, Memory[4]=200, Memory[8]=300, $t4=300'
  },
  'Branch Test': {
    code: 'addi $t0, $zero, 15\naddi $t1, $zero, 15\nadd $t2, $t0, $t1\nbeq $t0, $t1, 2\nsub $t3, $t0, $t2\nand $t5, $t1, $t2',
    description: 'Conditional branching with arithmetic',
    expectedResults: 'Expected: Branch TAKEN, $t2=30, $t3=0, $t5=0'
  }
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
