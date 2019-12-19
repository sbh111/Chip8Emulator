/*
Author = Saad Bhatti
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class Main extends Application{

    int WINDOW_W = 800;
    int WINDOW_H = 400;
    float offset = WINDOW_W/25; //offset from top and side edges

    float blockW = WINDOW_W/100;
    float blockH = WINDOW_H/40;
    float pixelW = 64 * blockW;
    float pixelH = 32 * blockH;
    float pixelX = offset;
    float pixelY = offset;

    float valuesW = 20 * WINDOW_W/100;
    float valuesH = pixelH;
    float valuesX = WINDOW_W - (valuesW + offset);
    float valuesY = pixelY;



    public static void main(String args[])  {
        launch(args);
    }

    public void drawGfx(char[] frameBuffer, GraphicsContext gc){
        for(int i = 0; i < 64; i++){
            for(int j = 0; j < 32; j++){
                float x = (i * blockW) + offset;
                float y = (j * blockH) + offset;

                int loc = i*32 + j;
                if((int)frameBuffer[loc] == 1){
                    gc.setFill(Color.WHITE);
                }
                else{
                    gc.setFill(Color.MIDNIGHTBLUE);
                }
                gc.fillRect(x, y, blockW, blockH);
            }
        }
    }

    public void loadMap(HashMap<String, Integer> keyMap){
        /*
         * Keypad is set up like so:
         * "1", "2", "3", "4"
         * "Q", "W", "E", "R"
         * "A", "S", "D", "F"
         * "Z", "X", "C", "V"
         */
        keyMap.put("DIGIT1", 1);
        keyMap.put("DIGIT2", 2);
        keyMap.put("DIGIT3", 3);
        keyMap.put("DIGIT4", 12);
        keyMap.put("Q", 4);
        keyMap.put("W", 5);
        keyMap.put("E", 6);
        keyMap.put("R", 13);
        keyMap.put("A", 7);
        keyMap.put("S", 8);
        keyMap.put("D", 9);
        keyMap.put("F", 14);
        keyMap.put("Z", 10);
        keyMap.put("X", 0);
        keyMap.put("C", 11);
        keyMap.put("V", 15);
    }
    public void start(Stage primaryStage) {
        //main code here

        Chip8 myChip8 = new Chip8();
        HashMap <String, Integer> keyMap = new HashMap<>();
        loadMap(keyMap);
        boolean keys[] = new boolean[16];


        primaryStage.setTitle("Chip-8 Emulator");
        Group root = new Group();
        Scene scene = new Scene(root, WINDOW_W, WINDOW_H, Color.DARKGRAY);
        primaryStage.setScene(scene);

        //make 2 canvases, one with gfx, other with memory values.
        Canvas pixels = new Canvas(WINDOW_W, WINDOW_H);
        Canvas values = new Canvas(WINDOW_W, WINDOW_H);

        root.getChildren().add(pixels);
        root.getChildren().add(values);


        FileChooser fc = new FileChooser();
        Label label = new Label("no files selected");
        Button button = new Button("Select ROM");
        ToggleButton pauseButton = new ToggleButton("Pause ");

        pauseButton.setOnAction(
                e ->{
                    if(pauseButton.isSelected()){
                        pauseButton.setText("Paused");
                        myChip8.paused = true;
                    }
                    else{
                        pauseButton.setText("Pause ");
                        myChip8.paused = false;
                    }
                }
        );

        button.setOnAction(
                e ->{
                    // get the file selected
                    File file = fc.showOpenDialog(primaryStage);

                    if (file != null) {

                        label.setText(file.getName()
                                + " selected");


                        myChip8.reset();
                        try {
                            myChip8.loadROM(file.getAbsolutePath(), true);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
        );
        BorderPane bp = new BorderPane();
        HBox hbox = new HBox(20, button, pauseButton, label);
        bp.setCenter(hbox);
        root.getChildren().add(bp);


        ArrayList<String> input = new ArrayList<>();
        scene.setOnKeyPressed(
                e -> {
                    String code = e.getCode().toString();
                    if ( !input.contains(code) )
                        input.add( code );
                    if(keyMap.containsKey(code)){
                        keys[keyMap.get(code)] = true;
                    }
                });
        scene.setOnKeyReleased(
                e -> {
                    String code = e.getCode().toString();
                    input.remove( code );
                    if(keyMap.containsKey(code)){
                        keys[keyMap.get(code)] = false;
                    }
                });


        AnimationTimer ani = new AnimationTimer() {
            GraphicsContext pixelsGC = pixels.getGraphicsContext2D();
            GraphicsContext valuesGC = values.getGraphicsContext2D();

            public void handle(long currentNanoTime) {

                if(!myChip8.paused){
                    //emulate 1 cycle
                    myChip8.emulateCycle(keys);
                }


                //build the emulator values string
                StringBuilder bldr = new StringBuilder();
                bldr.append(String.format("OpCode:\t0x%s\n", Integer.toHexString(0xffff & myChip8.getOpCode())));
                bldr.append(String.format("PC:\t0x%s\n", Integer.toHexString(myChip8.getPc())));
                bldr.append(String.format("I:\t0x%s\n", Integer.toHexString(myChip8.getI())));

                for (int i = 0; i < 16; i++) {
                    bldr.append(String.format("V%d:\t0x%s\n", i, Integer.toHexString(myChip8.getRegister(i))));
                }

                bldr.append(String.format("DelayTimer:\t%d\n", (int) myChip8.getDelayTimer()));
                bldr.append(String.format("SoundTimer:\t%d\n", (int) myChip8.getSoundTimer()));
                String valuesTxt = bldr.toString();


                //clear the canvases
                valuesGC.setFill(Color.MIDNIGHTBLUE);
                valuesGC.fillRect(valuesX, valuesY, valuesW, valuesH);
                valuesGC.setFill(Color.BLACK);
                valuesGC.setLineWidth(5);
                valuesGC.strokeRect(valuesX, valuesY, valuesW, valuesH);

                //Draw the emulator values
                valuesGC.setFill(Color.WHITE);
                valuesGC.setFont(new Font("Arial", 13));
                valuesGC.fillText(valuesTxt, valuesX + 1, valuesY + WINDOW_H / 40 + 1, valuesW);

                //Draw the emulator graphics
                drawGfx(myChip8.getFramebuffer(), pixelsGC);

                pixelsGC.setFill(Color.BLACK);
                pixelsGC.setLineWidth(5);
                pixelsGC.strokeRect(pixelX, pixelY, pixelW, pixelH);
            }
        };

        ani.start();
        primaryStage.show();
    }//end start class
}//end Main class
