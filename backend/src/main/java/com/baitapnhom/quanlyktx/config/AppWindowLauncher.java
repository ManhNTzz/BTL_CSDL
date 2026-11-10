package com.baitapnhom.quanlyktx.config;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Tự động mở cửa sổ giao diện ứng dụng (Desktop App Mode) ngay khi chương trình khởi động xong.
@Component
public class AppWindowLauncher {

    private static final Logger log = LoggerFactory.getLogger(AppWindowLauncher.class);

    @Value("${server.port:8080}")
    private int port;

    @Value("${app.auto-open-window:true}")
    private boolean autoOpen;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (!autoOpen) {
            return;
        }

        Thread launcherThread = new Thread(() -> {
            try {
                Thread.sleep(500);

                String url = "http://localhost:" + port;
                String os = System.getProperty("os.name", "").toLowerCase();

                if (os.contains("win")) {
                    File edge32 = new File("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
                    File edge64 = new File("C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe");
                    File chrome32 = new File("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
                    File chrome64 = new File("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");

                    if (edge32.exists()) {
                        log.info("Đang mở cửa sổ App qua Microsoft Edge: {}", url);
                        new ProcessBuilder(edge32.getAbsolutePath(), "--app=" + url).start();
                        return;
                    } else if (edge64.exists()) {
                        log.info("Đang mở cửa sổ App qua Microsoft Edge: {}", url);
                        new ProcessBuilder(edge64.getAbsolutePath(), "--app=" + url).start();
                        return;
                    } else if (chrome64.exists()) {
                        log.info("Đang mở cửa sổ App qua Google Chrome: {}", url);
                        new ProcessBuilder(chrome64.getAbsolutePath(), "--app=" + url).start();
                        return;
                    } else if (chrome32.exists()) {
                        log.info("Đang mở cửa sổ App qua Google Chrome: {}", url);
                        new ProcessBuilder(chrome32.getAbsolutePath(), "--app=" + url).start();
                        return;
                    }
                }

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    new ProcessBuilder("cmd", "/c", "start", url).start();
                }
            } catch (Exception e) {
                log.warn("Không thể tự động mở cửa sổ giao diện: {}", e.getMessage());
            }
        });

        launcherThread.setDaemon(true);
        launcherThread.start();
    }
}
