
import java.net.*;
import java.io.*;

public class S1 {
    public static void main(String[] args) throws IOException{
      final String rutaRemota="."+"/"+"remoto"+"/";
      
      String rutaLocal="."+"/"+"local"+"/";
      
     /* File local=new File(rutaServer);
      local.mkdir();  */                            
      System.out.println("Carpeta local lista...");
      
      
                    
        //2 servidores y 2 clientes, 1.- Solo manda nombre del archivo/carpeta
        //manda el tamaño, manda la indicación de qué acción se hará
        //La primer conexión siempre estará activo solo cada vez que se manden los archivos

      
      try{
          int pto = 8000;
          ServerSocket s = new ServerSocket(pto);
          s.setReuseAddress(true);
          System.out.println("Servidor iniciado...");
          
          //ServerSocket s2 = new ServerSocket(pto+1);
          //s2.setReuseAddress(true);
          for(;;){
              System.out.println("Esperando cliente...");
              Socket cl = s.accept();
              System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
              DataInputStream dis = new DataInputStream(cl.getInputStream());
              DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
              
              //String rutaLocal=rutaRemota;
              
              
             /* dos.writeUTF(rutaCliente);
              dos.flush();*/
              
              
              /*
              int solicitud;
              
              cicloWhile:
              while(true){
                  solicitud=recibeSolicitud(dis);

                    switch (solicitud) {
                        case 0: //La conexion se inicio
                            break;
                        case 1:  //Abrir carpeta
                            // ******** Cambiar a visualizar archivos/carpetas ****************
                            System.out.println("Case 1: Ver archivos carpeta local");
                            //recibimos la dirección de la carpeta que se quiere listar
                            
                            File localFiles = new File(rutaLocal);
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
                            break;
                            
                        case 2:
                            System.out.println("Case 2: Subir archivo/carpeta");
                            obtenerArchivo(dis, rutaLocal);
                            enviaLista(dos, rutaLocal);//"refresh" a la carpeta
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
                            enviaLista(dos, rutaLocal);//"refresh" a la carpeta
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
                            if (rutaLocal.equals(rutaRemota)) {
                                rutaLocal = rutaRemota;
                            } else {
                                //Obtenemos el padre y actualizamos la ruta actual a esa carpeta
                                rutaLocal = new File(rutaLocal).getParent() + System.getProperty("file.separator"); 
                            }
                            
                            //enviaRutaRemoto
                            dos.writeUTF(rutaLocal);
                            dos.flush();
                            
                            System.out.println("Ruta actual: " + rutaLocal);
                            enviaLista(dos, rutaLocal);
                            break;
                        case 6:
                            System.out.println("Case 6: cerrar la conexión");
                            dis.close();
                            dos.close();
                            cl.close();
                            System.out.println("Conexión finalizada, vuelva pronto:D ");
                            break cicloWhile;//para romper el ciclo y dejar de atender al cliente actual
                        case 7:
                            //****Nota***
                            //Si quiere subirse una carpeta al servidor esta primero debe crearse
                            System.out.println("Case 7: crear una carpeta");                           
                            String nombre = rutaLocal+recibeRuta(dis); //recibimos nombre de la carpeta a crear
                            System.out.println("Carpeta que se creara: "+ nombre);
                            File carpeta = new File(nombre);
                            if (carpeta.mkdir())
                                System.out.println("Creado con exito!");
                            else
                                System.out.println("Error en crear la carpeta a crear!");
                        break;
                            
                        case 8:
                            enviaLista(dos, rutaLocal);//refresh de la carpeta                          
                        break;
                    }  
                  
              } //fin ciclo While(true)*/
               obtenerArchivo(dis,"C://Users//Dell//Desktop");
              System.out.println("Archivo recibido..");
              dos.close();
              dis.close();
              cl.close();
          }//for
          
      }catch(Exception e){
          e.printStackTrace();
      }  
    }//main
    
    
            
    public static void enviaLista(DataOutputStream dos,String ruta)  throws IOException{ 
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
    
    
    public static String recibeRuta(DataInputStream dis) throws IOException{ //recibe direccion_archivo del cliente
        String rutaTemp;
        rutaTemp= dis.readUTF();
        return rutaTemp;
    }   
    
    
    public static int recibeSolicitud(DataInputStream dis) throws IOException{ 
        int solicitud;
        solicitud=dis.readInt();
        return solicitud;
    }
    
    
        public static void obtenerArchivo(DataInputStream dis, String Ruta_Archivo) throws IOException {             
            
        //ruta_archivo es la carpeta del servidor en donde se guardará el archivo que ha enviado el cliente
        String nombre = dis.readUTF();
        long tam = dis.readLong();
        System.out.println("Comienza descarga del archivo "+nombre+" de "+tam+" bytes\n\n");
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(Ruta_Archivo+'/'+nombre));
        long recibidos=0;
        int l=0, porcentaje=0;
        while(recibidos<tam){
            byte[] b = new byte[3500];
            l = dis.read(b);
            System.out.println("leidos: "+l);
            dos.write(b,0,l);
            dos.flush();
            recibidos = recibidos + l;
            porcentaje = (int)((recibidos*100)/tam);
            System.out.println("\rRecibido el "+ porcentaje +" % del archivo que envia el cliente");
        }
        dos.close();
    }

        
    public static void enviaArchivo(DataOutputStream dos, File file) throws IOException {
        long tam = file.length();
        String nombre_archivo = file.getName();
        String path = file.getPath();

        System.out.println("Preparandose pare enviar archivo "+path+"\n\n");

        DataInputStream dis = new DataInputStream(new FileInputStream(path));
        dos.writeUTF(nombre_archivo);//Se envía el nombre del archivo
        dos.flush();
        dos.writeLong(tam);//Se utiliza la longitud del archivo
        dos.flush();

        long enviados = 0;
        int l,porcentaje;
        while(enviados<tam){//Se utiliza la longitud del archivo
            byte[] b = new byte[3500];
            l=dis.read(b);
            System.out.println("enviados: "+l);
            dos.write(b,0,l);
            dos.flush();
            enviados = enviados + l;
            porcentaje = (int)((enviados*100)/tam);
            System.out.println("\rEnviado el "+porcentaje+" % del archivo");
        }
        System.out.println("\nArchivo enviado..");
        dis.close();
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
    
    public static void eliminarDirectorio(File file){  
        File[] contenidoDir = file.listFiles();
        if(contenidoDir != null){
            for(File child : contenidoDir){                
               if(child.isDirectory()){ 
                   C1.eliminarDirectorioLocal(child);                   
               }
               child.delete();                              
            }
        }
        file.delete(); // Se Elimina el directorio padre
    }
    
    
}     