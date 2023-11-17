# cliente.py
import requests

url_servidor = "http://localhost:8080"

def realizar_solicitud(opcion):
    if opcion == '1':
        response = requests.get(url_servidor + "/ejemplo.html")
        print("HTML Response:")
        print(response.text)
    elif opcion == '2':
        response = requests.get(url_servidor + "/ejemplo.jpg")
        with open("imagen_recibida.jpg", "wb") as archivo:
            archivo.write(response.content)
        print("Imagen JPEG recibida y guardada como 'imagen_recibida.jpg'")
    elif opcion == '3':
        response = requests.get(url_servidor + "/ejemplo.png")
        with open("imagen_recibida.png", "wb") as archivo:
            archivo.write(response.content)
        print("Imagen PNG recibida y guardada como 'imagen_recibida.png'")
    elif opcion == '4':
        response = requests.get(url_servidor + "/ejemplo.pdf")
        with open("documento_recibido.pdf", "wb") as archivo:
            archivo.write(response.content)
        print("Documento PDF recibido y guardado como 'documento_recibido.pdf'")
    elif opcion == '5':
        datos = {"key": "value"}
        response = requests.post(url_servidor, data=datos)
        print("POST Response:")
        print(response.text)
    elif opcion == '6':
        datos = {"key": "new_value"}
        response = requests.put(url_servidor, data=datos)
        print("PUT Response:")
        print(response.text)
    elif opcion == '7':
        response = requests.delete(url_servidor)
        print("DELETE Response:")
        print(response.text)
    else:
        print("Opción no válida")

if __name__ == "__main__":
    while True:
        print("\nMenú de opciones:")
        print("1. Realizar solicitud GET para HTML")
        print("2. Realizar solicitud GET para Imagen JPEG")
        print("3. Realizar solicitud GET para Imagen PNG")
        print("4. Realizar solicitud GET para Documento PDF")
        print("5. Realizar solicitud POST")
        print("6. Realizar solicitud PUT")
        print("7. Realizar solicitud DELETE")
        print("8. Salir")

        opcion = input("Seleccione una opción (1-8): ")

        if opcion == '8':
            break  # Salir del bucle si se selecciona la opción 8
        else:
            realizar_solicitud(opcion)

#C:/Users/emhurtad/AppData/Local/Microsoft/WindowsApps/python3.11.exe "c:/Users/emhurtad/Documents/Escuela/Duplex-ACR/Practica 4/cliente/cliente.py"