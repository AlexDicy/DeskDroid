package org.x.android;

@SuppressWarnings("JniMissingFunction") // Those methods exist and work.
public class XServerNative {
    public static native void buttonEvent(int i, boolean z, int i2);

    public static native int chmod(String str, int i);

    public static native String getenv(String key);

    public static native int setenv(String key, String value);

    public static native void keyEvent(int key, boolean down, int i2);

    public static native void motionEvent(int i, int i2);

    public static native int symlink(String str, String str2);
}
