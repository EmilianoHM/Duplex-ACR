# servidor.py

from http.server import SimpleHTTPRequestHandler
from socketserver import TCPServer

# Define el puerto en el que el servidor escuchará las conexiones
puerto = 8080

# Crea un manejador de solicitudes HTTP simple
manejador = SimpleHTTPRequestHandler

# Crea el servidor HTTP y enlázalo al puerto
servidor = TCPServer(("", puerto), manejador)

print(f"Servidor escuchando en el puerto {puerto}")

# Inicia el servidor
servidor.serve_forever()
