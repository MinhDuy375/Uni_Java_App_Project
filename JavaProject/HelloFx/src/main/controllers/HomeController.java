package main.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;

public class HomeController {
    @FXML
    private ImageView notificationIcon;
    @FXML
    private Button userButton;

    @FXML
    public void initialize() {
        // Cập nhật icon thông báo nếu cần
        notificationIcon
                .setImage(new Image(getClass().getResource("/resources/images/bell-icon.png").toExternalForm()));
    }
}
