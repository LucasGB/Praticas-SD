package atividade_01.exercicio_01;

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

    private void getTime() throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();

        out.writeUTF(formatter.format(date));
    }

    private void getDate() throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();

        out.writeUTF(formatter.format(date));
    }

    private void getFiles() throws IOException {
        String currentDirectory = System.getProperty("user.dir");

        File folder = new File(currentDirectory + "\\resources\\");
        File[] listOfFiles = folder.listFiles();

        out.writeUTF("" + listOfFiles.length);
        for (int i = 0; i < listOfFiles.length; i++) {
            out.writeUTF(listOfFiles[i].getName());
        }

    }

    private void downloadResource(String filename) throws IOException {
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
            String request;
            while (true) {
                request = in.readUTF();

                String[] params = request.split(" ", 2);

                if (params.length == 1) {
                    switch (params[0]) {
                        case "TIME":
                            getTime();
                            continue;

                        case "DATE":
                            getDate();
                            continue;

                        case "FILES":
                            getFiles();
                            continue;
                            
                        case "DOWN":
                            downloadResource(params[1]);
                            continue;
                            
                        case "EXIT":
                            break;
                        default:
                            ERROR();
                            continue;
                    }
                    break;
                } else {
                    switch (params[0]) {
                        case "DOWN":
                            downloadResource(params[1]);
                            continue;
                        default:
                            ERROR();
                            continue;
                    }

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
