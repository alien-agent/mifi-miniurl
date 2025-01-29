package org.dvpashkevich.miniurl.cli;

import org.dvpashkevich.miniurl.service.IService;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;

public class CLI {
    private String currentUserID = "";
    private final IService service;
    private final Scanner scanner;

    public CLI(IService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    private static class Command {
        private final String cmd;
        private final String[] args;

        public Command(String raw) {
            String[] parts = raw.split(" ");
            cmd = parts[0];
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);
        }

        String getCmd() {
            return cmd;
        }

        String[] getArgs() {
            return args;
        }
    }

    public void run() {
        printInfo();

        while (true) {
            System.out.print("\nВведите команду: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            Command cmd = new Command(input);
            switch (cmd.getCmd()) {
                case "help":
                    printInfo();
                    break;
                case "exit":
                    System.exit(0);
                    break;
                case "login":
                    login(cmd.getArgs());
                    break;
                case "list":
                    list(cmd.getArgs());
                    break;
                case "shorten":
                    shorten(cmd.getArgs());
                    break;
                case "edit":
                    edit(cmd.getArgs());
                    break;
                case "visit":
                    visit(cmd.getArgs());
                    break;
                case "remove":
                    remove(cmd.getArgs());
                    break;
                default:
                    println("Неизвестная команда!");
            }
        }
    }

    private void println(String s) {
        System.out.println("> " + s);
    }

    private void printInfo() {
        println("=== Список команд ===");
        println("help                                 --- показать эту справку;");
        println("exit                                 --- завершить работу;");
        println("login   <userID>                     --- авторизоваться как пользователь с ID <userID>;");
        println("list                                 --- вывести список кодов ссылок;");
        println("shorten <url> <ttlSecs> <maxVisits>  --- сократить ссылку <url> с блокировкой через <ttlSecs> секунд или <maxVisits> открытий;");
        println("edit    <code> <ttlSecs> <maxVisits> --- установить ссылке с ID <code> время истечения/макс. кол-во посещений в <ttlSecs>/<maxVisits>;");
        println("visit   <code>                       --- перейти по ссылке с ID <code>;");
        println("remove  <code>                       --- удалить сокращенную ссылку с ID <code>;");
    }

    private void errorInvalidArgs() {
        println("Неверные аргументы команды!");
    }

    private void errorUnauthorized() {
        println("Пользователь не авторизован или нет доступа!");
    }

    private void login(String[] args) {
        if (args.length != 1) {
            errorInvalidArgs();
            return;
        }

        if (!service.userExists(args[0])) {
            println("Пользователь не существует!");
            return;
        }

        currentUserID = args[0];
        println("Успешно авторизован");
    }

    private void list(String[] args) {
        if (currentUserID.isEmpty()) {
            errorUnauthorized();
            return;
        }

        List<String> codes = service.list(currentUserID);
        println("У вас есть " + codes.size() + " активных ссылок:");
        for (String code : codes) {
            System.out.println("\t- " + code);
        }
    }

    private void shorten(String[] args) {
        if (args.length != 3) {
            errorInvalidArgs();
            return;
        }

        String url = args[0];
        int ttlSecs, maxVisits;
        try {
            ttlSecs = Integer.parseInt(args[1]);
            maxVisits = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            errorInvalidArgs();
            return;
        }

        try {
            new URI(url);
        } catch (URISyntaxException e) {
            println("Неверная ссылка");
            return;
        }

        if (currentUserID.isEmpty()) {
            currentUserID = service.register();
            println("Вы зарегистрированы под ID: " + currentUserID);
        }

        String code = service.shorten(url, currentUserID, ttlSecs, maxVisits);
        println("Ваша уникальный код: " + code);
    }

    private void edit(String[] args) {
        if (args.length != 3) {
            errorInvalidArgs();
            return;
        }

        String code = args[0];
        int ttlSecs = Integer.parseInt(args[1]);
        int maxVisits = Integer.parseInt(args[2]);

        if (currentUserID.isEmpty() || !service.isOwnedBy(code, currentUserID)) {
            errorUnauthorized();
            return;
        }

        service.edit(code, currentUserID, ttlSecs, maxVisits);
        println("Ссылка успешно обновлена");
    }

    private void remove(String[] args) {
        if (args.length != 1) {
            errorInvalidArgs();
            return;
        }

        String code = args[0];

        if (currentUserID.isEmpty() || !service.isOwnedBy(code, currentUserID)) {
            errorUnauthorized();
            return;
        }

        service.remove(code, currentUserID);
        println("Ссылка удалена");
    }

    private void visit(String[] args) {
        if (args.length != 1) {
            errorInvalidArgs();
            return;
        }

        String code = args[0];

        if (!service.isActive(code)) {
            println("Ссылка просмотрена максимальное количество раз или устарела!");
            return;
        }

        String url = service.decode(code);
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }

        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            println("Не удалось открыть ссылку: " + e.getMessage());
            return;
        }
        println("Исходная ссылка открыта в браузере");
    }
}