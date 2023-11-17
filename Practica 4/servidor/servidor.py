# servidor.py
from http.server import BaseHTTPRequestHandler, HTTPServer
from socketserver import ThreadingMixIn
import os

class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    pass

class CustomRequestHandler(BaseHTTPRequestHandler):
    def do_HEAD(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        try:
            ruta = self.path[1:]  # Eliminar la barra inicial (/) de la ruta
            with open(ruta, 'rb') as archivo:
                contenido = archivo.read()

            # Obtener la extensión del archivo
            extension = os.path.splitext(ruta)[1]

            # Mapear las extensiones a los tipos de contenido MIME
            tipos_mimetype = {
                '.html': 'text/html',
                '.jpg': 'image/jpeg',
                '.png': 'image/png',
                '.pdf': 'application/pdf'
            }

            # Establecer el tipo de contenido MIME según la extensión del archivo
            tipo_mimetype = tipos_mimetype.get(extension, 'application/octet-stream')

            # Enviar la respuesta al cliente
            self.send_response(200)
            self.send_header('Content-type', tipo_mimetype)
            self.end_headers()
            self.wfile.write(contenido)

        except FileNotFoundError:
            self.send_response(404)
            self.send_header('Content-type', 'text/html')
            self.end_headers()
            self.wfile.write(b'Archivo no encontrado')

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)

        # Aquí puedes procesar los datos recibidos en la solicitud POST
        # (por ejemplo, almacenarlos en un archivo o en una base de datos)
        with open('datos_post.txt', 'wb') as archivo:
            archivo.write(post_data)

        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write(b'Solicitud POST recibida y procesada correctamente')

    def do_PUT(self):
        content_length = int(self.headers['Content-Length'])
        put_data = self.rfile.read(content_length)

        # Aquí puedes procesar los datos recibidos en la solicitud PUT
        # (por ejemplo, actualizar un archivo existente)
        with open('datos_put.txt', 'wb') as archivo:
            archivo.write(put_data)

        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write(b'Solicitud PUT recibida y procesada correctamente')

    def do_DELETE(self):
        # Aquí puedes implementar la lógica para manejar una solicitud DELETE
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write(b'Solicitud DELETE recibida y procesada correctamente')

def run(server_class=ThreadedHTTPServer, handler_class=CustomRequestHandler, port=8080):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print(f"Servidor escuchando en el puerto {port}")
    httpd.serve_forever()

if __name__ == '__main__':
    run()
