package net.minecraft;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class MinecraftLauncher extends JFrame {
    private final TokenCache tokenCache = new TokenCache();
    private final LauncherSettings settings = new LauncherSettings();
    private final JLabel statusLabel = new JLabel("Ready to play Minecraft " + BetaLauncher.DEFAULT_VERSION);
    private final JLabel welcomeLabel = new JLabel("Welcome, guest");
    private final JLabel lastPlayedLabel = new JLabel("Last played: never");
    private final JLabel versionStatusLabel = new JLabel("Version files: not downloaded yet");
    private final JLabel javaStatusLabel = new JLabel("Java: checking runtime");
    private final JLabel eraBadge = new JLabel("Beta");
    private final JTextField offlineName = new JTextField("Player");
    private final JComboBox<String> versionBox = new JComboBox<String>(new String[] { BetaLauncher.DEFAULT_VERSION });
    private final JComboBox<String> memoryBox = new JComboBox<String>(new String[] { "Classic 512MB", "Comfort 1024MB", "Overkill 2048MB", "Custom..." });
    private final JButton loginButton = new FooterButton("Microsoft Login");
    private final JButton playOnlineButton = new PlayButton("Play");
    private final JButton playOfflineButton = new FooterButton("Play Offline");
    private final JButton randomVersionButton = new FooterButton("Random");
    private final JButton signOutButton = new FooterButton("Forget Login");
    private final JButton redownloadButton = new FooterButton("Redownload Version");
    private final JCheckBox compactNewsBox = new JCheckBox("Patch Notes Mode!");
    private final TabLabel updateTab = new TabLabel("Update Notes", true);
    private final TabLabel logTab = new TabLabel("Launcher Log", false);
    private final TabLabel profileTab = new TabLabel("Profile Editor", false);
    private final JEditorPane news = new JEditorPane();
    private final StringBuilder launcherLog = new StringBuilder();
    private final JLabel skinLabel = new JLabel();
    private final Random random = new Random();
    private List<String> allVersions = new java.util.ArrayList<String>();
    private volatile AuthProfile currentProfile;
    private String activeTab = "notes";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MinecraftLauncher().setVisible(true);
            }
        });
    }

    private MinecraftLauncher() {
        super("Minecraft Launcher 1.6.89-j Revival");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(854, 560));
        setIconImage(loadImage("/net/minecraft/favicon.png"));
        buildUi();
        setSize(900, 590);
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(238, 238, 238));
        setContentPane(root);

        root.add(buildTabs(), BorderLayout.NORTH);
        root.add(buildNewsShell(), BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        playOnlineButton.setEnabled(false);
        loadSavedSettings();
        updateJavaStatusLabel();
        warnIfJavaUnusual();
        setOfflineHead();
        wireActions();
        maybeShowFirstRunWelcome();
        versionBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEraBadge();
                refreshActiveTab();
            }
        });
        refreshActiveTab();
        loadVersionsAsync();
        AuthProfile cached = tokenCache.cachedProfile();
        if (cached != null) {
            currentProfile = cached;
            offlineName.setText(cached.name);
            playOnlineButton.setEnabled(true);
            updateWelcome(cached);
            loadSkinPreview(cached);
            status("Ready to play Minecraft " + selectedVersion());
        }
    }

    private JPanel buildTabs() {
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setBackground(new Color(238, 238, 238));
        wireTab(updateTab, "notes");
        wireTab(logTab, "log");
        wireTab(profileTab, "profile");
        tabs.add(updateTab);
        tabs.add(logTab);
        tabs.add(profileTab);
        return tabs;
    }

    private JPanel buildNewsShell() {
        DarkNewsPanel shell = new DarkNewsPanel();
        shell.setLayout(new BorderLayout());
        shell.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(110, 110, 110)));

        news.setEditable(false);
        news.setOpaque(false);
        news.setContentType("text/html");
        news.setFont(new Font("Verdana", Font.PLAIN, 12));
        news.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (e.getDescription() != null && e.getDescription().indexOf("launcher:") == 0) {
                        handleLauncherAction(e.getDescription());
                    } else if (e.getURL() != null) {
                    browse(e.getURL().toString());
                    }
                }
            }
        });

        JScrollPane newsScroll = new JScrollPane(news);
        newsScroll.setOpaque(false);
        newsScroll.getViewport().setOpaque(false);
        newsScroll.setBorder(BorderFactory.createEmptyBorder());
        shell.add(newsScroll, BorderLayout.CENTER);

        JEditorPane links = htmlPane(sidebarHtml());
        links.setPreferredSize(new Dimension(210, 100));
        shell.add(links, BorderLayout.EAST);

        return shell;
    }

    private JPanel buildBottomBar() {
        JPanel footer = new JPanel(new BorderLayout(8, 2));
        footer.setBackground(new Color(232, 232, 232));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(170, 170, 170)),
                new EmptyBorder(4, 6, 4, 6)));

        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);

        JPanel profile = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        profile.setOpaque(false);
        JLabel profileLabel = new JLabel("Profile:");
        profileLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        profile.add(profileLabel);

        offlineName.setPreferredSize(new Dimension(132, 24));
        offlineName.setFont(new Font("Dialog", Font.PLAIN, 11));
        profile.add(offlineName);

        JLabel versionLabel = new JLabel("Version:");
        versionLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        profile.add(versionLabel);

        versionBox.setEditable(true);
        versionBox.setPreferredSize(new Dimension(88, 24));
        versionBox.setFont(new Font("Dialog", Font.PLAIN, 11));
        profile.add(versionBox);
        eraBadge.setOpaque(true);
        eraBadge.setForeground(Color.WHITE);
        eraBadge.setBackground(new Color(70, 95, 150));
        eraBadge.setBorder(new EmptyBorder(3, 6, 3, 6));
        eraBadge.setFont(new Font("Dialog", Font.BOLD, 10));
        profile.add(eraBadge);
        profile.add(randomVersionButton);
        memoryBox.setSelectedItem("Comfort 1024MB");
        memoryBox.setPreferredSize(new Dimension(126, 24));
        memoryBox.setEditable(true);
        memoryBox.setFont(new Font("Dialog", Font.PLAIN, 11));
        profile.add(memoryBox);
        profile.add(playOfflineButton);
        topRow.add(profile, BorderLayout.WEST);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.setOpaque(false);
        playOnlineButton.setPreferredSize(new Dimension(250, 34));
        center.add(playOnlineButton);
        topRow.add(center, BorderLayout.CENTER);
        footer.add(topRow, BorderLayout.NORTH);

        JPanel account = new JPanel(new BorderLayout(8, 0));
        account.setOpaque(false);
        account.setPreferredSize(new Dimension(1, 68));
        skinLabel.setPreferredSize(new Dimension(42, 62));
        skinLabel.setHorizontalAlignment(SwingConstants.CENTER);
        skinLabel.setVerticalAlignment(SwingConstants.CENTER);
        skinLabel.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170)));
        account.add(skinLabel, BorderLayout.WEST);
        JPanel text = new JPanel(new GridLayout(0, 1));
        text.setOpaque(false);
        welcomeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        lastPlayedLabel.setHorizontalAlignment(SwingConstants.LEFT);
        lastPlayedLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        versionStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        versionStatusLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        javaStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        javaStatusLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        text.add(welcomeLabel);
        text.add(lastPlayedLabel);
        text.add(versionStatusLabel);
        text.add(javaStatusLabel);
        text.add(statusLabel);
        account.add(text, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        buttons.setOpaque(false);
        compactNewsBox.setOpaque(false);
        compactNewsBox.setFont(new Font("Dialog", Font.PLAIN, 11));
        buttons.add(compactNewsBox);
        redownloadButton.setVisible(false);
        buttons.add(redownloadButton);
        buttons.add(loginButton);
        buttons.add(signOutButton);
        account.add(buttons, BorderLayout.EAST);
        footer.add(account, BorderLayout.CENTER);

        return footer;
    }

    private void wireActions() {
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doMicrosoftLogin(false);
            }
        });
        playOnlineButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentProfile == null) {
                    doMicrosoftLogin(true);
                } else {
                    launch(currentProfile);
                }
            }
        });
        playOfflineButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AuthProfile offline = AuthProfile.offline(offlineName.getText());
                setOfflineHead();
                status("Launching " + selectedVersion() + " offline as " + offline.name + "...");
                launch(offline);
            }
        });
        randomVersionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pickRandomVersion();
            }
        });
        redownloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                forceRedownloadSelectedVersion();
            }
        });
        signOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    tokenCache.clear();
                    currentProfile = null;
                    playOnlineButton.setEnabled(false);
                    welcomeLabel.setText("Welcome, guest");
                    setOfflineHead();
                    status("Forgot cached OAuth tokens.");
                } catch (IOException ex) {
                    showError(ex);
                }
            }
        });
        compactNewsBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveSettingsOnly();
                refreshActiveTab();
            }
        });
        memoryBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseCustomMemoryIfNeeded();
            }
        });
    }

    private void chooseCustomMemoryIfNeeded() {
        Object selected = memoryBox.getSelectedItem();
        if (selected == null || !"Custom...".equals(String.valueOf(selected))) {
            return;
        }
        String current = BetaLauncher.memoryPreview(settings.get("memory", "Comfort 1024MB"));
        String input = JOptionPane.showInputDialog(this,
                "Enter memory to allocate in MB.\nExample: 1536, 4096, 8192\nOnly choose what your PC can comfortably spare.",
                current);
        if (input == null || input.trim().length() == 0) {
            memoryBox.setSelectedItem(settings.get("memory", "Comfort 1024MB"));
            return;
        }
        String digits = input.replaceAll("[^0-9]", "");
        if (digits.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a number of megabytes.", "Custom memory", JOptionPane.WARNING_MESSAGE);
            memoryBox.setSelectedItem(settings.get("memory", "Comfort 1024MB"));
            return;
        }
        memoryBox.setSelectedItem(digits + "MB Custom");
        saveSettingsOnly();
    }

    private void handleLauncherAction(String action) {
        if ("launcher:backup-saves".equals(action)) {
            backupSaves();
        } else if ("launcher:import-texture-pack".equals(action)) {
            importTexturePack();
        }
    }

    private void backupSaves() {
        try {
            java.io.File savesDir = new java.io.File(TokenCache.minecraftDir(), "saves");
            if (!savesDir.exists() || !savesDir.isDirectory()) {
                JOptionPane.showMessageDialog(this, "No saves folder was found yet.", "Backup Saves", JOptionPane.INFORMATION_MESSAGE);
                status("No saves folder found to back up.");
                return;
            }
            java.io.File backupDir = new java.io.File(new java.io.File(TokenCache.minecraftDir(), "launcher_revive"), "backups");
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                throw new IOException("Could not create " + backupDir.getAbsolutePath());
            }
            String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            java.io.File zipFile = new java.io.File(backupDir, "saves-" + stamp + ".zip");
            java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zipFile));
            try {
                zipFolder(savesDir, savesDir, zip);
            } finally {
                zip.close();
            }
            status("Backed up saves to " + zipFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Saves backed up to:\n" + zipFile.getAbsolutePath(), "Backup Saves", JOptionPane.INFORMATION_MESSAGE);
            refreshActiveTab();
        } catch (Exception ex) {
            showError(ex instanceof Exception ? (Exception) ex : new IOException(ex.toString()));
        }
    }

    private void importTexturePack() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Import texture pack .zip");
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(java.io.File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".zip");
            }

            public String getDescription() {
                return "Texture pack zip files (*.zip)";
            }
        });
        if (chooser.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File source = chooser.getSelectedFile();
        if (source == null) {
            return;
        }
        if (!source.getName().toLowerCase().endsWith(".zip")) {
            JOptionPane.showMessageDialog(this, "Old texture packs should be .zip files.", "Import Texture Pack", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            java.io.File textureDir = new java.io.File(TokenCache.minecraftDir(), "texturepacks");
            if (!textureDir.exists() && !textureDir.mkdirs()) {
                throw new IOException("Could not create " + textureDir.getAbsolutePath());
            }
            java.io.File target = new java.io.File(textureDir, source.getName());
            if (target.exists()) {
                int choice = JOptionPane.showConfirmDialog(this, "Replace existing texture pack?\n" + target.getName(), "Import Texture Pack", JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            copyFile(source, target);
            status("Imported texture pack: " + target.getName());
            JOptionPane.showMessageDialog(this, "Texture pack imported to:\n" + target.getAbsolutePath(), "Import Texture Pack", JOptionPane.INFORMATION_MESSAGE);
            refreshActiveTab();
        } catch (Exception ex) {
            showError(ex instanceof Exception ? (Exception) ex : new IOException(ex.toString()));
        }
    }

    private static void zipFolder(java.io.File root, java.io.File file, java.util.zip.ZipOutputStream zip) throws IOException {
        String name = root.toURI().relativize(file.toURI()).getPath();
        if (file.isDirectory()) {
            if (name.length() > 0) {
                zip.putNextEntry(new java.util.zip.ZipEntry(name.endsWith("/") ? name : name + "/"));
                zip.closeEntry();
            }
            java.io.File[] children = file.listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    zipFolder(root, children[i], zip);
                }
            }
            return;
        }
        zip.putNextEntry(new java.util.zip.ZipEntry(name));
        java.io.FileInputStream in = new java.io.FileInputStream(file);
        try {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                zip.write(buffer, 0, read);
            }
        } finally {
            in.close();
            zip.closeEntry();
        }
    }

    private static void copyFile(java.io.File source, java.io.File target) throws IOException {
        java.io.FileInputStream in = new java.io.FileInputStream(source);
        java.io.FileOutputStream out = new java.io.FileOutputStream(target);
        try {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            out.close();
            in.close();
        }
    }

    private void doMicrosoftLogin(final boolean launchAfterLogin) {
        setBusy(true);
        status("Starting Microsoft browser login...");
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    ModernAuth auth = new ModernAuth(tokenCache, new SwingStatus());
                    final AuthProfile profile = auth.login();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            currentProfile = profile;
                            offlineName.setText(profile.name);
                            playOnlineButton.setEnabled(true);
                            updateWelcome(profile);
                            loadSkinPreview(profile);
                            setBusy(false);
                            status("Ready to play Minecraft " + selectedVersion());
                            if (launchAfterLogin) {
                                launch(profile);
                            }
                        }
                    });
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setBusy(false);
                            showError(ex);
                        }
                    });
                }
            }
        }, "Microsoft Login");
        worker.setDaemon(true);
        worker.start();
    }

    private void launch(final AuthProfile profile) {
        setBusy(true);
        saveLastPlayed(profile);
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    new BetaLauncher(selectedVersion(), selectedMemoryMegabytes(), new SwingStatus()).launch(profile);
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            showError(ex);
                        }
                    });
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setBusy(false);
                        }
                    });
                }
            }
        }, "Minecraft Launch");
        worker.setDaemon(true);
        worker.start();
    }

    private void setBusy(boolean busy) {
        setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        loginButton.setEnabled(!busy);
        playOfflineButton.setEnabled(!busy);
        randomVersionButton.setEnabled(!busy);
        redownloadButton.setEnabled(!busy);
        playOnlineButton.setEnabled(!busy && currentProfile != null);
        signOutButton.setEnabled(!busy);
    }

    private void status(String message) {
        statusLabel.setText(message);
        appendLog(message);
        updateRedownloadVisibility();
    }

    private void updateJavaStatusLabel() {
        String warning = javaWarningText();
        if (warning.length() == 0) {
            javaStatusLabel.setText("Java: " + javaRuntimeShort() + " ready");
            javaStatusLabel.setForeground(new Color(45, 95, 45));
        } else {
            javaStatusLabel.setText("Java: " + javaRuntimeShort() + " - Java 8 recommended");
            javaStatusLabel.setForeground(new Color(135, 80, 20));
        }
        javaStatusLabel.setToolTipText(warning.length() == 0 ? "Java runtime looks good for the revived launcher." : warning);
    }

    private void warnIfJavaUnusual() {
        String warning = javaWarningText();
        if (warning.length() > 0) {
            appendLog("JAVA WARNING: " + warning + " Current runtime: " + javaRuntimeSummary() + ".");
        }
    }

    private static String javaRuntimeSummary() {
        String version = System.getProperty("java.version", "unknown");
        String vendor = System.getProperty("java.vendor", "Java");
        return version + " (" + vendor + ")";
    }

    private static String javaRuntimeShort() {
        return System.getProperty("java.version", "unknown");
    }

    private static String javaWarningText() {
        int major = javaMajorVersion();
        if (major == 0) {
            return "Could not detect Java version. If old Minecraft refuses to start, try Java 8.";
        }
        if (major < 8) {
            return "This launcher and modern Microsoft login expect Java 8 or newer.";
        }
        if (major > 8) {
            return "Old Beta/Alpha Minecraft is happiest on Java 8. If launch fails or the window closes, run this launcher with Java 8.";
        }
        return "";
    }

    private static int javaMajorVersion() {
        String version = System.getProperty("java.specification.version", "");
        try {
            if (version.startsWith("1.")) {
                return Integer.parseInt(version.substring(2));
            }
            int dot = version.indexOf('.');
            return Integer.parseInt(dot >= 0 ? version.substring(0, dot) : version);
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateWelcome(AuthProfile profile) {
        welcomeLabel.setText("Welcome, " + profile.name);
    }

    private void wireTab(final TabLabel label, final String tab) {
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                switchTab(tab);
            }
        });
    }

    private void switchTab(String tab) {
        activeTab = tab;
        updateTab.setActive("notes".equals(tab));
        logTab.setActive("log".equals(tab));
        profileTab.setActive("profile".equals(tab));
        refreshActiveTab();
    }

    private void refreshActiveTab() {
        if ("log".equals(activeTab)) {
            setNewsHtml(logPage());
        } else if ("profile".equals(activeTab)) {
            setNewsHtml(profilePage());
        } else {
            setNewsHtml(VersionNotes.page(selectedVersion(), compactNewsBox.isSelected(), false));
        }
    }

    private void appendLog(String message) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        launcherLog.append("[").append(time).append("] ").append(message).append("\n");
        if (launcherLog.length() > 16000) {
            launcherLog.delete(0, launcherLog.length() - 16000);
        }
        if ("log".equals(activeTab) && news != null) {
            setNewsHtml(logPage());
        }
    }

    private void loadSavedSettings() {
        offlineName.setText(settings.get("profile.name", offlineName.getText()));
        versionBox.getEditor().setItem(settings.get("version", BetaLauncher.DEFAULT_VERSION));
        String memory = settings.get("memory", "Comfort 1024MB");
        memoryBox.setSelectedItem(memory);
        compactNewsBox.setSelected(settings.getBoolean("compactNews", false));
        updateLastPlayedLabel();
        updateEraBadge();
    }

    private void saveSettingsOnly() {
        settings.put("profile.name", offlineName.getText());
        settings.put("version", selectedVersion());
        settings.put("memory", String.valueOf(memoryBox.getSelectedItem()));
        settings.putBoolean("compactNews", compactNewsBox.isSelected());
        try {
            settings.save();
        } catch (IOException e) {
            appendLog("Could not save launcher settings: " + e.getMessage());
        }
    }

    private void saveLastPlayed(AuthProfile profile) {
        settings.put("profile.name", profile.name);
        settings.put("version", selectedVersion());
        settings.put("memory", String.valueOf(memoryBox.getSelectedItem()));
        settings.put("last.name", profile.name);
        settings.put("last.version", selectedVersion());
        settings.put("last.mode", profile.online ? "online" : "offline");
        settings.put("last.time", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        settings.putBoolean("compactNews", compactNewsBox.isSelected());
        try {
            settings.save();
        } catch (IOException e) {
            appendLog("Could not save last played: " + e.getMessage());
        }
        updateLastPlayedLabel();
    }

    private void updateLastPlayedLabel() {
        String version = settings.get("last.version", "");
        String name = settings.get("last.name", "");
        if (version.length() == 0 || name.length() == 0) {
            lastPlayedLabel.setText("Last played: never");
        } else {
            lastPlayedLabel.setText("Last: " + version + " as " + name);
        }
    }

    private void maybeShowFirstRunWelcome() {
        if (settings.getBoolean("firstRunSeen", false)) {
            return;
        }
        switchTab("notes");
        setNewsHtml("<html><body text='#e8e8e8' link='#aaaaff' vlink='#aaaaff' style='font-family:Verdana,Arial,sans-serif;font-size:11px;margin:24px;background-color:transparent'>"
                + "<font size='+3'><b>Welcome to MCLauncherRevive</b></font><br><br>"
                + "<p>This is a 2011-flavored launcher shell with modern Minecraft auth behind the curtain.</p>"
                + "<p><b>Quick start:</b><br>"
                + "+ Use <b>Microsoft Login</b> for online profile auth.<br>"
                + "+ Use <b>Play Offline</b> for singleplayer without logging in.<br>"
                + "+ Pick old versions from Beta 1.8.x down through Alpha, Infdev, Classic, and Pre-Classic.<br>"
                + "+ Press <b>Random</b> when you want the launcher to choose chaos.<br>"
                + "+ Use <b>Patch Notes Mode!</b>, <b>Launcher Log</b>, and <b>Profile Editor</b> for useful details.</p>"
                + "<p><font color='#ffff55'><b>Vibe-Coded with Codex.</b></font></p>"
                + "</body></html>");
        settings.putBoolean("firstRunSeen", true);
        try {
            settings.save();
        } catch (IOException e) {
            appendLog("Could not save first-run state: " + e.getMessage());
        }
    }

    private void updateEraBadge() {
        String era = eraName(selectedVersion());
        eraBadge.setText(era);
        if ("beta".equals(era)) {
            eraBadge.setBackground(new Color(70, 95, 150));
        } else if ("alpha".equals(era)) {
            eraBadge.setBackground(new Color(88, 130, 70));
        } else if ("infdev".equals(era)) {
            eraBadge.setBackground(new Color(135, 96, 45));
        } else if ("classic".equals(era)) {
            eraBadge.setBackground(new Color(105, 105, 105));
        } else {
            eraBadge.setBackground(new Color(120, 70, 70));
        }
        updateRedownloadVisibility();
    }

    private void updateRedownloadVisibility() {
        if (redownloadButton == null || versionBox == null) {
            return;
        }
        java.io.File versionDir = new java.io.File(new java.io.File(TokenCache.minecraftDir(), "versions"), selectedVersion());
        boolean downloaded = versionDir.exists();
        redownloadButton.setVisible(downloaded);
        if (versionStatusLabel != null) {
            versionStatusLabel.setText("Version files: " + (downloaded ? "downloaded" : "not downloaded yet"));
            versionStatusLabel.setForeground(downloaded ? new Color(45, 95, 45) : new Color(90, 90, 90));
        }
        versionBox.setToolTipText(downloaded ? "Selected version is downloaded." : "Selected version will download on launch.");
    }

    private void pickRandomVersion() {
        int count = versionBox.getItemCount();
        if (count <= 0) {
            status("Version list is not loaded yet.");
            return;
        }
        String version = versionBox.getItemAt(random.nextInt(count));
        versionBox.setSelectedItem(version);
        versionBox.getEditor().setItem(version);
        status("Random classic version selected: " + version);
        refreshActiveTab();
    }

    private void forceRedownloadSelectedVersion() {
        String version = selectedVersion();
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete and re-download only this selected version?\n\n" + version + "\n\nYour saves and auth tokens will not be touched.",
                "Force Redownload",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        java.io.File versionDir = new java.io.File(new java.io.File(TokenCache.minecraftDir(), "versions"), version);
        try {
            deleteRecursive(versionDir);
            status("Deleted selected version folder for re-download: " + version);
            appendLog("Next launch will re-download " + version + ".");
        } catch (IOException e) {
            showError(e);
        }
    }

    private void deleteRecursive(java.io.File file) throws IOException {
        if (file == null || !file.exists()) {
            return;
        }
        java.io.File root = new java.io.File(TokenCache.minecraftDir(), "versions").getCanonicalFile();
        java.io.File target = file.getCanonicalFile();
        if (!target.getPath().startsWith(root.getPath())) {
            throw new IOException("Refusing to delete outside versions folder: " + target.getAbsolutePath());
        }
        if (file.isDirectory()) {
            java.io.File[] children = file.listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    deleteRecursive(children[i]);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("Could not delete " + file.getAbsolutePath());
        }
    }

    private void refillVersionBox(List<String> versions, String selected) {
        versionBox.removeAllItems();
        for (String version : versions) {
            versionBox.addItem(version);
        }
        versionBox.setSelectedItem(selected);
        versionBox.getEditor().setItem(selected);
        updateRedownloadVisibility();
    }

    private void setOfflineHead() {
        try {
            Image image = ImageIO.read(MinecraftLauncher.class.getResource("/net/minecraft/StevePlaceholder.jpg"));
            if (image != null) {
                skinLabel.setIcon(new ImageIcon(image.getScaledInstance(40, 40, Image.SCALE_FAST)));
                return;
            }
        } catch (Exception ignored) {
        }
        BufferedImage head = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics g = head.getGraphics();
        g.setColor(new Color(184, 145, 104));
        g.fillRect(4, 4, 32, 32);
        g.dispose();
        skinLabel.setIcon(new ImageIcon(head));
    }

    private String launchPreview() {
        String mode = currentProfile == null ? "Offline or login-on-play" : "Online as " + currentProfile.name;
        String memory = BetaLauncher.memoryPreview(selectedMemoryMegabytes()) + " MB";
        return "javaw -Xmx" + memory + " -Djava.library.path=<selected version natives> -cp <libraries + " + selectedVersion() + ".jar> <main class> <safe auth/session args hidden>\n"
                + "Mode: " + mode + "\n"
                + "Version: " + selectedVersion() + "\n"
                + "Memory: " + memory + "\n"
                + "Game dir: " + TokenCache.minecraftDir().getAbsolutePath();
    }

    private String eraName(String version) {
        if (version.startsWith("b")) {
            return "beta";
        }
        if (version.startsWith("a")) {
            return "alpha";
        }
        if (version.startsWith("inf")) {
            return "infdev";
        }
        if (version.startsWith("c")) {
            return "classic";
        }
        if (version.startsWith("rd")) {
            return "pre-classic";
        }
        return "classic";
    }

    private void loadSkinPreview(final AuthProfile profile) {
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    final ImageIcon icon = fetchSkinHead(profile.uuid);
                    if (icon != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                skinLabel.setIcon(icon);
                            }
                        });
                    }
                } catch (Exception e) {
                    appendLog("Skin preview unavailable: " + e.getMessage());
                }
            }
        }, "Skin Preview Loader");
        worker.setDaemon(true);
        worker.start();
    }

    private ImageIcon fetchSkinHead(String uuid) throws IOException {
        if (uuid == null || uuid.length() == 0) {
            return null;
        }
        String profileJson = BetaLauncher.downloadStringForLauncher("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
        Map<String, Object> object = Json.object(Json.parse(profileJson));
        List<Object> properties = Json.array(object.get("properties"));
        if (properties == null) {
            return null;
        }
        for (Object propertyValue : properties) {
            Map<String, Object> property = Json.object(propertyValue);
            if (!"textures".equals(Json.string(property, "name"))) {
                continue;
            }
            String encoded = Json.string(property, "value");
            if (encoded == null) {
                continue;
            }
            String decoded = new String(Base64.getDecoder().decode(encoded), "UTF-8");
            Map<String, Object> texturesRoot = Json.object(Json.parse(decoded));
            Map<String, Object> textures = Json.object(texturesRoot.get("textures"));
            Map<String, Object> skin = Json.object(textures == null ? null : textures.get("SKIN"));
            String url = Json.string(skin, "url");
            if (url == null || url.length() == 0) {
                return null;
            }
            BufferedImage image = ImageIO.read(new URL(url));
            if (image == null || image.getWidth() < 16 || image.getHeight() < 16) {
                return null;
            }
            BufferedImage head = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics g = head.getGraphics();
            try {
                g.drawImage(image, 0, 0, 32, 32, 8, 8, 16, 16, null);
                if (image.getWidth() >= 64 && image.getHeight() >= 32) {
                    g.drawImage(image, 0, 0, 32, 32, 40, 8, 48, 16, null);
                }
            } finally {
                g.dispose();
            }
            return new ImageIcon(head.getScaledInstance(40, 40, Image.SCALE_FAST));
        }
        return null;
    }

    private String selectedVersion() {
        Object value = versionBox.isEditable() ? versionBox.getEditor().getItem() : versionBox.getSelectedItem();
        String text = value == null ? "" : value.toString().trim();
        return text.length() == 0 ? BetaLauncher.DEFAULT_VERSION : text;
    }

    private String selectedMemoryMegabytes() {
        Object value = memoryBox.getSelectedItem();
        String text = value == null ? "" : value.toString();
        if (text.indexOf("512") >= 0) {
            return "512";
        }
        if (text.indexOf("2048") >= 0) {
            return "2048";
        }
        return "1024";
    }

    private void loadVersionsAsync() {
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    final List<String> versions = BetaLauncher.loadLegacyVersions();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            Object current = versionBox.getEditor().getItem();
                            allVersions = new java.util.ArrayList<String>(versions);
                            versionBox.removeAllItems();
                            for (String version : versions) {
                                versionBox.addItem(version);
                            }
                            if (current != null && current.toString().trim().length() > 0) {
                                versionBox.getEditor().setItem(current);
                            } else {
                                versionBox.setSelectedItem(BetaLauncher.DEFAULT_VERSION);
                            }
                            refreshActiveTab();
                            status("Loaded classic versions through Beta 1.8.x");
                        }
                    });
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            status("Version list unavailable; type a version id manually");
                        }
                    });
                }
            }
        }, "Version Manifest Loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void showError(Exception ex) {
        String message = ex.getMessage() == null ? ex.toString() : ex.getMessage();
        status("Launcher error");
        appendLog("ERROR: " + message);
        setNewsHtml(errorNews(message));
    }

    private void setNewsHtml(String html) {
        news.setText(html);
        news.setCaretPosition(0);
    }

    private JEditorPane htmlPane(String html) {
        JEditorPane pane = new JEditorPane();
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setContentType("text/html");
        pane.setText(html);
        pane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && e.getURL() != null) {
                    browse(e.getURL().toString());
                }
            }
        });
        return pane;
    }

    private static String defaultNews() {
        return "<html><body text='#e8e8e8' link='#aaaaff' vlink='#aaaaff' style='font-family:Verdana,Arial,sans-serif;font-size:11px;margin:24px;background-color:transparent'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'><tr>"
                + "<td><font size='+3'><b>Minecraft News</b></font></td>"
                + "<td align='right'></td>"
                + "</tr></table>"
                + "<br><br>"

                + "<p><a href='https://www.minecraft.net/'><b>MCLauncherRevive Modern Released</b></a></p>"
                + "<p>Surprise! The old launcher window has stumbled out of a 2011 hard drive and learned a new trick: "
                + "Microsoft OAuth, Xbox Live, XSTS, Minecraft services, and profile lookup, all without asking for a raw password.</p>"
                + "<p>+ Added browser-based Microsoft login<br>"
                + "+ Added local token caching with a Forget Login button<br>"
                + "+ Added offline singleplayer for rainy afternoons<br>"
                + "+ Changed the news panel to look like it still believes Tumblr is powering half the internet</p>"
                + "<p>The update is available in this folder, enjoy!</p>"
                + "<p>Happy mining from the MCLauncherRevive team.</p>"

                + "<br><p><a href='https://www.minecraft.net/'><b>Minecraft Beta 1.8 and older - The Archive Update</b></a></p>"
                + "<p>New mobs and new places to put your precious items:</p>"
                + "<p>+ Added old-school terrain vibes<br>"
                + "+ Added a version selector for Beta 1.8.x down through the earliest classic builds<br>"
                + "+ Added official Mojang client download metadata<br>"
                + "+ Added LWJGL natives that do not pretend to be normal jars<br>"
                + "+ Added the sacred pre-hunger gameplay loop<br>"
                + "+ Removed Herobrine from the OAuth redirect, probably</p>"

                + "<br><p><a href='https://www.minecraft.net/'><b>Humble Blockjam Bundle</b></a></p>"
                + "<table cellpadding='6' cellspacing='0' bgcolor='#0f0f0f' style='border:1px solid #555555'><tr>"
                + "<td><img src='" + blockImageUrl() + "' width='72' height='72'></td>"
                + "<td><font color='#eeeeee'><b>HUMBLE BLOCKJAM BUNDLE</b><br>"
                + "Get 5 pretend indie games, 12 fake capes, and one emotionally important launcher window.</font></td>"
                + "</tr></table>"

                + "<br><hr color='#333333'>"
                + "<p><font color='#888888'>MCLauncherRevive Modern. Vibe-Coded with Codex. "
                + "Not affiliated with Mojang or Microsoft. Fake articles, real nostalgia.</font></p>"
                + "</body></html>";
    }

    private static String sidebarHtml() {
        return "<html><body text='#eeeeee' link='#aaaaff' vlink='#aaaaff' style='font-family:Verdana,Arial,sans-serif;font-size:10px;margin:18px;background-color:transparent'>"
                + "<font size='+2'><b>Official links:</b></font><br><br>"
                + "<a href='https://www.minecraft.net/'>Minecraft.net</a><br>"
                + "<a href='https://www.minecraft.net/realms'>Minecraft Realms</a><br>"
                + "<a href='https://www.facebook.com/minecraft'>Minecraft on Facebook</a><br>"
                + "<a href='https://minecraftshop.com/'>Merchandise</a><br><br>"
                + "<a href='https://bugs.mojang.com/'>Bug tracker</a><br>"
                + "<a href='https://help.minecraft.net/'>Account Support</a><br><br>"
                + "<a href='https://twitter.com/Minecraft'>Mojang on Twitter</a><br>"
                + "<a href='https://twitter.com/MojangSupport'>Support on Twitter</a><br><br>"

                + "<font size='+1'><b>Try our other games!</b></font><br><br>"
                + "<center><a href='https://www.minecraft.net/en-us/article/scrolls-now-free'><img src='" + scrollsLogoUrl() + "' width='150' height='44' border='0'></a><br>"
                + "<a href='https://playcobalt.com/'><img src='" + cobaltLogoUrl() + "' width='130' height='38' border='0'></a></center><br>"

                + "<font size='+1'><b>Community links:</b></font><br>"
                + "<a href='https://www.minecraftforum.net/'>Minecraft Forums</a><br>"
                + "<a href='https://minecraft.wiki/'>Minecraft Wiki</a><br><br>"

                + "<hr color='#333333'>"
                + "<font color='#888888'>Modern twist:</font><br>"
                + "<font color='#cccccc'>OAuth backstage.<br>Old vibes up front.</font><br><br>"
                + "</body></html>";
    }

    private String logPage() {
        java.io.File launchLog = new java.io.File(new java.io.File(new java.io.File(TokenCache.minecraftDir(), "launcher_revive"), "logs"), "last-launch.log");
        return "<html><body text='#e8e8e8' link='#aaaaff' vlink='#aaaaff' style='font-family:Verdana,Arial,sans-serif;font-size:11px;margin:24px;background-color:transparent'>"
                + "<font size='+3'><b>Launcher Log</b></font><br><br>"
                + "<p><font color='#999999'>Current launcher session messages. Game output is also written to disk after Minecraft starts.</font></p>"
                + "<p><b>Selected version:</b> " + escape(selectedVersion()) + "<br>"
                + "<b>Java runtime:</b> " + escape(javaRuntimeSummary()) + "<br>"
                + "<b>Java safety:</b> " + escape(javaWarningText().length() == 0 ? "Looks good. Java 8 detected." : javaWarningText()) + "<br>"
                + "<b>Minecraft folder:</b> " + escape(TokenCache.minecraftDir().getAbsolutePath()) + "<br>"
                + "<b>Auth cache:</b> " + escape(tokenCache.configFile().getAbsolutePath()) + "<br>"
                + "<b>Game launch log:</b> " + escape(launchLog.getAbsolutePath()) + "</p>"
                + "<table width='100%' cellpadding='8' cellspacing='0' bgcolor='#0d0d0d' style='border:1px solid #444444'><tr><td>"
                + "<pre style='font-family:Consolas,Monospaced;font-size:11px;color:#dddddd;white-space:pre-wrap'>" + escape(launcherLog.toString()) + "</pre>"
                + "</td></tr></table>"
                + "<br><p><b>Launch arguments preview:</b></p>"
                + "<table width='100%' cellpadding='8' cellspacing='0' bgcolor='#0d0d0d' style='border:1px solid #444444'><tr><td>"
                + "<pre style='font-family:Consolas,Monospaced;font-size:11px;color:#dddddd;white-space:pre-wrap'>" + escape(launchPreview()) + "</pre>"
                + "</td></tr></table>"
                + "</body></html>";
    }

    private String profilePage() {
        String mode = currentProfile == null ? "No Microsoft profile loaded for this session" : "Logged in as " + currentProfile.name;
        String uuid = currentProfile == null ? "(not loaded)" : currentProfile.uuid;
        String tokenState = tokenCache.hasRefreshToken() ? "OAuth refresh token cached" : "No cached Microsoft refresh token";
        String minecraftDir = TokenCache.minecraftDir().getAbsolutePath();
        String savesDir = new java.io.File(TokenCache.minecraftDir(), "saves").getAbsolutePath();
        String modsDir = new java.io.File(TokenCache.minecraftDir(), "mods").getAbsolutePath();
        String texturePacksDir = new java.io.File(TokenCache.minecraftDir(), "texturepacks").getAbsolutePath();
        String backupsDir = new java.io.File(new java.io.File(TokenCache.minecraftDir(), "launcher_revive"), "backups").getAbsolutePath();
        String selectedVersionDir = new java.io.File(new java.io.File(TokenCache.minecraftDir(), "versions"), selectedVersion()).getAbsolutePath();
        String lastPlayed = settings.get("last.version", "(never)") + " as " + settings.get("last.name", "(nobody)");
        String javaWarning = javaWarningText();
        return "<html><body text='#e8e8e8' link='#aaaaff' vlink='#aaaaff' style='font-family:Verdana,Arial,sans-serif;font-size:11px;margin:24px;background-color:transparent'>"
                + "<font size='+3'><b>Profile Editor</b></font><br><br>"
                + "<p><font color='#999999'>Classic launcher-style profile controls, focused on the settings this revived launcher actually uses.</font></p>"
                + "<table cellpadding='6' cellspacing='0' bgcolor='#0d0d0d' style='border:1px solid #444444'>"
                + "<tr><td><b>Offline name</b></td><td>" + escape(offlineName.getText()) + "</td></tr>"
                + "<tr><td><b>Selected version</b></td><td>" + escape(selectedVersion()) + "</td></tr>"
                + "<tr><td><b>Memory preset</b></td><td>" + escape(String.valueOf(memoryBox.getSelectedItem())) + "</td></tr>"
                + "<tr><td><b>Java runtime</b></td><td>" + escape(javaRuntimeSummary()) + "</td></tr>"
                + "<tr><td><b>Java safety</b></td><td>" + escape(javaWarning.length() == 0 ? "Looks good. Java 8 detected." : javaWarning) + "</td></tr>"
                + "<tr><td><b>Era</b></td><td>" + escape(eraName(selectedVersion())) + "</td></tr>"
                + "<tr><td><b>Last played</b></td><td>" + escape(lastPlayed) + "</td></tr>"
                + "<tr><td><b>Microsoft profile</b></td><td>" + escape(mode) + "</td></tr>"
                + "<tr><td><b>UUID</b></td><td>" + escape(uuid) + "</td></tr>"
                + "<tr><td><b>Token cache</b></td><td>" + escape(tokenState) + "</td></tr>"
                + "<tr><td><b>Cache file</b></td><td>" + escape(tokenCache.configFile().getAbsolutePath()) + "</td></tr>"
                + "<tr><td><b>Settings file</b></td><td>" + escape(settings.file().getAbsolutePath()) + "</td></tr>"
                + "</table>"
                + "<br><p><b>Folder shortcuts:</b><br>"
                + "<a href='" + fileUrl(minecraftDir) + "'>Open .minecraft folder</a><br>"
                + "<a href='" + fileUrl(savesDir) + "'>Open saves folder</a><br>"
                + "<a href='" + fileUrl(modsDir) + "'>Open mods folder</a><br>"
                + "<a href='" + fileUrl(texturePacksDir) + "'>Open texturepacks folder</a><br>"
                + "<a href='" + fileUrl(backupsDir) + "'>Open backups folder</a><br>"
                + "<a href='" + fileUrl(selectedVersionDir) + "'>Open selected version folder</a></p>"
                + "<p><b>Maintenance:</b><br>"
                + "<a href='launcher:backup-saves'>Backup saves now</a><br>"
                + "<a href='launcher:import-texture-pack'>Import texture pack .zip</a><br>"
                + "Use <b>Redownload Version</b> in the bottom-right to delete only the selected version folder and fetch it again on next launch.</p>"
                + "<p><b>Launch arguments preview:</b></p>"
                + "<table width='100%' cellpadding='8' cellspacing='0' bgcolor='#0d0d0d' style='border:1px solid #444444'><tr><td>"
                + "<pre style='font-family:Consolas,Monospaced;font-size:11px;color:#dddddd;white-space:pre-wrap'>" + escape(launchPreview()) + "</pre>"
                + "</td></tr></table>"
                + "<br><p><b>Useful bits:</b><br>"
                + "+ Change the offline/profile name in the bottom-left Profile field.<br>"
                + "+ Pick or type an old version in the Version field.<br>"
                + "+ Pick a memory preset in the bottom bar.<br>"
                + "+ Toggle Patch Notes Mode! to switch the news feed into concise patch-style notes.<br>"
                + "+ Use Microsoft Login to refresh the online profile.<br>"
                + "+ Use Forget Login to clear cached OAuth tokens.</p>"
                + "<p><font color='#888888'>This keeps the old Profile Editor tab feeling, but avoids pretending to support complex profile features that the revived launcher does not need.</font></p>"
                + "</body></html>";
    }

    private static String errorNews(String message) {
        return "<html><body text='#eeeeee' link='#aaaaff' style='font-family:Verdana,Arial,sans-serif;font-size:11px;margin:24px;background-color:transparent'>"
                + "<font size='+3'><b>Launcher error</b></font><br><br>"
                + "<p><font color='#ff9999'>" + escape(message) + "</font></p>"
                + "<p>Offline singleplayer mode is still available from the bottom bar.</p>"
                + "<hr color='#333333'>"
                + "<p><font color='#888888'>Tip from the fake newsroom: if online login gets weird, Forget Login and try again.</font></p>"
                + "</body></html>";
    }

    private static String fileUrl(String path) {
        try {
            java.io.File file = new java.io.File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.toURI().toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String blockImageUrl() {
        try {
            return MinecraftLauncher.class.getResource("/net/minecraft/Block.png").toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String scrollsLogoUrl() {
        try {
            return MinecraftLauncher.class.getResource("/net/minecraft/scrolls.png").toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String cobaltLogoUrl() {
        try {
            return MinecraftLauncher.class.getResource("/net/minecraft/cobalt.png").toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static void browse(String uri) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(uri));
            }
        } catch (Exception ignored) {
        }
    }

    private static Image loadImage(String path) {
        try {
            return ImageIO.read(MinecraftLauncher.class.getResource(path));
        } catch (Exception e) {
            return null;
        }
    }

    private final class SwingStatus implements StatusSink {
        public void status(final String message) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MinecraftLauncher.this.status(message);
                    if (message.indexOf("Microsoft") >= 0 || message.indexOf("login") >= 0 || message.indexOf("browser") >= 0) {
                        setNewsHtml("<html><body text='#e8e8e8' link='#aaaaff' style='font-family:Verdana,Arial,sans-serif;font-size:11px;margin:24px;background-color:transparent'>"
                                + "<font size='+3'><b>Microsoft Login</b></font><br><br>"
                                + "<p>" + escape(message) + "</p>"
                                + "<p>The launcher uses a Windows browser helper to catch the OAuth URL before Microsoft changes it to <b>removed=true</b>. "
                                + "If that helper cannot capture it, the launcher falls back to a paste-url prompt.</p>"
                                + "</body></html>");
                    }
                }
            });
        }

        public String ask(final String title, final String message) {
            final String[] result = new String[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        result[0] = JOptionPane.showInputDialog(MinecraftLauncher.this, message, title, JOptionPane.PLAIN_MESSAGE);
                    }
                });
            } catch (Exception e) {
                return null;
            }
            return result[0];
        }
    }

    private static final class TabLabel extends JLabel {
        TabLabel(String text, boolean selected) {
            super(text);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setActive(selected);
        }

        void setActive(boolean selected) {
            setOpaque(true);
            setFont(new Font("Dialog", Font.PLAIN, 11));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(145, 145, 145)),
                    new EmptyBorder(5, 7, 5, 7)));
            setBackground(selected ? Color.WHITE : new Color(230, 230, 230));
            setForeground(Color.BLACK);
        }
    }

    private static final class FooterButton extends JButton {
        FooterButton(String text) {
            super(text);
            setFocusPainted(false);
            setFont(new Font("Dialog", Font.PLAIN, 11));
            setMargin(new Insets(3, 8, 3, 8));
        }
    }

    private static final class PlayButton extends JButton {
        PlayButton(String text) {
            super(text);
            setFocusPainted(false);
            setFont(new Font("Dialog", Font.BOLD, 13));
            setForeground(Color.BLACK);
            setMargin(new Insets(5, 80, 5, 80));
        }

        protected void paintComponent(Graphics g) {
            if (isEnabled()) {
                GradientPaint paint = new GradientPaint(0, 0, new Color(245, 250, 255), 0, getHeight(), new Color(196, 219, 240));
                ((java.awt.Graphics2D) g).setPaint(paint);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            super.paintComponent(g);
        }
    }

    private static final class DarkNewsPanel extends JPanel {
        private final Image dirt = loadImage("/net/minecraft/dirt.png");

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (dirt != null) {
                int tileW = dirt.getWidth(this) * 2;
                int tileH = dirt.getHeight(this) * 2;
                for (int x = 0; x < getWidth(); x += tileW) {
                    for (int y = 0; y < getHeight(); y += tileH) {
                        g.drawImage(dirt, x, y, tileW, tileH, this);
                    }
                }
            } else {
                g.setColor(new Color(30, 30, 30));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setColor(new Color(0, 0, 0, 145));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}

