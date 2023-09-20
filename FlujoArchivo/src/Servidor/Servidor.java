
package Servidor;


import static Cliente.BackCliente.eliminarDirectorioLocal;
import java.io.*;
import java.net.*;

public class Servidor {

    public static void main(String[] args) {
        // Creamos carpeta en el servidor
        final String RutaRaizServer="."+System.getProperty("file.separator")+"archivosServidor"+System.getProperty("file.separator"); 
        
        File DirectorioServer=new File(RutaRaizServer);        
        DirectorioServer.mkdir();                              
        System.out.println("La carpeta del servidor está lista para usarse");
    
        try{
            int pto =8000;                                                           
            ServerSocket servidor = new ServerSocket(pto);
            System.out.println("Servidor iniciado en el puerto "+pto);                                                          
           
            while(true){ //ciclo "Esperando nuevo cliente"
                System.out.println("Esperando un cliente...");
                Socket cliente = servidor.accept(); 
                DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());
                DataInputStream dis = new DataInputStream(cliente.getInputStream());      
                System.out.println("Cliente conectado desde "+cliente.getInetAddress()+":"+cliente.getPort()); 
                
                String RutaActualCliente=RutaRaizServer;
                EnviaRutaRemoto(dos, RutaActualCliente);
                        
                ///Enviamos la lista de nombres de archivos de la carpeta del servidor
                EnviaListaRemoto(dos, RutaActualCliente);
                                                
                int solicitud;
                                                          
                label:
                while(true){//ciclo "cliente conectado"

                    solicitud=recibesolicitud(dis);

                    switch (solicitud) {
                        case 0: //La conexion se inicio
                            break;
                        case 1:  //Abrir carpeta
                            // ******** Cambiar a visualizar archivos/carpetas ****************
                            System.out.println("Case 1: Abrir carpeta");
                            //recibimos la dirección de la carpeta que se quiere listar
                            RutaActualCliente = recibeRuta(dis); //actualizamos la ruta en la que se encuentra el cliente
                            System.out.println(RutaActualCliente);
                            EnviaListaRemoto(dos, RutaActualCliente);
                            System.out.println("Termino case 1");
                            break;
                        case 2:
                            System.out.println("Case 2: Subir archivo/carpeta");
                            obtenerArchivo(dis, RutaActualCliente);
                            EnviaListaRemoto(dos, RutaActualCliente);//"refresh" a la carpeta
                            break;
                        case 3: {
                            System.out.println("Case 3: Descargar archivo/carpeta");
                            String direccionArchivo = recibeRuta(dis);
                            File archivoSolicitado = new File(direccionArchivo); //Abrimos el archivo deseado
                            enviaArchivo(dos, archivoSolicitado); //Mandamos a cliente el archivo
                            break;
                        }
                        case 4: {
                            System.out.println("Case 4: Eliminar archivo/carpeta");
                            String direccionArchivo = recibeRuta(dis);
                            eliminarArchivo(direccionArchivo);
                            EnviaListaRemoto(dos, RutaActualCliente);//"refresh" a la carpeta
                            break;
                        }
                        // *********** Agregar Renombrar archivos/carpetas ************
                        // *********** Agregar Copiar archivos/carpetas ***************
                        // *********** Agregar Crear carpetas *************************
                        case 5:
                            //****NOTA****
                            //si la carpeta actual es la creada en el server, no se puede regresar mas
                            // ESTA NO ES NECESARIA
                            System.out.println("Case 5: volver a carpeta anterior");
                            //Verificamos que no supere a la ruta inicial (rutaCarpetaServer)
                            if (RutaActualCliente.equals(RutaRaizServer)) {
                                RutaActualCliente = RutaRaizServer;
                            } else {
                                //Obtenemos el padre y actualizamos la ruta actual a esa carpeta
                                RutaActualCliente = new File(RutaActualCliente).getParent() + System.getProperty("file.separator"); 
                            }
                            EnviaRutaRemoto(dos, RutaActualCliente);
                            System.out.println("Ruta actual: " + RutaActualCliente);
                            EnviaListaRemoto(dos, RutaActualCliente);
                            break;
                        case 6:
                            System.out.println("Case 6: cerrar la conexión");
                            dis.close();
                            dos.close();
                            cliente.close();
                            System.out.println("Conexión finalizada, vuelva pronto:D ");
                            break label;//para romper el ciclo y dejar de atender al cliente actual
                        case 7:
                            //****Nota***
                            //Si quiere subirse una carpeta al servidor esta primero debe crearse
                            System.out.println("Case 7: crear una carpeta");                           
                            String nombre = RutaActualCliente+recibeRuta(dis); //recibimos nombre de la carpeta a crear
                            System.out.println("Carpeta que se creara: "+ nombre);
                            File carpeta = new File(nombre);
                            if (carpeta.mkdir())
                                System.out.println("Creado con exito!");
                            else
                                System.out.println("Error en crear la carpeta a crear!");
                        break;
                            
                        case 8:
                            EnviaListaRemoto(dos, RutaActualCliente);//refresh de la carpeta                          
                        break;
                    }
                }//ciclo "cliente conectado"
                cliente.close();
                System.out.println("Cliente: "+ cliente.getInetAddress()+ " ha abandonado la conexión\n\n\n\n");
            }//ciclo "Esperando nuevo cliente"
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }//main 
    
    
    public static void EnviaRutaRemoto(DataOutputStream dos, String ruta) throws IOException{
     dos.writeUTF(ruta);
     dos.flush();
    }
    public static String recibeRuta(DataInputStream dis) throws IOException{ //recibe direccion_archivo del cliente
        String rutaTemp;
        rutaTemp= dis.readUTF();
        return rutaTemp;
    }   
    public static int recibesolicitud(DataInputStream dis) throws IOException{ 
        int solicitud;
        solicitud=dis.readInt();
        return solicitud;
    }
    public static void EnviaListaRemoto(DataOutputStream dos,String ruta) throws IOException{ 
        File localFiles = new File(ruta);
        File[] listaArchivos = localFiles.listFiles();
        String nombre;
        boolean esDir; //si es directorio
        int length = listaArchivos.length;
        dos.writeInt(length);
        dos.flush();
        for(File file: listaArchivos){
            esDir = file.isDirectory();
            nombre=file.getName();
            dos.writeBoolean(esDir);
            dos.flush();
            dos.writeUTF(nombre);
            dos.flush();
        }       
    }

