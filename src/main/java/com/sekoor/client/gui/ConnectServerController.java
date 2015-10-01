package com.sekoor.client.gui;

import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.sekoor.client.gui.utils.GuiUtils.*;

/** A class that manages the connect to server screen. */
public class ConnectServerController {
    private final static Logger log = LoggerFactory.getLogger(ConnectServerController.class);
    private final static int REFUND_CONNECT_TIMEOUT_MSEC = 1000;

    public Button connectBtn;
    public TextField server;
    public Label titleLabel;
    public Main.OverlayUI overlayUi;
    private String defaultTitle;

    // Called by FXMLLoader
    public void initialize() {
        server.textProperty().addListener((observableValue, prev, current) -> connectBtn.setDisable(current.trim().isEmpty()));
        defaultTitle = titleLabel.getText();
        // Restore the server used last time, minus the port part if it was the default.
        HostAndPort lastServer = HostAndPort.fromString("sekoor.com:9907");
        if (lastServer != null)
            server.setText(lastServer.getHostText());
    }

    public void connect(ActionEvent event) {
        final String serverName = server.getText().trim();
        connectBtn.setDisable(true);
    }
}


