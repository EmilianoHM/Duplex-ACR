
package Cliente;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;

public class Cliente extends JFrame {
 
       String ElemSelectedRemote;      
       String  ListaSelectedRemote[];        
    //ruta de los archivos descargados
        final String rutaDescargas="."+System.getProperty("file.separator")+"archivosDescargados"+System.getProperty("file.separator"); 
        File  SelectedLocal[]; //JFILECHOOSER
        String rutaActualArchivos; 
        String listaArchivos []; 

        //variables para la Interfaz
    Font fuentes; 
    JPanel divCliente; 
            JPanel divCarpetaLocal; 
                JLabel tituloLocal; 
                JFileChooser navegadorCarpetaLocal;   
            JPanel divOpcionesLocal; 
                JToolBar opcionesCarpetaLocal; 
                    JButton btnSubirArchivo;
                    JButton btnEliminarArchivoLocal;
    JPanel divServer; 
            JPanel divCarpetaRemota;    
                JLabel tituloRemota; 
                JScrollPane divScrollRemota; 
                    JList <String> listaCarpetaRemota; 
            JPanel BotonesServer; 
                JToolBar OpcCarpetaRemote; 
                    JButton btnAbrir; 
                    JButton btnDescargar;
                    JButton btnEliminarRemoto;
                    JButton btnRegresar;
                    
    public static void main(String[] args) throws UnsupportedEncodingException, IOException{
        
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();              
            dim.width=dim.width*105/114; dim.height=dim.height*4/6; 
            Cliente aplicacion =new Cliente(dim);                    
            aplicacion.setDefaultCloseOperation(Cliente.EXIT_ON_CLOSE);                
            aplicacion.setSize(dim);              
            aplicacion.setResizable(false);       
            aplicacion.ElemSelectedRemote="*"; //ningún archivo remoto se encuentra seleccionado en la ventana
            aplicacion.navegadorCarpetaLocal.setCurrentDirectory(new File(aplicacion.rutaDescargas));
            aplicacion.btnAbrir.setEnabled(false);         
            aplicacion.btnDescargar.setEnabled(false);
            aplicacion.btnEliminarRemoto.setEnabled(false);
            aplicacion.btnRegresar.setEnabled(true);
            aplicacion.btnSubirArchivo.setEnabled(false);
            aplicacion.btnEliminarArchivoLocal.setEnabled(false);
           
            Socket socketCliente = BackCliente.creaSocket();
            DataOutputStream dos = new DataOutputStream(socketCliente.getOutputStream());
            DataInputStream dis = new DataInputStream(socketCliente.getInputStream());
            aplicacion.rutaActualArchivos=BackCliente.ObtenerRutaRemoto(dis); //ruta del server
            System.out.println(aplicacion.rutaActualArchivos);
            aplicacion.listaArchivos=BackCliente.ObtenerListaRemoto(dis);
            
            //acondicionamos la carpeta de descargas
            File carpetaDescargas=new File(aplicacion.rutaDescargas);        
            carpetaDescargas.mkdir(); //si no existe la creamos  
            aplicacion.setVisible(true);    
   
        ///Damos refresh a la interfaz de las carperts del servidor/////////////
            aplicacion.listaCarpetaRemota.setListData(aplicacion.listaArchivos);      
            aplicacion.tituloRemota.setText("Carpeta remota");                
        
            aplicacion.listaCarpetaRemota.addMouseListener(new MouseListener(){ 
                public void mouseClicked(MouseEvent e){    
                    int idArchivo=aplicacion.listaCarpetaRemota.locationToIndex(e.getPoint());      
                    aplicacion.ElemSelectedRemote=aplicacion.listaArchivos[idArchivo];
                    if(aplicacion.ElemSelectedRemote.endsWith(System.getProperty("file.separator"))){//si es carpeta        
                        //Activamos el botón de descargar
                        aplicacion.btnDescargar.setEnabled(true); 
                        aplicacion.btnAbrir.setEnabled(true);   
                        
                    }else {//si es archivo      
                        aplicacion.btnDescargar.setEnabled(true); 
                        aplicacion.btnAbrir.setEnabled(false);         
                    }
                    aplicacion.btnEliminarRemoto.setEnabled(true);      
                }
                //eventos del raton vaquero
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
                
            });       
            
            aplicacion.navegadorCarpetaLocal.addActionListener(new ActionListener() { //Para el JFileChooser
               public void actionPerformed(ActionEvent evento) {
                   String command = evento.getActionCommand();
                  if (command.equals(JFileChooser.APPROVE_SELECTION)) {//si seleccionamos varios archivos
                      aplicacion.SelectedLocal= aplicacion.navegadorCarpetaLocal.getSelectedFiles();
                       System.out.println("Se han seleccionado "+aplicacion.SelectedLocal.length+" archivos\n");
                        aplicacion.btnSubirArchivo.setEnabled(true);
                        aplicacion.btnEliminarArchivoLocal.setEnabled(true);
                   }  else if (command.equals(JFileChooser.CANCEL_SELECTION)) {//deseleccionamos archivos                  
                       
                       System.out.println("Ningún archivo seleccionado");
                       //bloquemos los botones 
                       aplicacion.btnSubirArchivo.setEnabled(false);
                       aplicacion.btnEliminarArchivoLocal.setEnabled(false);    
                   }     
               }
            });                   
            
            aplicacion.btnAbrir.addMouseListener(new MouseListener(){ 
                public void mouseClicked(MouseEvent e){    
                    try {                        
                        //lisDimensionos una carpeta hija
                        BackCliente.enviaSolicitud(dos,1); 
                        aplicacion.rutaActualArchivos=aplicacion.rutaActualArchivos+aplicacion.ElemSelectedRemote;
                                System.out.println(aplicacion.rutaActualArchivos);           
                        BackCliente.enviaRuta(dos, aplicacion.rutaActualArchivos);//mandamos ruta de la carpeta a abrir
                        //el server nos regresa la lista de archivos de esa carpeta
                        aplicacion.listaArchivos=BackCliente.ObtenerListaRemoto(dis);
                        aplicacion.listaCarpetaRemota.setListData(aplicacion.listaArchivos);//refresh
                    } catch (IOException ex) {
                    }
                }
                //eventos del raton vaquero
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}                  
            });     

