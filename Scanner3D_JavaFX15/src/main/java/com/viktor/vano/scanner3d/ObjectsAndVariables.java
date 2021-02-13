package com.viktor.vano.scanner3d;

import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.maths.Coord3d;

import java.io.File;

public class ObjectsAndVariables {
    public static SerialPort[] serialPorts;
    public static ChoiceBox<String> choiceBoxSerialPorts;
    public static Button btnConnect;
    public static Label labelFile, labelFileSize, labelFileSizeBar, labelFlashProgress;
    public static FileChooser fileChooser;
    public static Stage stageReference;
    public static File file;
    public static long fileSize = 0, readChars = 0;
    public static ProgressBar progressBarMemory, progressBarFlashedApp;
    public static byte[] binaryContent;

    public static Button btnRender, btnScan;
    public static final BorderPane borderPane = new BorderPane();
    public static final Pane paneCenter = new Pane();
    public static final HBox hBoxBottom = new HBox();
    public static ImageView imageView;
    public static AWTChart chart;
    public static JavaFXChartFactory factory;
    public static Scene scene;
    public static Coord3d[] points;
    public static int pitchMax = 90, yawMax = 180;
    public static int pitch = pitchMax-1;
    public static int yaw = 0;
    public static int x = 0;
    public static boolean direction = true;
    public static Label labelProgress;
    public static ProgressBar progressBar;
    public static TextField textFieldPitchMax;
    public static int distance = 0;
    public static boolean getNextMeasurement = true, measureFlag = false;
    public static final int maxSensorRange = 4000;
}
