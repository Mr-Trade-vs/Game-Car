module integrative.task {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics;

    opens integrative.task.control to javafx.fxml;
    exports integrative.task.control;

    opens integrative.map to javafx.fxml;

    exports integrative.task.view;
    opens integrative.task.view to javafx.fxml;

    exports integrative.task.model;
    opens integrative.task.model to javafx.fxml;
}
