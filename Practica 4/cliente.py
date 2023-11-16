# cliente.py

import urllib.request

# URL del servidor
url_servidor = "http://localhost:8080"

# Realiza una solicitud GET al servidor
respuesta = urllib.request.urlopen(url_servidor)

# Lee y muestra la respuesta del servidor
contenido = respuesta.read().decode('utf-8')
print(contenido)
