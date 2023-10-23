import java.io.*;
import java.net.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;

public class SRecibe {

    public static void main(String[] args) {
        final String RutaServidor = "C:\\FlujoArchivo_modificado\\FlujoArchivo\\archivosServidor\\";

        try {
            int puerto = 8000;
            DatagramSocket socket = new DatagramSocket(puerto);
            System.out.println("Servidor iniciado esperando por archivos...");

            System.out.println("ruta:" + RutaServidor);
            File f2 = new File(RutaServidor);
            f2.mkdirs();
            f2.setWritable(true);

            while (true) {
                byte[] receiveData = new byte[65535];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                //String solicitud = new String(receivePacket.getData(), 0, receivePacket.getLength());

                InetAddress clientAddress = receivePacket.getAddress();
                System.out.println("La dirección del cliente en teoría desde SRecibe es: "+clientAddress);
                int clientPort = receivePacket.getPort();

                String solicitud = new String(receivePacket.getData(), 0, receivePacket.getLength());
                String[] parts = solicitud.split(" ");
                System.out.println("\n\n\n la solicitud recibida sin separar es: "+solicitud);
                if (parts.length == 2) {
                    String tipoSolicitud = parts[0];
                    String nombreArchivo = parts[1];
                    System.out.println("\n\n\n la solicitud recibida ya separada es: "+tipoSolicitud);
                    System.out.println("\n\n\n El nombreArchivo recibido ya separada es: "+nombreArchivo);
                    if (tipoSolicitud.equals("SOLICITUD_ARCHIVO")) {
                        enviarArchivoAlCliente(RutaServidor, clientAddress, clientPort, socket, nombreArchivo);
                    } else if (tipoSolicitud.equals("ARCHIVO")) {
                        recibirArchivoIndividual(socket, RutaServidor, nombreArchivo);
                    } else if (tipoSolicitud.equals("FINALIZAR_CLIENTE")) {
                        break;
                    } else {
                        System.out.println("Solicitud no válida.");
                    }
                } else {
                    System.out.println("Solicitud mal formateada.");
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enviarArchivoAlCliente(String Ruta_Archivo, InetAddress clientAddress, int clientPort, DatagramSocket socket, String nombreArchivo) throws IOException {
        File archivo = new File(Ruta_Archivo, nombreArchivo);
        if (archivo.exists()) {
            // Enviar una respuesta al cliente en CEnvia para indicar que el archivo está disponible
            String respuesta = "ARCHIVO_DISPONIBLE";
            byte[] respuestaBytes = respuesta.getBytes();
            DatagramPacket respuestaPacket = new DatagramPacket(respuestaBytes, respuestaBytes.length, clientAddress, clientPort);
            socket.send(respuestaPacket);
            enviarArchivoIndividual(socket, archivo, clientAddress, clientPort);
        } else {
            
            System.out.println("El archivo especificado no existe en la ubicación proporcionada.");
        }
    }



private static void enviarArchivoIndividual(DatagramSocket socket, File archivo, InetAddress clientAddress, int clientPort) {
    try (DataInputStream dis = new DataInputStream(new FileInputStream(archivo))) {
        byte[] buffer = new byte[65535];
        int bytesRead;
        long totalBytesSent = 0;
        long fileSize = archivo.length();

        DatagramPacket packet;

        while ((bytesRead = dis.read(buffer)) != -1) {
            packet = new DatagramPacket(buffer, 0, bytesRead, clientAddress, clientPort);
            socket.send(packet);
            totalBytesSent += bytesRead;

            // Calcular el porcentaje y mostrarlo
            double porcentajeEnviado = ((double) totalBytesSent / fileSize) * 100;
            System.out.printf("\rBytes enviados: %d / %d (%.2f%%)", totalBytesSent, fileSize, porcentajeEnviado);
        }

        // Envía un paquete vacío para indicar el final de la transmisión
        packet = new DatagramPacket(new byte[0], 0, clientAddress, clientPort);
        socket.send(packet);

        System.out.println("\nEnvío de archivo completado.");
    } catch (IOException e) {
        e.printStackTrace();
    }
}


private static void recibirArchivoIndividual(DatagramSocket socket, String directorioDestino, String nombreArchivo) {
    try {
        byte[] buffer = new byte[65535];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivePacket);

        if (nombreArchivo.equals("ARCHIVO_NO_ENCONTRADO")) {
            System.out.println("El archivo solicitado no se encontró en el servidor.");
        } else {
            File archivoDestino = new File(directorioDestino, nombreArchivo);
            FileOutputStream fos = new FileOutputStream(archivoDestino);

            while (true) {
                socket.receive(receivePacket);
                int bytesRead = receivePacket.getLength();

                if (bytesRead <= 0) {
                    break; // Fin de la transmisión
                }
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            System.out.println("Archivo " + nombreArchivo + " recibido y guardado en " + directorioDestino);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

}
