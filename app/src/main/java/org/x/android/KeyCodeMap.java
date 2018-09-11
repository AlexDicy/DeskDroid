package org.x.android;

import android.util.SparseArray;
import android.view.KeyEvent;

public class KeyCodeMap {
    private static final SparseArray<KeyCode> keyCodeMap = new SparseArray<>();

    static {
        keyCodeMap.put(111, new KeyCode(9, 0));
        keyCodeMap.put(8, new KeyCode(10, 0));
        keyCodeMap.put(9, new KeyCode(11, 0));
        keyCodeMap.put(77, new KeyCode(11, 1));
        keyCodeMap.put(10, new KeyCode(12, 0));
        keyCodeMap.put(18, new KeyCode(12, 1));
        keyCodeMap.put(11, new KeyCode(13, 0));
        keyCodeMap.put(12, new KeyCode(14, 0));
        keyCodeMap.put(13, new KeyCode(15, 0));
        keyCodeMap.put(14, new KeyCode(16, 0));
        keyCodeMap.put(15, new KeyCode(17, 0));
        keyCodeMap.put(17, new KeyCode(17, 1));
        keyCodeMap.put(16, new KeyCode(18, 0));
        keyCodeMap.put(KeyCode.I162, new KeyCode(18, 1));
        keyCodeMap.put(7, new KeyCode(19, 0));
        keyCodeMap.put(KeyCode.I163, new KeyCode(19, 1));
        keyCodeMap.put(69, new KeyCode(20, 0));
        keyCodeMap.put(70, new KeyCode(21, 0));
        keyCodeMap.put(81, new KeyCode(21, 1));
        keyCodeMap.put(67, new KeyCode(22, 0));
        keyCodeMap.put(61, new KeyCode(23, 0));
        keyCodeMap.put(45, new KeyCode(24, 0));
        keyCodeMap.put(51, new KeyCode(25, 0));
        keyCodeMap.put(33, new KeyCode(26, 0));
        keyCodeMap.put(46, new KeyCode(27, 0));
        keyCodeMap.put(48, new KeyCode(28, 0));
        keyCodeMap.put(53, new KeyCode(29, 0));
        keyCodeMap.put(49, new KeyCode(30, 0));
        keyCodeMap.put(37, new KeyCode(31, 0));
        keyCodeMap.put(43, new KeyCode(32, 0));
        keyCodeMap.put(44, new KeyCode(33, 0));
        keyCodeMap.put(71, new KeyCode(34, 0));
        keyCodeMap.put(72, new KeyCode(35, 0));
        keyCodeMap.put(66, new KeyCode(36, 0));
        keyCodeMap.put(113, new KeyCode(37, 0));
        keyCodeMap.put(29, new KeyCode(38, 0));
        keyCodeMap.put(47, new KeyCode(39, 0));
        keyCodeMap.put(32, new KeyCode(40, 0));
        keyCodeMap.put(34, new KeyCode(41, 0));
        keyCodeMap.put(35, new KeyCode(42, 0));
        keyCodeMap.put(36, new KeyCode(43, 0));
        keyCodeMap.put(38, new KeyCode(44, 0));
        keyCodeMap.put(39, new KeyCode(45, 0));
        keyCodeMap.put(40, new KeyCode(46, 0));
        keyCodeMap.put(74, new KeyCode(47, 0));
        keyCodeMap.put(75, new KeyCode(48, 0));
        keyCodeMap.put(59, new KeyCode(50, 0));
        keyCodeMap.put(73, new KeyCode(51, 0));
        keyCodeMap.put(54, new KeyCode(52, 0));
        keyCodeMap.put(52, new KeyCode(53, 0));
        keyCodeMap.put(31, new KeyCode(54, 0));
        keyCodeMap.put(50, new KeyCode(55, 0));
        keyCodeMap.put(30, new KeyCode(56, 0));
        keyCodeMap.put(42, new KeyCode(57, 0));
        keyCodeMap.put(41, new KeyCode(58, 0));
        keyCodeMap.put(55, new KeyCode(59, 0));
        keyCodeMap.put(56, new KeyCode(60, 0));
        keyCodeMap.put(76, new KeyCode(61, 0));
        keyCodeMap.put(60, new KeyCode(62, 0));
        keyCodeMap.put(17, new KeyCode(63, 0));
        keyCodeMap.put(57, new KeyCode(64, 0));
        keyCodeMap.put(62, new KeyCode(65, 0));
        keyCodeMap.put(115, new KeyCode(66, 0));
        keyCodeMap.put(131, new KeyCode(67, 0));
        keyCodeMap.put(KeyCode.AE13, new KeyCode(68, 0));
        keyCodeMap.put(KeyCode.LWIN, new KeyCode(69, 0));
        keyCodeMap.put(KeyCode.RWIN, new KeyCode(70, 0));
        keyCodeMap.put(KeyCode.COMP, new KeyCode(71, 0));
        keyCodeMap.put(KeyCode.STOP, new KeyCode(72, 0));
        keyCodeMap.put(KeyCode.AGAI, new KeyCode(73, 0));
        keyCodeMap.put(KeyCode.PROP, new KeyCode(74, 0));
        keyCodeMap.put(KeyCode.UNDO, new KeyCode(75, 0));
        keyCodeMap.put(KeyCode.FRNT, new KeyCode(76, 0));
        keyCodeMap.put(KeyCode.PAST, new KeyCode(77, 0));
        keyCodeMap.put(116, new KeyCode(78, 0));
        keyCodeMap.put(KeyCode.VOL_PLUS, new KeyCode(79, 0));
        keyCodeMap.put(KeyCode.I157, new KeyCode(86, 0));
        keyCodeMap.put(KeyCode.VOL_MINUS, new KeyCode(87, 0));
        keyCodeMap.put(KeyCode.COPY, new KeyCode(95, 0));
        keyCodeMap.put(KeyCode.OPEN, new KeyCode(96, 0));
        keyCodeMap.put(KeyCode.I160, new KeyCode(104, 0));
        keyCodeMap.put(114, new KeyCode(105, 0));
        keyCodeMap.put(KeyCode.I154, new KeyCode(106, 0));
        keyCodeMap.put(58, new KeyCode(108, 0));
        keyCodeMap.put(3, new KeyCode(110, 0));
        keyCodeMap.put(19, new KeyCode(111, 0));
        keyCodeMap.put(92, new KeyCode(112, 0));
        keyCodeMap.put(21, new KeyCode(113, 0));
        keyCodeMap.put(22, new KeyCode(114, 0));
        keyCodeMap.put(20, new KeyCode(116, 0));
        keyCodeMap.put(93, new KeyCode(117, 0));
        keyCodeMap.put(KeyCode.POWR, new KeyCode(118, 0));
        keyCodeMap.put(KeyCode.I164, new KeyCode(KeyCode.MUTE, 0));
        keyCodeMap.put(24, new KeyCode(KeyCode.VOL_PLUS, 0));
        keyCodeMap.put(25, new KeyCode(KeyCode.VOL_MINUS, 0));
        keyCodeMap.put(26, new KeyCode(KeyCode.POWR, 0));
        keyCodeMap.put(KeyCode.I161, new KeyCode(KeyCode.KPEQ, 0));
        keyCodeMap.put(102, new KeyCode(KeyCode.LWIN, 0));
        keyCodeMap.put(103, new KeyCode(KeyCode.RWIN, 0));
        keyCodeMap.put(82, new KeyCode(KeyCode.COMP, 0));
    }

    public static boolean hasKeyCode(KeyEvent keyEvent) {
        return keyCodeMap.indexOfKey(keyEvent.getKeyCode()) > -1;
    }

    public static KeyCode getKeyCode(KeyEvent keyEvent) {
        KeyCode keyCode = keyCodeMap.get(keyEvent.getKeyCode());
        if (keyCode == null) {
            return null;
        }
        int i = 0;
        if ((keyEvent.getMetaState() & 193) != 0) {
            i = 1;
        }
        if ((keyEvent.getMetaState() & 28672) != 0) {
            i |= 4;
        }
        if ((keyEvent.getMetaState() & 50) != 0) {
            i |= 8;
        }
        return new KeyCode(keyCode.getKeyCode(), keyCode.getState() | i);
    }

    public static KeyCode getKeyCode(int i) {
        return keyCodeMap.get(i);
    }
}
