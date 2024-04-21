package com.prasaddevelops.mikee;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class BitmapParcelable implements Parcelable {

    private Bitmap bitmap;

    public BitmapParcelable(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private BitmapParcelable(Parcel in) {
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bitmap, flags);
    }

    public static final Creator<BitmapParcelable> CREATOR = new Creator<BitmapParcelable>() {
        @Override
        public BitmapParcelable createFromParcel(Parcel in) {
            return new BitmapParcelable(in);
        }

        @Override
        public BitmapParcelable[] newArray(int size) {
            return new BitmapParcelable[size];
        }
    };
}
