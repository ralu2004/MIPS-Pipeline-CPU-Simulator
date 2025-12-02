package simulator;

import java.util.*;

public class Assembler {

    private static final Map<String, Integer> REGISTER_MAP = new HashMap<>();
    private static final Map<String, OpcodeInfo> OPCODE_MAP = new HashMap<>();

    static {
        REGISTER_MAP.put("$zero", 0); REGISTER_MAP.put("$0", 0);
        REGISTER_MAP.put("$at", 1); REGISTER_MAP.put("$1", 1);
        REGISTER_MAP.put("$v0", 2); REGISTER_MAP.put("$2", 2);
        REGISTER_MAP.put("$v1", 3); REGISTER_MAP.put("$3", 3);
        REGISTER_MAP.put("$a0", 4); REGISTER_MAP.put("$4", 4);
        REGISTER_MAP.put("$a1", 5); REGISTER_MAP.put("$5", 5);
        REGISTER_MAP.put("$a2", 6); REGISTER_MAP.put("$6", 6);
        REGISTER_MAP.put("$a3", 7); REGISTER_MAP.put("$7", 7);
        REGISTER_MAP.put("$t0", 8); REGISTER_MAP.put("$8", 8);
        REGISTER_MAP.put("$t1", 9); REGISTER_MAP.put("$9", 9);
        REGISTER_MAP.put("$t2", 10); REGISTER_MAP.put("$10", 10);
        REGISTER_MAP.put("$t3", 11); REGISTER_MAP.put("$11", 11);
        REGISTER_MAP.put("$t4", 12); REGISTER_MAP.put("$12", 12);
        REGISTER_MAP.put("$t5", 13); REGISTER_MAP.put("$13", 13);
        REGISTER_MAP.put("$t6", 14); REGISTER_MAP.put("$14", 14);
        REGISTER_MAP.put("$t7", 15); REGISTER_MAP.put("$15", 15);
        REGISTER_MAP.put("$s0", 16); REGISTER_MAP.put("$16", 16);
        REGISTER_MAP.put("$s1", 17); REGISTER_MAP.put("$17", 17);
        REGISTER_MAP.put("$s2", 18); REGISTER_MAP.put("$18", 18);
        REGISTER_MAP.put("$s3", 19); REGISTER_MAP.put("$19", 19);
        REGISTER_MAP.put("$s4", 20); REGISTER_MAP.put("$20", 20);
        REGISTER_MAP.put("$s5", 21); REGISTER_MAP.put("$21", 21);
        REGISTER_MAP.put("$s6", 22); REGISTER_MAP.put("$22", 22);
        REGISTER_MAP.put("$s7", 23); REGISTER_MAP.put("$23", 23);
        REGISTER_MAP.put("$t8", 24); REGISTER_MAP.put("$24", 24);
        REGISTER_MAP.put("$t9", 25); REGISTER_MAP.put("$25", 25);
        REGISTER_MAP.put("$k0", 26); REGISTER_MAP.put("$26", 26);
        REGISTER_MAP.put("$k1", 27); REGISTER_MAP.put("$27", 27);
        REGISTER_MAP.put("$gp", 28); REGISTER_MAP.put("$28", 28);
        REGISTER_MAP.put("$sp", 29); REGISTER_MAP.put("$29", 29);
        REGISTER_MAP.put("$fp", 30); REGISTER_MAP.put("$30", 30);
        REGISTER_MAP.put("$ra", 31); REGISTER_MAP.put("$31", 31);

        // R-Type
        OPCODE_MAP.put("add", new OpcodeInfo(InstructionType.R, 0x00, 0x20));
        OPCODE_MAP.put("sub", new OpcodeInfo(InstructionType.R, 0x00, 0x22));
        OPCODE_MAP.put("and", new OpcodeInfo(InstructionType.R, 0x00, 0x24));
        OPCODE_MAP.put("or", new OpcodeInfo(InstructionType.R, 0x00, 0x25));
        OPCODE_MAP.put("xor", new OpcodeInfo(InstructionType.R, 0x00, 0x26));
        OPCODE_MAP.put("nor", new OpcodeInfo(InstructionType.R, 0x00, 0x27));
        OPCODE_MAP.put("slt", new OpcodeInfo(InstructionType.R, 0x00, 0x2A));

        // I-Type
        OPCODE_MAP.put("addi", new OpcodeInfo(InstructionType.I, 0x08, 0));
        OPCODE_MAP.put("andi", new OpcodeInfo(InstructionType.I, 0x0C, 0));
        OPCODE_MAP.put("ori", new OpcodeInfo(InstructionType.I, 0x0D, 0));
        OPCODE_MAP.put("slti", new OpcodeInfo(InstructionType.I, 0x0A, 0));
        OPCODE_MAP.put("lw", new OpcodeInfo(InstructionType.I, 0x23, 0));
        OPCODE_MAP.put("sw", new OpcodeInfo(InstructionType.I, 0x2B, 0));
        OPCODE_MAP.put("beq", new OpcodeInfo(InstructionType.I, 0x04, 0));
        OPCODE_MAP.put("bne", new OpcodeInfo(InstructionType.I, 0x05, 0));

        // J-Type
        OPCODE_MAP.put("j", new OpcodeInfo(InstructionType.J, 0x02, 0));
        OPCODE_MAP.put("jal", new OpcodeInfo(InstructionType.J, 0x03, 0));
    }

