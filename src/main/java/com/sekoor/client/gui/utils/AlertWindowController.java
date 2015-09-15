package com.sekoor.client.gui.utils;


import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AlertWindowController {
    public Label messageLabel;
    public Label detailsLabel;
    public Button okButton;
    public Button cancelButton;
    public Button actionButton;

    /** Initialize this alert dialog for information about a crash. */
    public void crashAlert(Stage stage, String crashMessage) {
        messageLabel.setText("Unfortunately, we screwed up and the app crashed. Sorry about that!");
        detailsLabel.setText(crashMessage);

        cancelButton.setVisible(false);
        actionButton.setVisible(false);
        okButton.setOnAction(actionEvent -> stage.close());
    }

    /** Initialize this alert for general information: OK button only, nothing happens on dismissal. */
    public void informational(Stage stage, String message, String details) {
        messageLabel.setText(message);
        detailsLabel.setText(details);
        cancelButton.setVisible(false);
        actionButton.setVisible(false);
        okButton.setOnAction(actionEvent -> stage.close());
    }

    // TODO:Olle remove?
    public void withMessageSent(Stage stage, int secondsTaken) {
        messageLabel.setText("Message sent");
        detailsLabel.setText(String.format("'Message was successfully sent in %d seconds.", secondsTaken));
        cancelButton.setVisible(false);
        actionButton.setText("Open ...");
        actionButton.setVisible(true);
        okButton.setOnAction((event) -> stage.close());
    }
}
