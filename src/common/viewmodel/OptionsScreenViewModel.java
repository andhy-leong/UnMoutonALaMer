package common.viewmodel;

import common.ThemeManager;


import common.locales.I18N;
import common.navigation.NavigationService;
import esp.services.NavigationServiceESP;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import players.idjr.services.NavigationServiceIDJR;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Locale;

public class OptionsScreenViewModel {
	private NavigationService navigationService;
    private NavigationServiceIDJR navigationServiceIdjr;
    private NavigationServiceESP navigationServiceEsp;

    private StringProperty titleLabelProperty;
    private StringProperty languesLabelProperty;

    private StringProperty themeLabelProperty;
    private StringProperty couleurLabelProperty;

    private IntegerProperty themeNumber;
    private BooleanProperty isDarkMode;

    private StringProperty selectedLanguage;

    public OptionsScreenViewModel(NavigationService navigationService, NavigationServiceIDJR navigationServiceIdjr, NavigationServiceESP navigationServiceEsp ,  boolean isSpyUser) {
        this.navigationService = navigationService;
        this.navigationServiceIdjr = navigationServiceIdjr;
        this.navigationServiceEsp = navigationServiceEsp;

        titleLabelProperty = new SimpleStringProperty(I18N.get("optionsLabelOptionsIDJR"));
        languesLabelProperty = new SimpleStringProperty(I18N.get("languesLabelOptionsIDJR"));
        themeLabelProperty = new SimpleStringProperty(I18N.get("themesLabelOptionsIDJR"));
        couleurLabelProperty = new SimpleStringProperty(I18N.get("couleurLabelOptionsIDJR"));

        this.themeNumber = new SimpleIntegerProperty(1);
        this.isDarkMode = new SimpleBooleanProperty(false);

        this.selectedLanguage = new SimpleStringProperty();
        setInitialLanguage();

        themeNumber.addListener((obs, oldVal, newVal) -> applyTheme());
        isDarkMode.addListener((obs, oldVal, newVal) -> applyTheme());
    }

    private void setInitialLanguage() {
        String systemLanguage = Locale.getDefault().getLanguage();
        switch (systemLanguage) {
            case "fr":
                selectedLanguage.set("Français");
                break;
            case "en":
                selectedLanguage.set("English");
                break;
            case "es":
                selectedLanguage.set("Español");
                break;
            case "it":
                selectedLanguage.set("Italiano");
                break;
            case "de":
                selectedLanguage.set("Deutsch");
                break;
            case "pt":
                selectedLanguage.set("Português");
                break;
            default:
                selectedLanguage.set("English");
                break;
        }
    }

    public StringProperty selectedLanguageProperty() {
        return selectedLanguage;
    }

    public String getSelectedLanguage() {
        return selectedLanguage.get();
    }

    public void setSelectedLanguage(String language) {
        selectedLanguage.set(language);
    }

    public void onLangueButtonClicked(String langue) {
        Locale newLocale;
        switch (langue) {
            case "Fr":
                newLocale = new Locale("fr", "FR");
                selectedLanguage.set("Français");
                break;
            case "En":
                newLocale = new Locale("en", "US");
                selectedLanguage.set("English");
                break;
            case "Es":
                newLocale = new Locale("es", "ES");
                selectedLanguage.set("Español");
                break;
            case "It":
                newLocale = new Locale("it", "IT");
                selectedLanguage.set("Italiano");
                break;
            case "De":
                newLocale = new Locale("de", "DE");
                selectedLanguage.set("Deutsch");
                break;
            case "Pt":
                newLocale = new Locale("pt", "PT");
                selectedLanguage.set("Português");
                break;
            default:
                newLocale = I18N.getDefaultLocale();
        }

        I18N.setLocale(newLocale);

        titleLabelProperty.set(I18N.get("optionsLabelOptionsIDJR"));
        languesLabelProperty.set(I18N.get("languesLabelOptionsIDJR"));
        themeLabelProperty.set(I18N.get("themesLabelOptionsIDJR"));
        couleurLabelProperty.set(I18N.get("couleurLabelOptionsIDJR"));
    }

    public IntegerProperty themeNumberProperty() {
        return themeNumber;
    }

    public BooleanProperty isDarkModeProperty() {
        return isDarkMode;
    }

    public void setThemeNumber(int themeNumber) {
        this.themeNumber.set(themeNumber);
    }

    private void applyTheme() {
        navigationService.applyThemeToAllViews(themeNumber.get(), isDarkMode.get());
    }

    public StringProperty languesLabelProperty() {
        return languesLabelProperty;
    }

    public StringProperty themeLabelProperty() {
        return themeLabelProperty;
    }

    public StringProperty couleurLabelProperty() {
        return couleurLabelProperty;
    }

    public StringProperty titleLabelProperty() {
        return titleLabelProperty;
    }

    public void onBackButtonClicked() {
        navigationService.navigateBack();
    }
}