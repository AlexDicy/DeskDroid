package us.halex.deskdroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/**
 * Created by HAlexTM on 08/09/2018 09:58
 */
public class App {
    private String name; // Should be unique because it's used as key
    private Drawable logo;
    private Drawable textLogo;
    // App requirements
    private List<String> dependencies = new ArrayList<>();
    // Execution info
    private File folder; // .../app/intellij/
    private String executable;
    private String[] arguments;

    public App(@Nullable Context context, @StringRes int name, @DrawableRes int logo, @DrawableRes int textLogo) {
        if (context != null) {
            Resources resources = context.getResources();
            this.name = resources.getString(name);
            this.logo = resources.getDrawable(logo, null);
            this.textLogo = resources.getDrawable(textLogo, null);
        }
    }

    public App(String name, Drawable logo, Drawable textLogo) {
        this.name = name;
        this.logo = logo;
        this.textLogo = textLogo;
    }

    public App(String name, Drawable logo, Drawable textLogo, String folder) {
        this(name, logo, textLogo);
        this.folder = new File(DeskDroidApp.getAppFolder(), "app/" + folder);
    }

    public App(String name, Drawable logo, Drawable textLogo, String folder, String executable) {
        this(name, logo, textLogo, folder);
        this.executable = executable;
    }

    public App(String name, Drawable logo, Drawable textLogo, String folder, String executable, String[] arguments) {
        this(name, logo, textLogo, folder, executable);
        this.arguments = arguments;
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

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public File getFolder() {
        return folder;
    }

    public String getExecutable() {
        return executable;
    }

    public String[] getArguments() {
        return arguments == null ? new String[0] : arguments;
    }
}
