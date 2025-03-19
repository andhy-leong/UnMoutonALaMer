package common.model;

import common.ImageHandler;
import common.locales.I18N;

public class HelpScreenModel {
    private static final int TOTAL_PAGES = 2;

    public String getHelpText(int page) {
        return I18N.get("helpTextHelpScreenIDJR.page" + (page + 1));
    }

    public String getHelpImage(int page) {
        return "/common/images/help_image_" + (page + 1) + ".jpg";
    }

    public int getTotalPages() {
        return TOTAL_PAGES;
    }
} 