            aplicacion.btnSubirArchivo.addMouseListener(new MouseListener(){ 
                public void mouseClicked(MouseEvent e){
                    try {
                         BackCliente.enviaMultiplesArchivos(dos,dis, aplicacion.SelectedLocal, aplicacion.rutaActualArchivos);
                        BackCliente.enviaSolicitud(dos, 8); //refresh a la carpeta remota
                        aplicacion.listaArchivos=BackCliente.ObtenerListaRemoto(dis);
                        aplicacion.listaCarpetaRemota.setListData(aplicacion.listaArchivos);           
                    } catch (IOException ex) {
                    }
                }                
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
            });
            
            aplicacion.btnDescargar.addMouseListener(new MouseListener(){
                public void mouseClicked(MouseEvent e){    
                    int[] f =aplicacion.listaCarpetaRemota.getSelectedIndices();
                    aplicacion.ListaSelectedRemote=new String [f.length];//guardamos los archivos seleccionados 
                    
                    for(int i=0;i<f.length ;i++){
                        aplicacion.ListaSelectedRemote[i]=aplicacion.listaArchivos[f[i]];
                    }
                    try {
                        BackCliente.obtenerMultiplesArchivos(dos,dis, //descargar multiples archivos
                                aplicacion.ListaSelectedRemote, 
                                aplicacion.rutaActualArchivos, 
                                aplicacion.rutaDescargas); 
                        
                    } catch (IOException ex) {
                    }
                    aplicacion.navegadorCarpetaLocal.setCurrentDirectory(new File("./"));
                    aplicacion.navegadorCarpetaLocal.setCurrentDirectory(new File(aplicacion.rutaDescargas));                                                            
                }
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
                
            });     
           
