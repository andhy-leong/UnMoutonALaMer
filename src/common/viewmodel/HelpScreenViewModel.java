package common.viewmodel;

import common.locales.I18N;
import common.model.HelpScreenModel;
import common.navigation.NavigationService;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HelpScreenViewModel {
    private HelpScreenModel model;
    private NavigationService navigationService;
    private StringBinding helpTextProperty;
    private StringProperty helpImage;
    private IntegerProperty currentPage = new SimpleIntegerProperty(0);
    private StringBinding titleLabelProperty;
    private boolean isSpyUser;

    public HelpScreenViewModel(HelpScreenModel model, NavigationService navigationService, boolean isSpyUser) {
        this.model = model;
        this.navigationService = navigationService;
        this.isSpyUser = isSpyUser;
        
        titleLabelProperty = I18N.createStringBinding("titleLabelHelpScreenIDJR");

        if (isSpyUser) {
            helpTextProperty = Bindings.createStringBinding(() -> I18N.get("helpTextHelpScreenESP.page" + (currentPage.get() + 1)),currentPage);

        } else {
            helpTextProperty = Bindings.createStringBinding(() -> I18N.get("helpTextHelpScreenIDJR.page" + (currentPage.get() + 1)),currentPage);
        }
        helpImage = new SimpleStringProperty();
        updateHelpContent();
    }
    
    public StringBinding titleLabelProperty() {
    	return titleLabelProperty;
    }

    public StringBinding helpTextProperty() {
        return helpTextProperty;
    }

    public StringProperty helpImageProperty() {
        return helpImage;
    }

    public IntegerProperty currentPageProperty() {
        return currentPage;
    }

    public int getTotalPages() {
        return model.getTotalPages();
    }

    public void onBackButtonClicked() {
        navigationService.navigateBack();
    }

    public void onPreviousButtonClicked() {
        if (currentPage.get() > 0) {
            currentPage.set(currentPage.get() - 1);
            updateHelpContent();
        }
    }

    public void onNextButtonClicked() {
        if (currentPage.get() < model.getTotalPages() - 1) {
            currentPage.set(currentPage.get() + 1);
            updateHelpContent();
        }
    }

    public void onCircleClicked(int circleIndex) {
        currentPage.set(circleIndex);
        updateHelpContent();
    }

    public boolean isFirstPage() {
        return currentPage.get() == 0;
    }

    public boolean isLastPage() {
        return currentPage.get() == model.getTotalPages() - 1;
    }

    public void updateHelpContent() {
    	//helpTextProperty.set(model.getHelpText(currentPage.get()));
        helpImage.set(model.getHelpImage(currentPage.get()));
    }
}