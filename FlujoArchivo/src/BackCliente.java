
package Cliente;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class BackCliente {
    
    public static Socket creaSocket() throws IOException{
        int pto = 8000;
            BufferedReader entradaTeclado = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.ISO_8859_1));
            InetAddress host = null; 
            String dir; 
            
            try{                
                System.out.println("Teclea la direccion del servidor, en un formato valido :"); 
                dir = entradaTeclado.readLine();  
                host = InetAddress.getByName(dir); //pasamos a formato de IP
            }catch(Exception n){
                System.out.println("Error al encontrar la IP, intente de nuevo");                                                
            }
            Socket temp = new Socket(host,pto);            
            System.out.println("Conexion establecida con el servidor \n"); 
        
        return temp;
    }//CrearSocket

    public static void enviaSolicitud(DataOutputStream dos, int i) throws IOException{
        dos.writeInt(i);
        dos.flush();
    }
    
    public static void enviaRuta(DataOutputStream dos, String path) throws IOException{
        dos.writeUTF(path);
        dos.flush();
    }

    public static String ObtenerRutaRemoto(DataInputStream dis) throws IOException{
        String temp;
        temp=dis.readUTF();
        return temp;
    }

    public static String[] ObtenerListaRemoto(DataInputStream dis) throws IOException {
        int numArchivos = dis.readInt();
        String[] temp = new String[numArchivos];
        boolean[] type = new boolean[numArchivos];
        for(int i=0;i<numArchivos;i++){
            if(dis.readBoolean()){ //si es carpeta, agregamos una diagonal al final
                temp[i]=dis.readUTF()+System.getProperty("file.separator");
                System.out.println(temp[i]);
            }else{//si es archivo, se manda tal cual
                temp[i]= dis.readUTF();
                System.out.println(temp[i]);
            }
        }
        return temp;
    }

    public static void obtenerArchivo(DataInputStream dis, String ruta_archivo) throws IOException {
      
        String nombre = dis.readUTF();
        long Dimension = dis.readLong();

        System.out.println("Comienza descarga del archivo "+nombre+" de "+Dimension+" bytes\n\n");
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(ruta_archivo+nombre));
        long recibidos=0;
        int l, porcentaje;
        while(recibidos<Dimension){
            byte[] b = new byte[1500]; //en paquetes de 1500 bytes
            l = dis.read(b);
            System.out.println("leidos: "+l);
            dos.write(b,0,l);
            dos.flush();
            recibidos = recibidos + l;
            porcentaje = (int)((recibidos*100)/Dimension);
            System.out.println("\rRecibido el "+ porcentaje +" % del archivo");
        }
        System.out.println("\nTransmisión finalizada \n");
        dos.close();
    }

    public static void obtenerMultiplesArchivos(DataOutputStream dos, DataInputStream dis, String[] listaArchivos, String rutaActualArchivosRemotos, String rutaLocalGuardado ) throws IOException{

        for (String archivo : listaArchivos) {

            if (archivo.endsWith(System.getProperty("file.separator"))) {//si es carpeta

                String CarpetaADescargar = archivo;
                String rutaCarpeta = rutaActualArchivosRemotos + CarpetaADescargar;
                enviaSolicitud(dos, 1); //abrimos la carpeta 
                enviaRuta(dos, rutaCarpeta);

                //Recibimos la lista de archivos de la carpeta que queremos descargar
                String[] ArchivosHijos;
                ArchivosHijos = ObtenerListaRemoto(dis);
                //Creamos un directorio con el mismo nombre en local
                String RutaLocalDir = rutaLocalGuardado + CarpetaADescargar;

                File carpeta = new File(RutaLocalDir.substring(0, RutaLocalDir.length() - 1)); 
                boolean f = carpeta.mkdir();
                if (f)
                    System.out.println("Directorio local creado" + RutaLocalDir.substring(0, RutaLocalDir.length() - 1));
                else
                    System.out.println("Error en descargar!");

                //Recursion
                //descargamos todos los archivos de la carpeta
                obtenerMultiplesArchivos(dos, dis,
                        ArchivosHijos,
                        rutaCarpeta,
                        RutaLocalDir
                );
                enviaSolicitud(dos, 5); //regresamos a la carpeta padre
                ObtenerRutaRemoto(dis); //ruta del padre  
                ObtenerListaRemoto(dis);//rchivos descargados
            } else { //si es archivo
                enviaSolicitud(dos, 3); //descargar archivo
                String nameElementoADescargar = archivo;
                enviaRuta(dos, rutaActualArchivosRemotos + nameElementoADescargar);//mandamos a server ruta del archivo a descargar
                obtenerArchivo(dis, rutaLocalGuardado);//obtenemos el archivo
            }
        }//for
    }

    public static void enviaArchivo(DataOutputStream dos, File file) throws IOException {//subir archivo al server

        long longitud = file.length();
        String nombre_archivo = file.getName();
        String path = file.getAbsolutePath();
        System.out.println("Preparandose pare enviar archivo "+path+"\n\n");
        DataInputStream dis = new DataInputStream(new FileInputStream(path));
        dos.writeUTF(nombre_archivo);
        dos.writeLong(longitud);
        dos.flush();

        long enviados = 0;
        int l,porcentaje;
        while(enviados<longitud){
            byte[] b = new byte[1500];
            l=dis.read(b);
            System.out.println("enviados: "+l);
            dos.write(b,0,l);
            dos.flush();
            enviados = enviados + l;
            porcentaje = (int)((enviados*100)/longitud);
            System.out.print("\rEnviado el "+porcentaje+" % del archivo");
        }
        System.out.println("\nArchivo enviado..");
        dis.close();
   }
 
    public static void enviaMultiplesArchivos(DataOutputStream dos, DataInputStream dis, File archivosAEnviar[], String rutaActual) throws IOException{
        
        for(int i=0;i<archivosAEnviar.length;i++){
            File archivo=archivosAEnviar[i];
            if(archivo.isFile()){ //si es archivo 
                enviaSolicitud(dos,2); //subir archivo                      
                enviaArchivo(dos, archivo);
                ObtenerListaRemoto(dis);
            }else { //si es carpeta 
                    enviaSolicitud(dos,7); //creamos carpeta
                    enviaRuta(dos, archivo.getName()); //Enviamos al servidor el nombre de la carpeta a crear
 
                    enviaSolicitud(dos,1); //abrimos la carpeta creada
                    rutaActual=rutaActual+archivo.getName()+System.getProperty("file.separator"); //ruta creada en el server
                    enviaRuta(dos, rutaActual); 
                    ObtenerListaRemoto(dis);   
                    File hijosCarpeta[]=archivo.listFiles();
                    //**Recursion para enviar los hijos de los hijos
                    enviaMultiplesArchivos(dos, dis, hijosCarpeta, rutaActual);
                    enviaSolicitud(dos,5); //salimos de la carpeta
                    rutaActual=ObtenerRutaRemoto(dis); 
                    ObtenerListaRemoto(dis);              
            }
        }           
    }
    
    public static void eliminarDirectorioLocal(File file){
        File[] contenidoDir = file.listFiles();
        if(contenidoDir != null){
            for(File child : contenidoDir){
                if(child.isDirectory()){ //si el hijo es directorio
                    //**Recursion para eliminar
                    eliminarDirectorioLocal(child);
                }
                child.delete();
            }
        }        
        file.delete(); //Elimina directorio principal(padre)
    }

    public static void eliminarArchivoLocal(String nombre){
        File temp = new File(nombre);
        if(temp.isDirectory()){
            eliminarDirectorioLocal(temp);
        }else{
            if(temp.delete()){
                System.out.println("Elemento eliminado "+temp.getName()+"\n");
            }else{
                System.out.println("No se pudo eliminar el elemento:c " +temp.getName()+"\n");
            }
        }
    }
    
    public static void eliminarMultiplesArchivosRemotos(DataOutputStream dos, String listaArchivos[], String rutaActualArchivos){
        try {
            for(int i=0; i<listaArchivos.length;i++){
                String archivoAEliminar=listaArchivos[i];
                System.out.println("Eliminando el archivo"+archivoAEliminar+"\n");
                enviaSolicitud(dos,4); //eliminar archivo 
                enviaRuta(dos, rutaActualArchivos + archivoAEliminar);                                   
            }
        } catch (IOException ex) {
            
        }
    }

}//BackCliente
