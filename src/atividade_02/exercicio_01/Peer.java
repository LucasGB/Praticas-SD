package atividade_02.exercicio_01;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import javax.swing.JOptionPane;

public class Peer {

    public static void main(String args[]) {
        DatagramSocket dgramSocket = null;
        int resp = 0;

        try {
            dgramSocket = new DatagramSocket();

            System.out.println("Local port: " + dgramSocket.getLocalPort());

            InputThread in = new InputThread(dgramSocket);
            in.start();
            
            String nickName = JOptionPane.showInputDialog("Username?");
            String dstIP = JOptionPane.showInputDialog("IP Destino?");
            int dstPort = Integer.parseInt(JOptionPane.showInputDialog("Porta Destino?"));

            do {

                byte[] buffer = new byte[1024];

                String messageBody = JOptionPane.showInputDialog("Mensagem: ");
                String message = new String().format("%d%s%d%s", (byte) nickName.length(), nickName, (byte) messageBody.length(), messageBody, Charset.forName("UTF-8"));
                DatagramPacket messagePacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(dstIP), dstPort);
                dgramSocket.send(messagePacket);

                resp = JOptionPane.showConfirmDialog(null, "Nova mensagem?",
                        "Continuar", JOptionPane.YES_NO_OPTION);

            } while (resp != JOptionPane.NO_OPTION);

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            dgramSocket.close();
        }
    }
}
