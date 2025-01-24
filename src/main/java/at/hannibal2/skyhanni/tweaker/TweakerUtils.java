package at.hannibal2.skyhanni.tweaker;

import javax.swing.JButton;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class TweakerUtils {

    public static void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Taken from Skytils
    public static void exit() {
        try {
            Class<?> clazz = Class.forName("java.lang.Shutdown");
            Method method = clazz.getDeclaredMethod("exit", int.class);
            method.setAccessible(true);
            method.invoke(null, 0);
        } catch (Exception e) {
            e.printStackTrace();
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                Runtime.getRuntime().exit(1);
                return null;
            });
        }
    }

    public static JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });
        return button;
    }
}
