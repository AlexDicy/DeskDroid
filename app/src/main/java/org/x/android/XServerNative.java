package org.x.android;

@SuppressWarnings("JniMissingFunction") // Those methods exist and work.
public class XServerNative {
    /**
     * Perform a Mouse button event
     *
     * @param id   button type (1=left click, 3=right click...)
     * @param down True if pressed, false if released
     * @param i2   idk
     */
    public static native void buttonEvent(int id, boolean down, int i2);

    public static native int chmod(String file, int mod);

    public static native String getenv(String key);

    public static native int setenv(String key, String value);

    public static native void keyEvent(int key, boolean down, int i2);

    public static native void motionEvent(int i, int i2);

    public static native int symlink(String str, String str2);
}
