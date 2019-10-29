/*
Author = Saad Bhatti
 */

import java.io.IOException;

public class Main {
    public static void main(String args[]) throws IOException {

        System.out.println("Chip-8 Emulator");
        Chip8 myChip8 = new Chip8();
        String FILE_PATH = "res/maze.txt";
        myChip8.loadROM(FILE_PATH);
    }
}
