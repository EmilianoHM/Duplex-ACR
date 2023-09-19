import java.net.*;
import java.io.*;
import javax.swing.JFileChooser;
/**
 *
 * @author axele
 */
public class C1 {
    public static void main(String[] args){
        try{
            int pto = 8000;
            String dir = "127.0.0.1";
            Socket cl = new Socket(dir,pto);
            System.out.println("Conexion con servidor establecida.. lanzando FileChooser..");
            JFileChooser jf = new JFileChooser(); // Habilita la elecciòn de archivos
            //jf.setMultiSelectionEnabled(true);
            int r = jf.showOpenDialog(null); // Hacer visible la caja de JFIleChooser
            jf.setRequestFocusEnabled(true);
            if(r==JFileChooser.APPROVE_OPTION){ // Si escoge Aceptar, obtiene la constante APPROVE_OPTION
                File f = jf.getSelectedFile();
                String nombre = f.getName();
                String path = f.getAbsolutePath();
                long tam = f.length();
                System.out.println("Preparandose pare enviar archivo "+path+" de "+tam+" bytes\n\n");
                DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                DataInputStream dis = new DataInputStream(new FileInputStream(path));
                dos.writeUTF(nombre);
                dos.flush();
                dos.writeLong(tam);
                dos.flush();
                long enviados = 0;
                int l=0,porcentaje=0;
                while(enviados<tam){
                    byte[] b = new byte[3500]; // No importa el tamaño de buffer, porque al final como va por
                    // socket de flujo, TCP se va a encargar de hacer la segmentaciòn
                    l=dis.read(b);
                    System.out.println("enviados: "+l);
                    dos.write(b,0,l);
                    dos.flush();
                    enviados = enviados + l;
                    porcentaje = (int)((enviados*100)/tam);
                    System.out.print("\rEnviado el "+porcentaje+" % del archivo");
                }//while
                System.out.println("\nArchivo enviado..");
                dis.close();
                dos.close();
                cl.close();
            }//if
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//main
}
