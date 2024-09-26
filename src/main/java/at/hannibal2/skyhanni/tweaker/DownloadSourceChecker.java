package at.hannibal2.skyhanni.tweaker;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadSourceChecker {

    private static final String MOD_VERSION = "@MOD_VERSION@";
    private static final String GITHUB_REPO = "511310721";
    private static final String GITHUB_REPO_TEXT = "repo_id=" + GITHUB_REPO;
    private static final String MODRINTH_URL = "/data/byNkmv5G/";
    private static final String THE_PASSWORD = "danger";

    private static final String[] PASSWORD_POPUP = {
        "If someone asks you to type in here,",
        "",
        "the likelihood of them ratting you is high!",
        "",
        "Enter the password:"
    };

    private static final String[] SECURITY_POPUP = {
        "The file you are trying to run is hosted on a non-trusted domain.",
        "",
        "Host: %s",
        "",
        "Please download the file from a trusted source.",
        "",
        "IF YOU DO NOT KNOW WHAT YOU ARE DOING, CLOSE THIS WINDOW!",
        "",
        "And download from the official link below."
    };

    public static void init() {
        if (!TweakerUtils.isOnWindows()) return;
        URI host = getDangerousHost();
        if (host != null) {
            openMenu(host);
        }
    }

    private static void openMenu(URI host) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        AtomicBoolean close = new AtomicBoolean(true);

        JPanel links = new JPanel();

        links.add(TweakerUtils.createButton(
            "Discord",
            () -> TweakerUtils.openUrl("https://discord.com/invite/skyhanni-997079228510117908")
        ));

        links.add(TweakerUtils.createButton(
            "Official Download",
            () -> TweakerUtils.openUrl("https://github.com/hannibal002/SkyHanni/releases")
        ));

        JPanel buttons = new JPanel();

        buttons.add(TweakerUtils.createButton(
            "Skip (Trusted Users Only)",
            () -> {
                String password = JOptionPane.showInputDialog(frame, String.join("\n", PASSWORD_POPUP));
                if (password != null && password.equals(THE_PASSWORD)) {
                    close.set(false);
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                }
            }
        ));

        buttons.add(TweakerUtils.createButton(
            "Close",
            () -> {
                close.set(true);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        ));

        JOptionPane.showOptionDialog(
            frame,
            String.format(String.join("\n", SECURITY_POPUP), uriToSimpleString(host)),
            "SkyHanni " + MOD_VERSION + " Security Error",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            new JPanel[] { links, buttons },
            links
        );

        if (!close.get()) return;
        TweakerUtils.exit();
    }

    private static String uriToSimpleString(URI uri) {
        return uri.getScheme() + "://" + uri.getHost() + uri.getPath();
    }

    private static URI getDangerousHost() {
        try {
            URL url = DownloadSourceChecker.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(url.getFile());
            if (!file.isFile()) return null;
            URI host = getHost(file);
            if (host == null) return null;

            if (host.getHost().equals("objects.githubusercontent.com")) {
                if (host.getQuery().contains(GITHUB_REPO_TEXT)) {
                    return null;
                } else if (host.getPath().contains("/" + GITHUB_REPO + "/")) {
                    return null;
                }
            } else if (host.getHost().equals("cdn.modrinth.com") && host.getPath().startsWith(MODRINTH_URL)) {
                return null;
            }
            return host;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static URI getHost(File file) throws Exception {
        final File adsFile = new File(file.getAbsolutePath() + ":Zone.Identifier:$DATA");
        String host = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(adsFile))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("HostUrl=")) {
                    host = line.substring(8);
                    break;
                }
                line = reader.readLine();
            }
        }
        return host != null ? new URI(host) : null;
    }
}
