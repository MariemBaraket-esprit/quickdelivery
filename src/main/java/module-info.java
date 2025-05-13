module com.quickdelivery {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.web;
    requires javafx.swing;
    requires mysql.connector.j;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.json;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires java.net.http;
    requires java.sql;
    requires com.theokanning.openai.gpt3;

    opens Controllers to javafx.fxml;
    opens models to javafx.fxml;
    opens main to javafx.fxml;
    opens utils to javafx.fxml;
    opens services to javafx.fxml;
    
    exports Controllers;
    exports models;
    exports main;
    exports utils;
    exports services;
} 