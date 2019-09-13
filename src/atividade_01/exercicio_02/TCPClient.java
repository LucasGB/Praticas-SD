package atividade_01.exercicio_02;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class TCPClient {
	public static void main (String args[]) {
	    Socket clientSocket = null;
            Scanner reader = new Scanner(System.in);
            
            try{
                int serverPort = 6666;   
                InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
                
                clientSocket = new Socket(serverAddr, serverPort);  
                
                DataInputStream in = new DataInputStream( clientSocket.getInputStream());
                DataOutputStream out =new DataOutputStream( clientSocket.getOutputStream());
            
                String buffer = "";
                while (true) {
                    System.out.print("Mensagem: ");
                    buffer = reader.nextLine();
                
                    out.writeUTF(buffer);
		
                    if (buffer.equals("PARAR")) break;
                    
                    buffer = in.readUTF();
                    System.out.println("Server disse: " + buffer);
                } 
	    } catch (UnknownHostException ue){
		System.out.println("Socket:" + ue.getMessage());
            } catch (EOFException eofe){
		System.out.println("EOF:" + eofe.getMessage());
            } catch (IOException ioe){
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
