package us.halex.deskdroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.x.android.KeyCode;
import org.x.android.KeyCodeMap;
import org.x.android.XServerNative;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import us.halex.deskdroid.execute.Executor;

/**
 * Created by HAlexTM on 11/09/2018 09:51
 */
public class XView extends View implements GestureDetector.OnGestureListener {
    private static final long REFRESH_DURATION = 33L;
    private Bitmap bitmap;
    private MappedByteBuffer buffer;
    /**
     * If true the user is using a mouse or a pointer like the Galaxy S Pen
     */
    private boolean hasPointer;
    private final GestureDetector gestureDetector;
    private int lastMouseButton;
    private float lastX;
    private float lastY;
    private Listener listener;
    private Matrix matrix = new Matrix();
    private final Paint paint = new Paint();

    public XView(Context context) {
        super(context);
        gestureDetector = new GestureDetector(context, this);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setFocusableInTouchMode(true);
        paint.setAntiAlias(true);
        paint.setColor(-1);
        int size = Math.max(DeskDroidApp.getWidth(), DeskDroidApp.getHeight());
        bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        try {
            File localFile = new File(getContext().getCacheDir(), "Xvfb_screen0");
            RandomAccessFile accessFile = new RandomAccessFile(localFile, "r");
            buffer = accessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 928L, this.bitmap.getByteCount());
        } catch (IOException localIOException) {
            throw new Error(localIOException);
        }
    }

    private void sendKeyCode(int key, boolean down) {
        XServerNative.keyEvent(KeyCodeMap.getKeyCode(key).getKeyCode(), down, 0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        KeyCode keyCode = KeyCodeMap.getKeyCode(event);
        if (keyCode != null) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_UP:
                    XServerNative.keyEvent(keyCode.getKeyCode(), false, keyCode.getState());
                    return true;
                case KeyEvent.ACTION_DOWN:
                    XServerNative.keyEvent(keyCode.getKeyCode(), true, keyCode.getState());
                    return true;
            }
        } else if (event.getAction() == KeyEvent.ACTION_MULTIPLE && event.getKeyCode() == 0) {
            for (int c : event.getCharacters().toCharArray()) {
                sendKeyCode(113, true);
                sendKeyCode(59, true);
                sendKeyCode(49, true);
                sendKeyCode(49, false);
                for (int n : String.format("%04x", c).toCharArray()) {
                    if ((48 <= n) && (n <= 57)) {
                        n = 7 + n - 48;
                        sendKeyCode(n, true);
                        sendKeyCode(n, false);
                    } else {
                        n = 29 + n - 97;
                        sendKeyCode(n, true);
                        sendKeyCode(n, false);
                    }
                }
                sendKeyCode(59, false);
                sendKeyCode(113, false);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo paramEditorInfo) {
        return new BaseInputConnection(this, false);
    }

    @Override
    public boolean onDown(MotionEvent paramMotionEvent) {
        return true;
    }

    @Override
    protected void onDraw(Canvas paramCanvas) {
        super.onDraw(paramCanvas);
        long l = System.currentTimeMillis();
        this.buffer.position(0);
        this.bitmap.copyPixelsFromBuffer(this.buffer);
        paramCanvas.drawBitmap(this.bitmap, this.matrix, this.paint);
        postInvalidateDelayed(Math.max(REFRESH_DURATION, (System.currentTimeMillis() - l) * 2L));
    }

    @Override
    public boolean onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (!hasPointer) {
            hasPointer = true;
            if (listener != null) {
                listener.onHasPointer(true);
            }
        }
        if (event.getAction() != MotionEvent.ACTION_SCROLL) {
            return super.onGenericMotionEvent(event);
        }
        float f = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
        int i;
        if (f < 0.0F) {
            for (i = 0; i <= Math.abs(f / 120.0F); i++) {
                XServerNative.buttonEvent(5, true, 0);
                XServerNative.buttonEvent(5, false, 0);
            }
        }
        if (0.0F < f) {
            for (i = 0; i <= Math.abs(f / 120.0F); i++) {
                XServerNative.buttonEvent(4, true, 0);
                XServerNative.buttonEvent(4, false, 0);
            }
        }
        return true;
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if (!hasPointer) {
            hasPointer = true;
            if (listener != null) {
                listener.onHasPointer(true);
            }
        }
        float f = DeskDroidApp.getScale();
        int i = (int) (event.getX() / f);
        int j = (int) (event.getY() / f);
        int k = event.getAction();
        if (k != 7) {
            switch (k) {
                default:
                    return super.onHoverEvent(event);
            }
        }
        XServerNative.motionEvent(i, j);
        return true;
    }

    @Override
    public boolean onKeyPreIme(int paramInt, KeyEvent paramKeyEvent) {
        if (paramKeyEvent.getKeyCode() == 4) {
            setSystemUiVisibility(4871);
        }
        return super.onKeyPreIme(paramInt, paramKeyEvent);
    }

    @Override
    public void onLongPress(MotionEvent paramMotionEvent) {
        float f = DeskDroidApp.getScale();
        XServerNative.motionEvent((int) (paramMotionEvent.getX() / f), (int) (paramMotionEvent.getY() / f));
        XServerNative.buttonEvent(3, true, 0);
        XServerNative.buttonEvent(3, false, 0);
    }

    @Override
    public boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
        float f1 = DeskDroidApp.getScale();
        float f2 = paramFloat1 / f1;
        f1 = paramFloat2 / f1;
        paramFloat2 = Math.abs(f2);
        if (10.0F < paramFloat2) {
            paramFloat1 = f2 * 4.0F;
        } else {
            paramFloat1 = f2;
            if (5.0F < paramFloat2) {
                paramFloat1 = f2 * 2.0F;
            }
        }
        f2 = Math.abs(f1);
        if (10.0F < f2) {
            paramFloat2 = f1 * 4.0F;
        } else {
            paramFloat2 = f1;
            if (5.0F < f2) {
                paramFloat2 = f1 * 2.0F;
            }
        }
        this.lastX -= paramFloat1;
        this.lastY -= paramFloat2;
        if (this.lastX < 0.0F) {
            this.lastX = 0.0F;
        }
        if (this.bitmap.getWidth() < this.lastX) {
            this.lastX = this.bitmap.getWidth();
        }
        if (this.lastY < 0.0F) {
            this.lastY = 0.0F;
        }
        if (this.bitmap.getHeight() < this.lastY) {
            this.lastY = this.bitmap.getHeight();
        }
        XServerNative.motionEvent((int) this.lastX, (int) this.lastY);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent paramMotionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
        float f = DeskDroidApp.getScale();
        XServerNative.motionEvent((int) (paramMotionEvent.getX() / f), (int) (paramMotionEvent.getY() / f));
        XServerNative.buttonEvent(1, true, 0);
        XServerNative.buttonEvent(1, false, 0);
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float f = DeskDroidApp.getScale();
        matrix = new Matrix();
        matrix.setScale(f, f);
        lastX = ((int) (w / f / 2.0F));
        lastY = ((int) (h / f / 2.0F));

        boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        int width = landscape ? DeskDroidApp.getWidth() : DeskDroidApp.getHeight();
        int height = landscape ? DeskDroidApp.getHeight() : DeskDroidApp.getWidth();
        String mode = width + "x" + height;

        new Executor.Builder()
                .setExecutable("xrandr")
                .setArguments(new String[]{"--newmode", mode, "0", String.valueOf(width), "0", "0", "0", String.valueOf(height), "0", "0", "0", "0"})
                .waitFor().create().execute();

        new Executor.Builder()
                .setExecutable("xrandr")
                .setArguments(new String[]{"--addmode", "screen", mode})
                .waitFor().create().execute();

        new Executor.Builder()
                .setExecutable("xrandr")
                .setArguments(new String[]{"--output", "screen", "--mode", mode})
                .waitFor().create().execute();

        new Executor.Builder()
                .setExecutable("xrandr")
                .setArguments(new String[]{"--verbose"})
                .waitFor().create().execute();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
        float f = DeskDroidApp.getScale();
        int i = (int) (paramMotionEvent.getX() / f);
        int j = (int) (paramMotionEvent.getY() / f);
        if (paramMotionEvent.getSource() == 8194) {
            if (!hasPointer) {
                hasPointer = true;
                if (listener != null) {
                    listener.onHasPointer(true);
                }
            }
            switch (paramMotionEvent.getAction()) {
                default:
                    break;
                case 2:
                    XServerNative.motionEvent(i, j);
                    return true;
                case 1:
                    XServerNative.motionEvent(i, j);
                    XServerNative.buttonEvent(this.lastMouseButton, false, 0);
                    return true;
                case 0:
                    if (paramMotionEvent.getButtonState() == 2) {
                        this.lastMouseButton = 3;
                    } else {
                        this.lastMouseButton = 1;
                    }
                    XServerNative.motionEvent(i, j);
                    XServerNative.buttonEvent(this.lastMouseButton, true, 0);
                    return true;
            }
        }
        if (hasPointer) {
            hasPointer = false;
            if (listener != null) {
                listener.onHasPointer(false);
            }
        }
        if (this.gestureDetector.onTouchEvent(paramMotionEvent)) {
            return true;
        }
        return super.onTouchEvent(paramMotionEvent);
    }

    public void setHasPointerListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        /**
         * @param hasPointer is True if the user is using a pointer like a mouse or a Galaxy S Pen
         */
        void onHasPointer(boolean hasPointer);
    }
}
