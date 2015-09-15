package com.sekoor.client;

import com.sekoor.client.gui.Message;
import javafx.animation.*;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import com.sekoor.client.gui.controls.ClickableBitcoinAddress;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static javafx.beans.binding.Bindings.isNull;
import static com.sekoor.client.gui.utils.GuiUtils.*;

/**
 * Gets created auto-magically by FXMLLoader via reflection. The widget fields are set to the GUI controls they're named
 * after. This class handles all the updates and event handling for the main UI.
 */
public class Controller {
    public ProgressBar progressBar;
    public Label progressBarLabel;
    public VBox syncBox;
    public HBox controlsBox;
    public Label balance;
    public Button sendMoneyOutBtn;
    public ClickableBitcoinAddress addressControl;

    // PayFile specific stuff
    public Button downloadBtn, cancelBtn;
    public ListView<Message> messagesList;
    private ObservableList<Message> messages;
    private ReadOnlyObjectProperty<Message> selectedMessages;

    // Called by FXMLLoader.
    public void initialize() {
        progressBar.setProgress(-1);

        cancelBtn.setVisible(false);

        // The SekoorClient.File.toString() method is good enough for rendering list cells for now.
        messages = FXCollections.observableArrayList();
        messagesList.setItems(messages);
        selectedMessages = messagesList.getSelectionModel().selectedItemProperty();
        // Don't allow the user to press download unless an item is selected.
        downloadBtn.disableProperty().bind(isNull(selectedMessages));
    }


    public void onBitcoinSetup() {
        addressControl.setAddress("1Jpm2ibWaMwDJn8PgvyrgVCVr9sHTR3KfA");
        refreshBalanceLabel();
    }

    public void sendMoneyOut(ActionEvent event) {
        // Free up the users money, if any is suspended in a payment channel for this server.
        //
        // The UI races the broadcast here - we could/should throw up a spinner until the server finishes settling
        // the channel and we know we've got the money back.
        // Hide this UI and show the send money UI. This UI won't be clickable until the user dismisses send_money.
        Main.instance.overlayUI("send_money.fxml");
    }

    public void disconnect(ActionEvent event) {
        Main.client = null;
        fadeOut(Main.instance.mainUI);
        messages.clear();
        Main.instance.overlayUI("connect_server.fxml");
    }



    public void fileEntryClicked(MouseEvent event) throws Exception {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            // Double click on a file: shortcut for downloading.
            decryptMessage(null);
        }
    }


    private void decryptMessage(Message msg) {
        // TODO:Olle Should do something?
    }

    public void cancelOperation(ActionEvent event) {
        // Nothing
    }

    public void readyToGoAnimation() {
        // Sync progress bar slides out ...
        TranslateTransition leave = new TranslateTransition(Duration.millis(600), syncBox);
        leave.setByY(80.0);
        // Buttons slide in and clickable address appears simultaneously.
        TranslateTransition arrive = new TranslateTransition(Duration.millis(600), controlsBox);
        arrive.setToY(0.0);
        FadeTransition reveal = new FadeTransition(Duration.millis(500), addressControl);
        reveal.setToValue(1.0);
        ParallelTransition group = new ParallelTransition(arrive, reveal);
        // Slide out happens then slide in/fade happens.
        SequentialTransition both = new SequentialTransition(leave, group);
        both.setCycleCount(1);
        both.setInterpolator(Interpolator.EASE_BOTH);
        both.play();
    }

    private boolean controlsBoxOnScreen = true;


    public void refreshBalanceLabel() {
        checkGuiThread();
        BigInteger amount = getBalance();
        balance.setText(amount.toString());
    }

    private BigInteger getBalance() {
        BigInteger amount;
        if (Main.client != null)
            amount = Main.client.getRemainingBalance();
        else
            throw new IllegalStateException("Must have client");
        return amount;
    }

}
