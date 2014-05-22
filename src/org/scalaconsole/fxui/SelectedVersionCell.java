package org.scalaconsole.fxui;

import javafx.scene.control.ListCell;

public class SelectedVersionCell extends ListCell<String> {
    public SelectedVersionCell() {
        textProperty().bind(itemProperty());
        setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2 && getItem() != null) {
                this.getListView().getItems().remove(getItem());
            }
        });
    }
}
