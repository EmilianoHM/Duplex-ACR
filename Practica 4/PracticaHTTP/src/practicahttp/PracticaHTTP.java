package practicahttp;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class PracticaHTTP {

    public static final int port = 8000;
    ServerSocket servSoc;
    public static int poolSize = 1;

    static class Manejador extends Thread {

        int codeNumber = 200;
        String response = "OK";
        String path = "src/ContenidoServer/";
        protected Socket socket;
        protected PrintWriter pw;
        protected BufferedOutputStream bos;
        protected BufferedReader br;
        protected DataInputStream dis;
        protected String FileName = "";
        protected Dictionary<String, String> MIME = new Hashtable<String, String>();
        protected String deleteHtml_Ok = "<html><head><meta charset='UTF-8'><title>" + codeNumber + "  </title></head>"
                + "<body><h1>  </h1>"
                + "<p>Elemento: " + FileName + " fue eliminado exitosamente mediante DELETE</p>"
                + "</body></html>";
        protected String E404 = "HTTP/1.1 404 Not Found\n"
                + "Date: " + new Date() + "\n"
                + "Server: Server de Jorge & Emi /1.0\n"
                + "Content-Type: text/html \n\n"
                + "<html><head><meta charset='UTF-8'><title>404 NOT FOUND  </title></head>"
                + "<body><h1> La página no se encontró o no existe</h1>";
        protected final String E500 = "<html><head><meta charset='UTF-8'><title>500 INTERNAL SERVER ERROR  </title></head>"
                + "<body><h1> UPS, OCURRIÃ“ UN ERROR INESPERADO</h1>"
                + "<p>No se pudo concretar la operacion</p>"
                + "</body></html>";
        protected final String E403 = "<html><head><meta charset='UTF-8'><title>403 FORBIDDEN  </title></head>"
                + "<body><h1>The server understands the request but refuses to authorize it</h1>"
                + "<p>The access is tied to the application logic, such as insufficient rights to a resource.</p>"
                + "</body></html>";

        public Manejador(Socket s) throws IOException {
            this.socket = s;
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));//Input : Lectura de linea
            this.dis = new DataInputStream(this.socket.getInputStream());
            this.bos = new BufferedOutputStream(socket.getOutputStream());//Output
            this.pw = new PrintWriter(new OutputStreamWriter(bos));//Output
            this.MIME.put("txt", "text/plain");
            this.MIME.put("html", "text/html");
            this.MIME.put("htm", "text/html");
            this.MIME.put("jpg", "image/jpeg");
            this.MIME.put("jpeg", "image/jpeg");
            this.MIME.put("png", "image/png");
            this.MIME.put("pdf", "application/pdf");
            this.MIME.put("doc", "application/msword");
            this.MIME.put("rar", "application/x-rar-compressed");
            this.MIME.put("mp3", "audio/mpeg");
            this.MIME.put("mp4", "video/mp4");
            this.MIME.put("c", "text/plain");
            this.MIME.put("java", "text/plain");
        } // CONSTRUCTOR

        public void run() { // START..........
            try {
                String linea = br.readLine(); // Se lee la peticion que se va a ejecutar
                if (linea == null) { // si la linea esta vací­a se envia texto html
                    pw.print("<html><head><title>Servidor Jorge & Emi");
                    pw.print("</title><body bgcolor=\"#AACCFF\"<br>Linea Vacia</br>");
                    pw.print("</body></html>");
                    socket.close();
                    return;
                }//END IF LINE = NULL

                System.out.println("\nCliente Conectado desde: " + socket.getInetAddress());
                System.out.println("Por el puerto: " + socket.getPort());
                System.out.println("Datos: " + linea + "\r\n\r\n");

                if (linea.toUpperCase().startsWith("GET")) // Caso que la peticion empiece con la palabra "GET"
                {
                    getFileName(linea);//Se actualiza el file name segun la peticion recibida
                    GET();//Se ejecuta el metodo GET usando la variable global actualizada 'FileName'
                } else if (linea.toUpperCase().startsWith("DELETE"))// Caso que la peticion empiece con la palabra "DELETE"
                {
                    getFileName(linea);//Se actualiza el file name segun la peticion recibida
                    DELETE();//Se ejecuta el metodo DELETE usando la variable global actualizada 'FileName'
                } else if (linea.toUpperCase().startsWith("HEAD")) {
                    //getFileName es llamado dentro de la misma funcion HEAD
                    HEAD(linea);//Se ejecuta el metodo HEAD usando la variable global actualizada 'FileName'
                } else if (linea.toUpperCase().startsWith("PUT")) {
                    getFileName(linea);//Se actualiza el file name segun la peticion recibida
                    PUT();
                } else if (linea.toUpperCase().startsWith("POST")) {
                        System.out.println("Recibiendo POST...");
                        int tam = dis.available(); // Se lee el tamaño del flujo de entrada para saber que tamaño tiene que leer
                        byte[] b = new byte[tam];//Se inicia un arreglo de bytes con el tamaño obtenido
                        dis.read(b);//Se leen los datos
                        String request;
                        if(linea.contains("=")){
                            request = linea;
                        }else{
                            request = new String(b, 0, tam);
                        }

                        String htmlPost = POST(request);// Se manda a llamar la función POST recuperando una String que representa la respuesta
                        System.out.println("Respuesta:");
                        System.out.println(htmlPost);
                        pw.println(htmlPost);
                        pw.flush();
                        bos.flush();
                    }
                    else if (!linea.contains("?"))
                    {
                        getFileName(linea);
                        if(FileName.compareTo("")==0)
                            SendF(path+"index.html");
                        else
                            SendF(path+FileName);
                    }
                    else
                    {
                        pw.println("HTTP/1.0 501 Not Implemented");
                        pw.println();
                    }
                    pw.flush();
                    bos.flush();

            } catch (Exception e) { // End of TRY.... RUN
                e.printStackTrace();
            }
        }//END RUN....

        public void DELETE() {
            try {
                System.out.println("peticion de eliminado del archivo: " + FileName);
                File file = new File(path + FileName);
                if (file.exists()) {
                    if (file.delete()) {//Si fue posible eliminarlo:
                        System.out.println("peticion de eliminado de: " + FileName + "   ha sido exitosa.");
                        this.codeNumber = 200;
                        this.response = "OK";
                        String contentType = "text/html";
                        String headerHTTP = "HTTP/1.1 " + 200 + " " + "OK" + "\n"
                                + "Date: " + new Date() + "\n"
                                + "Server: Server de Jorge & Emi /1.0\n"
                                + "Content-Type: " + contentType + " \n\n";
                        pw.println(headerHTTP + deleteHtml_Ok);
                        pw.flush();
                    } else {//Si no fue posible eliminarlo:

                        String contentType = "text/html";
                        String headerHTTP = "HTTP/1.1 " + 500 + " " + "Internal Server Error" + "\n"
                                + "Date: " + new Date() + "\n"
                                + "Server: Server de Jorge & Emi /1.0\n"
                                + "Content-Type: " + contentType + " \n";
                        pw.println(headerHTTP + "\n" + E500);//Se actualizan los parametros anteriores y envia encabezado y http con mensaje.
                        pw.flush();
                    }
                } else {//Si no existe el archivo....
                    System.out.println("peticion de eliminado de: " + FileName + "   ha fracasado. NO SE ENCONTRÃ“");
                    this.codeNumber = 404;
                    this.response = "Not Found";
                    pw.println(E404 + "<p>Elemento: " + FileName + " NO EXISTE O FUE ELIMINADO</p>"
                            + "</body></html>");//Se envia la String E404 que contiene encabezado y texto http
                    pw.flush();
                }
                pw.flush();
                bos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }//END of Method DELETE....

        

public void PUT() {
    try {
        // Leer el cuerpo de la solicitud
        StringBuilder requestBody = new StringBuilder();
        boolean bodyStarted = false;  // Indicador de inicio del cuerpo del mensaje

        char[] buffer = new char[1024]; // Utilizaremos un buffer para la lectura

        // Mientras haya datos disponibles en el InputStream
        while (br.ready()) {
            int bytesRead = br.read(buffer);
            
            // Comprueba si ha empezado el cuerpo del mensaje
            if (!bodyStarted && new String(buffer, 0, bytesRead).contains("\r\n\r\n")) {
                bodyStarted = true;
                requestBody.append(new String(buffer, 0, bytesRead).split("\r\n\r\n")[1]);
            } else if (bodyStarted && bytesRead > 0) {
                requestBody.append(buffer, 0, bytesRead);
            }
        }

        // Crea o actualiza el archivo
        File file = new File(path + FileName);
        boolean isNewFile = file.createNewFile();

        if (FileName.endsWith(".pdf")) {
            // Si el archivo es un PDF, utiliza iTextPDF para generar el contenido del PDF
            generatePdf(requestBody.toString(), file);
        } else {
            // Para otros tipos de archivos, simplemente escribe el cuerpo de la solicitud directamente al archivo
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                fileOutputStream.write(requestBody.toString().getBytes());
            }
        }

        // Construir la respuesta
        String responseHeader = isNewFile ? "HTTP/1.1 201 Created\n" : "HTTP/1.1 200 OK\n";
        String responseBody = "Date: " + new Date() + "\n"
                + "Server: Server de Jorge & Emi /1.0\n"
                + "Content-Length: " + requestBody.length() + "\n\n";

        // Enviar respuesta
        pw.println(responseHeader + responseBody);
        pw.flush();

        // Imprimir detalles en el registro del servidor
        System.out.println("Cliente Conectado desde: " + socket.getInetAddress());
        System.out.println("Por el puerto: " + socket.getPort());
        System.out.println("Datos: PUT" + " " + FileName + " HTTP/1.1");
        System.out.println("Código de respuesta: " + (isNewFile ? 201 : 200));
        System.out.println("Respuesta: " + (isNewFile ? "Created" : "OK"));
        System.out.println("Detalles adicionales de la respuesta: " + responseBody);


    } catch (Exception e) {
        // Manejar excepciones y enviar una respuesta de error si es necesario
        e.printStackTrace();
        // Añade una respuesta de error al cliente
        pw.println("HTTP/1.1 500 Internal Server Error\n\n");
        pw.flush();
    } finally {
        // Cierra las conexiones y flujos
        try {
            br.close();
            bos.close();
            pw.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


private void generatePdf(String content, File file) {
    try {
        // Utiliza iTextPDF para generar un archivo PDF con el contenido proporcionado
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        document.add(new Paragraph(content));
        document.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

        
        
        


        public void GET() {
            try {
                File file = new File(path + FileName);

                // Si es un directorio, lista los archivos dentro
                if (file.isDirectory()) {
                    File[] files = file.listFiles();

                    // Construye la respuesta con la lista de archivos y sus tamaños
                    StringBuilder responseContent = new StringBuilder();
                    responseContent.append("<html><body><h2>Archivos en la carpeta: ").append(FileName).append("</h2><ul>");

                    if (files != null) {
                        for (File f : files) {
                            responseContent.append("<li>").append(f.getName()).append("         - Tamanio: ").append(f.length()).append(" bytes</li>");
                        }
                    }

                    responseContent.append("</ul></body></html>");

                    // Envía la respuesta al cliente
                    String responseHeader = "HTTP/1.1 200 OK\n"
                            + "Date: " + new Date() + "\n"
                            + "Server: TuServidor/1.0\n"
                            + "Content-Type: text/html\n"
                            + "Content-Length: " + responseContent.length() + "\n\n";

                    pw.println(responseHeader + responseContent.toString());
                    pw.flush();
                } else {
                File temp = new File(path + FileName);
                if (temp.exists()) {
                    if (SendF(path + FileName)) {//Si la funcion de mandado de archivo regresa true:
                        System.out.println("peticion de lectura de: " + FileName + "   ha sido exitosa.");
                    } else {//Si hubo un error en el enviado del archivo solicitado:
                        System.out.println("peticion de lectura de: " + FileName + "   ha fracasado.");
                        String contentType = "text/html";
                        String headerHTTP = "HTTP/1.1 " + 500 + " " + "Internal Server Error" + "\n"
                                + "Date: " + new Date() + "\n"
                                + "Server: Server de Jorge & Emi /1.0\n"
                                + "Content-Type: " + contentType + " \n";
                        pw.println(headerHTTP + "\n" + E500);//Se manda el encabezado actualizado y html de Error 500
                        pw.flush();
                    }
                } else {//Si no existe el archivo que se está intentando obtener:
                    System.out.println("peticion de lectura de: " + FileName + "   ha fracasado. NOT FOUND");
                    this.codeNumber = 404;
                    pw.println(E404);//Se manda encabezado y texto html de Error 404
                    pw.flush();
                }
                pw.flush();
                bos.flush();
            } }catch (Exception e) {
                e.printStackTrace();
            }
        }//END of Method GET....

      public String POST(String request) {
                int indice = request.indexOf("/");
                if(request.contains("/")){
                    if(request.contains("?"))
                        request = request.substring(indice+2);
                    else
                        request = request.substring(indice+1);
                    StringTokenizer post = new StringTokenizer(request," ");
                    request = post.nextToken();
                }
                String[] reqLineas = request.split("\n");
                StringTokenizer tokens = new StringTokenizer(reqLineas[reqLineas.length-1], "&?");
                System.out.println(reqLineas[reqLineas.length-1]);

                String contentType = "text/html";
                String headerHTTP = "HTTP/1.1 "+ "200" +" "+"OK"+"\n"
                        + "Date: "+new Date()+"\n"
                        + "Server: EGZ_KYF Server/1.0\n"
                        + "Content-Type: "+contentType+" \n\n";
                String html = headerHTTP // encabezado http con los valores actualizados:
                        + "<html><head><meta charset='UTF-8'><title> Método POST </title></head>\n"
                        + "<body ><center><h2> Se han obtenido los siguientes valores con sus respectivos parámetros</h2><br>\n"
                        + "<table border='2'><tr><th>Valores:</th><th>Valor</th></tr>";

                while (tokens.hasMoreTokens()) {
                    String postValues = tokens.nextToken();
                    System.out.println(postValues);
                    StringTokenizer postValue = new StringTokenizer(postValues, "=");
                    String parametro = ""; //Parámetro
                    String valor = ""; //Valor
                    if (postValue.hasMoreTokens()) {
                        parametro = postValue.nextToken();
                    }
                    if (postValue.hasMoreTokens()) {
                        valor = postValue.nextToken();
                    }
                    html = html + "<tr><td><b>" + parametro + "</b></td><td>" + valor + "</td></tr>\n";
                }
                html = html + "</table></center></body></html>";
                return html;
            }

        public void HEAD(String linea) throws Exception {
            getFileName(linea);
            File file = new File(path + FileName);
            if (!linea.contains("?") || file.exists()) {
                //Se actualiza el FileName segun la linea de peticion recibida
                if (file.isDirectory()) {//Si se esta intentando acceder a un directorio:
                    this.FileName = "page403.html";
                    int codeNumber = 403;
                    String response = "Forbidden\n";
                    String contentType = "text/html";
                    String headerHTTP = "HTTP/1.1 " + codeNumber + " " + response + "\n"
                            + "Date: " + new Date() + "\n"
                            + "Server: Server de Jorge & Emi /1.0\n"
                            + "Content-Type: " + contentType + " \n";
                    pw.println(headerHTTP + "\n" + E403);//Se manda el encabezado actualizado y la seccion html Error 403
                } else {//Si el archivo existe: se mandan los encabezados actualizando el tipo de archivo
                    int posExt = FileName.indexOf(".") + 1;
                    String ext = FileName.substring(posExt);
                    String contentType = MIME.get(ext);
                    String headerHTTP = "HTTP/1.1 " + codeNumber + " " + response + "\n"
                            + "Date: " + new Date() + "\n"
                            + "Server: Server de Jorge & Emi /1.0\n"
                            + "Content-Type: " + contentType + " \n";
                    pw.println(headerHTTP + "Content-Length: " + file.length() + " \n\n");//Se aÃ±ade fileLength al encabezado
                    System.out.println(headerHTTP + "Content-Length: " + file.length() + " \n\n");
                }

            } else if (!file.exists()) {//Si el archivo no existe:
                this.FileName = "page404.html";
                this.codeNumber = 404;
                this.response = "Not Found";
                pw.println(E404 + "<p>Elemento: " + FileName + " NO EXISTE O FUE ELIMINADO</p>"
                        + "</body></html>");//Se manda el encabezado y texto html de Error 404

                pw.flush();
            } else { // Si la linea contiene: "?"
                // Se envenvia unicamente header html
                this.codeNumber = 200;
                this.response = "OK";
                String contentType = "text/html";
                String headerHTTP = "HTTP/1.1 " + codeNumber + " " + response + "\n"
                        + "Date: " + new Date() + "\n"
                        + "Server: Server de Jorge & Emi /1.0\n"
                        + "Content-Type: " + contentType + " \n";
                pw.println(headerHTTP);
            }
            pw.flush();
            bos.flush();
        }//End of Method HEAD...

        void getFileName(String comando) {//Actualiza la variable global FileName a partir del archivo solicitado
            //Se usa la linea de peticion que se le pasa como parametro:
            int i = comando.indexOf("/");
            if (comando.indexOf("?") == (i + 1)) {
                i++;
            }
            int f = comando.indexOf(" ", i);
            this.FileName = comando.substring(i + 1, f);
        }

        public boolean SendF(String file) {//Funcion Send File
            try {
                //Manejo del tipo de contenido: MIME:
                int posExt = FileName.indexOf(".") + 1;
                String ext = FileName.substring(posExt);//Se obtiene la extension del archivo y

                int b_leidos;
                BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(file));
                byte[] buf = new byte[1024];
                int tam_archivo = bis2.available();//Se mide el tamaño a partir del flujo obtenido

                //Se prepara y se escribe el encabezado del archivo que se va a mandar:
                String head = "HTTP/1.0 202 Accepted\n"
                        + "Server: Server de Jorge & Emi /1.0\n"
                        + "Date: " + new Date() + " \n"
                        + "Content-Type: " + MIME.get(ext) + " \n"//Se utiliza el contentType obtenido con el diccionario MIME
                        + "Content-Length: " + tam_archivo + " \n\n";

                bos.write(head.getBytes());//Se escribe la cadena 
                bos.flush();

                while ((b_leidos = bis2.read(buf, 0, buf.length)) != -1) {
                    bos.write(buf, 0, b_leidos);
                }
                bos.flush();
                bis2.close();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
            return true;
        } // END of Method SendF....

    }//END MANEJADOR CLASS

    public PracticaHTTP() throws Exception { // CONSTRUCTOR DE LA CLASE SERVER

        System.out.println("Servidor iniciado...");
        try ( Scanner scan = new Scanner(System.in)) {
            System.out.print("Ingresa el tamaño del pool de conexiones que desea: \n");
            poolSize = scan.nextInt();
        }
        ExecutorService poolHilos = Executors.newFixedThreadPool(poolSize); // aquí­ se define el tamaño

        this.servSoc = new ServerSocket(port);
        for (;;) {
            System.out.println("Esperando Cliente... en el puerto " + port);
            Socket cliente = servSoc.accept();
            poolHilos.execute(new Manejador(cliente)); // Se ejecuta segun el pool de hilos :)
        }
    }//Constructor

    public static void main(String[] args) {
        try {
            PracticaHTTP Servidor = new PracticaHTTP();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
