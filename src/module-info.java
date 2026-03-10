module todo.again {
    requires java.desktop;
    requires java.prefs;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.apache.commons.lang3;
    requires com.google.gson;

    exports yapilacaklarListesi;
    exports yapilacaklarListesi.mediator;
    exports yapilacaklarListesi.muzik;
    exports yapilacaklarListesi.pomodoro.model;
    exports yapilacaklarListesi.service;
    exports yapilacaklarListesi.veriler;

    opens yapilacaklarListesi to javafx.fxml;
    opens yapilacaklarListesi.veriler to com.google.gson;
}
