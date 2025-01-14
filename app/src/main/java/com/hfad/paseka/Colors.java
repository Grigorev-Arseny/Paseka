package com.hfad.paseka;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;

public class Colors {
    private static int primary;
    private static int container;
    private static int accent;
    private static int backgroundFloating;
    private static int textAppearanceListItemSecondary;

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

        typedValue = new TypedValue();
        theme.resolveAttribute(com.google.android.material.R.attr.textAppearanceListItemSecondary, typedValue, true);
        textAppearanceListItemSecondary = typedValue.data;
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

    public static int getTextAppearanceListItemSecondary() { return textAppearanceListItemSecondary; }

    public static int getExpired() {
        return Color.RED;
    }
    public static int getPresent() {
        return Color.BLUE;
    }
    public static int getCompleted() {
        return Color.GRAY;
    }
}