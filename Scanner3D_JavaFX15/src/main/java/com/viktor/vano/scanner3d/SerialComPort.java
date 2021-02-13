package com.viktor.vano.scanner3d;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.viktor.vano.scanner3d.App.customPrompt;
import static com.viktor.vano.scanner3d.AppFunctions.*;
import static com.viktor.vano.scanner3d.ObjectsAndVariables.*;


public class SerialComPort {
    private static String receiveBuffer = "";
    public static void initializeComPortMenu()
    {
        serialPorts = SerialPort.getCommPorts();
        if(serialPorts.length>0)
        {
            System.out.println("\nPorts available:");
            for (SerialPort port: serialPorts) {
                System.out.println(port.getPortDescription() + " (" + port.getSystemPortName() + ")");
            }
        }else
        {
            System.err.println("\nError: Ports unavailable...");
            customPrompt("COM Port ERROR",
                    "No COM ports found.\nPlease connect some and restart this app.",
                    Alert.AlertType.ERROR);
        }

        ArrayList<String> stringsPort = new ArrayList<>();
        for (SerialPort port: serialPorts)
            stringsPort.add(port.getPortDescription() + " (" + port.getSystemPortName() + ")");
        choiceBoxSerialPorts = new ChoiceBox<>(FXCollections.observableArrayList(stringsPort));
        hBoxBottom.getChildren().add(choiceBoxSerialPorts);
        choiceBoxSerialPorts.setOnAction(event -> {
            if(btnConnect!=null && btnConnect.isDisabled())
                btnConnect.setDisable(false);
        });

        btnConnect = new Button("Connect");
        btnConnect.setDisable(true);
        hBoxBottom.getChildren().add(btnConnect);
        btnConnect.setOnAction(event -> {
            if(btnConnect.getText().equals("Connect"))
            {
                connectToSerialPort();
            }
            else
            {
                disconnectFromSerialPort();
            }
        });
    }

    public static void connectToSerialPort()
    {
        if(choiceBoxSerialPorts.getSelectionModel().getSelectedIndex()>=0)
        {
            System.out.println("Connecting to " + choiceBoxSerialPorts.getValue());
            btnConnect.setDisable(true);
            serialPorts[choiceBoxSerialPorts.getSelectionModel().getSelectedIndex()].setBaudRate(115200);
            if(serialPorts[choiceBoxSerialPorts.getSelectionModel().getSelectedIndex()].openPort())
            {
                System.out.println("Connected to " + choiceBoxSerialPorts.getValue());
                choiceBoxSerialPorts.setDisable(true);
                btnConnect.setText("Disconnect");
                btnScan.setDisable(false);
                textFieldPitchMax.setDisable(false);
                System.out.println(serialPorts[choiceBoxSerialPorts.getSelectionModel().getSelectedIndex()].getBaudRate());
                serialPorts[choiceBoxSerialPorts.getSelectionModel().getSelectedIndex()].addDataListener(new SerialPortDataListener() {
                    @Override
                    public int getListeningEvents() {
                        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                    }

                    @Override
                    public void serialEvent(SerialPortEvent serialPortEvent) {
                        if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                            return;
                        byte[] newData = new byte[serialPorts[choiceBoxSerialPorts.getSelectionModel().getSelectedIndex()].bytesAvailable()];
                        serialPorts[choiceBoxSerialPorts.getSelectionModel().getSelectedIndex()].readBytes(newData, newData.length);
                        if(new String(newData, StandardCharsets.UTF_8).contains("\n"))
                        {
                            if(newData.length > 0)
                            {
                                //System.out.println("New data length: " + newData.length);
                                String stringData = new String(newData, StandardCharsets.UTF_8);
                                receiveBuffer += stringData;
                            }
                            //System.out.println("Received Message: " + receiveBuffer);
                            if(receiveBuffer.contains("Error"))
                            {
                                customPrompt("Error received from STM32",
                                        receiveBuffer, Alert.AlertType.ERROR);
                            }else
                            {
                                messageHandler(receiveBuffer);
                            }
                            receiveBuffer = "";
                        }
                        else
                            receiveBuffer += new String(newData, StandardCharsets.UTF_8);
                    }
                });
            }
            else
            {
                customPrompt("COM Port Error",
                        "Failed to connect to " + choiceBoxSerialPorts.getValue(),
                        Alert.AlertType.ERROR);
            }

            btnConnect.setDisable(false);
        }
    }

    public static void disconnectFromSerialPort()
    {
        if(choiceBoxSerialPorts.getSelectionModel().getSelectedIndex()>=0)
        {
            System.out.println("Disconnecting from " + choiceBoxSerialPorts.getValue());
            btnConnect.setDisable(true);
            if(serialPorts[choiceBoxSerialPorts.getSelectionModel().getSelectedIndex()].closePort())
            {
                System.out.println("Disconnected from " + choiceBoxSerialPorts.getValue());
                choiceBoxSerialPorts.setDisable(false);
                btnConnect.setText("Connect");
                btnScan.setDisable(true);
                textFieldPitchMax.setDisable(false);
            }
            else
                System.out.println("Failed to disconnect from " + choiceBoxSerialPorts.getValue());

            btnConnect.setDisable(false);
        }
    }
}
