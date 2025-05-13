module User {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires mysql.connector.j;


    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.json;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires java.sql;

    opens Controllers to javafx.fxml;
    opens models to javafx.fxml;

    exports Controllers;
    exports models;


    opens main to javafx.fxml;
    exports main;
}