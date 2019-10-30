/*
Author: Saad Bhatti
 */

import java.io.*;
import java.util.Stack;

public class Chip8 {


    //FIXME: The stack should probably be like an array or something
    private Stack<Integer> stack;
    private short pc;
    private short I;
    private byte sp;
    private byte memory[];
    private byte registers[];
    private byte framebuffer[];
    private int soundTimer;
    private int delayTimer;



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

    }

    public void loadROM(String filename) throws IOException {
        //load ROM into memory starting from 0x200 to 0xfff

        FileInputStream fin = new FileInputStream(filename);
        DataInputStream din = new DataInputStream(fin);
        int i = 0;
        while(din.available() > 0){
            short s = din.readShort();


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
