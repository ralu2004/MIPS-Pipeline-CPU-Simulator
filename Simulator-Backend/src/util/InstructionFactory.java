package util;

import model.instruction.ITypeInstruction;
import model.instruction.Instruction;
import model.instruction.JTypeInstruction;
import model.instruction.RTypeInstruction;

/**
 * Instruction Factory
 * Parses 32-bit binary instructions and creates appropriate objects
 */
public class InstructionFactory {
    
    /**
     * Parse a 32-bit binary instruction word into an Instruction object
     * 
     * @param binaryWord 32-bit instruction word
     * @return Instruction object
     */
    public static Instruction parseInstruction(int binaryWord) {

        int opcode = (binaryWord >> 26) & 0x3F;
        
        if (opcode == 0x00) {
            return new RTypeInstruction(opcode, binaryWord);
        } else if (opcode == 0x02 || opcode == 0x03) {
            return new JTypeInstruction(opcode, binaryWord);
        } else {
            return new ITypeInstruction(opcode, binaryWord);
        } 
    }
}

