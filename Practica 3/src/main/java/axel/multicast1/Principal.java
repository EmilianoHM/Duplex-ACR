package axel.multicast1;
import java.net.*;
import java.io.*;
import java.util.*;

class Envia extends Thread {
    MulticastSocket socket;
    BufferedReader br;
    String nombre;
    ArrayList<String> usuariosConectados; // Lista de usuarios conectados

    public Envia(MulticastSocket m, BufferedReader br, String nombre, ArrayList<String> usuariosConectados) {
        this.socket = m;
        this.br = br;
        this.nombre = nombre;
        this.usuariosConectados = usuariosConectados;
    }

    public void run() {
        try {
            String dir = "231.1.1.1";
            String dir6 = "ff3e::1234:1";
            int pto=9931;
            InetAddress gpo = InetAddress.getByName(dir);

            // Cuando el usuario se une al grupo, envía un mensaje especial
            String unidoAlGrupo = nombre + " se ha unido al grupo";
            byte[] bUnido = unidoAlGrupo.getBytes();
            DatagramPacket pUnido = new DatagramPacket(bUnido, bUnido.length, gpo, pto);
            socket.send(pUnido);
            
            // Agrega el nombre del usuario actual a la lista de usuarios conectados
            usuariosConectados.add(nombre);
            
            // Envía la lista de usuarios conectados al grupo
            enviarListaUsuarios(usuariosConectados);
            
            System.out.println("Escribe un mensaje para ser enviado:");
            for (;;) {
                String mensaje = br.readLine();
                mensaje = nombre + ": " + mensaje;
                byte[] b = mensaje.getBytes();
                DatagramPacket p = new DatagramPacket(b, b.length, gpo, pto);
                socket.send(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void enviarListaUsuarios(ArrayList<String> usuariosConectados) {
        try {
            String dir = "231.1.1.1";
            int pto = 9931;
            InetAddress gpo = InetAddress.getByName(dir);
            String listaUsuarios = "Usuarios Conectados: " + String.join(", ", usuariosConectados);
            byte[] b = listaUsuarios.getBytes();
            DatagramPacket listaPacket = new DatagramPacket(b, b.length, gpo, pto);
            socket.send(listaPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Recibe extends Thread {
    MulticastSocket socket;
    ArrayList<String> usuariosConectados; // Lista de usuarios conectados

    public Recibe(MulticastSocket m, ArrayList<String> usuariosConectados) {
        this.socket = m;
        this.usuariosConectados = usuariosConectados;
    }

    public void run() {
        try {
            for (;;) {
                DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
                socket.receive(p);
                String msj = new String(p.getData(), 0, p.getLength());
                if (msj.endsWith(" se ha unido al grupo")) {
                    String nombreUsuario = msj.replace(" se ha unido al grupo", "");
                    usuariosConectados.add(nombreUsuario);
                    //enviarListaUsuarios();
                    System.out.println(nombreUsuario + " se ha unido al grupo");
                } /*else if (msj.startsWith("Usuarios Conectados: ")) {
                    String listaUsuarios = msj.substring(21);
                    String[] usuarios = listaUsuarios.split(", ");
                    usuariosConectados.clear();
                    usuariosConectados.addAll(Arrays.asList(usuarios));
                    //System.out.println("Usuarios Conectados: " + listaUsuarios);
                }*/ else {
                    System.out.println("Mensaje recibido por el usuario -> " + msj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class Principal {
    static void despliegaInfoNIC(NetworkInterface netint) throws SocketException {
        System.out.printf("Nombre de despliegue: %s\n", netint.getDisplayName());
        System.out.printf("Nombre: %s\n", netint.getName());
        String multicast = (netint.supportsMulticast()) ? "Soporta multicast" : "No soporta multicast";
        System.out.printf("Multicast: %s\n", multicast);
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("Direccion: %s\n", inetAddress);
        }
        System.out.printf("\n");
    }

    public static void main(String[] args) {
        try {
            int pto = 9931, z = 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "ISO-8859-1"));
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                System.out.print("[Interfaz " + ++z + "]:");
                despliegaInfoNIC(netint);
            }
            System.out.print("\nElige la interfaz multicast:");
            int interfaz = Integer.parseInt(br.readLine());
            NetworkInterface ni = NetworkInterface.getByIndex(interfaz);
            System.out.println("\nElegiste " + ni.getDisplayName());
            System.out.print("Ingresa tu nombre de usuario: ");
            String nombre = br.readLine();

            ArrayList<String> usuariosConectados = new ArrayList<>();

            MulticastSocket m = new MulticastSocket(pto);
            m.setReuseAddress(true);
            m.setTimeToLive(255);
            String dir = "231.1.1.1";
            InetAddress gpo = InetAddress.getByName(dir);
            SocketAddress dirm = new InetSocketAddress(gpo, pto);
            m.joinGroup(dirm, ni);
            System.out.println("Socket unido al grupo " + gpo);

            Recibe r = new Recibe(m, usuariosConectados);
            Envia e = new Envia(m, br, nombre, usuariosConectados);
            e.setPriority(10);
            r.start();
            e.start();
            r.join();
            e.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
