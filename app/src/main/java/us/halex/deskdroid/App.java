package us.halex.deskdroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.Random;

/**
 * Created by HAlexTM on 08/09/2018 09:58
 */
public class App {

    private String name;
    private Drawable logo;
    private Drawable textLogo;

    public App(@Nullable Context context, @StringRes int name, @DrawableRes int logo, @DrawableRes int textLogo) {
        if (context != null) {
            Resources resources = context.getResources();
            this.name = resources.getString(name) + new Random().nextInt(100);
            this.logo = resources.getDrawable(logo, null);
            this.textLogo = resources.getDrawable(textLogo, null);
        }
    }

    public App(String name, Drawable logo, Drawable textLogo) {
        this.name = name;
        this.logo = logo;
        this.textLogo = textLogo;
    }

    public String getName() {
        return name;
    }

    public Drawable getLogo() {
        return logo;
    }

    public Drawable getTextLogo() {
        return textLogo;
    }
}
