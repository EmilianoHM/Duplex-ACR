
package axel.multicast1;


import java.net.*;
import java.io.*;
import java.util.*;

class ClienteInfo {
    InetAddress address;
    int port;
    String nombre;

    public ClienteInfo(InetAddress address, int port, String nombre) {
        this.address = address;
        this.port = port;
        this.nombre = nombre;
    }
}

class MulticastServer extends Thread {
    MulticastSocket socket;
    InetAddress multicastGroup;
    int multicastPort;
    List<ClienteInfo> clientes;

    public MulticastServer(String multicastAddress, int multicastPort) throws IOException {
        this.multicastPort = multicastPort;
        this.multicastGroup = InetAddress.getByName(multicastAddress);
        this.socket = new MulticastSocket(multicastPort);
        this.clientes = new ArrayList<>();
    }

    public void run() {
        try {
            socket.joinGroup(multicastGroup);

            while (true) {
                // Recibe mensajes de clientes
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String mensaje = new String(packet.getData(), 0, packet.getLength());

                // Si un cliente se registra con su nombre
                if (mensaje.startsWith("REGISTRO:")) {
                    String nombre = mensaje.substring(10); // Extraer el nombre del mensaje
                    ClienteInfo nuevoCliente = new ClienteInfo(packet.getAddress(), packet.getPort(), nombre);
                    clientes.add(nuevoCliente);
                    System.out.println(nombre + " se ha registrado.");
                    enviarListaUsuarios();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarListaUsuarios() {
        try {
            StringBuilder listaUsuarios = new StringBuilder("Usuarios conectados:\n");

            for (ClienteInfo cliente : clientes) {
                listaUsuarios.append(cliente.nombre).append("\n");
            }

            byte[] buffer = listaUsuarios.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastGroup, multicastPort);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Servidor {
    public static void main(String[] args) {
        try {
            MulticastServer server = new MulticastServer("231.1.1.1", 9931);
            server.start();

            while (true) {
                // Puedes agregar una lógica aquí para permitir al servidor enviar la lista de usuarios
                // a los clientes cuando sea necesario.
                // Por ejemplo, cuando un nuevo cliente se registra.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
