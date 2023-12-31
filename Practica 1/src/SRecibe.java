
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

                    if ((solicitud.equals("SOLICITUD_ARCHIVO")) || (solicitud.equals("SOLICITUD_CARPETA"))) {
                        enviarArchivoAlCliente(dos, RutaServidor, cl);
                    } else if (solicitud.equals("ARCHIVO")) {
                        recibirArchivoIndividual(dis, RutaServidor);
                    } else if (solicitud.equals("CARPETA")) {
                        recibirCarpeta(dis, RutaServidor, cl);
                    } else if (solicitud.equals("LISTAR_CARPETA_SERVIDOR")) {
                        listarDirectorioServidor(dos, RutaServidor);
                    } else if (solicitud.equals("ELIMINAR_ELEMENTO")) {
                        eliminarElemento(dos, dis, RutaServidor);
                    } else if (solicitud.equals("RENOMBRAR_ARCHIVO_CARPETA")) {
                        renombrarArchivoOCarpeta(dos, dis, RutaServidor);
                    } else if (solicitud.equals("FINALIZAR_CLIENTE")) {
                        break label;
                    } else if (solicitud.equals("CREAR_CARPETA_SERVIDOR")) {
                        crearCarpetaEnServidor(dos, dis, RutaServidor);
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
            if (archivo.isDirectory()) {
                enviarDirectorio(cl, archivo);
            } else {
                enviarArchivoIndividual(cl, archivo);
            }
        } else {
            System.out.println("El archivo especificado no existe en la ubicación proporcionada.");
        }
    }

    private static void enviarDirectorio(Socket cl, File directorio) throws IOException {
        String sourceFolder = directorio.getAbsolutePath();
        String zipFilePath = "C:\\FlujoArchivo_modificado\\FlujoArchivo\\archivosServidor\\archivo.zip";
        FileOutputStream fos = new FileOutputStream(zipFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File folderToCompress = new File(sourceFolder);
        zipFolder(folderToCompress, folderToCompress.getName(), zipOut);
        zipOut.close();
        System.out.println("Carpeta comprimida exitosamente en " + zipFilePath);
        File folder = new File(zipFilePath);
        enviarArchivoIndividual(cl, folder);
        folder.delete();
    }

    private static void zipFolder(File folderToZip, String baseName, ZipOutputStream zipOut) throws IOException {
        File[] files = folderToZip.listFiles();
        byte[] buffer = new byte[1024];

        for (File file : files) {
            if (file.isDirectory()) {
                zipFolder(file, baseName + "/" + file.getName(), zipOut);
            } else {
                FileInputStream fis = new FileInputStream(file);
                zipOut.putNextEntry(new ZipEntry(baseName + "/" + file.getName()));

                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, length);
                }

                fis.close();
            }
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

    private static void enviarCarpeta(DataInputStream dis, String directorioDestino) {
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

    private static void recibirCarpeta(DataInputStream dis, String directorioDestino, Socket cl) {
        try {
            String zipFile = "C:\\FlujoArchivo_modificado\\FlujoArchivo\\archivosServidor\\archivo.zip";
            String destDir = "C:\\FlujoArchivo_modificado\\FlujoArchivo\\archivosServidor";
            File dir = new File(destDir);
            
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("file unzip : " + newFile.getAbsoluteFile());
            
            
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            
            zis.closeEntry();
            zis.close();
            System.out.println("Carpeta ZIP descomprimida exitosamente en " + destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listarDirectorioServidor(DataOutputStream dos, String rutaServidor) {
        try {

            File carpetaServidor = new File(rutaServidor);

            if (carpetaServidor.exists() && carpetaServidor.isDirectory()) {
                File[] archivosYCarpetas = carpetaServidor.listFiles();

                if (archivosYCarpetas != null) {
                    // Indica que se encontró la carpeta en el servidor
                    dos.writeUTF("CARPETA_ENCONTRADA");
                    dos.flush();

                    // Envía la cantidad de archivos y carpetas
                    dos.writeInt(archivosYCarpetas.length);
                    dos.flush();

                    for (File archivoOCarpeta : archivosYCarpetas) {
                        // Envía el nombre de archivo o carpeta
                        dos.writeUTF(archivoOCarpeta.getName());
                        dos.flush();

                        // Si es una carpeta y tiene archivos internos, envía la lista de archivos
                        if (archivoOCarpeta.isDirectory() && archivoOCarpeta.listFiles() != null) {
                            dos.writeInt(archivoOCarpeta.listFiles().length);
                            dos.flush();
                            for (File archivoInterno : archivoOCarpeta.listFiles()) {
                                dos.writeUTF((archivoInterno.isDirectory() ? "/" : "") + archivoInterno.getName());
                                dos.flush();
                            }
                        } else {
                            // Si no es carpeta o no tiene archivos internos, envía 0
                            dos.writeInt(0);
                            dos.flush();
                        }
                    }
                } else {
                    // Indica que la carpeta en el servidor está vacía
                    dos.writeUTF("CARPETA_VACIA");
                    dos.flush();

                }
            } else {
                // Indica que la carpeta en el servidor no se encontró
                dos.writeUTF("CARPETA_NO_ENCONTRADA");
                dos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listarDirectorioRecursivo(File directorio, DataOutputStream dos, String prefijo) {
        try {
            File[] archivosYCarpetas = directorio.listFiles();

            if (archivosYCarpetas != null) {
                for (File archivoOcarpeta : archivosYCarpetas) {
                    dos.writeUTF(prefijo + (archivoOcarpeta.isDirectory() ? "Carpeta: " : "Archivo: ") + archivoOcarpeta.getName());
                    dos.flush();

                    if (archivoOcarpeta.isDirectory()) {
                        listarDirectorioRecursivo(archivoOcarpeta, dos, prefijo + "  ");
                    }
                }
            } else {
                dos.writeUTF("El directorio está vacío.");
                dos.flush();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void renombrarArchivoOCarpeta(DataOutputStream dos, DataInputStream dis, String rutaServidor) {
        try {
            String rutaRelativa = dis.readUTF(); // Ruta completa del archivo o carpeta
            String nuevoNombre = dis.readUTF(); // Nuevo nombre del archivo o carpeta

            File elemento = new File(rutaServidor + File.separator + rutaRelativa);

            if (elemento.exists()) {
                File nuevaRuta = new File(elemento.getParentFile(), nuevoNombre);

                if (elemento.renameTo(nuevaRuta)) {
                    dos.writeUTF("RENOMBRADO_EXITOSAMENTE");
                    dos.flush();
                    System.out.println("Elemento renombrado con éxito.");
                } else {
                    dos.writeUTF("NO_SE_PUDO_RENOMBRAR");
                    dos.flush();
                    System.out.println("No se pudo renombrar el elemento.");
                }
            } else {
                dos.writeUTF("ELEMENTO_NO_ENCONTRADO");
                dos.flush();
                System.out.println("El elemento no se encontró en la ruta especificada.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void eliminarElemento(DataOutputStream dos, DataInputStream dis, String rutaServidor) {
        try {
            String nombreElemento = dis.readUTF();
            File elemento = new File(rutaServidor + File.separator + nombreElemento);

            if (eliminarRecursivamente(elemento)) {
                // Envía una confirmación al cliente de que el elemento se ha eliminado correctamente
                dos.writeUTF("ELIMINADO_EXITOSAMENTE");
                dos.flush();
            } else {
                // Envía una respuesta al cliente si no se pudo eliminar el elemento
                dos.writeUTF("NO_SE_PUDO_ELIMINAR");
                dos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean eliminarRecursivamente(File elemento) {
        if (elemento == null || !elemento.exists()) {
            return false;
        }

        if (elemento.isDirectory()) {
            File[] archivos = elemento.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    eliminarRecursivamente(archivo);
                }
            }
        }

        return elemento.delete();
    }

    private static void crearCarpetaEnServidor(DataOutputStream dos, DataInputStream dis, String rutaServidor) {
        try {
            String rutaCarpeta = dis.readUTF();
            File nuevaCarpeta = new File(rutaServidor + File.separator + rutaCarpeta);

            if (nuevaCarpeta.mkdirs()) { // Utiliza mkdirs() para crear subdirectorios si es necesario
                dos.writeUTF("CARPETA_CREADA");
                dos.flush();
                System.out.println("Carpeta creada con éxito en el servidor: " + nuevaCarpeta.getAbsolutePath());
            } else {
                dos.writeUTF("ERROR_CREAR_CARPETA");
                dos.flush();
                System.out.println("No se pudo crear la carpeta en el servidor.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