    public static AssemblyResult assemble(String[] assemblyLines, int startAddress) {
        List<Integer> machineCode = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, Integer> labels = new HashMap<>();

        int currentAddress = startAddress;
        List<String> cleanedLines = new ArrayList<>();

        for (int i = 0; i < assemblyLines.length; i++) {
            String line = assemblyLines[i].trim();

            int commentIdx = line.indexOf('#');
            if (commentIdx >= 0) {
                line = line.substring(0, commentIdx).trim();
            }

            if (line.isEmpty()) continue;

            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                String label = parts[0].trim();
                labels.put(label, currentAddress);
                line = parts.length > 1 ? parts[1].trim() : "";
                if (line.isEmpty()) continue;
            }

            cleanedLines.add(line);
            currentAddress += 4;
        }

        currentAddress = startAddress;
        for (int i = 0; i < cleanedLines.size(); i++) {
            try {
                int instr = assembleLine(cleanedLines.get(i), currentAddress, labels);
                machineCode.add(instr);
                currentAddress += 4;
            } catch (Exception e) {
                errors.add("Line " + (i + 1) + ": " + e.getMessage());
            }
        }

        return new AssemblyResult(machineCode, errors);
    }

    private static int assembleLine(String line, int address, Map<String, Integer> labels) {
        String[] tokens = line.split("[,\\s()]+");
        List<String> parts = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isEmpty()) parts.add(token.toLowerCase());
        }

        if (parts.isEmpty()) {
            throw new IllegalArgumentException("Empty instruction");
        }

        String op = parts.get(0);
        OpcodeInfo info = OPCODE_MAP.get(op);

        if (info == null) {
            throw new IllegalArgumentException("Unknown instruction: " + op);
        }

        switch (info.type) {
            case R:
                return assembleRType(op, parts, info);
            case I:
                return assembleIType(op, parts, info, address, labels);
            case J:
                return assembleJType(op, parts, info, labels);
            default:
                throw new IllegalArgumentException("Unknown instruction type");
        }
    }

    private static int assembleRType(String op, List<String> parts, OpcodeInfo info) {
        if (op.equals("sll") || op.equals("srl") || op.equals("sra")) {
            if (parts.size() != 4) {
                throw new IllegalArgumentException("Invalid format for " + op);
            }
            int rd = parseRegister(parts.get(1));
            int rt = parseRegister(parts.get(2));
            int shamt = parseImmediate(parts.get(3)) & 0x1F;
            return (info.opcode << 26) | (rt << 16) | (rd << 11) | (shamt << 6) | info.funct;
        } else {
            if (parts.size() != 4) {
                throw new IllegalArgumentException("Invalid format for " + op);
            }
            int rd = parseRegister(parts.get(1));
            int rs = parseRegister(parts.get(2));
            int rt = parseRegister(parts.get(3));
            return (info.opcode << 26) | (rs << 21) | (rt << 16) | (rd << 11) | info.funct;
        }
    }

    private static int assembleIType(String op, List<String> parts, OpcodeInfo info,
                                     int address, Map<String, Integer> labels) {
        if (op.equals("lw") || op.equals("sw")) {
            if (parts.size() != 4) {
                throw new IllegalArgumentException("Invalid format for " + op);
            }
            int rt = parseRegister(parts.get(1));
            int offset = parseImmediate(parts.get(2)) & 0xFFFF;
            int rs = parseRegister(parts.get(3));
            return (info.opcode << 26) | (rs << 21) | (rt << 16) | offset;
        } else if (op.equals("beq") || op.equals("bne")) {
            if (parts.size() != 4) {
                throw new IllegalArgumentException("Invalid format for " + op);
            }
            int rs = parseRegister(parts.get(1));
            int rt = parseRegister(parts.get(2));
            int offset;

            if (labels.containsKey(parts.get(3))) {
                int targetAddress = labels.get(parts.get(3));
                offset = ((targetAddress - (address + 4)) / 4) & 0xFFFF;
            } else {
                offset = parseImmediate(parts.get(3)) & 0xFFFF;
            }

            return (info.opcode << 26) | (rs << 21) | (rt << 16) | offset;
        } else {
            if (parts.size() != 4) {
                throw new IllegalArgumentException("Invalid format for " + op);
            }
            int rt = parseRegister(parts.get(1));
            int rs = parseRegister(parts.get(2));
            int imm = parseImmediate(parts.get(3)) & 0xFFFF;
            return (info.opcode << 26) | (rs << 21) | (rt << 16) | imm;
        }
    }

    private static int assembleJType(String op, List<String> parts, OpcodeInfo info,
                                     Map<String, Integer> labels) {
        if (parts.size() != 2) {
            throw new IllegalArgumentException("Invalid format for " + op);
        }

        int address;
        if (labels.containsKey(parts.get(1))) {
            address = labels.get(parts.get(1)) / 4;
        } else {
            address = parseImmediate(parts.get(1));
        }

        address &= 0x3FFFFFF;
        return (info.opcode << 26) | address;
    }

    private static int parseRegister(String reg) {
        reg = reg.toLowerCase().trim();
        if (!REGISTER_MAP.containsKey(reg)) {
            throw new IllegalArgumentException("Invalid register: " + reg);
        }
        return REGISTER_MAP.get(reg);
    }

    private static int parseImmediate(String imm) {
        imm = imm.trim();
        try {
            if (imm.startsWith("0x") || imm.startsWith("0X")) {
                return (int) Long.parseLong(imm.substring(2), 16);
            } else {
                return Integer.parseInt(imm);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid immediate value: " + imm);
        }
    }

    private enum InstructionType { R, I, J }

    private static class OpcodeInfo {
        InstructionType type;
        int opcode;
        int funct;

        OpcodeInfo(InstructionType type, int opcode, int funct) {
            this.type = type;
            this.opcode = opcode;
            this.funct = funct;
        }
    }

    public static class AssemblyResult {
        public final List<Integer> machineCode;
        public final List<String> errors;

        public AssemblyResult(List<Integer> machineCode, List<String> errors) {
            this.machineCode = machineCode;
            this.errors = errors;
        }
    }
}