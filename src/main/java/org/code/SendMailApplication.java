package org.code;

import com.arpitram.SendMailFrame;

import java.awt.*;

public class SendMailApplication {
    public static void main(String[] args) {
        try {
            String mailConfigFile = "config.properties";
            EventQueue.invokeLater(() -> new SendMailFrame(mailConfigFile).setVisible(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
