package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        //Список разрешенных путей
        final var validPaths = List.of("/index.html",
                "/spring.svg", "/spring.png", "/resources.html",
                "/styles.css", "/app.js", "/links.html", "/forms.html",
                "/classic.html", "/events.html", "/events.js");

        try (final var serverSocket = new ServerSocket(9999)) {
            while (true) {
                try (
                        final var socket = serverSocket.accept();
                        final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        final var out = new BufferedOutputStream(socket.getOutputStream());
                ) {


                    /*
                    описание вопроса состоит из метода/пути/версии

                    // read only request line for simplicity
                    // must be in form GET /path HTTP/1.1

                     */
                    final var requestLine = in.readLine();
                    final var parts = requestLine.split(" ");

                    /*
                    проверка: что запрос разбивается на три части
                        continue - переводи цикл whilе на след кру, т.е. закрывает ресурсы и разрывает сокет
                     */
                    if (parts.length != 3) {
                        // just close socket
                        continue;
                    }

                    /*
                    мапим путь из запроса в путь к файлу на диске
                     */
                    final var path = parts[1];
                    /*
                    проверяем может ли быть осуществлен запрос к ресурсам (есть лит запрошенный путь среди наших сохраненных путей)
                     */
                    if (!validPaths.contains(path)) {
                        //возвращаем ответ, который состоит:
                        out.write((
                                "HTTP/1.1 404 Not Found\r\n" +
                                        "Content-Length: 0\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        //закрываем поток
                        out.flush();
                        //закрываем цикл
                        continue;
                    }
                    /*
                    в разработке обычно используются относительный путь
                    т.к. папка нах в проекте исп. относ. путь то назв. пути начинается с точки,
                    т.к мы знаем что все ресурсы лежат в папке то добаляем знач папки, и исп. значение запроса
                     */
                    final var filePath = Path.of(".", "public", path);
                    //указ тип содержимого файла()
                    final var mimeType = Files.probeContentType(filePath);


                    /*
                    если запрашивается требуемый класс
                    тогда считаем файл
                    с помощью стринг реплейс ищем шаблон и меняем его значение на тек время
                    получаем байты
                    сообщаем клиенту окей
                    отправляем контент
                    выходим из обработчика
                     */
                    // special case for classic
                    if (path.equals("/classic.html")) {
                        final var template = Files.readString(filePath);
                        final var content = template.replace(
                                "{time}",
                                LocalDateTime.now().toString()
                        ).getBytes();
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + content.length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        out.write(content);
                        out.flush();
                    }

                    /*
                    для правильной формировки заголовков
                    указываем длину файла
                    */
                    final var length = Files.size(filePath);
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +                             // сообщаем об удачном запросе
                                    "Content-Type: " + mimeType + "\r\n" +      // указ. заголовок
                                    "Content-Length: " + length + "\r\n" +      // указ. длину тела
                                    "Connection: close\r\n" +                   // указа что мы закрываем
                                    "\r\n"                                      //и
                    ).getBytes());                                              //отправялем данные клиенту
                    /*
                    копируем выходной поток байт
                     */
                    Files.copy(filePath, out);
                    // принудительно отправляем содерж буфера
                    out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}