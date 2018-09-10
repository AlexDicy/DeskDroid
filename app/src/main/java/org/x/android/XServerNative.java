package org.x.android;

public class XServerNative {
    public static native void buttonEvent(int i, boolean z, int i2);

    public static native int chmod(String str, int i);

    public static native String getenv(String str);

    public static native void keyEvent(int i, boolean z, int i2);

    public static native void motionEvent(int i, int i2);

    public static native int setenv(String str, String str2);

    public static native int symlink(String str, String str2);
}
