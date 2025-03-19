package ppti.view;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ppti.viewmodel.StartingScreenViewModel;

public abstract class BaseView extends BorderPane{

    protected BorderPane root = new BorderPane();

    protected abstract void customizeScreen();

    protected abstract void bindToViewModel();
    
	public abstract void setViewModel(Object viewModel);

        
    protected void addContainerRoot(Node top, Node left, Node center, Node right, Node bottom) {
        if (top != null) {
            this.setTop(top);
        }
        if (left != null) {
            this.setLeft(left);
        }
        if (right != null) {
            this.setRight(right);
        }
        if (bottom != null) {
            this.setBottom(bottom);
        }
        if (center != null) {
            this.setCenter(center);
        }
    }

    public BorderPane getRoot() {
        return root;
    }


	

}
