import socket
import threading

# Crear un socket del servidor
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Configurar el servidor
host = "0.0.0.0"
port = 12345
server_socket.bind((host, port))
server_socket.listen(5)

# Lista de clientes conectados y sus nombres
clients = {}

# Función para enviar mensajes a todos los clientes
def broadcast(message, client_socket):
    for client in clients:
        if client != client_socket:
            try:
                client.send(message)
            except:
                # En caso de error, si el cliente no puede recibir el mensaje, lo desconectamos
                client.close()
                remove_client(client)

# Función para enviar la lista de usuarios conectados a todos los clientes
def send_user_list(client_socket):
    users = ', '.join(clients.values())
    user_list_message = f'Usuarios conectados: {users}'
    client_socket.send(user_list_message.encode('utf-8'))

# Función para eliminar a un cliente de la lista
def remove_client(client_socket):
    if client_socket in clients:
        name = clients[client_socket]
        del clients[client_socket]
        print(f"{name} se ha desconectado.")

# Función para manejar las conexiones entrantes
def handle_client(client_socket):
    try:
        # Pedir el nombre del cliente
        client_socket.send("Ingrese su nombre: ".encode('utf-8'))
        name = client_socket.recv(1024).decode('utf-8')

        # Saludar al cliente
        welcome_message = f"Bienvenido, {name}! Para salir del chat, escriba 'salir'."
        client_socket.send(welcome_message.encode('utf-8'))

        # Notificar a todos que un nuevo cliente se ha unido
        message = f"{name} se ha unido al chat."
        broadcast(message.encode('utf-8'), client_socket)

        # Enviar la lista de usuarios conectados a todos los clientes
        send_user_list(client_socket)

        # Agregar al cliente a la lista de clientes
        clients[client_socket] = name

        while True:
            message = client_socket.recv(1024)
            if message:
                if message.decode('utf-8') == 'salir':
                    break
                message_to_send = f"{name}: {message.decode('utf-8')}"
                broadcast(message_to_send.encode('utf-8'), client_socket)

    except:
        pass
    finally:
        # Cuando un cliente se desconecta, eliminarlo de la lista y notificar a los demás
        remove_client(client_socket)
        client_socket.close()
        broadcast(f"{name} se ha desconectado.".encode('utf-8'), client_socket)
        # Enviar la lista de usuarios actualizada a todos los clientes
        broadcast("".encode('utf-8'), client_socket)

# Ciclo principal para aceptar conexiones de clientes
while True:
    client_socket, client_address = server_socket.accept()
    print(f"Conexión desde: {client_address[0]}:{client_address[1]}")
    client_socket.send("Conectado al servidor de chat. Escribe 'salir' para salir.".encode('utf-8'))

    # Iniciar un hilo para manejar al cliente
    client_handler = threading.Thread(target=handle_client, args=(client_socket,))
    client_handler.start()
