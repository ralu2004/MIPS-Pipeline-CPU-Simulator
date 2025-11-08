import model.instruction.ITypeInstruction;
import model.instruction.Instruction;
import model.instruction.JTypeInstruction;
import model.instruction.RTypeInstruction;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
	public static void main(String[] args) {
		/*try {
			simulator.ApiServer api = new simulator.ApiServer(8081);
			api.start();
		} catch (Exception e) {
			System.err.println("Failed to start API: " + e.getMessage());
			e.printStackTrace();
		}*/

		//R-type
		// add $t0, $t1, $t2  (rd=8, rs=9, rt=10, funct=0x20)
		int addInstruction = 0x012A4020;
		// sub $s0, $s1, $s2  (rd=16, rs=17, rt=18, funct=0x22)
		int subInstruction = 0x02328022;
		// and $t3, $t4, $t5  (rd=11, rs=12, rt=13, funct=0x24)
		int andInstruction = 0x018D5824;
		// or $t6, $t7, $t8   (rd=14, rs=15, rt=16, funct=0x25)
		int orInstruction = 0x01F07025;
		// slt $s3, $s4, $s5  (rd=19, rs=20, rt=21, funct=0x2A)
		int sltInstruction = 0x0295982A;

		//I-type
		// addi $t0, $t1, 100  (opcode=0x08, rs=9, rt=8, imm=100)
		int addiInstruction = 0x21280064;
		// lw $t2, 4($t3)      (opcode=0x23, rs=11, rt=10, offset=4)
		int lwInstruction = 0x8D6A0004;
		// sw $t4, 8($t5)      (opcode=0x2B, rs=13, rt=12, offset=8)
		int swInstruction = 0xADAC0008;
		// beq $t0, $t1, 16    (opcode=0x04, rs=8, rt=9, offset=16)
		int beqInstruction = 0x11090010;
		// bne $s0, $s1, -8    (opcode=0x05, rs=16, rt=17, offset=-8)
		int bneInstruction = 0x1611FFF8;

		//J-type
		// j 0x00400000        (opcode=0x02, target=0x00400000)
		int jInstruction = 0x08100000;
		// jal 0x00400020      (opcode=0x03, target=0x00400020)
		int jalInstruction = 0x0C100008;

		// Test R-type instruction
		Instruction addInst = new RTypeInstruction(0x00, 0x012A4020);
		addInst.decodeFields(); // Should decode rs=9, rt=10, rd=8, funct=0x20
		System.out.println(addInst.toString());

// Test I-type instruction
		Instruction addiInst = new ITypeInstruction(0x08, 0x21280064);
		addiInst.decodeFields(); // Should decode rs=9, rt=8, immediate=100
		System.out.println(addiInst.toString());

// Test memory instruction
		Instruction lwInst = new ITypeInstruction(0x23, 0x8D6A0004);
		lwInst.decodeFields(); // Should decode rs=11, rt=10, offset=4
		System.out.println(lwInst.toString());

// Test branch instruction
		Instruction beqInst = new ITypeInstruction(0x04, 0x11090010);
		beqInst.decodeFields(); // Should decode rs=8, rt=9, offset=16
		System.out.println(beqInst.toString());

// Test jump instruction
		Instruction jInst = new JTypeInstruction(0x02, 0x08100000);
		jInst.decodeFields(); // Should decode target address
		System.out.println(jInst.toString());
	}
}