module rached {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires java.sql;
    requires java.prefs;
    requires java.base;
    requires java.net.http;
    requires org.json;
    requires java.desktop;
    requires jdk.jsobject;

    opens controllers to javafx.fxml;
    opens models to javafx.base, javafx.fxml;
    opens com.rached to javafx.fxml, javafx.graphics;
    opens utils to javafx.fxml;
    opens services to javafx.fxml;
    

    exports com.rached;
    exports controllers;
    exports models;
    exports services;
    exports utils;
}