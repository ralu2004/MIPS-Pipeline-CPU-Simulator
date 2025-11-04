package model;

public class RegisterFile {

    private int[] registers;

    public RegisterFile() {}

    public RegisterFile(int[] registers) {
        this.registers = registers;
    }

    public int readReg(int regNum) {
        return registers[regNum];
    }

    public void writeReg(int regNum, int value) {
        registers[regNum] = value;
    }

    public int[] getRegisters() {
        return registers;
    }
}
