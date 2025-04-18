module org.example.guia {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.httpserver;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.services.gmail;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;

    opens org.example.guia to javafx.fxml;
    exports org.example.guia;
}