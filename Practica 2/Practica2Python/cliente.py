import socket
import os
import time

def send_file(client_socket): #////////////////////////////////////////////////
    # Configuración del cliente
    host = '127.0.0.1'
    port = 12345
    buffer_size = 30720

    # Carpeta de archivos del cliente
    client_folder = "C:/ArchivosCliente/"

     # Enviar una solicitud al servidor para recibir un archivo
    client_socket.sendto(b"SEND", (host, port))

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
    input("Presiona Enter...")
    os.system('cls')

def main():
    # Configuración del cliente
    host = '127.0.0.1'  # Dirección IP del servidor
    port = 12345       # Puerto del servidor

    # Crear un socket UDP
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    while True:
        print("Menu:")
        print("1. Enviar un archivo al servidor")
        print("2. Salir")

        choice = input("Elija una opción (1/2): ")

        if choice == '1':
            send_file(client_socket)
        elif choice == '2':
            print("Gracias por usarnos. Hasta la próxima...")
            break
        else:
            print("Opción no válida. Intente de nuevo.")

    client_socket.close()

if __name__ == "__main__":
    main()