package model.memory;

public class DataMemory {

    private byte[] data;

    public DataMemory() {}

    public DataMemory(byte[] data) {
        this.data = data;
    }

    public int readWord(int address) {
        return data[address] & 0xff;
    }

    public void writeWord(int address, int value) {
        data[address] = (byte) (value & 0xff);
    }

    public byte[] getData() {
        return data;
    }
}
