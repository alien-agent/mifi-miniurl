package org.dvpashkevich.miniurl;

import org.dvpashkevich.miniurl.cli.CLI;
import org.dvpashkevich.miniurl.service.Service;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var service = new Service();
        var scanner = new Scanner(System.in);
        var cli = new CLI(service, scanner);

        cli.run();
    }
}