package com.viktor.vano.scanner3d;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.jzy3d.chart.AWTChart;
import org.jzy3d.chart.controllers.mouse.AWTDualModeMouseSelector;
import org.jzy3d.chart.controllers.mouse.selection.AWTScatterMouseSelector;
import org.jzy3d.colors.Color;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.selectable.SelectableScatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

import static com.viktor.vano.scanner3d.ObjectsAndVariables.*;
import static com.viktor.vano.scanner3d.SerialComPort.*;


public class App extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }
    private static Timeline timelineProgress, timelineRender;
    private static MyThread myThread;

    @Override
    public void start(Stage stage) {
        stage.setTitle("3D Scanner");
        points = new Coord3d[pitchMax*yawMax];
        stage.setMinWidth(800);
        stage.setMinHeight(800);

        // Jzy3d
        factory = new JavaFXChartFactory();
        chart  = getDemoChart(factory, "offscreen", pitch, yaw, pitchMax, yawMax, x);
        imageView = factory.bindImageView(chart);

        // JavaFX
        borderPane.setBottom(hBoxBottom);
        borderPane.setCenter(paneCenter);
        scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.show();
        paneCenter.getChildren().add(imageView);

        stage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                double size;
                size = stage.getWidth();
                stage.setHeight(size);
                stage.setWidth(size);
            }
        });

        stage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                double size;
                size = stage.getHeight();
                stage.setHeight(size);
                stage.setWidth(size);
            }
        });

        hBoxBottom.setPadding(new Insets(15, 50, 15, 50));
        hBoxBottom.setSpacing(30);
        hBoxBottom.setStyle("-fx-background-color: #336699;");

        factory.addSceneSizeChangedListener(chart, paneCenter);

        btnRender = new Button("Preview");
        btnRender.setOnAction(event -> {
            System.gc();//try to run garbage collector - jzy3D has a memory leak by generating 3D chart
            Runtime.getRuntime().gc();//try to run garbage collector
            System.runFinalization();//try to run garbage collector

            renderRoutine();
        });
        btnRender.setDisable(true);
        hBoxBottom.getChildren().add(btnRender);

        stage.setWidth(800);
        stage.setHeight(800);

        progressBar = new ProgressBar();
        progressBar.setMaxWidth(100);
        progressBar.setMinWidth(100);

        labelProgress = new Label("0%");

        timelineProgress = new Timeline(new KeyFrame(Duration.millis(200), event -> {
            if(x != pitchMax*yawMax)
            {
                progressBar.setProgress((double)x / ((double)pitchMax*(double)yawMax));
                labelProgress.setText(Math.round(((double)x / ((double)pitchMax*(double)yawMax))*100.0) + "%");
            }
        }));
        timelineProgress.setCycleCount(Timeline.INDEFINITE);

        timelineRender = new Timeline(new KeyFrame(Duration.millis(1), event -> {
            renderRoutine();
        }));
        timelineRender.setCycleCount(1);

        btnScan = new Button("Scan");
        btnScan.setOnAction(event -> {
            resetValues();
            myThread = new MyThread();
            myThread.start();
            timelineProgress.play();
            textFieldPitchMax.setDisable(true);
            btnScan.setDisable(true);
            btnRender.setDisable(false);
            measureFlag = true;
            getNextMeasurement = true;
        });
        btnScan.setDisable(true);
        hBoxBottom.getChildren().add(btnScan);
        hBoxBottom.getChildren().add(progressBar);
        hBoxBottom.getChildren().add(labelProgress);

        textFieldPitchMax = new TextField();
        textFieldPitchMax.setPromptText("Pitch");
        textFieldPitchMax.setMaxWidth(40);
        textFieldPitchMax.setText(String.valueOf(pitchMax));
        textFieldPitchMax.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                int number = 0;
                try{
                    if(textFieldPitchMax.getText().length() != 0)
                        number = Integer.parseInt(textFieldPitchMax.getText());
                }catch (Exception e)
                {
                    number = pitchMax;
                    textFieldPitchMax.setText(String.valueOf(pitchMax));
                }
                if(number < 1)
                    number = 1;
                if(number > 90)
                    number = 90;

                pitchMax = number;
                if(textFieldPitchMax.getText().length() != 0)
                    textFieldPitchMax.setText(String.valueOf(pitchMax));
                x = 0;
            }
        });
        hBoxBottom.getChildren().add(textFieldPitchMax);
        initializeComPortMenu();
    }

    public static void resetScan()
    {
        myThread.stopThread();
        System.out.println(x + " " + pitchMax*yawMax);
        measureFlag = false;
        textFieldPitchMax.setDisable(false);
        btnScan.setDisable(false);
        btnRender.setDisable(true);
        timelineRender.play();
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    private static void resetValues()
    {
        pitch = pitchMax-1;
        yaw = 0;
        x = 0;
        direction = true;
        points = new Coord3d[pitchMax*yawMax];
        progressBar.setProgress(0);
        labelProgress.setText("0%");
    }

    private static void renderRoutine(){
        paneCenter.getChildren().remove(imageView);
        Coord3d coord3d;
        try{
            coord3d = chart.getView().getViewPoint();
        }catch (Exception e){
            coord3d = new Coord3d();
        }

        imageView = null;
        chart = null;

        chart  = getDemoChart(factory, "offscreen", pitch, yaw, pitchMax, yawMax, x);
        chart.getView().setViewPoint(coord3d);
        imageView = factory.bindImageView(chart);

        paneCenter.getChildren().add(imageView);
        factory.addSceneSizeChangedListener(chart, paneCenter);
        double size;
        if(paneCenter.getWidth() < paneCenter.getHeight())
            size = paneCenter.getWidth();
        else
            size = paneCenter.getHeight();
        factory.resetSize(chart, size, size);
    }

    private static AWTChart getDemoChart(JavaFXChartFactory factory, String toolkit, int pitch, int yaw,
                                  int pitchMax, int yawMax, int arrayIndex) {
        Quality quality = Quality.Advanced;
        SelectableScatter scatter = getDataPoints(pitch, yaw, pitchMax, yawMax, arrayIndex);
        AWTChart chart = (AWTChart) factory.newChart(quality, toolkit);
        chart.getScene().add(scatter);
        chart.getView().setMaximized(true);
        chart.getView().setSquared(false);

        AWTScatterMouseSelector selector = new AWTScatterMouseSelector(scatter);
        AWTDualModeMouseSelector mouse = new AWTDualModeMouseSelector(chart, selector);

        return chart;
    }

    protected static SelectableScatter getDataPoints(int pitch, int yaw, int pitchMax, int yawMax, int arrayIndex) {
        Color[] colors;
        /*
        * x = r*sin(pitch)*cos(yaw);
        * y = r*sin(pitch)*sin(yaw);
        * z = r*cos(pitch);
        * (int)((yaw/2.0)-yaw) //range
        */
        Coord3d[] tempPoints;
        if(!getNextMeasurement && arrayIndex != 0)
            arrayIndex--;

        if(arrayIndex == 0)
        {
            tempPoints = new Coord3d[1];
            tempPoints[0] = new Coord3d();
            colors = new Color[1];
            colors[0] = new Color(0,0,0, 0);
        }else {
            tempPoints = new Coord3d[arrayIndex];
            colors = new Color[arrayIndex];
            for(int i=0; i<arrayIndex; i++)
            {
                tempPoints[i] = points[i];
                if(tempPoints[i].x == 0 && tempPoints[i].y == 0 && tempPoints[i].z == 0)
                    colors[i] = new Color(0,0,0, 0);//invisible point
                else
                    colors[i] = new Color(0,0,255);
            }
        }
        SelectableScatter dots;
        try{
            dots = new SelectableScatter(tempPoints, colors);
        }catch (Exception e)
        {
            System.out.println("pitch: " + pitch + "\nyaw: " + yaw + "\narrayIndex: " + arrayIndex);
            tempPoints = new Coord3d[1];
            tempPoints[0] = new Coord3d();
            colors = new Color[1];
            colors[0] = new Color(0,0,0, 0);
            dots = new SelectableScatter(tempPoints, colors);
        }
        dots.setWidth(3);
        dots.setHighlightColor(Color.YELLOW);
        return dots;
    }

    private static Coord3d getXYZ(double pitch, double yaw){
        double r = distance;
        if(r > maxSensorRange)
            r=0;
        return new Coord3d(getSphericalToCartesianX(r, pitch, yaw),
                getSphericalToCartesianY(r, pitch, yaw),
                getSphericalToCartesianZ(r, pitch));
    }

    public static double getSphericalToCartesianX(double r, double pitch, double yaw)
    {
        return r*Math.sin(pitch/57.295779513)*Math.cos(yaw/57.295779513);
    }

    public static double getSphericalToCartesianY(double r, double pitch, double yaw)
    {
        return r*Math.sin(pitch/57.295779513)*Math.sin(yaw/57.295779513);
    }

    public static double getSphericalToCartesianZ(double r, double pitch)
    {
        return r*Math.cos(pitch/57.295779513);
    }

    public static void customPrompt(@NotNull String title, @NotNull String message, @NotNull Alert.AlertType alertType)
    {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1), event -> {
            Alert alert = new Alert(alertType);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
            if(alertType.equals(Alert.AlertType.ERROR))
                alert.setOnCloseRequest(event1 -> {
                    System.out.println("Leaving app from Error Prompt Handler......");
                    System.exit(-23);
                });
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }

    static class MyThread extends Thread{
        private boolean runFlag = true;
        public MyThread(){
        }

        public void stopThread(){
            runFlag = false;
        }

        @Override
        public void run() {
            super.run();
            while(runFlag)
            {
                try {
                    this.sleep(8);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if((pitchMax*yawMax) == x)
                {
                    resetScan();
                }else if(measureFlag)
                {
                    if(getNextMeasurement)
                    {
                        measureFlag = false;
                        getNextMeasurement = false;
                        String messageToSend = "";
                        if((pitchMax - 1 - pitch) < 10)
                        {
                            messageToSend = "0" + (pitchMax - 1 - pitch);
                        }else {
                            messageToSend = String.valueOf(pitchMax - 1 - pitch);
                        }

                        messageToSend += "," + yaw + "\n";

                        if(!messageToSend.contains(",-1") && !messageToSend.contains(",180"))
                        {
                            serialPorts[choiceBoxSerialPorts.getSelectionModel().getSelectedIndex()].
                                    writeBytes(messageToSend.getBytes(StandardCharsets.UTF_8), messageToSend.length());
                            System.out.println("Sent: " + messageToSend.substring(0, messageToSend.length()-1));
                        }else
                        {
                            System.out.println("Debug: " + messageToSend + "\n\n\n");
                            getNextMeasurement = false;
                            measureFlag = true;
                        }
                    }else
                    {
                        getNextMeasurement = true;
                        if(yaw < yawMax && direction)
                        {
                            try {
                                points[x] = getXYZ(89 - (pitchMax - 1 - pitch), yaw);
                                System.out.println("Index: " + x + " Pitch: " + (pitchMax - 1 - pitch) + " Yaw: " + yaw + " direction: " + direction + "\n\n\n");
                            }catch (Exception e)
                            {
                                System.out.println("Index: " + x + " Pitch: " + pitch + " Yaw: " + yaw + " direction: " + direction);
                                e.printStackTrace();
                            }
                            yaw++;
                            x++;
                        }
                        else if(yaw == yawMax && direction)
                        {
                            yaw--;
                            if(pitch > 0)
                            {
                                pitch--;
                                direction = false;
                            }
                        }else if(yaw >= 0 && !direction)
                        {
                            try {
                                points[x] = getXYZ(89 - (pitchMax - 1 - pitch), yaw);
                                System.out.println("Index: " + x + " Pitch: " + (pitchMax - 1 - pitch) + " Yaw: " + yaw + " direction: " + direction + "\n\n\n");
                            }catch (Exception e)
                            {
                                System.out.println("Index: " + x + " Pitch: " + pitch + " Yaw: " + yaw + " direction: " + direction);
                                e.printStackTrace();
                            }
                            yaw--;
                            x++;
                        }
                        else if(yaw == -1)
                        {
                            yaw = 0;
                            if(pitch > 0)
                            {
                                pitch--;
                                direction = true;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        myThread.stopThread();
        if(btnConnect.getText().equals("Disconnect"))
            disconnectFromSerialPort();
    }
}