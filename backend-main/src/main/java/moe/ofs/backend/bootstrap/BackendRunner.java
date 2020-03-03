package moe.ofs.backend.bootstrap;

import moe.ofs.backend.ControlPanelApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BackendRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Spring Boot application started");

        new Thread(() -> javafx.application.Application.launch(ControlPanelApplication.class)).start();

    }
}
