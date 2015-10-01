package com.sekoor.client.gui;


import com.google.common.net.HostAndPort;
import com.sekoor.client.FileSystemHandler;
import com.sekoor.client.KeybaseConnector;
import com.sekoor.client.SekoorClient;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.sekoor.client.gui.utils.TextFieldValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.sekoor.client.gui.utils.GuiUtils.*;
import static com.sekoor.client.utils.Exceptions.evalUnchecked;
import static com.sekoor.client.utils.Exceptions.runUnchecked;

// To do list:
//
// Payments:
// - Progress indicator for negotiating a payment channel?
// - Bug: If the server fails to broadcast the contract tx then the client gets out of sync with the server.
//
// Misc code quality:
// - Consider switching to P2Proto (question: how to do SSL with that?). Simplifies the core protocol.
// - SSL support
//
// Generic UI:
// - Solve the Mac menubar issue. Port the Mac specific tweaks to wallet-template.
// - Write a test plan that exercises every reasonable path through the app and test it.
// - Get an Apple developer ID and a Windows codesigning cert.
// - Find/beg/buy/borrow/steal a nice icon.
// - Find a way to dual boot Windows on my laptop.
// - Build, sign and test native packages!
//
// Future ideas:
// - Merkle tree validators for files, to avoid a server maliciously serving junk instead of the real deal.


public class Main extends Application {
    private final static Logger log = LoggerFactory.getLogger(Main.class);

    public static final String APP_NAME = "SekoorBox";
    public static final int CONNECT_TIMEOUT_MSEC = 2000;


    public static Main instance;
    public static SekoorClient client;
    public static HostAndPort serverAddress;
    private static String filePrefix;

    private List<String> contacts;

    private StackPane uiStack;
    public Pane mainUI;
    public Controller controller;
    public Stage mainWindow;

    public List<String> getContacts() {
        return contacts;
    }

    @Override
    public void start(Stage mainWindow) throws Exception {
        instance = this;
        // Show the crash dialog for any exceptions that we don't handle and that hit the main loop.
        handleCrashesOnThisThread();
        try {
            init(mainWindow);
        } catch (Throwable t) {
            log.error("Error during init: " + t.getMessage());
            crashAlert(t);
        }
    }

    private void init(Stage mainWindow) throws IOException, InterruptedException  {
        this.mainWindow = mainWindow;

        // Get the contacts
        contacts = KeybaseConnector.getContacts();
        FileSystemHandler.buildFoldersIfNeeded(contacts, FileSystemHandler.baseDir);

        // Load the GUI. The Controller class will be automagically created and wired up.
        URL location = getClass().getResource("main.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        mainUI = loader.load();
        controller = loader.getController();
        // Configure the window with a StackPane so we can overlay things on top of the main UI.
        uiStack = new StackPane(mainUI);
        mainWindow.setTitle(APP_NAME);
        final Scene scene = new Scene(uiStack);
        TextFieldValidator.configureScene(scene);   // Add CSS that we need.
        mainWindow.setScene(scene);


        controller.onBitcoinSetup();




        //overlayUI("connect_server.fxml");
        mainUI.setVisible(true);
        mainWindow.show();
    }


    public class OverlayUI<T> {
        public Node ui;
        public T controller;

        public OverlayUI(Node ui, T controller) {
            this.ui = ui;
            this.controller = controller;
        }

        public void show() {
            blurOut(mainUI);
            uiStack.getChildren().add(ui);
            fadeIn(ui);
        }

        public void done() {
            checkGuiThread();
            fadeOutAndRemove(ui, uiStack);
            blurIn(mainUI);
            this.ui = null;
            this.controller = null;
        }
    }

    public Pane new600x800Window(String name) {
        return evalUnchecked(() -> {
            checkGuiThread();
            URL location = getClass().getResource(name);
            FXMLLoader loader = new FXMLLoader(location);
            Pane ui = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Write Message");
            stage.setScene(new Scene(ui, 800, 600));
            stage.show();
            return ui;
        });
    }

    public <T> OverlayUI<T> overlayUI(Node node, T controller) {
        checkGuiThread();
        OverlayUI<T> pair = new OverlayUI<T>(node, controller);
        // Auto-magically set the overlayUi member, if it's there.
        runUnchecked(() -> controller.getClass().getDeclaredField("overlayUi").set(controller, pair));
        pair.show();
        return pair;
    }

    /** Loads the FXML file with the given name, blurs out the main UI and puts this one on top. */
    public <T> OverlayUI<T> overlayUI(String name) {
        return evalUnchecked(() -> {
            checkGuiThread();
            // Load the UI from disk.
            URL location = getClass().getResource(name);
            FXMLLoader loader = new FXMLLoader(location);
            Pane ui = loader.load();
            T controller = loader.getController();
            OverlayUI<T> pair = new OverlayUI<>(ui, controller);
            // Auto-magically set the overlayUi member, if it's there.
            controller.getClass().getDeclaredField("overlayUi").set(controller, pair);
            pair.show();
            return pair;
        });
    }

    public static CompletableFuture<SekoorClient> connect(HostAndPort server) {
        serverAddress = server;
        return connect(serverAddress, CONNECT_TIMEOUT_MSEC);
    }

    public static CompletableFuture<SekoorClient> connect(HostAndPort server, int timeoutMsec) {
        return CompletableFuture.supplyAsync(() ->
                        evalUnchecked(() -> {
                            return new SekoorClient(server);
                        })
        );
    }

    @Override
    public void stop() throws Exception {
        // TODO:Olle stop something?
        super.stop();
    }

    public static void main(String[] args) throws Exception {


        String options = "";
        if (args.length > 0) {
            options = args[0];
        }

        if (options.equalsIgnoreCase("help")) {
            System.out.println("help - No need! Just run this without arguments");
            return;
        }

        launch(args);
    }
}
