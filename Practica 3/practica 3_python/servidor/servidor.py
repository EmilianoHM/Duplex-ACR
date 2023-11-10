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
def broadcast(message, sender_socket, recipient_socket=None):
    for client_socket in clients:
        if client_socket != sender_socket and (recipient_socket is None or client_socket == recipient_socket):
            try:
                client_socket.send(message)
            except:
                # En caso de error, si el cliente no puede recibir el mensaje, lo desconectamos
                remove_client(client_socket)

# Función para enviar la lista de usuarios conectados a todos los clientes
def broadcast_users_list():
    users = ', '.join(clients.values())
    user_list_message = f'Usuarios conectados: {users}'

    # Enviar la lista de usuarios actualizada a todos los clientes
    broadcast(user_list_message.encode('utf-8'), None)

# Función para eliminar a un cliente de la lista
def remove_client(client_socket):
    global clients  # Hacer referencia a la variable global
    if client_socket in clients:
        name = clients[client_socket]
        del clients[client_socket]
        print(f"{name} se ha desconectado.")

        # Notificar a todos que un cliente se ha desconectado antes de imprimir la lista
        broadcast(f"{name} se ha desconectado.".encode('utf-8'), client_socket)

        # Enviar la lista de usuarios actualizada a todos los clientes
        broadcast_users_list()

# Función para manejar las conexiones entrantes
def handle_client(client_socket):
    global clients  # Hacer referencia a la variable global
    try:
        # Pedir el nombre del cliente
        client_socket.send("Ingrese su nombre: ".encode('utf-8'))
        name = client_socket.recv(1024).decode('utf-8')

        # Saludar al cliente
        welcome_message = f"Bienvenido, {name}! Para salir del chat, escriba 'salir'."
        client_socket.send(welcome_message.encode('utf-8'))

        # Agregar al cliente a la lista de clientes
        clients[client_socket] = name

        # Notificar a todos que un nuevo cliente se ha unido
        message = f"{name} se ha unido al chat."
        broadcast(message.encode('utf-8'), client_socket)

        # Enviar la lista de usuarios conectados a todos los clientes, incluido el nuevo cliente
        broadcast_users_list()

        while True:
            message = client_socket.recv(1024)
            if message:
                decoded_message = message.decode('utf-8')
                if decoded_message == 'salir':
                    break
                elif decoded_message.startswith('@'):
                    # El mensaje comienza con '@', interpretamos como un mensaje privado
                    recipient_name, private_message = decoded_message[1:].split(':', 1)
                    recipient_socket = get_client_socket_by_name(recipient_name)
                    if recipient_socket:
                        private_message = f"{name} (privado): {private_message}"
                        recipient_socket.send(private_message.encode('utf-8'))
                else:
                    # Mensaje para todo el chat
                    message_to_send = f"{name}: {decoded_message}"
                    broadcast(message_to_send.encode('utf-8'), client_socket)

    except Exception as e:
        print(f"Error en handle_client: {e}")
    finally:
        # Cuando un cliente se desconecta, eliminarlo de la lista y notificar a los demás
        remove_client(client_socket)
        client_socket.close()

# Función auxiliar para obtener el socket del destinatario por su nombre
def get_client_socket_by_name(client_name):
    for socket, name in clients.items():
        if name == client_name:
            return socket
    return None

# Ciclo principal para aceptar conexiones de clientes
while True:
    client_socket, client_address = server_socket.accept()
    print(f"Conexión desde: {client_address[0]}:{client_address[1]}")
    client_socket.send("Conectado al servidor de chat. Escribe 'salir' para salir.".encode('utf-8'))

    # Iniciar un hilo para manejar al cliente
    client_handler = threading.Thread(target=handle_client, args=(client_socket,))
    client_handler.start()
