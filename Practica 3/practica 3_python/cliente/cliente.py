import socket
import threading

# Configurar el cliente
host = "localhost"
port = 12345

# Función para recibir mensajes del servidor
def receive_messages(client_socket):
    while True:
        try:
            message = client_socket.recv(1024).decode('utf-8')
            if message == "refresh_users":
                # Si se recibe "refresh_users", solicitar y mostrar la lista actualizada de usuarios
                client_socket.send("get_users".encode('utf-8'))
            elif message == "get_users":
                # Si se recibe "get_users", recibir y mostrar la lista de usuarios
                users_message = client_socket.recv(1024).decode('utf-8')
                print(users_message)
            else:
                print(message)
        except:
            # En caso de error, si no se puede recibir el mensaje, desconectar el cliente
            print("¡Error de conexión!")
            client_socket.close()
            break

# Iniciar el cliente
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect((host, port))

# Pedir y enviar el nombre del cliente
name = input("Ingrese su nombre: ")
client_socket.send(name.encode('utf-8'))

# Iniciar un hilo para recibir mensajes
receive_thread = threading.Thread(target=receive_messages, args=(client_socket,))
receive_thread.start()

# Enviar mensajes al servidor
while True:
    message = input()
    if message == 'salir':
        client_socket.send(message.encode('utf-8'))
        break
    else:
        client_socket.send(message.encode('utf-8'))

# Cerrar el cliente
client_socket.close()
