package ppti.model;

import common.locales.I18N;

public class HelpScreenModel {
    private static final int TOTAL_PAGES = 3;

    public String getHelpText(int page) {
        return I18N.get("helpTextHelpScreenPPTI.page" + (page + 1));
    }

    public int getTotalPages() {
        return TOTAL_PAGES;
    }
}