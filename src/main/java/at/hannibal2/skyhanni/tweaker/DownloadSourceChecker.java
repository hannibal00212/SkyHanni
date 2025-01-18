package at.hannibal2.skyhanni.tweaker;

import at.hannibal2.skyhanni.utils.OSUtils;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadSourceChecker {

    private static final String MOD_VERSION = "@MOD_VERSION@";
    private static final String GITHUB_REPO = "511310721";
    private static final String GITHUB_REPO_TEXT = "repo_id=" + GITHUB_REPO;
    private static final String MODRINTH_URL = "/data/byNkmv5G/";

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
        if (!OSUtils.INSTANCE.isWindows()) return;
        URI host = getDangerousHost();
        if (host != null) {
            openMenu(uriToSimpleString(host));
        } else {
            // Please do not remove the comments for testing purposes
//             openMenu("https://github.com/");
//             openMenu("https://github.com/scray-github-user/fake-skyhanni-repo");
        }
    }

    private static void openMenu(String host) {
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
            "Open Discord",
            () -> TweakerUtils.openUrl("https://discord.com/invite/skyhanni-997079228510117908")
        ));

        links.add(TweakerUtils.createButton(
            "Open Modrinth",
            () -> TweakerUtils.openUrl("https://modrinth.com/mod/skyhanni")
        ));

        links.add(TweakerUtils.createButton(
            "Open Github",
            () -> TweakerUtils.openUrl("https://github.com/hannibal002/SkyHanni/releases")
        ));

        JPanel buttons = new JPanel();

        buttons.add(TweakerUtils.createButton(
            "Close",
            () -> {
                close.set(true);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        ));

        // Compile the regex pattern for matching an empty host
        Pattern pattern = Pattern.compile("https:\\/\\/.*.com\\/$|about:internet");
        Matcher matcher = pattern.matcher(host);

        // Check if the host is empty (Brave is cutting everything past .com/ from the host)
        String message;
        if (matcher.find()) {
            message = metadataStripped(host);
        } else {
            message = wrongDownloadSource(host);
        }

        System.err.println("SkyHanni-" + MOD_VERSION + " detected a untrusted download source host: '" + host + "'");
        JOptionPane.showOptionDialog(
            frame,
            message,
            "SkyHanni " + MOD_VERSION + " Security Error",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            new JPanel[]{links, buttons},
            links
        );

        if (!close.get()) return;
        TweakerUtils.exit();
    }

    private static String wrongDownloadSource(String host) {
        return "<html><body style='width: 350px; font-family: Arial, sans-serif; padding: 20px;'>"
            + "<h3 style='color: #D32F2F;'>Failed to Verify File Authenticity</h2>"
            + "<p><strong>SkyHanni " + MOD_VERSION + "</strong> is downloaded from an unofficial source. "
            + "To protect you from potentially malicious versions of the mod, we cannot allow Minecraft to run.</p>"
            + "<h3>How to Fix It</h3>"
            + "<ol>"
            + "<li>Ensure you are downloading from our official GitHub or Modrinth page.</li>"
            + "<li>Re-download the file and restart Minecraft with the new file.</li>"
            + "</ol>"
            + "<p><em>Note: If this location is unexpected, it’s possible the file was tampered with or downloaded from an incorrect link.</em></p>"
            + "<p></p>"
            + "<p>Your safety is our top priority. Sorry for the inconvenience!</p>"
            + "<hr style='margin: 20px 0;'>"
            + "<p>File Source: <strong> " + host + "</strong></p>"
            + "<p></p>"
            + "<p>You can use the buttons below to access our official channels:</p>"
            + "</body></html";
    }

    private static String metadataStripped(String host) {
        return "<html><body style='width: 350px; font-family: Arial, sans-serif; padding: 20px;'>"
            + "<h3 style='color: #D32F2F;'>Failed to Verify File Authenticity</h2>"
            + "<p>File Source: <strong> " + host + "</strong></p>"
            + "<p>We couldn’t verify the authenticity of <strong>SkyHanni " + MOD_VERSION + "</strong>. "
            + "The issue occurred because your browser removed essential metadata during the download. "
            + "To protect you from potentially malicious versions of the mod, we cannot allow Minecraft to run.</p>"
            + "<h3>How to Fix It</h3>"
            + "<ol style='text-align: left; display: inline-block;'>"
            + "<li>Use a different browser (e.g., Chrome, Firefox, Edge).</li>"
            + "<li>Re-download the file from our official GitHub or Modrinth page.</li>"
            + "<li>Restart Minecraft with the new file.</li>"
            + "</ol>"
            + "<p>Note: This issue commonly happens with browsers like <strong>Brave</strong>.</p>"
            + "<p></p>"
            + "<p>Your safety is our top priority. Sorry for the inconvenience!</p>"
            + "<hr style='margin: 20px 0;'>"
            + "<p>File Source: <strong> " + host + "</strong></p>"
            + "<p></p>"
            + "<p>You can use the buttons below to access our official channels:</p>"
            + "</body></html>";
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
