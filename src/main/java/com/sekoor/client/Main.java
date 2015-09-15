package com.sekoor.client;


import com.google.common.net.HostAndPort;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.sekoor.client.gui.utils.TextFieldValidator;

import java.io.IOException;
import java.net.URL;
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
    public static final String APP_NAME = "PayFile";
    public static final int CONNECT_TIMEOUT_MSEC = 2000;


    public static Main instance;
    public static SekoorClient client;
    public static HostAndPort serverAddress;
    private static String filePrefix;

    private StackPane uiStack;
    public Pane mainUI;
    public Controller controller;
    public Stage mainWindow;

    @Override
    public void start(Stage mainWindow) throws Exception {
        instance = this;
        // Show the crash dialog for any exceptions that we don't handle and that hit the main loop.
        handleCrashesOnThisThread();
        try {
            init(mainWindow);
        } catch (Throwable t) {
            throw t;
        }
    }

    private void init(Stage mainWindow) throws IOException {
        this.mainWindow = mainWindow;

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
        
        overlayUI("connect_server.fxml");
        mainUI.setVisible(false);
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
