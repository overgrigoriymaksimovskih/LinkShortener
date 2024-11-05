package View;


import java.util.Scanner;

public class View implements ControllerObserver {

    private Scanner scanner;

    public View() {
        this.scanner = new Scanner(System.in);
        System.out.println("+--------------------------------------+");
        System.out.println("| Бобро пожаловать.....................|");
        System.out.println("| Введите строку (или 'q' для выхода): |");
        System.out.println("+--------------------------------------+");
    }

    public String readLine() {
        return scanner.nextLine();
    }

    @Override
    public void printQuit() {
        System.out.println("Программа завершена.");
    }
    @Override
    public void printNotUrl() {
        System.out.println("Введенная строка не является ссылкой \n+--------------------------------------+");
    }
    @Override
    public void printHandleUrl() {
        System.out.println("Ваша ссылка обрабатывается...");
    }
    @Override
    public void printHandleFtp() {
        System.out.println("Ваша Ftp ссылка обрабатывается...");
    }
    @Override
    public void printDomainNotFound() {
        System.out.println("domain not found \n" + "+--------------------------------------+");
    }
    @Override
    public void printResult(String message) {
        System.out.println("domain ID: " + message + "\n"  + "+--------------------------------------+");
    }
}