            aplicacion.btnEliminarRemoto.addMouseListener(new MouseListener(){ 
               public void mouseClicked(MouseEvent e){    
                   try {
                       int[] f =aplicacion.listaCarpetaRemota.getSelectedIndices();
                       aplicacion.ListaSelectedRemote=new String [f.length];//para guardar los nombres de los archivos
                       
                       for(int i=0;i<f.length ;i++){ 
                               aplicacion.ListaSelectedRemote[i]=aplicacion.listaArchivos[f[i]];
                       }
                       BackCliente.eliminarMultiplesArchivosRemotos(dos, //funcion para eliminar muchos archivos de BackCliente
                               aplicacion.ListaSelectedRemote, 
                                aplicacion.rutaActualArchivos);
                       
                        for(int i=0;i<f.length ;i++){//Refresh
                            aplicacion.listaArchivos=BackCliente.ObtenerListaRemoto(dis);
                            aplicacion.listaCarpetaRemota.setListData(aplicacion.listaArchivos);
                        }                            
                       aplicacion.btnDescargar.setEnabled(false);
                       aplicacion.btnEliminarRemoto.setEnabled(false);
                   } catch (IOException ex) {
                   }              
               }
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
            });     
            
            aplicacion.btnRegresar.addMouseListener(new MouseListener(){ 
                public void mouseClicked(MouseEvent e){                    
                    try {
                        BackCliente.enviaSolicitud(dos,5); // volver atrás 
                        aplicacion.rutaActualArchivos=BackCliente.ObtenerRutaRemoto(dis);
                        aplicacion.listaArchivos=BackCliente.ObtenerListaRemoto(dis);//lista de la nueva carpeta
                        
                        aplicacion.listaCarpetaRemota.setListData(aplicacion.listaArchivos);//refresh
                    } catch (IOException ex) {
                    }
                }   
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
            });     
            
            aplicacion.addWindowListener(new WindowAdapter(){//boton de cerrar la ventana por tanto la conexion
                public void windowClosing(WindowEvent we){
                    try {
                        BackCliente.enviaSolicitud(dos,6);//cerrar la conexion
                        System.out.println("Conexión finalizada");
                    } catch (IOException ex) {
                    }     
                    System.out.println("VENTANA CERRADA");

                    try {
                        dis.close();
                        dos.close();
                        socketCliente.close();
                        System.out.println("SOCKET CERRADO");
                    } catch (IOException ex) {
                       
                    }                
                }
            });

