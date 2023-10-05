
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import javax.swing.JFileChooser;

public class CEnvia {

    public static void main(String[] args) {
        final String rutaClienteLocal = "C:\\FlujoArchivo_modificado\\FlujoArchivo\\src\\archivosLocal\\";

        try {
            int pto = 8000;
            String dir = "127.0.0.1";
            Socket cl = new Socket(dir, pto);
            System.out.println("Conexion con servidor establecida...");

            File f2 = new File(rutaClienteLocal);
            f2.mkdirs();
            f2.setWritable(true);
            DataInputStream dis = new DataInputStream(cl.getInputStream());
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream());

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Menú:\n");
                System.out.println("1) Subir archivos/carpetas al servidor");//falta poder pasar una carpeta con sus archivos
                System.out.println("2) Descargar archivos/carpetas del servidor");//falta recibir multiples archivos y carpetas con archivos
                System.out.println("0) Salir\n");

                String respuesta = scanner.nextLine();
                int resp = Integer.parseInt(respuesta);

                switch (resp) {
                    case 1:
                        enviaArchivo(cl, dos, dis);
                        break;
                    case 2:
                        solicitarArchivoAlServidor(cl, rutaClienteLocal);
                        break;
                    case 0:
                        System.out.println("\nFinalizando..");
                        dos.writeUTF("FINALIZAR_CLIENTE");
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



public static void enviaArchivo(Socket cl, DataOutputStream dos, DataInputStream dis) {
    try {
        JFileChooser jf = new JFileChooser("C:\\FlujoArchivo_modificado\\FlujoArchivo\\src\\archivosLocal");
        jf.setMultiSelectionEnabled(true);
        jf.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); // Permite seleccionar directorios

        int r = jf.showOpenDialog(null);

        if (r == JFileChooser.APPROVE_OPTION) {
            File[] seleccionados = jf.getSelectedFiles();
            for (File seleccionado : seleccionados) {
                    // Si es un archivo, envía el archivo individual
                    enviarArchivoIndividual(cl, seleccionado, dos);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    private static void enviarArchivoIndividual(Socket cl, File archivo, DataOutputStream dos) {
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(archivo));
            dos.writeUTF("ARCHIVO");
            dos.flush();

            dos.writeUTF(archivo.getName());
            dos.flush();
            dos.writeLong(archivo.length());
            dos.flush();
            System.out.println("Preparandose para enviar el archivo: " + archivo.getName());
            byte[] buffer = new byte[1500];
            int bytesRead;
            while ((bytesRead = dis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
                dos.flush();
            }
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void solicitarArchivoAlServidor(Socket cl, String rutaClienteLocal) throws IOException {
        try {
            DataInputStream dis = new DataInputStream(cl.getInputStream());
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream());

            // Envía una solicitud al servidor para obtener un archivo específico
            dos.writeUTF("SOLICITUD_ARCHIVO");

            System.out.print("Ingrese el nombre del archivo a enviar: ");

            Scanner scanner = new Scanner(System.in);
            String nombre = scanner.nextLine();
            dos.writeUTF(nombre);

            // Recibe el nombre del archivo desde el servidor
            String nombreArchivo = dis.readUTF();

            if (nombreArchivo.equals("ARCHIVO_NO_ENCONTRADO")) {
                System.out.println("El archivo solicitado no se encontró en el servidor.");
            } else {
                recibirArchivoDelServidor(dis, rutaClienteLocal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recibirArchivoDelServidor(DataInputStream dis, String directorioDestino) throws IOException {
        try {
            String nombre = dis.readUTF();

            if (nombre.equals("ARCHIVO_NO_ENCONTRADO")) {
                System.out.println("El archivo solicitado no se encontró en el servidor.");
            } else {
                long Dimension = dis.readLong();
                System.out.println("Comienza descarga del archivo " + nombre + " de " + Dimension + " bytes\n\n");

                File directorio = new File(directorioDestino);
                if (!directorio.exists()) {
                    directorio.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(directorioDestino + File.separator + nombre);
                byte[] buffer = new byte[1500];
                int bytesRead;
                long recibidos = 0;
                int porcentaje = 0;

                while (recibidos < Dimension) {
                    bytesRead = dis.read(buffer);
                    fos.write(buffer, 0, bytesRead);
                    recibidos += bytesRead;
                    porcentaje = (int) ((recibidos * 100) / Dimension);
                    System.out.print("\rRecibido el " + porcentaje + " % del archivo");
                }

                fos.close();
                System.out.println("\nArchivo " + nombre + " recibido y guardado en " + directorioDestino);

                if (nombre.endsWith(".zip")) {
                    String zipFile = "C:\\FlujoArchivo_modificado\\FlujoArchivo\\src\\archivosLocal\\archivo.zip";
                    String destDir = "C:\\FlujoArchivo_modificado\\FlujoArchivo\\src\\archivosLocal";
                    File dir = new File(destDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    byte[] buffer1 = new byte[1024];
                    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
                    ZipEntry ze = zis.getNextEntry();
                    while (ze != null) {
                        String fileName = ze.getName();
                        File newFile = new File(destDir + File.separator + fileName);
                        System.out.println("file unzip : " + newFile.getAbsoluteFile());
                        new File(newFile.getParent()).mkdirs();
                        FileOutputStream fos1 = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos1.write(buffer, 0, len);
                        }
                        fos1.close();
                        zis.closeEntry();
                        ze = zis.getNextEntry();
                    }
                    zis.closeEntry();
                    zis.close();
                    File folder = new File(zipFile);
                    folder.delete();
                    System.out.println("Carpeta ZIP descomprimida exitosamente en " + destDir);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
