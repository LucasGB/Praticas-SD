package atividade_4_5;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Properties;

/**
 * Gerencia o protocolo e o processamento das mensagens
 *
 * @author rodrigo
 */
public class ProtocolController {

    private final MulticastSocket multicastSocket;
    private final DatagramSocket udpSocket;
    private final InetAddress group;
    private final Integer mport, uport;
    private final String nick;
    private final HashMap<String, InetAddress> onlineUsers;
    private final UIControl ui;
    private final String ip;

    protected byte[] buffer;

    public ProtocolController(Properties properties) throws IOException {
        mport = (Integer) properties.get("multicastPort");
        uport = (Integer) properties.get("udpPort");
        group = (InetAddress) properties.get("multicastIP");
        nick = (String) properties.get("nickname");
        ui = (UIControl) properties.get("UI");

        buffer = new byte[1024];

        this.multicastSocket = new MulticastSocket(mport);
        udpSocket = new DatagramSocket(uport);

        onlineUsers = new HashMap<>();
        onlineUsers.put("Todos", group);

        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            this.ip = socket.getLocalAddress().getHostAddress();
        }
    }

    public void send(String targetUser, String msg) throws IOException {
        Message message;
        byte typeMsg;
        if (targetUser.equals("Todos")) {
            typeMsg = (byte) 0x03;
            message = new Message(typeMsg, nick, msg);
            this.sendMessageGroup(message);
        } else {
            typeMsg = (byte) 0x04;
            message = new Message(typeMsg, nick, msg);
            this.sendMessage(message, onlineUsers.get(targetUser));
        }
    }

    private void sendMessageGroup(Message msg) throws IOException {
        byte[] messagePacket = msg.getBytes();

        System.out.println("Sending message to group");
        DatagramPacket packet = new DatagramPacket(messagePacket, messagePacket.length, group, mport);
        this.multicastSocket.send(packet);
        System.out.println("Sent message to group");
    }

    private void sendMessage(Message msg, InetAddress target) throws IOException {
        byte[] messagePacket = msg.getBytes();

        System.out.println("Sending message to target");
        DatagramPacket packet = new DatagramPacket(messagePacket, messagePacket.length, target, uport);
        udpSocket.send(packet);
        System.out.println("Sent message to target");
    }

    public void join() throws IOException {
        this.multicastSocket.joinGroup(group);

        byte typeMsg = (byte) 0x01;
        String msg = "";

        System.out.println("Joined group");

        Message message = new Message(typeMsg, nick, msg);
        sendMessageGroup(message);
    }

    public void leave() throws IOException {
        byte typeMsg = (byte) 0x05;
        String msg = "";

        Message message = new Message(typeMsg, nick, msg);
        this.sendMessageGroup(message);

        this.multicastSocket.leaveGroup(group);
        close();
    }

    public void close() throws IOException {
        if (udpSocket != null) {
            udpSocket.close();
        }
        if (this.multicastSocket != null) {
            this.multicastSocket.close();
        }
    }

    public void processPacket(DatagramPacket packet) throws IOException {
        Message message = new Message(packet.getData());

        switch (message.getType()) {
            case 1:
                ui.update(message);
                break;
            case 2:
                ui.update(message);
                byte type = (byte) 0x02;
                Message JOINACK = new Message(type, nick, "");
                System.out.println("Sending JOINACK");
                this.sendMessage(JOINACK, packet.getAddress());
                System.out.println("Sent JOINACK");
                break;
            case 3:

                // Mostrar para o professor
                // O cliente que envia o multicast recebe o próprio pacote pelo grupo.
                // Verifico e ignoro os pacotes com endereço igual ao dos sockets.
                if (packet.getAddress() == this.multicastSocket.getInetAddress()
                        || packet.getAddress() == udpSocket.getInetAddress()) {
                    break;
                } else {
                    ui.update(message);
                }
                break;
            case 4:
                System.out.println("Packet Address: " + packet.getAddress());
                System.out.println("this.multicastSocket Address: " + this.multicastSocket.getInetAddress());
                System.out.println("udpSocket Address: " + udpSocket.getInetAddress());
                // Mostrar para o professor
                // O cliente que envia o multicast recebe o próprio pacote pelo grupo.
                // Verifico e ignoro os pacotes com endereço igual ao dos sockets.
                if (packet.getAddress() == this.multicastSocket.getInetAddress()
                        || packet.getAddress() == udpSocket.getInetAddress()) {
                    break;
                } else {
                    ui.update(message);
                }
                break;
            case 5:
                ui.update(message);
                break;
        }
    }

    public void receiveMulticastPacket() throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        this.multicastSocket.receive(packet);

        System.out.println("Packet Address: " + packet.getAddress().toString());
        System.out.println("multicastSocket Address: " + this.multicastSocket.getInetAddress());
        System.out.println("udpSocket Address: " + udpSocket.getInetAddress());
        System.out.println(this.ip);

        if (!(packet.getAddress().toString().equals(this.ip)
                || packet.getAddress().toString().equals(this.ip))) {
            System.out.println("Process");
            processPacket(packet);
        }

    }

    public void receiveUdpPacket() throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        udpSocket.receive(packet);
        processPacket(packet);
    }
}
