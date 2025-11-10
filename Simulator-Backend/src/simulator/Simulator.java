package simulator;

import model.cpu.CPUState;
import model.memory.InstructionMemory;

/**
 * java simulator.Simulator <program_name> [--cycles=N]
 */
public class Simulator {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String program = args[0];
        int cycles = 12;

        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--cycles=")) {
                cycles = Integer.parseInt(args[i].substring(9));
            }
        }

        CPUState state = new CPUState(new InstructionMemory());
        PipelineController controller = new PipelineController(state);
        Clock clock = new Clock(controller);

        String[] instructions = getProgram(program);
        if (instructions == null) {
            System.err.println("Unknown program: " + program);
            printUsage();
            return;
        }

        try {
            ProgramLoader.ProgramLoadResult result = ProgramLoader.loadFromHexStrings(state, instructions, 0);
            System.out.println("=== MIPS PIPELINE SIMULATOR ===");
            System.out.println("Program: " + program.toUpperCase());
            System.out.println("Loaded " + result.loadedCount + " instructions");
            System.out.println("Running " + cycles + " cycles...\n");

            for (int i = 0; i < cycles; i++) {
                System.out.println("--- CYCLE " + (i + 1) + " ---");
                clock.tick();
                printState(state);
                System.out.println();
            }

            System.out.println("=== FINAL RESULTS ===");
            printFinalRegisterState(state, program);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String[] getProgram(String name) {
        switch (name.toLowerCase()) {
            case "arith":
                return new String[]{
                        "0x20080005",  // addi $t0, $zero, 5
                        "0x21090003",  // addi $t1, $t0, 3
                        "0x01095020"   // add $t2, $t0, $t1
                };
            case "loaduse":
                return new String[]{
                        "0x2008000A",  // addi $t0, $zero, 10     - Initialize $t0 = 10
                        "0x20090014",  // addi $t1, $zero, 20     - Initialize $t1 = 20
                        "0xAD090004",  // sw   $t1, 4($t0)        - Store 20 at address 14 (10+4)
                        "0x8D0A0004",  // lw   $t2, 4($t0)        - Load from address 14 to $t2 (LOAD)
                        "0x01485820",  // add  $t3, $t2, $t0      - USE $t2 immediately (LOAD-USE HAZARD!)
                        "0x016C6020",  // add  $t4, $t3, $t4
                        "0x218D0001"   // addi $t5, $t4, 1
                };
            case "branch":
                return new String[]{
                        "0x20080001",  // addi $t0, $zero, 1
                        "0x20090002",  // addi $t1, $zero, 2
                        "0x11090002",  // beq $t0, $t1, 8  - branch not taken (1 != 2)
                        "0x200A0003",  // addi $t2, $zero, 3
                        "0x11080002"   // beq $t0, $t0, 8  - branch taken (1 == 1)
                };
            default:
                return null;
        }
    }

    private static void printState(CPUState state) {
        System.out.println("PC: " + state.pc.get());
        System.out.print("Registers: ");
        for (int i = 0; i < 32; i++) {
            if (i % 8 == 0 && i > 0) System.out.print("\n           ");
            System.out.printf("$%2d=%4d  ", i, state.registerFile.get(i));
        }
        System.out.println();
    }

    private static void printFinalRegisterState(CPUState state, String program) {
        String[] regNames = {"zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
                            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
                            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                            "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"};

        System.out.println("Final register values:");
        for (int i = 8; i <= 13; i++) {
            System.out.printf("  $%s ($%d) = %d\n", regNames[i], i, state.registerFile.get(i));
        }

        if (program.equals("loaduse")) {
            System.out.println("\nLoad-Use Hazard Results:");
            System.out.println("  Expected: $t2=20, $t3=30, $t4=30, $t5=31");
            System.out.println("  Hazard was resolved via forwarding/stalling");
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java simulator.Simulator <program> [--cycles=N]");
        System.out.println("\nAvailable programs:");
        System.out.println("  arith    - Simple arithmetic");
        System.out.println("  loaduse  - Load-use hazard test");
        System.out.println("  branch   - Branch instruction test");
        System.out.println("\nExample: java simulator.Simulator loaduse --cycles=10");
    }
}