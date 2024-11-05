package View;

public interface ControllerObserver {
    public void printQuit();
    public void printNotUrl();
    public void printHandleUrl();
    public void printHandleFtp();
    public void printDomainNotFound();
    public void printResult(String message);
}