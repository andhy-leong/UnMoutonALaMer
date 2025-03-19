package ppti.viewmodel;

import common.locales.I18N;
import javafx.beans.property.*;
import ppti.view.singleton.SingletonView;

import java.util.Locale;

public class OptionsScreenViewModel {
	private SingletonView singleton;

    private IntegerProperty themeNumber;
    private BooleanProperty isDarkMode;

    private StringProperty selectedLanguageProperty = new SimpleStringProperty();

    private StringProperty titleLabelProperty = new SimpleStringProperty();
    private StringProperty darkModeLabelProperty = new SimpleStringProperty();
    private StringProperty themesLabelProperty = new SimpleStringProperty();
    private StringProperty theme1ButtonProperty = new SimpleStringProperty();
    private StringProperty theme2ButtonProperty = new SimpleStringProperty();
    private StringProperty theme3ButtonProperty = new SimpleStringProperty();
    private StringProperty languesLabelProperty = new SimpleStringProperty();

	
    public OptionsScreenViewModel(SingletonView singleton) {
    	this.singleton = singleton;

        this.themeNumber = new SimpleIntegerProperty(1);
        this.isDarkMode = new SimpleBooleanProperty(false);

        // Initialize selectedLanguageProperty
        switch (I18N.getLocale().getLanguage()) {
            case "fr" -> selectedLanguageProperty.set("Français");
            case "es"  -> selectedLanguageProperty.set("Español");
            case "it" -> selectedLanguageProperty.set("Italiano");
            case "de" -> selectedLanguageProperty.set("Deutsch");
            case "pt" -> selectedLanguageProperty.set("Português");
            default -> selectedLanguageProperty.set("English");
        }

        // Add listeners to apply theme when properties change
        themeNumber.addListener((obs, oldVal, newVal) -> applyTheme());
        isDarkMode.addListener((obs, oldVal, newVal) -> applyTheme());

        loadLangue();
    }

    public StringProperty selectedLanguageProperty() {
        return selectedLanguageProperty;
    }

    public void setSelectedLanguage(String language) {
        selectedLanguageProperty.set(language);
    }

    public String getSelectedLanguage() {
        return selectedLanguageProperty.get();
    }

    public void onLangueButtonClicked(String langue) {
        Locale newLocale;
        switch (langue) {
            case "Fr":
                newLocale = new Locale("fr", "FR");
                setSelectedLanguage("Français");
                break;
            case "En":
                newLocale = new Locale("en", "US");
                setSelectedLanguage("English");
                break;
            case "Es":
                newLocale = new Locale("es", "ES");
                setSelectedLanguage("Español");
                break;
            case "It":
                newLocale = new Locale("it", "IT");
                setSelectedLanguage("Italiano");
                break;
            case "De":
                newLocale = new Locale("de", "DE");
                setSelectedLanguage("Deutsch");
                break;
            case "Pt":
                newLocale = new Locale("pt", "PT");
                setSelectedLanguage("Português");
                break;
            default:
                newLocale = I18N.getDefaultLocale();
        }

        I18N.setLocale(newLocale);

        loadLangue();
    }

    public void loadLangue() {
        titleLabelProperty.set(I18N.get("optionsLabelPPTI"));
        darkModeLabelProperty.set(I18N.get("darkModeLabelPPTI"));
        themesLabelProperty.set(I18N.get("themesLabelPPTI"));
        theme1ButtonProperty.set(I18N.get("theme1ButtonPPTI"));
        theme2ButtonProperty.set(I18N.get("theme2ButtonPPTI"));
        theme3ButtonProperty.set(I18N.get("theme3ButtonPPTI"));
        languesLabelProperty.set(I18N.get("languesLabelPPTI"));
    }

    /* Thème */
    public IntegerProperty themeNumberProperty() {
        return themeNumber;
    }

    public BooleanProperty isDarkModeProperty() {
        return isDarkMode;
    }

    private void applyTheme() {
        singleton.applyThemeToAllViews(themeNumber.get(), isDarkMode.get());
    }

    public void setThemeNumber(int themeNumber) {
        this.themeNumber.set(themeNumber);
    }


    /* Navigation */

    public void onBackButtonClicked() {
        singleton.setViewVisible(0);
    }


    /* Internationalization */
    public StringProperty titleLabelProperty() {
    	return titleLabelProperty;
    }

    public StringProperty darkModeLabelProperty() {
        return darkModeLabelProperty;
    }

    public StringProperty themesLabelProperty() {
        return themesLabelProperty;
    }

    public StringProperty theme1ButtonProperty() {
        return theme1ButtonProperty;
    }

    public StringProperty theme2ButtonProperty() {
        return theme2ButtonProperty;
    }

    public StringProperty theme3ButtonProperty() {
        return theme3ButtonProperty;
    }

    public StringProperty languesLabelProperty() {
        return languesLabelProperty;
    }
}
