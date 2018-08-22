package com.mercadopago.android.px.internal.view.exploding;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

public class ViewStylingParams implements Parcelable {

    private final int primaryColor;
    private final int darkPrimaryColor;
    private final int statusIcon;

    public ViewStylingParams(@ColorRes int primaryColor, @ColorRes int darkPrimaryColor, @DrawableRes int statusIcon) {
        this.primaryColor = primaryColor;
        this.darkPrimaryColor = darkPrimaryColor;
        this.statusIcon = statusIcon;
    }

    @ColorRes
    public int getPrimaryColor() {
        return primaryColor;
    }

    @ColorRes
    public int getDarkPrimaryColor() {
        return darkPrimaryColor;
    }

    @DrawableRes
    public int getStatusIcon() {
        return statusIcon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(new int[]{primaryColor, darkPrimaryColor, statusIcon});
    }

    @SuppressWarnings("checkstyle:magicnumber")
    protected ViewStylingParams(Parcel in) {
        int[] inValues = new int[3];
        in.readIntArray(inValues);

        primaryColor = inValues[0];
        darkPrimaryColor = inValues[1];
        statusIcon = inValues[2];
    }

    public static final Creator<ViewStylingParams> CREATOR = new Creator<ViewStylingParams>() {
        public ViewStylingParams createFromParcel(Parcel source) {
            return new ViewStylingParams(source);
        }

        public ViewStylingParams[] newArray(int size) {
            return new ViewStylingParams[size];
        }
    };
}