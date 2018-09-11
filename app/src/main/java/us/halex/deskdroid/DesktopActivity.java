package us.halex.deskdroid;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.x.android.XServerNative;

/**
 * Created by HAlexTM on 10/09/2018 20:49
 */
public class DesktopActivity extends AppCompatActivity {

    private XView xView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop);

        xView = findViewById(R.id.xview);
        findViewById(R.id.left_button).setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    XServerNative.buttonEvent(1, false, 0);
                    return view.performClick();
                case MotionEvent.ACTION_DOWN:
                    XServerNative.buttonEvent(1, true, 0);
                    return true;
            }
            return false;
        });
        findViewById(R.id.right_button).setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    XServerNative.buttonEvent(3, false, 0);
                    return view.performClick();
                case MotionEvent.ACTION_DOWN:
                    XServerNative.buttonEvent(3, true, 0);
                    return true;
            }
            return false;
        });
        findViewById(R.id.keyboard_button).setOnClickListener(view -> {
            xView.requestFocus();
            InputMethodManager manager = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
            if (manager != null) {
                manager.showSoftInput(xView, InputMethodManager.SHOW_FORCED);
            }
        });

        findViewById(R.id.slider).setOnTouchListener(new View.OnTouchListener() {
            private boolean moved;
            private float startX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    startX = event.getRawX();
                    moved = false;
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float f = 10.0f * getResources().getDisplayMetrics().density;
                    if (startX - event.getRawX() > f) {
                        XServerNative.buttonEvent(4, true, 0);
                        XServerNative.buttonEvent(4, false, 0);
                        startX -= f;
                        moved = true;
                    } else if (startX - event.getRawX() < -f) {
                        XServerNative.buttonEvent(5, true, 0);
                        XServerNative.buttonEvent(5, false, 0);
                        startX += f;
                        moved = true;
                    }
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setPressed(false);
                    if (!moved) {
                        return v.performClick();
                    }
                }
                return false;
            }
        });
    }
}
