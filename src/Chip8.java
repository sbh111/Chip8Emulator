/*
Author: Saad Bhatti
 */

import jdk.internal.cmm.SystemResourcePressureImpl;

import java.io.*;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.Stack;

public class Chip8 {


    //FIXME: The stack should probably be like an array or something
    private Stack<Integer> stack;
    private short pc;
    private short I;
    private char sp;
    private char memory[];
    private char registers[];
    private char framebuffer[];
    private int soundTimer;
    private int delayTimer;



    public Chip8(){
        //initialize variables
        stack = new Stack<>();
        memory = new char[4096];
        registers = new char[16];
        framebuffer = new char[64 * 32];
        pc = 0x200;
        sp = 0;
        I = 0;
        soundTimer = 0;
        delayTimer = 0;

        //load fontset from 0x000 to 0x080 in memory

    }

    public void loadROM(String filename) throws IOException {
        //load ROM into memory starting from 0x200 to 0xfff

        File file = new File(filename);

        InputStream in = new FileInputStream(file);
        Reader reader = new InputStreamReader(in, Charset.defaultCharset());
        Reader buffReader = new BufferedReader(reader);
        int r;
        while((r = buffReader.read()) != -1){
            char ch = (char)r;
            System.out.println(ch);
            //System.out.println(Integer.toHexString(r));
        }


    }//end loadROM

    public void emulateCycle(){
        //perform one cycle
    }

}
