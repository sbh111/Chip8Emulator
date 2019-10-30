/*
Author = Saad Bhatti
 */

import java.io.IOException;

public class Main {
    public static void main(String args[]) throws IOException {

        System.out.println("|====== Chip-8 Emulator ======|\n");
        Chip8 myChip8 = new Chip8();
        String FILE_PATH = "res/maze.ch8";
        myChip8.loadROM(FILE_PATH);
    }
}
