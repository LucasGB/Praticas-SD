package atividade_02.exercicio_02;

import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class UDPClient {

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    public static void main(String args[]) {
        DatagramSocket dgramSocket;
        int resp = 0;

        try {
            dgramSocket = new DatagramSocket();

            //String dstIP = JOptionPane.showInputDialog("IP Destino?");
            String dstIP = "127.0.0.1";
            //int dstPort = Integer.parseInt(JOptionPane.showInputDialog("Porta Destino?"));
            int dstPort = 6666;

            InetAddress serverAddr = InetAddress.getByName(dstIP);
            int serverPort = dstPort;
            String currentDirectory = System.getProperty("user.home");
            do {
                final JFileChooser fc = new JFileChooser(currentDirectory);
                int result = fc.showOpenDialog(null);
                if (!(result == JFileChooser.APPROVE_OPTION)) {
                    JOptionPane.showMessageDialog(null, "Error ao abrir o arquivo.");
                    continue;
                }
                File file = fc.getSelectedFile();

                String msg = String.format("%d %s", file.length(), file.getName());
                System.out.println(msg);

                byte[] byteMsg = msg.getBytes();

                DatagramPacket headerPacket = new DatagramPacket(byteMsg, byteMsg.length, serverAddr, serverPort);
                dgramSocket.send(headerPacket);

                FileInputStream fis = new FileInputStream(file);

                // Read file into array:
                byte[] byteArray = new byte[1024];
                int bytesRead;
                int total = 0;
                while ((bytesRead = fis.read(byteArray, 0, byteArray.length)) != -1) {
                    total += bytesRead;
                    // Send the file:
                    DatagramPacket bodyPacket = new DatagramPacket(byteArray, bytesRead, serverAddr, serverPort);
                    dgramSocket.send(bodyPacket);
                }
                System.out.println(String.format("Upload complete. Transfered %d / %d bytes.", total, file.length()));

                MessageDigest md5Digest;
                try {
                    md5Digest = MessageDigest.getInstance("MD5");
                    String checksum = getFileChecksum(md5Digest, file);
                    DatagramPacket checksumPacket = new DatagramPacket(checksum.getBytes(), checksum.length(), serverAddr, serverPort);
                    dgramSocket.send(checksumPacket);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                DatagramPacket reply = new DatagramPacket(byteArray, byteArray.length);
                dgramSocket.receive(reply);
                
                System.out.println("AEIDaw");
                
                if(reply.equals("1")){
                    JOptionPane.showConfirmDialog(null, "Error ao fazer upload do arquivo.");
                } else {
                    JOptionPane.showConfirmDialog(null, "Upload realizado com sucesso.");
                }
                resp = JOptionPane.showConfirmDialog(null, "Nova mensagem?",
                        "Continuar", JOptionPane.YES_NO_OPTION);

            } while (resp != JOptionPane.NO_OPTION);

            dgramSocket.close();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}
