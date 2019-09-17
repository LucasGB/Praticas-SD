package atividade_02.exercicio_01;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import javax.swing.JOptionPane;


public class Peer {
    public static void main(String args[]){ 
    	DatagramSocket dgramSocket = null;
        try{
            dgramSocket = new DatagramSocket();
            
            System.out.println("Local port: " + dgramSocket.getLocalPort());                        
            
            InputThread in = new InputThread(dgramSocket);
            in.start();
            
            while(true){
                String dstIP = JOptionPane.showInputDialog("IP Destino?");
                int dstPort = Integer.parseInt(JOptionPane.showInputDialog("Porta Destino?"));
                
                byte[] buffer = new byte[1024];

                DatagramPacket dgramPacket = new DatagramPacket(buffer, buffer.length);
                dgramSocket.receive(dgramPacket);

                DatagramPacket reply = new DatagramPacket(dgramPacket.getData(),
                        dgramPacket.getLength(), dstIP, dstPort);
                
                DatagramPacket r = new DatagramPacket(buffer, dstPort, address)
                dgramSocket.send(reply);
            }
        }catch (SocketException e){
            System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            dgramSocket.close();
        }
    }
}