            aplicacion.btnEliminarArchivoLocal.addMouseListener(new MouseListener(){ 
                public void mouseClicked(MouseEvent e){    
                    String carpetaActual=aplicacion.SelectedLocal[0].getParent();
                    for(int i=0;i<aplicacion.SelectedLocal.length;i++){//eliminamos los archivos
                        String nombreArchivo=aplicacion.SelectedLocal[i].getPath();
                        BackCliente.eliminarArchivoLocal(nombreArchivo);
                    }                                        
                    aplicacion.navegadorCarpetaLocal.setCurrentDirectory(new File("./"));//refresh             
                    aplicacion.navegadorCarpetaLocal.setCurrentDirectory(new File(carpetaActual));
                }
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
            });  
    }//main
    
    public Cliente(Dimension dimVentana){ //inicializamos interfaz                   
                
        fuentes= new Font("Time new Roman",Font.BOLD, 20 ); 
        Container window=getContentPane();
        window.setLayout(new BoxLayout(window,BoxLayout.X_AXIS));
        ////***Lado del Servidor(izquierda)****//////////////       
        divServer=new JPanel();                                               
        divServer.setMaximumSize(new Dimension(dimVentana.width,dimVentana.height));                        
        divServer.setLayout(new BoxLayout(divServer,BoxLayout.X_AXIS)); //acomodo horizontal        
        window.add(divServer);        
            divCarpetaRemota= new JPanel();//cuadro de contenido para mostrar la carpeta remota                                                 
            divCarpetaRemota.setMaximumSize(new Dimension(7000,divServer.getMaximumSize().height));
            divCarpetaRemota.setLayout(new BoxLayout(divCarpetaRemota,BoxLayout.Y_AXIS));             
            divCarpetaRemota.setBorder(new EmptyBorder(10,10,10,10));              
            divServer.add(divCarpetaRemota);
                tituloRemota=new JLabel("CARPETA REMOTA"); 
                tituloRemota.setFont(fuentes);
                divCarpetaRemota.add(tituloRemota);                
                    divScrollRemota=new JScrollPane();                                
                    listaCarpetaRemota=new JList<>();
                    divCarpetaRemota.add(divScrollRemota);                        
                        listaCarpetaRemota.setMaximumSize(new Dimension(5000,5000));
                        divScrollRemota.setViewportView(listaCarpetaRemota);
            
            BotonesServer= new JPanel();                    
            BotonesServer.setBorder(new EmptyBorder(10,10,10,10)); 
            BotonesServer.setLayout(new BoxLayout(BotonesServer,BoxLayout.Y_AXIS)); //despliegue en vertical
            BotonesServer.setMaximumSize(new Dimension(20000,divServer.getMaximumSize().height));  
            divServer.add(BotonesServer);   
                OpcCarpetaRemote= new JToolBar(JToolBar.VERTICAL); //barra VERTICAL de herramientas (para los botones)
                OpcCarpetaRemote.setMaximumSize(new Dimension(divServer.getMaximumSize().width,divServer.getMaximumSize().height));
                OpcCarpetaRemote.setBorder(new EmptyBorder(50,50, 50, 50));
                OpcCarpetaRemote.setLayout(new GridLayout(5,1));    
                
                BotonesServer.add(OpcCarpetaRemote);
                    btnAbrir=new JButton("Abrir carpeta");                                
                    OpcCarpetaRemote.add(btnAbrir);         
                    btnDescargar=new JButton("Descargar");
                    OpcCarpetaRemote.add(btnDescargar);           
                    btnRegresar=new JButton("Regresar");
                    OpcCarpetaRemote.add(btnRegresar);
                    btnEliminarRemoto=new JButton("Eliminar");
                OpcCarpetaRemote.add(btnEliminarRemoto); 
        
        ////***Lado del Cliente (derecha)****//////////////       
        divCliente=new JPanel();        
        divCliente.setBorder(BorderFactory.createLineBorder(Color.BLACK));        
        divCliente.setLayout(new BoxLayout(divCliente,BoxLayout.X_AXIS));   
        divCliente.setMaximumSize(new Dimension(dimVentana.width,dimVentana.height));     
        window.add(divCliente);                     
            divCarpetaLocal= new JPanel();                                
            divCarpetaLocal.setMaximumSize(new Dimension(8000,divCliente.getMaximumSize().height));            
            divCarpetaLocal.setLayout(new BoxLayout(divCarpetaLocal,BoxLayout.Y_AXIS));
            divCarpetaLocal.setBorder(new EmptyBorder(10,10,10,10)); 
            divCliente.add(divCarpetaLocal);                                
            
                tituloLocal= new JLabel("Bienvenido Cliente");
                tituloLocal.setFont(fuentes);
                divCarpetaLocal.add(tituloLocal);  
                
                navegadorCarpetaLocal= new JFileChooser();
                navegadorCarpetaLocal.setMultiSelectionEnabled(true); 
                navegadorCarpetaLocal.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); //permiso para seleccionar archivos
                divCarpetaLocal.add(navegadorCarpetaLocal);

            divOpcionesLocal=new JPanel();                        
            divOpcionesLocal.setBorder(new EmptyBorder(10,10,10,10)); 
            divOpcionesLocal.setLayout(new BoxLayout(divOpcionesLocal,BoxLayout.Y_AXIS)); //despliegue en vertical
            divOpcionesLocal.setMaximumSize(new Dimension(30000,divCliente.getMaximumSize().height));  
            divCliente.add(divOpcionesLocal);               
                opcionesCarpetaLocal=new JToolBar(JToolBar.VERTICAL);                
                opcionesCarpetaLocal.setMaximumSize(new Dimension(divOpcionesLocal.getMaximumSize().width,divOpcionesLocal.getMaximumSize().height));
                opcionesCarpetaLocal.setBorder(new EmptyBorder(25,25,25,25));             
                opcionesCarpetaLocal.setLayout(new GridLayout(5,1));
                divOpcionesLocal.add(opcionesCarpetaLocal);         
                    btnSubirArchivo=new  JButton("Subir");
                    opcionesCarpetaLocal.add(btnSubirArchivo);
                    btnEliminarArchivoLocal=new  JButton("Eliminar");
                    opcionesCarpetaLocal.add(btnEliminarArchivoLocal);       
    }                         
}//class
