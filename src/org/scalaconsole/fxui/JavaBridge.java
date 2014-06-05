package org.scalaconsole.fxui;

import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import org.scalaconsole.fxui.main.MainController;

import java.util.HashMap;
import java.util.Map;

public class JavaBridge {
    private MainController controller;

    public JavaBridge(MainController controller) {
        this.controller = controller;
    }

    public String getClipboardContent() {
        String content = Clipboard.getSystemClipboard().getString();
        if(content == null)
            return "";
        else return content;
    }
    public void setClipboardContent(String content) {
        Map<DataFormat, Object> data = new HashMap<>();
        data.put(DataFormat.PLAIN_TEXT, content);
        Clipboard.getSystemClipboard().setContent(data);
    }
    public void createTab() {
        controller.onNewTab(null);
    }
}
