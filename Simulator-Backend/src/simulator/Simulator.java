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
        int cycles = 10;

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
            System.out.println("Loaded " + result.loadedCount + " instructions at address " + result.startAddress);

            System.out.println("\nRunning " + cycles + " cycles...\n");
            for (int i = 0; i < cycles; i++) {
                System.out.println("=== Cycle " + (i + 1) + " ===");
                clock.tick();
                printState(state);
                System.out.println();
            }
            
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
                    "0x20080000",  // addi $t0, $zero, 0
                    "0x8D080000",  // lw $t0, 0($t0)  - load from address 0
                    "0x01094820"   // add $t1, $t0, $t1 - uses $t0 (load-use hazard)
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
    
    private static void printUsage() {
        System.out.println("Usage: java simulator.SimulatorHarness <program> [--cycles=N]");
        System.out.println("\nAvailable programs:");
        System.out.println("  arith    - Simple arithmetic");
        System.out.println("  loaduse  - Load-use hazard test");
        System.out.println("  branch   - Branch instruction test");
        System.out.println("\nExample: java simulator.SimulatorHarness arith --cycles=5");
    }
}


