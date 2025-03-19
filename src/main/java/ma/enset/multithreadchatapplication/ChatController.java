package ma.enset.multithreadchatapplication;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import java.io.*;
import java.net.Socket;
import java.util.Optional;

public class ChatController {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;

    public void initialize() {
        requestUsername();
    }

    private void requestUsername() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Username Required");
        dialog.setHeaderText("Enter your username to join the chat");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresentOrElse(name -> {
            username = name;
            connectToServer();
        }, () -> {
            Platform.exit();
        });
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 1234);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            listenForMessages();
        } catch (IOException e) {
            showError("Unable to connect to server.");
            Platform.exit();
        }
    }

    @FXML
    private void sendMessage() {
        String messageToSend = messageField.getText().trim();
        if (!messageToSend.isEmpty()) {
            try {
                String fullMessage = username + ": " + messageToSend;
                bufferedWriter.write(fullMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                Platform.runLater(() -> chatArea.appendText(fullMessage + "\n"));
                messageField.clear();
            } catch (IOException e) {
                showError("Failed to send message.");
            }
        }
    }

    private void listenForMessages() {
        new Thread(() -> {
            String msgFromChat;
            while (socket.isConnected()) {
                try {
                    msgFromChat = bufferedReader.readLine();
                    String finalMsgFromChat = msgFromChat;
                    Platform.runLater(() -> chatArea.appendText(finalMsgFromChat + "\n"));
                } catch (IOException e) {
                    closeEverything();
                    break;
                }
            }
        }).start();
    }

    private void closeEverything() {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
