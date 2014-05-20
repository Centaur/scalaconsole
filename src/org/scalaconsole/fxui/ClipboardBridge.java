package org.scalaconsole.fxui;

import com.google.common.base.Strings;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;

import java.util.HashMap;
import java.util.Map;

public class ClipboardBridge {
    public String getContent() {
        String content = Clipboard.getSystemClipboard().getString();
        return Strings.nullToEmpty(content);
    }
    public void setContent(String content) {
        Map<DataFormat, Object> data = new HashMap<>();
        data.put(DataFormat.PLAIN_TEXT, content);
        Clipboard.getSystemClipboard().setContent(data);
    }
}
