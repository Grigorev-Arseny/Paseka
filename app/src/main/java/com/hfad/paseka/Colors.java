package com.hfad.paseka;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class Colors {
    private static int primary;
    private static int container;
    private static int accent;
    private  static int backgroundFloating;

    public static void init(Context context) {
        TypedValue typedValue;
        Resources.Theme theme = context.getTheme();

        typedValue = new TypedValue();
        theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        primary = typedValue.data;

        typedValue = new TypedValue();
        theme.resolveAttribute(com.google.android.material.R.attr.colorContainer, typedValue, true);
        container = typedValue.data;

        typedValue = new TypedValue();
        theme.resolveAttribute(com.google.android.material.R.attr.colorAccent, typedValue, true);
        accent = typedValue.data;

        typedValue = new TypedValue();
        theme.resolveAttribute(com.google.android.material.R.attr.colorBackgroundFloating, typedValue, true);
        backgroundFloating = typedValue.data;
    }

    public static int getPrimary() {
        return primary;
    }

    public static int getContainer() {
        return container;
    }

    public static int getAccent() {
        return accent;
    }

    public static int getBackgroundFloating() {
        return backgroundFloating;
    }
}