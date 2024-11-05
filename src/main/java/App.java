import Controller.Controller;
import View.View;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws InterruptedException {
        View view = new View();
        Controller controller = new Controller(view);
        controller.getModel().initialisation();
//----------------------------------------------------------------------------------------------------------------------
        while (controller.handleInput(view.readLine().trim())) {
        }
        view.printQuit();
//----------------------------------------------------------------------------------------------------------------------
    }
//    public void printConsoleAnswer
}
