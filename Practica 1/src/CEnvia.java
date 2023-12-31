
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
                System.out.println("1) Ver carpeta local");
                System.out.println("2) Ver carpeta remota");
                System.out.println("3) Subir archivos/carpetas al servidor");//falta poder pasar una carpeta con sus archivos
                System.out.println("4) Descargar archivos/carpetas del servidor");//falta recibir multiples archivos y carpetas con archivos
                System.out.println("5) Renombrar archivos/carpetas local");
                System.out.println("6) Renombrar archivos/carpetas remota");
                System.out.println("7) Eliminar archivos/carpetas local");
                System.out.println("8) Eliminar archivos/carpetas remota");
                System.out.println("9) Crear carpetas en local");
                System.out.println("10) Crear carpetas en remoto");
                System.out.println("0) Salir\n");

                String respuesta = scanner.nextLine();
                int resp = Integer.parseInt(respuesta);

                switch (resp) {
                    case 1:
                        listarArchivosYCarpetasRecursivamente(rutaClienteLocal);
                        break;
                    case 2:
                        listarDirectorioServidor(cl, dos, dis);
                        break;
                    case 3:
                        enviaArchivo(cl, dos, dis);
                        break;
                    case 4:
                        solicitarArchivoAlServidor(cl, rutaClienteLocal);
                        break;
                    case 5:
                        renombrarArchivo(rutaClienteLocal);
                        break;
                    case 6:
                        solicitarRenombrarArchivoOCarpeta(cl, dos, dis);
                        break;
                    case 7:
                        eliminarArchivoLocal(rutaClienteLocal);
                        break;
                    case 8:
                        eliminarElementoServidor(cl, dos, dis);
                        break;
                    case 9:
                        crearCarpetaLocal(rutaClienteLocal);
                        break;
                    case 10:
                        crearCarpetaServidor(cl, dos, dis);
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

    public static void listarArchivosYCarpetasRecursivamente(String rutaClienteLocal) {
        File directorio = new File(rutaClienteLocal);

        if (directorio.exists() && directorio.isDirectory()) {
            System.out.println("Carpeta: " + directorio.getName());
            listarArchivosYCarpetasRecursivamenteAux(directorio, "");
        } else {
            System.out.println("La ruta no es un directorio válido.");
        }
    }

    private static void listarArchivosYCarpetasRecursivamenteAux(File directorio, String prefijo) {
        File[] archivosYCarpetas = directorio.listFiles();
        if (archivosYCarpetas != null) {
            for (File archivoOcarpeta : archivosYCarpetas) {
                System.out.println(prefijo + (archivoOcarpeta.isDirectory() ? "Carpeta: " : "/") + archivoOcarpeta.getName());

                if (archivoOcarpeta.isDirectory()) {
                    listarArchivosYCarpetasRecursivamenteAux(archivoOcarpeta, prefijo + "  ");
                }
            }
        } else {
            System.out.println("El directorio está vacío.");
        }
    }

    public static void listarDirectorioServidor(Socket cl, DataOutputStream dos, DataInputStream dis) {
        try {

            // Envía una solicitud al servidor para listar el directorio del servidor
            dos.writeUTF("LISTAR_CARPETA_SERVIDOR");
            dos.flush();

            // Recibe y muestra la respuesta del servidor
            String respuesta = dis.readUTF();

            if (respuesta.equals("CARPETA_ENCONTRADA")) {
                int numArchivos = dis.readInt();
                System.out.println("Contenido del directorio del servidor:");

                for (int i = 0; i < numArchivos; i++) {
                    String nombreArchivo = dis.readUTF();
                    int numArchivosInternos = dis.readInt();

                    // Agrega "/" antes del nombre del archivo si no es una carpeta
                    if (numArchivosInternos == -1 && !nombreArchivo.startsWith("/")) {
                        nombreArchivo = "/" + nombreArchivo;
                    }

                    System.out.println(nombreArchivo);

                    // Si es carpeta y tiene archivos internos, muestra los archivos internos
                    if (numArchivosInternos > 0) {
                        listarArchivosInternos(dis, numArchivosInternos);
                    }
                }
            } else if (respuesta.equals("CARPETA_VACIA")) {
                System.out.println("El directorio del servidor está vacío.");
            } else if (respuesta.equals("CARPETA_NO_ENCONTRADA")) {
                System.out.println("El directorio del servidor no se encontró.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al intentar listar");
        }
    }

    private static void listarArchivosInternos(DataInputStream dis, int numArchivos) throws IOException {
        for (int i = 0; i < numArchivos; i++) {
            String archivoInterno = dis.readUTF();

            // Agrega "/" antes del nombre del archivo si no es una carpeta
            if (!archivoInterno.startsWith("/")) {
                archivoInterno = "/" + archivoInterno;
            }

            System.out.println("  " + archivoInterno);
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
                if (seleccionado.isDirectory()) {
                    // Comprimir carpeta y enviar
                    String sourceFolder = seleccionado.getAbsolutePath();
                    String zipFilePath = "C:\\FlujoArchivo_modificado\\FlujoArchivo\\src\\archivosLocal\\archivo.zip";
                    FileOutputStream fos = new FileOutputStream(zipFilePath);
                    ZipOutputStream zipOut = new ZipOutputStream(fos);
                    File folderToCompress = new File(sourceFolder);
                    zipFolder(folderToCompress, folderToCompress.getName(), zipOut);
                    zipOut.close();
                    System.out.println("Carpeta comprimida exitosamente en " + zipFilePath);
                    File folder = new File(zipFilePath);
                    enviarArchivoIndividual(cl, folder, dos);
                    folder.delete();
                    dos.writeUTF("CARPETA");
                    dos.flush();
                } else {
                    // Si es un archivo, envía el archivo individual
                    enviarArchivoIndividual(cl, seleccionado, dos);
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
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
                    // create output directory if it doesn't exist
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    // buffer for read and write data to file
                    byte[] buffer1 = new byte[1024];
                    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
                    ZipEntry ze = zis.getNextEntry();
                    while (ze != null) {
                        String fileName = ze.getName();
                        File newFile = new File(destDir + File.separator + fileName);
                        System.out.println("file unzip : " + newFile.getAbsoluteFile());
                        // create all non exists folders
                        // else you will hit FileNotFoundException for compressed folder
                        new File(newFile.getParent()).mkdirs();
                        FileOutputStream fos1 = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos1.write(buffer, 0, len);
                        }
                        fos1.close();
                        // close this ZipEntry
                        zis.closeEntry();
                        ze = zis.getNextEntry();
                    }
                    // close last ZipEntry
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

    public static void renombrarArchivo(String rutaClienteLocal) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el nombre actual del archivo o carpeta a renombrar: ");
        String nombreActual = scanner.nextLine();

        // Llama a la función recursiva para buscar y renombrar el elemento
        if (renombrarArchivoRecursivo(new File(rutaClienteLocal), nombreActual)) {
            System.out.println("Elemento renombrado con éxito.");
        } else {
            System.out.println("No se encontró el elemento en la ruta especificada.");
        }
    }

    // Función recursiva para buscar y renombrar un archivo o carpeta
    private static boolean renombrarArchivoRecursivo(File directorio, String nombreActual) {
        File[] elementos = directorio.listFiles();

        if (elementos != null) {
            for (File elemento : elementos) {
                if (elemento.getName().equals(nombreActual)) {
                    Scanner scanner = new Scanner(System.in);
                    System.out.print("Ingrese el nuevo nombre para el archivo o carpeta: ");
                    String nuevoNombre = scanner.nextLine();

                    File elementoNuevo = new File(elemento.getParentFile(), nuevoNombre);

                    if (elemento.renameTo(elementoNuevo)) {
                        return true; // Elemento renombrado con éxito
                    } else {
                        return false; // No se pudo renombrar el elemento
                    }
                }

                if (elemento.isDirectory()) {
                    // Si es una carpeta, busca recursivamente dentro de ella
                    if (renombrarArchivoRecursivo(elemento, nombreActual)) {
                        return true; // Elemento renombrado con éxito
                    }
                }
            }
        }

        return false; // No se encontró el elemento en la ruta especificada
    }

    public static void solicitarRenombrarArchivoOCarpeta(Socket cl, DataOutputStream dos, DataInputStream dis) {
        try {
            dos.writeUTF("RENOMBRAR_ARCHIVO_CARPETA");
            dos.flush();

            Scanner scanner = new Scanner(System.in);
            System.out.print("Ingrese la ruta completa del archivo o carpeta a renombrar en el servidor: ");
            String rutaRelativa = scanner.nextLine();
            dos.writeUTF(rutaRelativa);
            dos.flush();

            System.out.print("Ingrese el nuevo nombre del archivo o carpeta: ");
            String nuevoNombre = scanner.nextLine();
            dos.writeUTF(nuevoNombre);
            dos.flush();

            String respuesta = dis.readUTF();

            if (respuesta.equals("RENOMBRADO_EXITOSAMENTE")) {
                System.out.println("Elemento renombrado con éxito en el servidor.");
            } else if (respuesta.equals("NO_SE_PUDO_RENOMBRAR")) {
                System.out.println("No se pudo renombrar el elemento en el servidor.");
            } else if (respuesta.equals("ELEMENTO_NO_ENCONTRADO")) {
                System.out.println("El elemento no se encontró en la ruta especificada.");
            } else {
                System.out.println("Respuesta desconocida del servidor: " + respuesta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// Función para eliminar un archivo o carpeta en la rutaClienteLocal
    public static void eliminarArchivoLocal(String rutaClienteLocal) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingresa el nombre del archivo/carpeta a eliminar, si se encuentra dentro de otra carpeta, indicarlo");
        System.out.print("Ejemplo: nombrecarpeta/archivo.txt");
        String rutaRelativa = scanner.nextLine();

        File elemento = new File(rutaClienteLocal + File.separator + rutaRelativa);

        if (elemento.exists()) {
            if (eliminarRecursivamente(elemento)) {
                System.out.println("Elemento eliminado con éxito.");
            } else {
                System.out.println("No se pudo eliminar el elemento.");
            }
        } else {
            System.out.println("El elemento no se encontró en la ruta especificada.");
        }
    }

    // Función recursiva para eliminar un archivo o carpeta
    private static boolean eliminarRecursivamente(File elemento) {
        if (elemento.isDirectory()) {
            File[] elementos = elemento.listFiles();

            if (elementos != null) {
                for (File subElemento : elementos) {
                    if (!eliminarRecursivamente(subElemento)) {
                        return false; // No se pudo eliminar subelemento
                    }
                }
            }
        }

        return elemento.delete(); // Elimina el elemento actual
    }

    public static void eliminarElementoServidor(Socket cl, DataOutputStream dos, DataInputStream dis) {
        try {

            // Envía una solicitud al servidor para eliminar un archivo o carpeta
            dos.writeUTF("ELIMINAR_ELEMENTO");
            dos.flush();

            // Solicita al usuario la ruta del elemento a eliminar en el servidor
            System.out.print("Ingrese la ruta relativa del elemento a eliminar en el servidor: ");
            Scanner scanner = new Scanner(System.in);
            String rutaRelativa = scanner.nextLine();
            dos.writeUTF(rutaRelativa);
            dos.flush();

            // Recibe la respuesta del servidor
            String respuesta = dis.readUTF();

            if (respuesta.equals("ELIMINADO_EXITOSAMENTE")) {
                System.out.println("Elemento eliminado con éxito en el servidor.");
            } else if (respuesta.equals("NO_SE_PUDO_ELIMINAR")) {
                System.out.println("No se pudo eliminar el elemento en el servidor.");
            } else {
                System.out.println("Respuesta desconocida del servidor: " + respuesta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void crearCarpetaLocal(String rutaClienteLocal) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese la ruta relativa de la carpeta a crear (incluyendo subcarpetas, ej: carpeta1/carpeta2): ");
        String rutaRelativa = scanner.nextLine();

        File nuevaCarpeta = new File(rutaClienteLocal + File.separator + rutaRelativa);

        if (nuevaCarpeta.mkdirs()) {
            System.out.println("Carpeta creada con éxito en " + nuevaCarpeta.getAbsolutePath());
        } else {
            System.out.println("No se pudo crear la carpeta.");
        }
    }

    public static void crearCarpetaServidor(Socket cl, DataOutputStream dos, DataInputStream dis) {
        try {
            dos.writeUTF("CREAR_CARPETA_SERVIDOR");
            dos.flush();

            Scanner scanner = new Scanner(System.in);
            System.out.print("Ingrese la ruta completa de la carpeta a crear en el servidor (puedes incluir subdirectorios): ");
            String rutaCarpeta = scanner.nextLine();

            // Envía la ruta de la carpeta al servidor
            dos.writeUTF(rutaCarpeta);
            dos.flush();

            // Recibe la respuesta del servidor
            String respuesta = dis.readUTF();

            if (respuesta.equals("CARPETA_CREADA")) {
                System.out.println("Carpeta creada con éxito en el servidor.");
            } else if (respuesta.equals("ERROR_CREAR_CARPETA")) {
                System.out.println("No se pudo crear la carpeta en el servidor.");
            } else {
                System.out.println("Respuesta desconocida del servidor: " + respuesta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
