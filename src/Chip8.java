/*
Author: Saad Bhatti
 */

import java.io.*;
import java.util.Stack;

public class Chip8 {


    //FIXME: The stack should probably be like an array or something
    private Stack<Short> stack;
    private short pc;
    private short I;
    private byte sp;
    private byte memory[];
    private byte registers[];
    private byte framebuffer[];
    private byte soundTimer;
    private byte delayTimer;



    public Chip8(){
        //initialize variables
        stack = new Stack<>();
        memory = new byte[4096];
        registers = new byte[16];
        framebuffer = new byte[64 * 32];
        pc = 0x200;
        sp = 0;
        I = 0;
        soundTimer = 0;
        delayTimer = 0;

        //load fontset from 0x000 to 0x080 in memory
        int fontSet[] = {
                0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        };
        int i = 0;
        for(int f : fontSet){
            memory[i++] = (byte)f;
        }


    }

    public void loadROM(String filename) throws IOException {
        //load ROM into memory starting from 0x200 to 0xfff

        //read the shorts from the chip8 file
        FileInputStream fin = new FileInputStream(filename);
        DataInputStream din = new DataInputStream(fin);
        int i = 0;
        while(din.available() > 0){
            short s = din.readShort();

            //break it up into bytes and store in memory
            byte firstByte = (byte)(((s & 0xff00) >> 8));
            byte secondByte = (byte)((s & 0x00ff));

            memory[0x200 + i++] = firstByte;
            memory[0x200 + i++] = secondByte;
        }
        din.close();


        //see what's in memory
        System.out.println("Memory:");
        for(int j = 0; j < i; j++){
            int memoryLoc = 0x200 + j;
            String val = Integer.toHexString(memory[memoryLoc] & 0xff);
            String text = "0x" + Integer.toHexString(memoryLoc) + ": 0x" + val;
            System.out.println(text);
        }
    }//end loadROM


    public void emulateCycle(){
        //perform one cycle
    }

}
