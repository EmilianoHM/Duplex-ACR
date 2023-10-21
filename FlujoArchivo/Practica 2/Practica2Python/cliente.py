import socket
import os
import time

def main():
    # Configuración del cliente
    host = '127.0.0.1'
    port = 12345
    buffer_size = 30720

    # Carpeta de archivos del cliente
    client_folder = "C:/Users/Dell/Desktop/ESCOM/Redes 2/Duplex-ACR/FlujoArchivo/Practica 2/Practica2Python/ArchivosCliente/"

    # Crear un socket UDP
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    # Solicitar al usuario el nombre del archivo a enviar
    file_name = input("Ingrese el nombre del archivo a enviar: ")
    file_path = os.path.join(client_folder, file_name)

    # Comprobar si el archivo existe y obtener su tamaño
    if os.path.exists(file_path):
        file_size = os.path.getsize(file_path)
        print(f"Enviando archivo: {file_name} ({file_size} bytes)")

        # Enviar el nombre del archivo como el primer paquete
        client_socket.sendto(file_name.encode('utf-8'), (host, port))

        # Leer el archivo y enviarlo al servidor en paquetes
        with open(file_path, 'rb') as file:
            packet_number = 0
            while True:
                data = file.read(buffer_size)
                if not data:
                    break
                packet_number += 1
                while True:
                    client_socket.sendto(data, (host, port))
                    print(f"Enviando paquete {packet_number} ({len(data)} bytes)")
                    ack, _ = client_socket.recvfrom(10)  # Esperar confirmación del servidor
                    if ack == b"ACK":
                        break
                time.sleep(0.1)  # Espera breve para evitar saturar la red

        # Envía un paquete vacío para indicar el final del archivo
        while True:
            client_socket.sendto(b"", (host, port))
            ack, _ = client_socket.recvfrom(10)  # Esperar confirmación del servidor
            if ack == b"ACK":
                break
        print("Archivo enviado con éxito")
    else:
        print(f"El archivo '{file_name}' no existe en la ubicación especificada.")

    client_socket.close()

if __name__ == "__main__":
    main()
