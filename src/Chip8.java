/*
Author: Saad Bhatti
 */

import java.io.*;
import java.util.Arrays;
import java.util.Stack;

public class Chip8 {

    private Stack<Short> stack;
    private short pc;
    private short I;
    private char memory[];
    private char registers[];
    private char framebuffer[];
    private char soundTimer;
    private char delayTimer;

    private final int fontSet[] = {
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
    public final int DRAW_FLAG = 0x0001;       //tells emu to draw stuff in frame buffer.
    public final int PRESS_KEY_FLAG = 0x0010;  //tells emu to wait for a key press.


    public void reset(){
        stack.clear();
        Arrays.fill(memory, (char)0x0);
        Arrays.fill(registers, (char)0x0);
        Arrays.fill(framebuffer, (char)0x0);
        pc = 0x200;
        I = 0;
        soundTimer = 0;
        delayTimer = 0;
        int i = 0;
        for (int f : fontSet) {
            memory[i++] = (char) f;
        }
    }

    public Chip8() {
        // initialize variables
        stack = new Stack<>();
        memory = new char[4096];
        registers = new char[16];
        framebuffer = new char[64 * 32];
        pc = 0x200;
        I = 0;
        soundTimer = 0;
        delayTimer = 0;

        // load fontset from 0x000 to 0x080 in memory
        int i = 0;
        for (int f : fontSet) {
            memory[i++] = (char) f;
        }
    }

    public char[] getFramebuffer(){
        return framebuffer;
    }
    public short getPc() {
        return pc;
    }
    public short getI() {
        return I;
    }
    public char getRegister(int i){
        return registers[i];
    }
    public char getSoundTimer(){
        return soundTimer;
    }
    public char getDelayTimer(){
        return delayTimer;
    }
    public short getOpCode(){
        return (short) (memory[pc] << 8 | memory[pc + 1]);
    }

    public void loadROM(String filename, boolean memOut) throws IOException {
        // load ROM into memory starting from 0x200 to 0xfff

        // read the shorts from the chip8 file
        FileInputStream fin = new FileInputStream(filename);
        DataInputStream din = new DataInputStream(fin);
        int i = 0;
        while (din.available() > 0) {
            short s = din.readShort();

            // break it up into bytes and store in memory
            char firstByte = (char) (((s & 0xff00) >> 8));
            char secondByte = (char) ((s & 0x00ff));

            memory[0x200 + i++] = firstByte;
            memory[0x200 + i++] = secondByte;
        }
        din.close();


        //See what's in memory
        if(memOut){
            System.out.println("Memory:");
            for(int j = 0; j < i; j++){
                int memoryLoc = 0x200 + j;
                String val = Integer.toHexString(memory[memoryLoc]);
                String text = "0x" + Integer.toHexString(memoryLoc) + ": 0x" + val;
                System.out.println(text);
            }
        }
    }// end loadROM


    private int execOp(boolean keys[]) {
        // decode the opcode and perform the instruction

        boolean incrPC = true;
        int flag = 0x0000;
        short opcode = (short) (memory[pc] << 8 | memory[pc + 1]);
        switch (opcode & (short) 0xf000) {

            case (short) 0x0000:
                if (opcode == (short) 0x00e0) {
                    // cls:
                    // clear display
                    Arrays.fill(framebuffer, (char)0x0);
                    flag |= DRAW_FLAG;

                } else if (opcode == (short) 0x00ee) {
                    // ret:
                    // Return from a subroutine.
                    // The interpreter sets the program counter to the address at the top of the stack,
                    // then subtracts 1 from the stack pointer.
                    pc = stack.pop();
                    //pc += 2 automatically
                } else if(opcode == (short) 0x0){
                    //nop
                    incrPC = false;
                }
                break;


            case (short) 0x1000:
                // 1nnn - JP addr:
                // Jump to location nnn.
                // The interpreter sets the program counter to nnn.

                incrPC = false;
                pc = (short)(opcode & 0x0fff);
                break;


            case (short) 0x2000:
                // 2nnn - CALL addr:
                // Call subroutine at nnn.
                // The interpreter increments the stack pointer,
                // then puts the current PC on the top of the stack.
                // The PC is then set to nnn

                incrPC = false;
                stack.push(pc);
                pc = (short)(opcode & 0x0fff);
                break;


            case (short) 0x3000:
                // 3xkk - SE Vx, byte:
                // Skip next instruction if Vx = kk.
                // The interpreter compares register Vx to kk, and if they are equal,
                // increments the program counter by 2 instructions == 4.

                incrPC = false;
                if(registers[(opcode & 0x0f00) >> 8] == (opcode & (short)0x00ff)){
                    pc += 4;
                }
                else{
                    pc += 2;
                }

                break;


            case (short) 0x4000:
                // 4xkk - SNE Vx, byte:
                // Skip next instruction if Vx != kk.
                // The interpreter compares register Vx to kk, and if they are not equal,
                // increments the program counter by 2 instructions == 4.

                incrPC = false;
                if(registers[(opcode & 0x0f00) >> 8] != (opcode & (short)0x00ff)){
                    pc += 4;
                }
                else{
                    pc += 2;
                }
                break;


            case (short) 0x5000:
                // 5xy0 - SE Vx, Vy:
                // Skip next instruction if Vx = Vy.
                // The interpreter compares register Vx to register Vy, and if they are equal,
                // increments the program counter by 2 full instructions, == 4.

                incrPC = false;
                if(registers[(opcode & 0x0f00) >> 8] == registers[(opcode & 0x00f0) >> 4]){
                    pc += 4;
                }
                else{
                    pc += 2;
                }
                break;


            case (short) 0x6000:
                // 6xkk - LD Vx, byte:
                // Set Vx = kk.
                // The interpreter puts the value kk into register Vx.
                registers[(opcode & 0x0F00) >> 8] = (char)(opcode & 0x00FF);
                break;


            case (short) 0x7000:
                // 7xkk - ADD Vx, byte:
                // Set Vx = Vx + kk.
                // Adds the value kk to the value of register Vx,
                // then stores the result in Vx.
                registers[(opcode & 0x0F00) >> 8] += (char)(opcode & 0x00FF);
                break;


            case (short) 0x8000:
                switch (opcode & (short) 0x000f) {
                    case (short) 0x0000:
                        // 8xy0 - LD Vx, Vy:
                        // Set Vx = Vy. Stores the value of register Vy in register Vx.
                        registers[(opcode & 0x0F00) >> 8] = registers[(opcode & 0x00F0) >> 4];
                        break;


                    case (short) 0x0001:
                        // 8xy1 - OR Vx, Vy:
                        // Set Vx = Vx OR Vy. Performs a bitwise OR on the values of Vx and Vy, then stores the result in Vx. A
                        // bitwise OR compares the corresponding bits from two values, and if either bit is 1, then the same bit in the
                        // result is also 1. Otherwise, it is 0.
                        registers[(opcode & 0x0F00) >> 8] |= registers[(opcode & 0x00F0) >> 4];
                        break;


                    case (short) 0x0002:
                        // 8xy2 - AND Vx, Vy:
                        // Set Vx = Vx AND Vy. Performs a bitwise AND on the values of Vx and Vy, then stores the result in Vx.
                        // A bitwise AND compares the corresponding bits from two values, and if both bits are 1, then the same bit
                        // in the result is also 1. Otherwise, it is 0.
                        registers[(opcode & 0x0F00) >> 8] &= registers[(opcode & 0x00F0) >> 4];
                        break;


                    case (short) 0x0003:
                        // 8xy3 - XOR Vx, Vy
                        // Set Vx = Vx XOR Vy. Performs a bitwise exclusive OR on the values of Vx and Vy, then stores the result
                        // in Vx. An exclusive OR compares the corresponding bits from two values, and if the bits are not both the
                        // same, then the corresponding bit in the result is set to 1. Otherwise, it is 0.
                        registers[(opcode & 0x0F00) >> 8] ^= registers[(opcode & 0x00F0) >> 4];
                        break;


                    case (short) 0x0004:
                        // 8xy4 - ADD Vx, Vy
                        // Set Vx = Vx + Vy, set VF = carry. The values of Vx and Vy are added together. If the result is greater
                        // than 8 bits (i.e., ¿ 255,) VF is set to 1, otherwise 0. Only the lowest 8 bits of the result are kept, and stored
                        // in Vx.
                        registers[(opcode & 0x0F00) >> 8] += registers[(opcode & 0x00F0) >> 4];
                        if(registers[(opcode & 0x00F0) >> 4] > (0xFF - registers[(opcode & 0x0F00) >> 8]))
                            registers[0xF] = 1; //carry
                        else
                            registers[0xF] = 0;
                        break;


                    case (short) 0x0005:
                        // 8xy5 - SUB Vx, Vy
                        // Set Vx = Vx - Vy, set VF = NOT borrow. If Vx ¿ Vy, then VF is set to 1, otherwise 0. Then Vy is
                        // subtracted from Vx, and the results stored in Vx.
                        if(registers[(opcode & 0x00F0) >> 4] > registers[(opcode & 0x0F00) >> 8])
                            registers[0xF] = 0; // there is a borrow
                        else
                            registers[0xF] = 1;
                        registers[(opcode & 0x0F00) >> 8] -= registers[(opcode & 0x00F0) >> 4];
                        break;


                    case (short) 0x0006:
                        // 8xy6 - SHR Vx {, Vy}
                        // Set Vx = Vx SHR 1. If the least-significant bit of Vx is 1, then VF is set to 1, otherwise 0. Then Vx is
                        // divided by 2.
                        registers[0xF] = (char)(registers[(opcode & 0x0F00) >> 8] & 0x1);
                        registers[(opcode & 0x0F00) >> 8] >>= 1;
                        break;


                    case (short) 0x0007:
                        // 8xy7 - SUBN Vx, Vy
                        // Set Vx = Vy - Vx, set VF = NOT borrow. If Vy ¿ Vx, then VF is set to 1, otherwise 0. Then Vx is
                        // subtracted from Vy, and the results stored in Vx
                        if(registers[(opcode & 0x0F00) >> 8] > registers[(opcode & 0x00F0) >> 4])	// VY-VX
                            registers[0xF] = 0; // there is a borrow
                        else
                            registers[0xF] = 1;
                        registers[(opcode & 0x0F00) >> 8] = (char)(registers[(opcode & 0x00F0) >> 4] - registers[(opcode & 0x0F00) >> 8]);
                        break;


                    case (short) 0x000e:
                        // 8xyE - SHL Vx {, Vy}
                        // Set Vx = Vx SHL 1. If the most-significant bit of Vx is 1, then VF is set to 1, otherwise to 0. Then Vx is
                        // multiplied by 2.
                        registers[0xF] = (char)(registers[(opcode & 0x0F00) >> 8] >> 7);
                        registers[(opcode & 0x0F00) >> 8] <<= 1;
                        break;
                }
                break;


            case (short) 0x9000:
                // 9xy0 - SNE Vx, Vy
                // Skip next instruction if Vx != Vy. The values of Vx and Vy are compared, and if they are not equal, the
                // program counter is increased by 2.

                incrPC = false;
                if (registers[(opcode & 0x0F00) >> 8] != registers[(opcode & 0x00F0) >> 4])
                    pc += 4;
                else
                    pc += 2;
                break;


            case (short) 0xa000:
                // Annn - LD I, addr
                // Set I = nnn. The value of register I is set to nnn.
                I = (short)(opcode & 0x0FFF);
                break;


            case (short) 0xb000:
                // Bnnn - JP V0, addr
                // Jump to location nnn + V0. The program counter is set to nnn plus the value of V0.

                incrPC = false;
                pc = (short)((opcode & 0x0FFF) + registers[0]);
                break;


            case (short) 0xc000:
                // Cxkk - RND Vx, byte
                // Set Vx = random byte AND kk. The interpreter generates a random number from 0 to 255, which is then
                // ANDed with the value kk. The results are stored in Vx. See instruction 8xy2 for more information on AND.

                int randInt = (int )(Math.random() * 50 + 1);
                registers[(opcode & 0x0F00) >> 8] = (char)((randInt % (0xFF + 1)) & (opcode & 0x00FF));
                break;


            case (short) 0xd000:
                // Dxyn - DRW Vx, Vy, nibble
                // Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision. The interpreter reads n
                // bytes from memory, starting at the address stored in I. These bytes are then displayed as sprites on screen
                // at coordinates (Vx, Vy). Sprites are XOR’d onto the existing screen. If this causes any pixels to be erased,
                // VF is set to 1, otherwise it is set to 0. If the sprite is positioned so part of it is outside the coordinates of
                // the display, it wraps around to the opposite side of the screen.

                flag |= DRAW_FLAG;
                short x = (short)registers[(opcode & 0x0F00) >> 8];
                short y = (short)registers[(opcode & 0x00F0) >> 4];
                short height = (short)(opcode & 0x000F);
                short pixel;

                registers[0xF] = 0;
                for (int yline = 0; yline < height; yline++)
                {
                    pixel = (short)memory[I + yline];
                    for(int xline = 0; xline < 8; xline++)
                    {
                        if((pixel & (0x80 >> xline)) != 0)
                        {
                            if(framebuffer[(x + xline + ((y + yline) * 64))] == 1)
                            {
                                registers[0xF] = 1;
                            }
                            framebuffer[x + xline + ((y + yline) * 64)] ^= 1;
                        }
                    }
                }
                break;


            case (short) 0xe000:
                if ((opcode & (short) 0x00ff) == (short) 0x009e) {
                    // Ex9E - SKP Vx
                    // Skip next instruction if key with the value of Vx is pressed. Checks the keyboard, and if the key corresponding
                    // to the value of Vx is currently in the down position, PC is increased by 2

                    incrPC = false;
                    if (keys[registers[(opcode & 0x0F00) >> 8]] != false)
                        pc +=  4;
                    else
                        pc += 2;

                } else if ((opcode & (short) 0x00ff) == (short) 0x00a1) {
                    // ExA1 - SKNP Vx
                    // Skip next instruction if key with the value of Vx is not pressed. Checks the keyboard, and if the key
                    // corresponding to the value of Vx is currently in the up position, PC is increased by 2.

                    incrPC = false;
                    if (keys[registers[(opcode & 0x0F00) >> 8]] == false)
                        pc +=  4;
                    else
                        pc += 2;
                }
                break;


            case (short) 0xf000:
                switch (opcode & (short) 0x00ff) {
                    case (short) 0x0007:
                        // Fx07 - LD Vx, DT
                        // Set Vx = delay timer value. The value of DT is placed into Vx
                        registers[(opcode & 0x0F00) >> 8] = delayTimer;
                        break;


                    case (short) 0x000a:
                        // Fx0A - LD Vx, K
                        // Wait for a key press, store the value of the key in Vx. All execution stops until a key is pressed, then the
                        // value of that key is stored in Vx.
                        int key = -1;
                        for(int i = 0; i < 16; i++){
                            if(keys[i] == true){
                                key = i;
                                registers[(opcode & 0x0F00) >> 8] = (char)key;
                                break;
                            }
                        }

                        if(key == -1){
                            incrPC = false;
                            flag |= PRESS_KEY_FLAG;
                        }
                        break;


                    case (short) 0x0015:
                        // Fx15 - LD DT, Vx
                        // Set delay timer = Vx. Delay Timer is set equal to the value of Vx.
                        delayTimer = registers[(opcode & 0x0F00) >> 8];
                        break;


                    case (short) 0x0018:
                        // Fx18 - LD ST, Vx
                        // Set sound timer = Vx. Sound Timer is set equal to the value of Vx.
                        soundTimer = registers[(opcode & 0x0F00) >> 8];
                        break;


                    case (short) 0x001e:
                        // Fx1E - ADD I, Vx
                        // Set I = I + Vx. The values of I and Vx are added, and the results are stored in I.
                        if(I + registers[(opcode & 0x0F00) >> 8] > 0xFFF)
                            registers[0xF] = 1;
                        else
                            registers[0xF] = 0;
                        I += registers[(opcode & 0x0F00) >> 8];
                        break;


                    case (short) 0x0029:
                        // Fx29 - LD F, Vx
                        // Set I = location of sprite for digit Vx. The value of I is set to the location for the hexadecimal sprite
                        // corresponding to the value of Vx. See section 2.4, Display, for more information on the Chip-8 hexadecimal
                        // font. To obtain this value, multiply VX by 5 (all font data stored in first 80 bytes of memory).
                        I = (short)(registers[(opcode & 0x0F00) >> 8] * 0x5);
                        break;


                    case (short) 0x0033:
                        // Fx33 - LD B, Vx
                        // Store BCD representation of Vx in memory locations I, I+1, and I+2. The interpreter takes the decimal
                        // value of Vx, and places the hundreds digit in memory at location in I, the tens digit at location I+1, and
                        // the ones digit at location I+2.
                        memory[I] = (char)(registers[(opcode & 0x0F00) >> 8] / 100);
                        memory[I + 1] = (char)((registers[(opcode & 0x0F00) >> 8] / 10) % 10);
                        memory[I + 2] = (char)(registers[(opcode & 0x0F00) >> 8] % 10);
                        break;


                    case (short) 0x0055:
                        // Fx55 - LD [I], Vx
                        // Stores V0 to VX in memory starting at address I. I is then set to I + x + 1.
                        for (int i = 0; i <= ((opcode & 0x0F00) >> 8); ++i)
                            memory[I + i] = registers[i];

                        I += ((opcode & 0x0F00) >> 8) + 1;
                        break;


                    case (short) 0x0065:
                        // Fx65 - LD Vx, [I]
                        // Fills V0 to VX with values from memory starting at address I. I is then set to I + x + 1.
                        for (int i = 0; i <= ((opcode & 0x0F00) >> 8); ++i)
                            registers[i] = memory[I + i];

                        I += ((opcode & 0x0F00) >> 8) + 1;
                        break;
                }
                break;

            default:
                System.out.println("Error, opcode " + opcode + " not recognized");
                incrPC = false;
                break;
        }// end switch

        if (incrPC)
            pc += 2;

        return flag;
    }// end execOp

    public int emulateCycle(boolean keys[]) {
        int out = execOp(keys);
        if(out != PRESS_KEY_FLAG){
            delayTimer = (char)(delayTimer > 0 ? delayTimer - 1 : delayTimer);
            soundTimer = (char)(soundTimer > 0 ? soundTimer - 1 : soundTimer);
        }
        return out;
    }
}
