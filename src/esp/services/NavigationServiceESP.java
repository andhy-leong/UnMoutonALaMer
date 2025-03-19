package esp.services;

import java.util.Stack;
import java.util.stream.Collectors;

import common.Config;
import common.ThemeManager;
import common.navigation.NavigationService;
import common.ui.BaseScreenView;
import common.ui.PopUpMenuView;
import common.ui.screens.HelpScreenView;
import common.ui.screens.OptionsScreenView;
import common.ui.screens.StartingScreenView;
import esp.view.EspConnectionScreenView;
import esp.view.GameScreenViewEsp;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class NavigationServiceESP extends NavigationService {
    private EspConnectionScreenView espConnectionScreenView;

    public NavigationServiceESP(Stage stage, StackPane root) {
        super(stage, root);
    }

    @Override
    public void initializeViews() {
        super.initializeViews();
        espConnectionScreenView = findView(EspConnectionScreenView.class);
    }

    @Override
    protected <T> T findView(Class<T> viewClass) {
        // Pour les vues spécifiques à l'un des deux types, retourner null si on ne les trouve pas
        if (viewClass == EspConnectionScreenView.class) {
            return root.getChildren().stream()
                .filter(node -> viewClass.isInstance(node))
                .map(node -> (T) node)
                .findFirst()
                .orElse(null);
        }
        // Pour les autres vues (communes), utiliser la méthode parent
        return super.findView(viewClass);
    }

    public void navigateToEspConnectionScreen() {
        navigateTo(espConnectionScreenView);
    }

    public void navigateToGameScreenViewEsp() {
        navigateTo(this.findView(GameScreenViewEsp.class));
    }
}