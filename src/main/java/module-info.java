module ma.enset.multithreadchatapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml.crypto;


    opens ma.enset.multithreadchatapplication to javafx.fxml;
    exports ma.enset.multithreadchatapplication;
}