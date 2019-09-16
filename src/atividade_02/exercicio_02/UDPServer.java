package atividade_02.exercicio_02;

import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPServer {

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
        DatagramSocket dgramSocket = null;
        try {
            dgramSocket = new DatagramSocket(6666);

            while (true) {
                byte[] buffer = new byte[1024];

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                dgramSocket.receive(packet);

                String[] request = new String(packet.getData(), 0, packet.getLength()).split(" ", 2);
                int fileLength = Integer.parseInt(request[0]);
                String fileName = request[1];

                String currentDirectory = System.getProperty("user.dir");
                File file = new File(currentDirectory + "/resources/" + fileName);

                FileOutputStream fos = new FileOutputStream(file);
                int total = 0;

                while (total < fileLength) {
                    dgramSocket.receive(packet);
                    fos.write(packet.getData(), 0, packet.getLength());
                    total += packet.getLength();
                    System.out.println(total);
                }
                
                System.out.println("WIjeia");

                dgramSocket.receive(packet);
                String remoteChecksum = new String(packet.getData(), 0, packet.getLength());
                String localChecksum = "";

                MessageDigest md5Digest;
                try {
                    md5Digest = MessageDigest.getInstance("MD5");
                    localChecksum = getFileChecksum(md5Digest, file);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                System.out.println("Done");

                String reply = "";
                if (!localChecksum.equals(remoteChecksum)) {
                    System.out.println("Erro ao baixar o arquivo.");
                    reply = "1";
                } else {
                    System.out.println("Sucess!");
                    reply = "0";
                }
                DatagramPacket replyPacket = new DatagramPacket(reply.getBytes(), reply.length(), packet.getAddress(), packet.getPort());
                dgramSocket.send(replyPacket);

                fos.close();

            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            dgramSocket.close();
        }
    }
}
