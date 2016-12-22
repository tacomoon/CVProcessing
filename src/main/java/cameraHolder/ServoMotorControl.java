package cameraHolder;

import com.fazecast.jSerialComm.SerialPort;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

public class ServoMotorControl extends CameraHolder {
    private SerialPort commPort;
    private OutputStream outputStream;

    public ServoMotorControl() {
        super(0, 180, 0, 0);
    }

    @Override
    public void setUpConnection(Map<String, Object> parameters) throws Exception {
        connected = false;

        String portName = (String) parameters.get("portName");
        if (portName == null) {
            throw new Exception("There is no 'portName' value in parameters.");
        }
        ArrayList<String> portNames = new ArrayList<>();
        for (SerialPort serialPort : SerialPort.getCommPorts()) {
            portNames.add(serialPort.getSystemPortName());
        }
        if (!portNames.contains(portName)) {
            throw new Exception("Can't find port with name \"" + portName + "\"");
        }
        commPort = SerialPort.getCommPort(portName);
        commPort.openPort();

        commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        outputStream = commPort.getOutputStream();

        connected = true;
    }

    @Override
    public void closeConnection() {
        if (commPort != null) {
            connected = false;
            commPort.closePort();
        }
    }

    public void sendSingleByte(byte myByte) {
        try {
            outputStream.write(myByte);
            outputStream.flush();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public void update(boolean isDetected, int x, int y) {
        System.out.println("__________________________________________");
        System.out.println("Detected = " + isDetected);

        byte sendingValue = 0;
        if (getHorizontalAngleMaxValue() > 0) {
            if (getHorizontalAngle() != x) {
                System.out.println("Got new x value = " + x);// TODO remove this
                setHorizontalAngle(getHorizontalAngle() + (int) Math.round((double) x / (600d / 78d)));// TODO replace hardcode

                sendingValue = mapIntToByteValue(getHorizontalAngle());
                System.out.println("Mapped value = " + sendingValue);
                System.out.println(String.format("%8s", Integer.toBinaryString(sendingValue & 0xFF)).replace(' ', '0'));
                if (isDetected) {
                    sendingValue = (byte) (sendingValue | (1 << 7));
                }
            }
            System.out.println("Sending value = " + sendingValue);
            System.out.println(String.format("%8s", Integer.toBinaryString(sendingValue & 0xFF)).replace(' ', '0'));
            sendSingleByte(sendingValue);
        }
        sendingValue = 0;
        if (getVerticalAngleMaxValue() > 0) {
            if (getVerticalAngle() != y) {
//                System.out.println("Got new y value = " + y);// TODO remove this
                setVerticalAngle(getVerticalAngle() + (int) Math.round((double) y / (600d / 78d)));// TODO replace hardcode
                sendingValue = mapIntToByteValue(getVerticalAngle());
                if (isDetected) {
                    sendingValue = (byte) (sendingValue | (1 << 7));
                }
            }
            sendSingleByte(sendingValue);
        }
    }

    /* Static methods */
    public static byte mapIntToByteValue(int value) {
        if (value == 0)
            return 0;
        return (byte) Math.round((value * 127d) / 180d);
    }
    /* Static methods */
}
