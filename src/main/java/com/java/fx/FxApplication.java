package com.java.fx;

import com.sun.tools.javac.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javafx.application.Application;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class FxApplication extends  Application{


	private static ApplicationContext springContext;

	public static void main(String[] args) {

		springContext = SpringApplication.run(FxApplication.class, args);
		launch();


	}

	@Override
	public void start(Stage stage) throws Exception {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Main.fxml"));
		fxmlLoader.setControllerFactory(springContext::getBean);

		Scene scene = new Scene(fxmlLoader.load());
		stage.setScene(scene);

		String title = springContext.getBean("title", String.class);
		stage.setTitle(title);

		stage.show();
	}
}
