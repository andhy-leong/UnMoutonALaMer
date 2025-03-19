package common.navigation;

import java.util.Stack;
import java.util.stream.Collectors;

import common.Config;
import common.ThemeManager;
import common.ui.BaseScreenView;
import common.ui.PopUpMenuView;
import common.ui.screens.HelpScreenView;
import common.ui.screens.OptionsScreenView;
import common.ui.screens.StartingScreenView;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class NavigationService {
    protected Stage stage;
    protected StackPane root;
    protected StartingScreenView startingScreenView;
    protected OptionsScreenView optionsScreenView;
    protected HelpScreenView helpScreenView;
    protected PopUpMenuView popUpMenuView;
    
    protected static final Stack<BaseScreenView> navigationHistory = new Stack<>();
    protected BaseScreenView currentView;

    public NavigationService(Stage stage, StackPane root) {
        this.stage = stage;
        this.root = root;
    }

    public void initializeViews() {
        startingScreenView = findView(StartingScreenView.class);
        optionsScreenView = findView(OptionsScreenView.class);
        helpScreenView = findView(HelpScreenView.class);
        popUpMenuView = findView(PopUpMenuView.class);
        
        currentView = startingScreenView;

        // appliquer le theme par défaut au démarrage (applyCurrentTheme)
		ThemeManager.applyCurrentTheme(this.stage.getScene());
    }

    protected <T> T findView(Class<T> viewClass) {
        return root.getChildren().stream()
            .filter(node -> viewClass.isInstance(node))
            .map(node -> (T) node)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("La vue n'a pas été trouvée: " + viewClass.getSimpleName()));
    }

    public void showPopUpMenu() {
        popUpMenuView.updateVisibility(true);
    }

    public void hidePopUpMenu() {
        popUpMenuView.updateVisibility(false);
    }

    public void applyThemeToAllViews(int themeNumber, boolean isDarkMode) {
        ThemeManager.setTheme(stage.getScene(), themeNumber, isDarkMode);
    }

    protected void navigateTo(BaseScreenView view) {
        hideAllScreens();
        if (currentView != null && currentView != startingScreenView) {
            navigationHistory.push(currentView);
            if (Config.DEBUG_MODE) {
                System.out.println(">>> Navigation: Ajout à l'historique - " + currentView.getClass().getSimpleName());
                System.out.println(">>> État de l'historique: " + getHistoryState());
            }
        }
        currentView = view;
        if (Config.DEBUG_MODE) {
            System.out.println(">>> Navigation: Vue courante changée pour - " + view.getClass().getSimpleName());
        }
        view.updateVisibility(true);
    }

    public void navigateToStartingScreen() {
        if (Config.DEBUG_MODE) {
            System.out.println(">>> Navigation: Retour à l'écran de démarrage - Vidage de l'historique");
        }
        navigationHistory.clear();
        currentView = startingScreenView;
        hideAllScreens();
        startingScreenView.updateVisibility(true);
        if (Config.DEBUG_MODE) {
            System.out.println(">>> État de l'historique: " + getHistoryState());
        }
    }

    public void navigateToOptionsScreen() {
        navigateTo(optionsScreenView);
    }

    public void navigateToHelpScreen() {
        navigateTo(helpScreenView);
    }

    public void navigateBack() {
        if (!navigationHistory.isEmpty()) {
            hideAllScreens();
            currentView = navigationHistory.pop();
            currentView.updateVisibility(true);
        } else {
            navigateToStartingScreen();
        }
    }

    protected void hideAllScreens() {
        root.getChildren().forEach(node -> {
            if (node instanceof BaseScreenView) {
                ((BaseScreenView) node).updateVisibility(false);
            }
        });
    }

    protected String getHistoryState() {
        return "Taille: " + navigationHistory.size() + " | Contenu: " + 
               navigationHistory.stream()
                   .map(view -> view.getClass().getSimpleName())
                   .collect(Collectors.joining(" -> "));
    }
} 