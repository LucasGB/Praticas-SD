package atividade_02.exercicio_01;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputThread extends Thread {

    private DatagramSocket socket = null;

    public InputThread(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            while (true) {
                socket.receive(packet);
                System.out.println(new String(packet.getData(), 0, packet.getLength(), Charset.forName("UTF-8")));
                int nickSize = Integer.parseInt(new String(packet.getData(), 0, 1, Charset.forName("UTF-8")));
                
                String nickName = new String(packet.getData(), 1, nickSize, Charset.forName("UTF-8"));
                int messageSize = Integer.parseInt(new String(packet.getData(), nickSize + 1, 1, Charset.forName("UTF-8")));
                String message = new String(packet.getData(), nickSize + 2, messageSize, Charset.forName("UTF-8"));
                
                System.out.println(nickName + ": " + message);
            }
        } catch (IOException ex) {
            Logger.getLogger(InputThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
