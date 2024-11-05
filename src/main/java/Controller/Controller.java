package Controller;

import Model.Model;
import View.View;
public class Controller implements ModelObserver {
    private Model model = new Model(this);
    private View view;

    public Controller(View view) {
        this.view = view;
    }
    public Model getModel() {
        return model;
    }
//----------------------------------------------------------------------------------------------------------------------

    public boolean handleInput(String str) throws InterruptedException {
        if (str.equalsIgnoreCase("q")) {
            return false;
        }else{
            if(!LineHandler.checkLine(str)){
                LineHandler.handleLine(str, view);
            }else{
                model.getDomain(LineHandler.handleLine(str, view));
            }
            return true;
        }
    }

//----------------------------------------------------------------------------------------------------------------------
    @Override
    public void update(int userId) {
        if(userId == -1){
            view.printDomainNotFound();
        }else{
            view.printResult(Integer.toString(userId));
        }
    }
}
