
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
            int pto = 8000;
            ServerSocket s = new ServerSocket(pto);
            s.setReuseAddress(true);
            System.out.println("Servidor iniciado esperando por archivos...");

            System.out.println("ruta:" + RutaServidor);
            File f2 = new File(RutaServidor);
            f2.mkdirs();
            f2.setWritable(true);

            for (;;) {
                Socket cl = s.accept();
                System.out.println("Cliente conectado desde " + cl.getInetAddress() + ":" + cl.getPort());

                DataInputStream dis = new DataInputStream(cl.getInputStream());
                DataOutputStream dos = new DataOutputStream(cl.getOutputStream());

                label:
                while (true) {

                    String solicitud = dis.readUTF();

                    if (solicitud.equals("SOLICITUD_ARCHIVO")) {
                        enviarArchivoAlCliente(dos, RutaServidor, cl);
                    } else if (solicitud.equals("ARCHIVO")) {
                        recibirArchivoIndividual(dis, RutaServidor);
                    } else if (solicitud.equals("FINALIZAR_CLIENTE")) {
                        break label;
                    } else {
                        System.out.println("Solicitud no válida.");
                    }

                }

                dis.close();
                dos.close();
                cl.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enviarArchivoAlCliente(DataOutputStream dos, String Ruta_Archivo, Socket cl) throws IOException {
        DataInputStream dis = new DataInputStream(cl.getInputStream());

        String nombreArchivo = dis.readUTF();
        File archivo = new File(Ruta_Archivo, nombreArchivo);
        if (archivo.exists()) {
                enviarArchivoIndividual(cl, archivo);
        } else {
            System.out.println("El archivo especificado no existe en la ubicación proporcionada.");
        }
    }


private static void enviarArchivoIndividual(Socket cl, File archivo) throws IOException {
    DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
    DataInputStream dis = new DataInputStream(new FileInputStream(archivo));
    dos.writeUTF("ARCHIVO");
    dos.writeUTF(archivo.getName());
    dos.writeLong(archivo.length());

    byte[] buffer = new byte[1500];
    int bytesRead;
    long totalBytesSent = 0; 
    long fileSize = archivo.length(); 

    while ((bytesRead = dis.read(buffer)) != -1) {
        dos.write(buffer, 0, bytesRead);
        totalBytesSent += bytesRead;

        // Calcular el porcentaje y mostrarlo
        double porcentajeEnviado = ((double) totalBytesSent / fileSize) * 100;
        System.out.printf("\rBytes enviados: %d / %d (%.2f%%)", totalBytesSent, fileSize, porcentajeEnviado);
    }

    dis.close();
    System.out.println("\nEnvío de archivo completado.");
}


    private static void recibirArchivoIndividual(DataInputStream dis, String directorioDestino) {
        try {
            String nombre = dis.readUTF();
            long Dimension = dis.readLong();
            FileOutputStream fos = new FileOutputStream(directorioDestino + "\\" + nombre);
            byte[] buffer = new byte[1500];
            int bytesRead;
            long recibidos = 0;

            while (recibidos < Dimension) {
                bytesRead = dis.read(buffer);
                fos.write(buffer, 0, bytesRead);
                recibidos += bytesRead;
            }

            fos.close();
            System.out.println("Archivo " + nombre + " recibido y guardado en " + directorioDestino);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
