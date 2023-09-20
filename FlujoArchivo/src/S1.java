import java.net.*;
import java.io.*;

public class S1 {
    public static void main(String[] args){
      final String rutaLocal="."+"/"+"Local"+"/";
      
      File local=new File(rutaLocal);
      local.mkdir();                              
      System.out.println("Carpeta local lista...");
      
      try{
          int pto = 8000;
          ServerSocket s = new ServerSocket(pto);
          s.setReuseAddress(true);
          System.out.println("Servidor iniciado...");
          for(;;){
              System.out.println("Esperando cliente...");
              Socket cl = s.accept();
              System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
              DataInputStream dis = new DataInputStream(cl.getInputStream());
              DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
              
              String rutaRemota=rutaLocal;
              
              dos.writeUTF(rutaRemota);
              dos.flush();
              
              EnviaListaRemoto(dos, rutaRemota);
              
              //2 servidores y 2 clientes, 1.- Solo manda nombre del archivo/carpeta
              //manda el tamaño, manda la indicación de qué acción se hará
              //La primer conexión siempre estará activo solo cada vez que se manden los archivos
              
            /*  long recibidos=0;
              int l=0, porcentaje=0;
              while(recibidos<tam){
                  byte[] b = new byte[1500];
                  l = dis.read(b);
                  System.out.println("leidos: "+l);
                  dos.write(b,0,l);
                  dos.flush();
                  recibidos = recibidos + l;
                  porcentaje = (int)((recibidos*100)/tam);
                  System.out.print("\rRecibido el "+ porcentaje +" % del archivo");
              }//while*/
              System.out.println("Archivo recibido..");
              dos.close();
              dis.close();
              cl.close();
          }//for
          
      }catch(Exception e){
          e.printStackTrace();
      }  
    }//main
    
    
            
    public static void EnviaListaRemoto(DataOutputStream dos,String ruta)  throws IOException{ 
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
    
    
}     