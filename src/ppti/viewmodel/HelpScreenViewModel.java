package ppti.viewmodel;

import common.locales.I18N;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ppti.model.HelpScreenModel;
import ppti.view.singleton.SingletonView;

public class HelpScreenViewModel {
    private HelpScreenModel model;
	private SingletonView singleton;
    private StringProperty helpText = new SimpleStringProperty();
    private IntegerProperty currentPage = new SimpleIntegerProperty(0);

    private StringBinding titleProperty;

    public HelpScreenViewModel(HelpScreenModel model, SingletonView singleton) {
    	this.model = model;
    	this.singleton = singleton;
        updateHelpContent();

        titleProperty = I18N.createStringBinding("titleLabelHelpScreenPPTI");

        // Add listener for language changes
        I18N.localeProperty().addListener((observable, oldValue, newValue) -> {
            updateHelpContent();
            titleProperty.invalidate();
        });
    }

    public int getTotalPages() {
        return model.getTotalPages();
    }

    private void updateHelpContent() {
        helpText.set(model.getHelpText(currentPage.get()));
    }

    public StringProperty helpTextProperty() {
        return helpText;
    }

    public IntegerProperty currentPageProperty() {
        return currentPage;
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

    public void onBackButtonClicked() {
        singleton.setViewVisible(0);
    }

    /* Internationalization */

    public StringBinding titleProperty() {
        return titleProperty;
    }
}
