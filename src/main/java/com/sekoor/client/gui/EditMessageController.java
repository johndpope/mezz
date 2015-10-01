package com.sekoor.client.gui;

import com.sekoor.client.FileSystemHandler;
import com.sekoor.client.HtmlUtil;
import com.sekoor.client.KeybaseConnector;
import com.sekoor.client.gui.utils.GuiUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.font.TextLabel;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class EditMessageController {

    private final static Logger log = LoggerFactory.getLogger(EditMessageController.class);

    public final static String ADD_NEW_RECEIVER = "(Add New Receiver)";

    private ObservableList<String> selectedContactObservableList;
    private ObservableList<String> notSelectedContactObservableList;

    @FXML private ListView contactsListView;
    @FXML private ComboBox<String> newContactCombo;
    @FXML private TextField topicField;
    @FXML private HTMLEditor htmlEditor;
    @FXML private TextArea logArea;
    @FXML private Button encryptBtn;
    @FXML private Button sendBtn;

    public void initialize() {

        // Add the selected contacts
        selectedContactObservableList = FXCollections.observableArrayList(
                "acke", "lopp");
        contactsListView.setItems(selectedContactObservableList);
        //List<String> commands = Arrays.asList("Remove");
        //contactsListView.setCellFactory(ComboBoxListCell.forListView(commands));

        // Add the NOT selected contacts
        log.debug("Fill comboBox contacts for the first time");
        notSelectedContactObservableList = FXCollections.observableArrayList();
        //notSelectedContactObservableList.add(ADD_NEW_RECEIVER);
        notSelectedContactObservableList.addAll(Main.instance.getContacts());
        //for(String selectedContact : selectedContactObservableList) {
        //    notSelectedContactObservableList.remove(selectedContact);
        //}
        newContactCombo.setItems(notSelectedContactObservableList);
    }


    public void cancel(ActionEvent event) {
        ((Node)(event.getSource())).getScene().getWindow().hide();
    }

    public void encrypt(ActionEvent event) {

        String topic = topicField.getText();
        if (topic.length() > 0) {
            log.debug("Topic of length " + topic.length());

            String html = htmlEditor.getHtmlText();
            Document doc = HtmlUtil.transformToDoc(html);

            if (HtmlUtil.hasBody(doc)) {
                log.debug("Content of length " + doc.body().data().length());

                // Add the topic as title
                HtmlUtil.addTitle(doc, topic);
                String message = HtmlUtil.getHtmlAsString(doc);

                List<String> sendToContacts = new ArrayList<String>();
                Object[] arr =  selectedContactObservableList.toArray();
                for (Object o : arr) {
                    String s = (String) o;
                    sendToContacts.add(s);
                }
                Map<String, BufferedReader> returnMap = null;
                try {
                    returnMap = encryptToAll(sendToContacts, message);
                    logArea.setText("--- Encrypting Message ---");
                    System.out.println("File encrypted");
                } catch (IllegalStateException e) {
                    logArea.setText("Encryption failed:" + "\n" + e.getMessage());
                    return;
                }

                Set<String> contactKeys = returnMap.keySet();

                for (String contactKey : contactKeys) {

                    BufferedReader bufferedReader = returnMap.get(contactKey);
                    try {
                        String line = bufferedReader.readLine();
                        while (line != null) {
                            System.out.println("Put line: " + line);
                            String oldText = logArea.getText();
                            logArea.setText(oldText + "\n" + line);
                            line = bufferedReader.readLine();
                        }

                        String oldText = logArea.getText();
                        logArea.setText(oldText + "\n" + "--- Encryption Done! ---");

                        bufferedReader.close();
                        encryptBtn.setDisable(true);
                        sendBtn.setDisable(false);

                        System.out.println("Done Putting lines " );
                    } catch (IOException e) {
                        throw new RuntimeException("Cannot read from keybase output " + e.getMessage());
                    }
                }


            } else {
                GuiUtils.informationalAlert("Message Without Content", "Are you sure you don't want content for this message?");
            }
        } else {
            GuiUtils.informationalAlert("Message Without Topic", "Are you sure you don't want a topic for this message?");
        }

    }

    private Map<String, BufferedReader> encryptToAll(List<String> contacts, String message) {
        Map<String, BufferedReader> readerMap = new HashMap<String, BufferedReader>();
        DateTime dt = new DateTime();

        for (String contact : contacts) {
            // Add the
            String newFileAbsolutePath = null;
            try {
                newFileAbsolutePath = FileSystemHandler.createFileWhenSendingMessageToContact(contact, message, FileSystemHandler.baseDir, dt);
            } catch (IOException e) {
                throw new RuntimeException("IOException when creating file: " + e.getMessage());
            }

            try {
                BufferedReader br = KeybaseConnector.encryptToContact(contact, message, newFileAbsolutePath);
                readerMap.put(newFileAbsolutePath, br);
            } catch (InterruptedException e) {
                throw new RuntimeException("InterruptedException during encryption: " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException("IOException during encryption: " + e.getMessage());
            }
        }
        return readerMap;
    }

    public void send(ActionEvent event) {
        System.out.println("Sending");
    }

    public void contactClicked(MouseEvent event) throws Exception {
        log.debug("contact clicked");
        if (event.getButton() == MouseButton.PRIMARY ) {
            Object target = event.getTarget();
            if (target instanceof Text) {
                Text contactText = (Text) event.getTarget();
                String currentContact = contactText.getText();
                System.out.println("res =" + currentContact);

                int pos = selectedContactObservableList.indexOf(currentContact);
                if (pos < 0) {
                    throw new IllegalStateException("Contact cannot be removed: " + currentContact);
                } else {
                    selectedContactObservableList.remove(currentContact);
                }
            }
        }
    }

    public void addReceiver(ActionEvent event) throws Exception {
        log.debug("Add Receiver clicked");
        Object target = event.getTarget();
        if(target instanceof ComboBox) {
            String selectedContact = newContactCombo.getValue();
            System.out.println("This was selected: " + selectedContact);
            if (!selectedContactObservableList.contains(selectedContact)) {
                selectedContactObservableList.add(selectedContact);
            }
        } else {
            log.warn("addReceiver from strange target");
        }
    }
}
