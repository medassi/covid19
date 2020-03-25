module com.medassi.coronavirus {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires org.controlsfx.controls;
    opens com.medassi.coronavirus to javafx.fxml;
    exports com.medassi.coronavirus;
    requires json.simple;
    
}