package com.sekoor.client.gui;

import com.sekoor.client.KeybaseConnector;
import com.sekoor.client.domain.Contact;
import com.sekoor.client.gui.Main;
import com.sekoor.client.gui.Message;
import javafx.animation.*;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.sekoor.client.gui.controls.ClickableBitcoinAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static javafx.beans.binding.Bindings.isNull;
import static com.sekoor.client.gui.utils.GuiUtils.*;

/**
 * Gets created auto-magically by FXMLLoader via reflection. The widget fields are set to the GUI controls they're named
 * after. This class handles all the updates and event handling for the main UI.
 */
public class Controller implements EventHandler<ActionEvent> {
    private final static Logger log = LoggerFactory.getLogger(Controller.class);

    public ProgressBar progressBar;
    public Label progressBarLabel;
    public VBox syncBox;
    public HBox controlsBox;
    public Label balance;
    public Button sendMoneyOutBtn;
    public ClickableBitcoinAddress addressControl;

    @FXML protected BorderPane mainBorderPane;
    @FXML protected Button writeMessageBtn = new Button(""); // No contact clicked yet
    private String currentContact = null;

    // Sekoor specific stuff
    public Button downloadBtn, cancelBtn;

    public ListView<String> contactListView;
    private ObservableList<String> contacts;

    public ListView<Message> messageList;
    private ObservableList<Message> messages;

    private ReadOnlyObjectProperty<Message> selectedMessages;

    // Called by FXMLLoader.
    public void initialize() {
        progressBar.setProgress(-1);

        cancelBtn.setVisible(false);

        // The SekoorClient.File.toString() method is good enough for rendering list cells for now.
        messages = FXCollections.observableArrayList();

        contacts = FXCollections.observableArrayList();
        List<String> mainsContactList = Main.instance.getContacts();
        contacts.addAll(mainsContactList);
        contactListView.setItems(contacts);
        contactListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        //selectedMessages = messageList.getSelectionModel().selectedItemProperty();

        // Don't allow the user to press download unless an item is selected.
        //downloadBtn.disableProperty().bind(isNull(selectedMessages));
    }


    public void onBitcoinSetup() {
        addressControl.setAddress("1Jpm2ibWaMwDJn8PgvyrgVCVr9sHTR3KfA");
        //refreshBalanceLabel();
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

    public void download(ActionEvent event) throws Exception {
        // Nothing
    }


    public void writeMessage(ActionEvent event) throws Exception {


    }


    public void contactEntryClicked(MouseEvent event) throws Exception {
        log.debug("contact clicked");
        if (event.getButton() == MouseButton.PRIMARY ) {
             if (event.getClickCount() == 2) {
                 // Double click on a contact: shortcut for write message
             } else {
                 // Single click: show conversation list
                 Object target = event.getTarget();
                 if (target instanceof Text) {
                     Text contactText = (Text) event.getTarget();
                     currentContact = contactText.getText();
                     System.out.println("res =" + currentContact);
                     writeMessageBtn.setText("Write to " + currentContact);
                     writeMessageBtn.setDefaultButton(true);
                     writeMessageBtn.setOnAction(this);

                     HBox buttonBox = new HBox();
                     buttonBox.setSpacing(10.0);
                     buttonBox.setPadding(new Insets(10, 10, 10, 10));
                     buttonBox.getChildren().add(writeMessageBtn);
                     mainBorderPane.setTop(buttonBox);
                 } else {
                     String msg = "What is this click? :" + target;
                     log.warn(msg);
                     System.out.println(msg);
                 }
             }
        }
    }


    public void handle(ActionEvent event) {
        // As of now, this can only be writeMessage click


        if (currentContact == null) {
            throw new IllegalStateException("Programming error: no currentContact");
        }

        Pane ui = Main.instance.new600x800Window("edit_message.fxml");


        //fadeOut(Main.instance.mainUI);
        //Main.instance.overlayUI("edit_message.fxml");
    }


    public void messageEntryClicked(MouseEvent event) throws Exception {
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
