import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.JFileChooser;

public class CEnvia {

    public static void main(String[] args) {
        final String rutaClienteLocal = "C:\\FlujoArchivo_modificado\\FlujoArchivo\\src\\archivosLocal\\";

        try {
            int puerto = 8000;
            InetAddress servidorAddress = InetAddress.getByName("127.0.0.1");
            DatagramSocket socket = new DatagramSocket();

            System.out.println("Conexión con el servidor establecida...");

            File f2 = new File(rutaClienteLocal);
            f2.mkdirs();
            f2.setWritable(true);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("Menú:\n");
                System.out.println("1) Subir archivos/carpetas al servidor");
                System.out.println("2) Descargar archivos/carpetas del servidor");
                System.out.println("0) Salir\n");

                String respuesta = scanner.nextLine();
                int resp = Integer.parseInt(respuesta);

                switch (resp) {
                    case 1:
                        enviaArchivo(socket, servidorAddress, puerto);
                        break;
                    case 2:
                        solicitarArchivoAlServidor(socket, servidorAddress, puerto, rutaClienteLocal);
                        break;
                    case 0:
                        System.out.println("\nFinalizando..");
                        System.exit(0);
                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static void enviaArchivo(DatagramSocket socket, InetAddress servidorAddress, int puerto) {
        try {
            JFileChooser jf = new JFileChooser("C:\\FlujoArchivo_modificado\\FlujoArchivo\\src\\archivosLocal");
            jf.setMultiSelectionEnabled(true);
            jf.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            int r = jf.showOpenDialog(null);

            if (r == JFileChooser.APPROVE_OPTION) {
                File[] seleccionados = jf.getSelectedFiles();
                for (File seleccionado : seleccionados) {
                    enviarArchivoIndividual(socket, servidorAddress, puerto, seleccionado);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

public static void enviarArchivoIndividual(DatagramSocket socket, InetAddress servidorAddress, int puerto, File archivo) {
    try {
        byte[] buffer = new byte[65535];
        int bytesRead;

        // Enviar solicitud de envío al servidor
        String nombreArchivo = archivo.getName();
        long fileSize = archivo.length();
        String solicitud = "ARCHIVO " + nombreArchivo + " " + fileSize;
        byte[] solicitudBytes = solicitud.getBytes();
        DatagramPacket requestPacket = new DatagramPacket(solicitudBytes, solicitudBytes.length, servidorAddress, puerto);
        socket.send(requestPacket);

        try (DataInputStream dis = new DataInputStream(new FileInputStream(archivo))) {
            while ((bytesRead = dis.read(buffer)) != -1) {
                DatagramPacket dataPacket = new DatagramPacket(buffer, bytesRead, servidorAddress, puerto);
                socket.send(dataPacket);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}


 public static void solicitarArchivoAlServidor(DatagramSocket socket, InetAddress servidorAddress, int puerto, String rutaClienteLocal) {
    try {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el nombre del archivo a recibir: ");
        String nombre = scanner.nextLine();

        // Enviar solicitud al servidor        
        String tipoSolicitud = "SOLICITUD_ARCHIVO";
        String nombreArchivo = nombre;

        byte[] tipoSolicitudBytes = tipoSolicitud.getBytes();
        byte[] nombreArchivoBytes = nombreArchivo.getBytes();

        byte[] solicitudBytes = new byte[tipoSolicitudBytes.length + 1 + nombreArchivoBytes.length];
        System.arraycopy(tipoSolicitudBytes, 0, solicitudBytes, 0, tipoSolicitudBytes.length);
        solicitudBytes[tipoSolicitudBytes.length] = ' '; // Agrega un espacio
        System.arraycopy(nombreArchivoBytes, 0, solicitudBytes, tipoSolicitudBytes.length + 1, nombreArchivoBytes.length);

        DatagramPacket requestPacket = new DatagramPacket(solicitudBytes, solicitudBytes.length, servidorAddress, puerto);
        socket.send(requestPacket);


        byte[] buffer = new byte[65535];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivePacket);

        String respuesta = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println("La respuesta que se envió desde SRecibe es: "+respuesta);
        if (respuesta.equals("ARCHIVO_DISPONIBLE")) {
            // Ahora sabes que el archivo está disponible y puedes proceder a recibirlo
            System.out.println("si entro a ARCHIVO_DISPONIBLE");
            recibirArchivoDelServidor(socket, rutaClienteLocal, nombre);
        } else if (respuesta.equals("ARCHIVO_NO_ENCONTRADO")) {
            System.out.println("El archivo solicitado no se encontró en el servidor.");
        } else {
            System.out.println("Respuesta no válida.");
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
}


public static void recibirArchivoDelServidor(DatagramSocket socket, String rutaClienteLocal, String nombre) {
    try {
        byte[] buffer = new byte[65535];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivePacket);

        String nombreArchivo = new String(receivePacket.getData(), 0, receivePacket.getLength());

        if (nombreArchivo.equals("ARCHIVO_NO_ENCONTRADO")) {
            System.out.println("El archivo solicitado no se encontró en el servidor.");
        } else {
            File archivoDestino = new File(rutaClienteLocal, nombre);
            FileOutputStream fos = new FileOutputStream(archivoDestino);

            while (true) {
                socket.receive(receivePacket);
                int bytesRead = receivePacket.getLength();

                if (bytesRead <= 0) {
                    // Paquete vacío, fin de la transmisión
                    break;
                }

                fos.write(buffer, 0, bytesRead);
            }

            fos.close();
            System.out.println("Archivo " + nombreArchivo + " recibido y guardado en " + rutaClienteLocal);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}





 
 
 
 
 

}
