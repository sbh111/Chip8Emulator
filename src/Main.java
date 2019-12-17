/*
Author = Saad Bhatti
 */

import java.io.IOException;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application{

    int WINDOW_W = 800;
    int WINDOW_H = 400;

    float pixelW = 64 * WINDOW_W/100;
    float pixelH = 32 * WINDOW_H/40;
    float pixelX = WINDOW_W/20;
    float pixelY = WINDOW_H/20;

    float valuesW = 20 * WINDOW_W/100;
    float valuesH = pixelH;
    float valuesX = WINDOW_W - (valuesW + WINDOW_W/20);
    float valuesY = pixelY;



    public static void main(String args[])  {
        launch(args);
    }

    public void start(Stage primaryStage) throws IOException{
        //main code here
        Chip8 myChip8 = new Chip8();
        myChip8.loadROM("res/stars.ch8", true);

        primaryStage.setTitle("Chip-8 Emulator");

        Group root = new Group();
        Scene scene = new Scene(root, WINDOW_W, WINDOW_H, Color.DARKGRAY);
        primaryStage.setScene(scene);

        //make 2 canvases, one with gfx, other with memory values.
        Canvas pixels = new Canvas(WINDOW_W, WINDOW_H);
        Canvas values = new Canvas(WINDOW_W, WINDOW_H);

        root.getChildren().add(pixels);
        root.getChildren().add(values);

        boolean keys[] = new boolean[16];


        //Now Graphics
        GraphicsContext pixelsGC = pixels.getGraphicsContext2D();
        GraphicsContext valuesGC = values.getGraphicsContext2D();

        final long startNanoTime = System.nanoTime();
        new AnimationTimer(){
            public void handle(long currentNanoTime){
                double elsapsedTime = (currentNanoTime - startNanoTime) / 1000000000.0;

                myChip8.emulateCycle(keys);
                StringBuilder bldr = new StringBuilder();

                bldr.append(String.format("OpCode:\t0x%s\n", Integer.toHexString(0xffff & myChip8.getOpCode())));
                bldr.append(String.format("PC:\t0x%s\n", Integer.toHexString(myChip8.getPc())));
                bldr.append(String.format("I:\t0x%s\n", Integer.toHexString(myChip8.getI())));

                for(int i = 0; i < 16; i++){
                    bldr.append(String.format("V%d:\t0x%s\n", i, Integer.toHexString(myChip8.getRegister(i))));
                }

                bldr.append(String.format("DelayTimer:\t%d\n", (int)myChip8.getDelayTimer()));
                bldr.append(String.format("SoundTimer:\t%d\n", (int)myChip8.getSoundTimer()));
                String valuesTxt = bldr.toString();


                //clear the canvases
                valuesGC.setFill(Color.MIDNIGHTBLUE);
                valuesGC.fillRect(valuesX, valuesY, valuesW, valuesH);
                valuesGC.setFill(Color.BLACK);
                valuesGC.strokeRect(valuesX, valuesY, valuesW, valuesH);



                pixelsGC.setFill(Color.MIDNIGHTBLUE);
                pixelsGC.fillRect(pixelX, pixelY, pixelW, pixelH);

                valuesGC.setFill(Color.WHITE);
                valuesGC.setFont(new Font("Arial", 13));
                valuesGC.fillText(valuesTxt, valuesX + 1, valuesY + WINDOW_H/40 + 1, valuesW);
            }
        }.start();
        primaryStage.show();
    }
}
