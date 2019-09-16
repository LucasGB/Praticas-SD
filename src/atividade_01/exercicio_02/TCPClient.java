package atividade_01.exercicio_02;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class TCPClient {

    public static void main(String args[]) {
        Socket clientSocket = null;
        Scanner reader = new Scanner(System.in);

        try {
            int serverPort = 6666;
            InetAddress serverAddr = InetAddress.getByName("127.0.0.1");

            clientSocket = new Socket(serverAddr, serverPort);

            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            String command;
            while (true) {
                System.out.print("Mensagem: ");
                command = reader.nextLine();

                String[] params = command.split(" ", 2);

                if (params.length == 1) {
                    switch (params[0]) {
                        case "GETFILESLIST":
                            byte[] request = new byte[259];
                            request[0] = (byte) 1;
                            request[1] = (byte) 3;
                            request[2] = (byte) 0;
                            request[3] = (byte) 0;
                            
                            out.write(request);
                            
                            //out.writeUTF(request);
                            
                            byte[] response = new byte[2048];
                            int bytesRead;
                            if(( bytesRead = in.read(response, 0, response.length) ) != -1){
                                
                            }
                            


                            /*
                            int chunk_size = Integer.parseInt(in.readUTF());
                            System.out.println(String.format("(%d) Resultados:", chunk_size));
                            for (int i = 0; i < chunk_size; i++) {
                                System.out.println("-> " + in.readUTF());
                            }*/
                            continue;
                        case "EXIT":
                            break;
                        default:
                            System.out.println("ERROR: comando desconhecido.");
                            continue;
                    }
                    break;
                } else {
                    switch (params[0]) {
                        case "DOWN":
                            //out.writeUTF(request);

                            String filename = System.getProperty("user.home") + "/" + params[1];
                            File file = new File(filename);

                            int byteSize = 256;
                            int bytesRead;

                            // Receives file from server:
                            int fileSize = (int) Integer.valueOf(in.readUTF());

                            if (fileSize == 0) {
                                System.out.println("Arquivo não encontrado.");
                                break;
                            }

                            System.out.println("File length: " + fileSize);
                            byteSize = Integer.valueOf(in.readUTF());
                            byte[] byteArray = new byte[byteSize];
                            FileOutputStream fos = new FileOutputStream(file);

                            //int readBytes = in.read(byteArray);
                            int readBytes;
                            // Continuously writes the file to the disk until complete:
                            int total = 0;

                            while (total < fileSize &&(bytesRead = in.read(byteArray, 0, byteArray.length)) != -1) {
                                fos.write(byteArray, 0, bytesRead);
                                total += bytesRead;
                            }

                            fos.close();
                            System.out.println("File downloaded. (" + total + " bytes read)");

                            continue;
                        default:
                            System.out.println("ERROR: comando desconhecido.");
                    }
                }
            }
        } catch (UnknownHostException ue) {
            System.out.println("Socket:" + ue.getMessage());
        } catch (EOFException eofe) {
            System.out.println("EOF:" + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IO:" + ioe.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ioe) {
                System.out.println("IO: " + ioe);;
            }
        }
    }
}
