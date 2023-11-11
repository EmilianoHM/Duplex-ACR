import socket
import threading

# Lista de emojis predefinidos para mostrar al inicio del chat
emojis_list = {
    "1": "😊",
    "2": "😂",
    "3": "😎",
    "4": "❤️",
    "5": "👍",
    "6": "😍"
}

# Función para mostrar la lista de emojis al inicio del chat
def show_emojis():
    print("Lista de Emojis:")
    for key, emoji in emojis_list.items():
        print(f"Opción {key}: {emoji}")

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
        except Exception as e:
            print(f"Error en receive_messages: {e}")
            client_socket.close()
            break

# Iniciar el cliente
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect((host, port))

# Pedir y enviar el nombre del cliente
name = input("Ingrese su nombre: ")
client_socket.send(name.encode('utf-8'))

# Mostrar la lista de emojis
show_emojis()

# Iniciar un hilo para recibir mensajes
receive_thread = threading.Thread(target=receive_messages, args=(client_socket,))
receive_thread.start()

# Enviar mensajes al servidor
while True:
    message = input("Ingrese un mensaje ('@nombre_destinatario:mensaje' para mensaje privado, 'salir' para salir): ")
    if message == 'salir':
        client_socket.send(message.encode('utf-8'))
        break
    elif message in emojis_list:
        # Si el mensaje corresponde a un emoji de la lista, enviar el emoji al servidor
        selected_emoji = emojis_list[message]
        client_socket.send(selected_emoji.encode('utf-8'))
    else:
        client_socket.send(message.encode('utf-8'))

# Cerrar el cliente
client_socket.close()

#C:/Users/emhurtad/AppData/Local/Microsoft/WindowsApps/python3.11.exe "c:/Users/emhurtad/Documents/Escuela/Duplex-ACR/Practica 3/practica 3_python/cliente/cliente.py"