import socket
import os
import time

def receive_file(server_socket): #////////////////////////////////////////////
    # Configuración del servidor
    host = '127.0.0.1'
    port = 12345
    buffer_size = 65535

    # Carpeta de archivos del servidor
    server_folder = "C:/ArchivosServidor/"

    packet_number = 0
    received_filename = None

    while True:
        data, client_address = server_socket.recvfrom(buffer_size)
        if not data:
            break
        if not received_filename:
            # El primer paquete recibido contiene el nombre del archivo
            received_filename = data.decode('utf-8')
            file_path = os.path.join(server_folder, received_filename)
            with open(file_path, 'wb') as file:
                continue
        packet_number += 1
        with open(file_path, 'ab') as file:
            file.write(data)
        print(f"Recibido paquete {packet_number} ({len(data)} bytes)")
        server_socket.sendto(b"ACK", client_address)  # Enviar confirmación al cliente

    print(f"Archivo '{received_filename}' recibido con éxito")
    server_socket.sendto(b"ACK", client_address)  # Enviar confirmación al cliente

def main():
    # Configuración del servidor
    host = '127.0.0.1'  # Dirección IP en la que el servidor escuchará
    port = 12345       # Puerto en el que el servidor escuchará
    buffer_size = 65535  # Tamaño del búfer para la lectura/escritura de datos

    # Crear un socket UDP y enlazarlo
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_socket.bind((host, port))

    client_address = None

    while True:
        data, client_address = server_socket.recvfrom(buffer_size)
        if not data:
            break
        if data == b"SEND":
            receive_file(server_socket)
        """elif data == b"RECEIVE":
            send_file(server_socket)"""

    server_socket.close()

if __name__ == "__main__":
    main()