    public static void enviaArchivo(DataOutputStream dos, File file) throws IOException {
        long longitud = file.length();
        String nombre_archivo = file.getName();
        String path = file.getPath();

        System.out.println("Preparandose pare enviar archivo "+path+"\n\n");

        DataInputStream dis = new DataInputStream(new FileInputStream(path));
        dos.writeUTF(nombre_archivo);//Se envía el nombre del archivo
        dos.flush();
        dos.writeLong(longitud);//Se utiliza la longitud del archivo
        dos.flush();

        long enviados = 0;
        int l,porcentaje;
        while(enviados<longitud){//Se utiliza la longitud del archivo
            byte[] b = new byte[1500];
            l=dis.read(b);
            System.out.println("enviados: "+l);
            dos.write(b,0,l);
            dos.flush();
            enviados = enviados + l;
            porcentaje = (int)((enviados*100)/longitud);
            System.out.println("\rEnviado el "+porcentaje+" % del archivo");
        }
        System.out.println("\nArchivo enviado..");
        dis.close();
    }
  
    public static void obtenerArchivo(DataInputStream dis, String Ruta_Archivo) throws IOException {
        //ruta_archivo es la carpeta del servidor en donde se guardará el archivo que ha enviado el cliente
        String nombre = dis.readUTF();
        long Dimension = dis.readLong();
        System.out.println("Comienza descarga del archivo "+nombre+" de "+Dimension+" bytes\n\n");
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(Ruta_Archivo+nombre));
        long recibidos=0;
        int l, porcentaje;
        while(recibidos<Dimension){
            byte[] b = new byte[1500];
            l = dis.read(b);
            System.out.println("leidos: "+l);
            dos.write(b,0,l);
            dos.flush();
            recibidos = recibidos + l;
            porcentaje = (int)((recibidos*100)/Dimension);
            System.out.println("\rRecibido el "+ porcentaje +" % del archivo");
        }
        dos.close();
    }

    public static void eliminarDirectorio(File file){  
        File[] contenidoDir = file.listFiles();
        if(contenidoDir != null){
            for(File child : contenidoDir){                
               if(child.isDirectory()){ 
                   eliminarDirectorioLocal(child);                   
               }
               child.delete();                              
            }
        }
        file.delete(); // Se Elimina el directorio padre
    }

    public static void eliminarArchivo(String nombre){
        File temp = new File(nombre);
        if(temp.isDirectory()){
            eliminarDirectorio(temp);
        }else{
            if(temp.delete()){
                System.out.println("Elemento eliminado\n");
            }else{
                System.out.println("No se pudo eliminar elemento \n");
            }
        }
    }
}//Servidorcito