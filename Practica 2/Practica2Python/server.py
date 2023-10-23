import socket
import os

def main():
    # Configuración del servidor
    host = '127.0.0.1'
    port = 12345
    buffer_size = 65535

    # Carpeta de archivos del servidor
    server_folder = "C:/Users/Dell/Desktop/ESCOM/Redes 2/Duplex-ACR/FlujoArchivo/Practica 2/Practica2Python/ArchivosServidor/"

    # Crear un socket UDP y enlace
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_socket.bind((host, port))

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
    server_socket.close()

if __name__ == "__main__":
    main()
