package atividade_01.exercicio_02;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPServer {

    public static void main(String args[]) {
        try {
            int serverPort = 6666;

            ServerSocket listenSocket = new ServerSocket(serverPort);

            while (true) {
                System.out.println("Servidor aguardando conexao ...");

                Socket clientSocket = listenSocket.accept();

                System.out.println("Cliente conectado ... Criando thread ...");

                ClientThread c = new ClientThread(clientSocket);

                c.start();
            }

        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }
    }
}

class ClientThread extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    private int CHUNK_SIZE = 256;

    public ClientThread(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Connection:" + ioe.getMessage());
        }
    }

    public static byte[] intToTwoBytes(int i, boolean bigEndian) {
        if (bigEndian) {
            byte[] data = new byte[2];
            data[1] = (byte) (i & 0xFF);
            data[0] = (byte) ((i >> 8) & 0xFF);
            return data;

        } else {
            byte[] data = new byte[2];
            data[0] = (byte) (i & 0xFF);
            data[1] = (byte) ((i >> 8) & 0xFF);
            return data;
        }
    }

    private void GETFILESLIST() throws IOException {
        String currentDirectory = System.getProperty("user.dir");

        File folder = new File(currentDirectory + "\\resources\\");
        File[] listOfFiles = folder.listFiles();

        byte[] response = new byte[2048];
        response[0] = (byte) 2;
        response[1] = (byte) 3;
        response[2] = (byte) 1;

        // Transforms number of files to 2 bytes big endian
        byte[] size = intToTwoBytes(listOfFiles.length, true);
        response[3] = size[0];
        response[4] = size[1];

        int i = 5;
        for (File entry : listOfFiles) {
            if (2048 - (i + entry.getName().length()) <= 0) {
                break;
            }
            response[i] = (byte) entry.getName().length();
            i++;
            for (byte b : entry.getName().getBytes()) {
                response[i] = b;
                i++;
            }
        }
        
        System.out.println("LENGTH O RESP: " + response.length);

        out.write(response);
    }

    private void GETFILE(String filename) throws IOException {
        String currentDirectory = System.getProperty("user.dir");

        File file = new File(currentDirectory + "/resources/" + filename);
        if (!file.exists()) {
            out.writeUTF("0");
            return;
        } else {
            out.writeUTF(String.valueOf((int) file.length()));
        }

        // Create array for storage of file bytes:
        byte[] byteArray = new byte[CHUNK_SIZE];
        out.writeUTF("" + CHUNK_SIZE);

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        // Read file into array:
        int bytesRead;
        int total = 0;
        while ((bytesRead = bis.read(byteArray, 0, byteArray.length)) != -1) {
            total += bytesRead;
            // Send the file:
            out.write(byteArray, 0, bytesRead);
        }
        System.out.println(String.format("Upload complete. Transfered %d / %d bytes.", total, file.length()));
    }

    private void ERROR() throws IOException {
        out.writeUTF("ERROR: comando desconhecido.");
    }

    @Override
    public void run() {
        try {
            byte[] request = new byte[259];
            while (true) {
                in.read(request, 0, 258);
                System.out.println("Op:" + request[0]);
                System.out.println("ID:" + request[1]);
                System.out.println("Size:" + request[2]);

                switch (request[1]) {
                    case (byte) 3:
                        GETFILESLIST();
                        break;
                }
            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
        System.out.println("Thread comunicação cliente finalizada.");
    }
}
