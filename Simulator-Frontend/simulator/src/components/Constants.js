export const SAMPLE_PROGRAMS = {
  'Simple ADD': {
    code: '20080005\n21090003\n01095020',
    assembly: 'addi $t0, $zero, 5\naddi $t1, $t0, 3\nadd $t2, $t0, $t1',
    description: 'Basic arithmetic operations'
  },
  'Memory Operations': {
    code: '8c010000\n8c020004\n00221820\nac030008',
    assembly: 'lw $1, 0($0)\nlw $2, 4($0)\nadd $3, $1, $2\nsw $3, 8($0)',
    description: 'Load, compute, and store'
  },
  'Branch Test': {
    code: '00221820\n10400002\n00832022\n00a42824',
    assembly: 'add $3, $1, $2\nbeq $2, $0, 2\nadd $4, $4, $3\nand $5, $5, $4',
    description: 'Conditional branching'
  },
  'Data Hazard Demo': {
    code: '00221820\n00622020\n00832820\n00a33020',
    assembly: 'add $3, $1, $2\nadd $4, $3, $2\nadd $5, $4, $3\nadd $6, $5, $3',
    description: 'Back-to-back dependencies'
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