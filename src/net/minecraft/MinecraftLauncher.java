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
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.Timer;

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
    private final JComboBox<String> memoryBox = new JComboBox<String>(new String[] { "Potato 256MB", "Low-end 384MB", "Classic 512MB", "Comfort 1024MB", "Overkill 2048MB", "Custom..." });
    private final JComboBox<String> styleBox = new JComboBox<String>(new String[] { "Auto", "Beta", "Alpha", "Infdev", "Classic", "Pre-Classic" });
    private final JButton loginButton = new FooterButton("Microsoft Login");
    private final JButton playOnlineButton = new PlayButton("Play");
    private final JButton playOfflineButton = new FooterButton("Play Offline");
    private final JButton randomVersionButton = new FooterButton("Random");
    private final JButton signOutButton = new FooterButton("Forget Login");
    private final JButton redownloadButton = new FooterButton("Redownload Version");
    private final JCheckBox compactNewsBox = new JCheckBox("Patch Notes Mode!");
    private final JCheckBox lowEndModeBox = new JCheckBox("Low-End");
    private final TabLabel updateTab = new TabLabel("Update Notes", true);
    private final TabLabel logTab = new TabLabel("Launcher Log", false);
    private final TabLabel profileTab = new TabLabel("Profile Editor", false);
    private final JEditorPane news = new JEditorPane();
    private final DarkNewsPanel newsShell = new DarkNewsPanel();
    private final SplashLabel splashLabel = new SplashLabel();
    private final StringBuilder launcherLog = new StringBuilder();
    private final JLabel skinLabel = new JLabel();
    private final Random random = new Random();
    private List<String> allVersions = new java.util.ArrayList<String>();
    private volatile AuthProfile currentProfile;
    private String activeTab = "notes";
    private EraTheme activeTheme = EraTheme.beta();
    private JEditorPane linksPane;
    private String lastNewsHtml = "";

    public static void main(String[] args) {
        configureCompatibilityProperties();
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

    private static void configureCompatibilityProperties() {
        if (System.getProperty("https.protocols") == null) {
            System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,TLSv1");
        }
    }

    private MinecraftLauncher() {
        super("Minecraft Launcher 1.6.89-j Revival");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(854, 560));
        setIconImage(loadImage("/net/minecraft/favicon.png"));
        buildUi();
        if (settings.getBoolean("lowEndMode", false)) {
            setSize(854, 560);
        } else {
            setSize(900, 590);
        }
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
        if (xpCompatibilityMode()) {
            loginButton.setEnabled(false);
            signOutButton.setEnabled(false);
        }
        loadSavedSettings();
        updateJavaStatusLabel();
        warnIfJavaUnusual();
        setOfflineHead();
        wireActions();
        applyToolTips();
        maybeShowFirstRunWelcome();
        versionBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEraBadge();
                refreshTheme();
                refreshActiveTab();
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                updateSplashAnimation();
            }

            public void componentHidden(ComponentEvent e) {
                updateSplashAnimation();
            }
        });
        int localVersions = loadLocalVersions();
        refreshTheme();
        refreshActiveTab();
        if (xpCompatibilityMode()) {
            if (localVersions > 0) {
                status("XP mode: loaded " + localVersions + " local version(s). Use Play Offline.");
            } else {
                status("XP mode: online version list unavailable. Type a version manually or copy prepared version files from a newer PC.");
            }
            appendLog("Windows XP compatibility mode is active. Microsoft login is disabled/best-effort. Fresh downloads may fail. Offline play works best with pre-cached .minecraft files.");
        } else {
            loadVersionsAsync();
        }
        AuthProfile cached = tokenCache.cachedProfile();
        if (cached != null && !xpCompatibilityMode()) {
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
        newsShell.setLayout(new BorderLayout());
        newsShell.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(110, 110, 110)));

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
        newsShell.add(newsScroll, BorderLayout.CENTER);

        JPanel splashRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 28, 8));
        splashRow.setOpaque(false);
        splashRow.add(splashLabel);
        newsShell.add(splashRow, BorderLayout.NORTH);

        linksPane = htmlPane(sidebarHtml(activeTheme));
        linksPane.setPreferredSize(new Dimension(activeTheme.sidebarWidth, 100));
        newsShell.add(linksPane, BorderLayout.EAST);

        return newsShell;
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
        JLabel styleLabel = new JLabel("Style:");
        styleLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        profile.add(styleLabel);
        styleBox.setPreferredSize(new Dimension(92, 24));
        styleBox.setFont(new Font("Dialog", Font.PLAIN, 11));
        profile.add(styleBox);
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
        lowEndModeBox.setOpaque(false);
        lowEndModeBox.setFont(new Font("Dialog", Font.PLAIN, 11));
        buttons.add(lowEndModeBox);
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
        lowEndModeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applyLowEndMode(true);
                saveSettingsOnly();
                refreshActiveTab();
            }
        });
        styleBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveSettingsOnly();
                refreshTheme();
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

    private void applyLowEndMode(boolean resizeWindow) {
        if (!lowEndModeBox.isSelected()) {
            updateSplashAnimation();
            return;
        }
        if (!"Potato 256MB".equals(String.valueOf(memoryBox.getSelectedItem()))) {
            memoryBox.setSelectedItem("Potato 256MB");
        }
        if (!compactNewsBox.isSelected()) {
            compactNewsBox.setSelected(true);
        }
        if (resizeWindow) {
            setSize(854, 560);
            appendLog("Low-end mode active: 256MB memory, compact notes, splash animation paused.");
        }
        updateSplashAnimation();
    }

    private void handleLauncherAction(String action) {
        if ("launcher:backup-saves".equals(action)) {
            backupSaves();
        } else if ("launcher:import-texture-pack".equals(action)) {
            importTexturePack();
        } else if ("launcher:first-run-guide".equals(action)) {
            showFirstRunGuide(false);
        } else if ("launcher:xp-version-guide".equals(action)) {
            showXpVersionGuide();
        }
    }

    private void applyToolTips() {
        offlineName.setToolTipText("Offline/singleplayer profile name. Microsoft passwords never go here.");
        versionBox.setToolTipText("Choose or type a classic Minecraft version.");
        styleBox.setToolTipText("Auto follows the selected version era; manual styles override the look.");
        memoryBox.setToolTipText("Choose how much RAM the Minecraft client may use.");
        randomVersionButton.setToolTipText("Pick a random loaded classic version.");
        playOfflineButton.setToolTipText("Launch singleplayer without Microsoft login.");
        playOnlineButton.setToolTipText("Launch with the current Microsoft/Minecraft session when available.");
        compactNewsBox.setToolTipText("Switch the news panel into concise patch-style notes.");
        lowEndModeBox.setToolTipText("Old-machine bundle: 256MB RAM, compact notes, no splash animation, and a smaller window.");
        redownloadButton.setToolTipText("Delete and re-fetch only the selected version folder.");
        loginButton.setToolTipText("Sign in through browser OAuth. The launcher should never ask for your Microsoft password.");
        signOutButton.setToolTipText("Remove cached local login tokens/settings.");
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
        if (xpCompatibilityMode()) {
            status("Microsoft Login is disabled in XP offline mode.");
            setNewsHtml(errorNews("Windows XP offline mode is enabled. Use Play Offline with versions that are already downloaded. Modern Microsoft login usually needs newer TLS/browser support than XP provides."));
            return;
        }
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
        if (macOs() && !confirmMacLaunch()) {
            status("macOS launch cancelled.");
            appendLog("User cancelled launch after macOS compatibility warning.");
            return;
        }
        if (macOs()) {
            appendLog("macOS experimental launch: old LWJGL clients may open blank or fail to render.");
        }
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
                            updateRedownloadVisibility();
                        }
                    });
                }
            }
        }, "Minecraft Launch");
        worker.setDaemon(true);
        worker.start();
    }

    private void setBusy(boolean busy) {
        boolean xp = xpCompatibilityMode();
        setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        loginButton.setEnabled(!busy && !xp);
        playOfflineButton.setEnabled(!busy);
        randomVersionButton.setEnabled(!busy);
        redownloadButton.setEnabled(!busy);
        playOnlineButton.setEnabled(!busy && currentProfile != null && !xp);
        signOutButton.setEnabled(!busy && !xp);
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
            javaStatusLabel.setText("Java: " + javaRuntimeShort() + (xpCompatibilityMode() ? " - XP offline mode" : " - Java 8 recommended"));
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
        if (xpCompatibilityMode()) {
            if (major == 0) {
                return "Windows XP compatibility mode is active. Java version could not be detected; Java 7 or an XP-compatible Java 8 build is recommended.";
            }
            if (major < 7) {
                return "Windows XP compatibility mode needs Java 7 or an XP-compatible Java 8 build.";
            }
            return "Windows XP compatibility mode is active. Offline play can work, but Microsoft login and fresh downloads may fail because XP-era TLS/browser support is limited.";
        }
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

    private static String javaSafetySummary() {
        String warning = javaWarningText();
        if (warning.length() > 0) {
            return warning;
        }
        return xpCompatibilityMode() ? "XP compatibility mode active." : "Looks good. Java 8 detected.";
    }

    private static boolean xpCompatibilityMode() {
        if (Boolean.getBoolean("mclauncher.xpMode")) {
            return true;
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.indexOf("windows xp") >= 0;
    }

    private static boolean windowsOs() {
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ENGLISH);
        return os.indexOf("win") >= 0;
    }

    private static boolean macOs() {
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ENGLISH);
        return os.indexOf("mac") >= 0;
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
        refreshTheme();
        if ("log".equals(activeTab)) {
            updateSplashAnimation();
            setNewsHtml(logPage());
        } else if ("profile".equals(activeTab)) {
            updateSplashAnimation();
            setNewsHtml(profilePage());
        } else {
            splashLabel.setSplash(SplashText.forKey(selectedVersion() + ":" + compactNewsBox.isSelected()));
            updateSplashAnimation();
            setNewsHtml(VersionNotes.page(selectedVersion(), compactNewsBox.isSelected(), false, activeTheme.id));
        }
    }

    private void refreshTheme() {
        EraTheme next = EraTheme.resolve(selectedStyleMode(), selectedVersion());
        activeTheme = next;
        if (newsShell != null) {
            newsShell.setTheme(next);
        }
        if (splashLabel != null) {
            splashLabel.setTheme(next);
        }
        if (linksPane != null) {
            linksPane.setText(sidebarHtml(next));
            linksPane.setPreferredSize(new Dimension(next.sidebarWidth, 100));
        }
        setTitle(next.windowTitle);
        updateTab.setText(next.updateTabTitle);
        logTab.setText(next.logTabTitle);
        profileTab.setText(next.profileTabTitle);
        updateTabsTheme();
        updateEraBadge();
        repaint();
    }

    private void updateSplashAnimation() {
        boolean notesVisible = "notes".equals(activeTab) && !lowEndModeBox.isSelected() && isDisplayable() && isVisible();
        splashLabel.setAnimationActive(notesVisible);
        splashLabel.setVisible(notesVisible);
    }

    private void updateTabsTheme() {
        updateTab.setTheme(activeTheme);
        logTab.setTheme(activeTheme);
        profileTab.setTheme(activeTheme);
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
        styleBox.setSelectedItem(settings.get("theme.mode", "Auto"));
        compactNewsBox.setSelected(settings.getBoolean("compactNews", false));
        lowEndModeBox.setSelected(settings.getBoolean("lowEndMode", false));
        applyLowEndMode(false);
        updateLastPlayedLabel();
        updateEraBadge();
    }

    private void saveSettingsOnly() {
        settings.put("profile.name", offlineName.getText());
        settings.put("version", selectedVersion());
        settings.put("memory", String.valueOf(memoryBox.getSelectedItem()));
        settings.put("theme.mode", selectedStyleMode());
        settings.putBoolean("compactNews", compactNewsBox.isSelected());
        settings.putBoolean("lowEndMode", lowEndModeBox.isSelected());
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
        settings.put("theme.mode", selectedStyleMode());
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
        showFirstRunGuide(true);
    }

    private void showFirstRunGuide(boolean markSeen) {
        setNewsHtml(firstRunWelcomeHtml());
        if (markSeen) {
            settings.putBoolean("firstRunSeen", true);
            try {
                settings.save();
            } catch (IOException ex) {
                appendLog("Could not save first-run guide state: " + ex.getMessage());
            }
        }
    }

    private static String firstRunWelcomeHtml() {
        return "<html>"
                + "<body text='#e8e8e8' link='#aaaaff' vlink='#aaaaff'"
                + " style='font-family:Verdana,Arial,sans-serif;font-size:11px;margin:24px;background-color:transparent'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'>"
                + "<tr>"
                + "<td><font size='+3'><b>Minecraft News</b></font></td>"
                + "<td align='right'><font color='#ffff55'><b>Alpha build!</b></font></td>"
                + "</tr>"
                + "</table>"
                + "<br><br>"
                + "<p><font color='#ffff55'><b>MCLauncherRevival Alpha - The First Run Update</b></font></p>"
                + "<p>The old launcher window has been revived for nostalgia, preservation, and learning. "
                + "It still looks like it came from another era, but the account flow has been modernized where possible.</p>"
                + "<p><font color='#999999'>This is an unofficial alpha project. It is not the official Minecraft Launcher "
                + "and is not affiliated with Mojang, Microsoft, Xbox, or Minecraft.</font></p>"
                + "<p><b>+ Added:</b><br>"
                + "+ Classic version selection for old Beta, Alpha, Infdev, Classic, and Pre-Classic builds where available.<br>"
                + "+ Play Offline for singleplayer without signing in.<br>"
                + "+ Microsoft Login for online profile authentication when supported.<br>"
                + "+ Style: Auto, which lets the launcher match the selected build era.<br>"
                + "+ Low-End mode for older machines.<br>"
                + "+ Patch Notes Mode for compact historical notes.<br>"
                + "+ Launcher Log for troubleshooting.<br>"
                + "+ Profile Editor for local folders, Java status, launch settings, and maintenance shortcuts.</p>"
                + "<p><b>+ How to play:</b><br>"
                + "+ Pick a classic version.<br>"
                + "+ Type a player name.<br>"
                + "+ Press <b>Play Offline</b> for singleplayer.<br>"
                + "+ Use <b>Microsoft Login</b> only when you want online profile authentication.<br>"
                + "+ Sign-in happens in your browser. The launcher should never ask for your raw Microsoft password.</p>"
                + "<p><b>+ Changed:</b><br>"
                + "+ Low-End mode uses lighter settings for older computers.<br>"
                + "+ Patch Notes Mode keeps version history short and readable.<br>"
                + "+ Style: Auto changes the look without changing how the game launches.</p>"
                + "<p><b>+ Windows XP note:</b><br>"
                + "+ XP mode is focused on offline/classic play.<br>"
                + "+ Modern login and fresh HTTPS downloads are best-effort or unavailable on XP.<br>"
                + "+ Prepare version files on Windows 7 or newer, then copy them to XP when needed.</p>"
                + "<p><b>+ Important:</b><br>"
                + "+ Use the GitHub Releases ZIP for normal play.<br>"
                + "+ Do not use GitHub's source-code ZIP unless you are building from source.<br>"
                + "+ Alpha build. Review the source before using account features.</p>"
                + "<br>"
                + "<p>The update is available in this folder, enjoy!</p>"
                + "<p><font color='#ffff55'><b>Happy mining from MCLauncherRevival.</b></font></p>"
                + "<hr color='#333333'>"
                + "<p><font color='#888888'>MCLauncherRevival Alpha. Unofficial project. Use at your own risk.</font></p>"
                + "</body></html>";
    }

    private void showXpVersionGuide() {
        VersionReadiness readiness = selectedVersionReadiness();
        setNewsHtml(htmlStart("#e8e8e8", "#aaaaff", "Verdana,Arial,sans-serif", 11, 24)
                + "<font size='+3'><b>Preparing Minecraft versions for XP</b></font><br><br>"
                + "<p><b>Selected version:</b> " + escape(selectedVersion()) + "<br>"
                + "<b>Current local status:</b> " + escape(readiness.label) + "</p>"
                + "<p>Windows XP Offline mode runs best when the selected version is already prepared locally. "
                + "Offline means no Microsoft login; it does not remove the need for the version jar, JSON metadata, "
                + "libraries, natives, and sometimes assets.</p>"
                + "<p><b>Preferred method:</b><br>"
                + "1. On Windows 7 or newer, run MCLauncherRevival.<br>"
                + "2. Select the same classic version and click <b>Play Offline</b> once.<br>"
                + "3. Copy these folders from the newer PC:<br>"
                + "%APPDATA%\\.minecraft\\versions<br>"
                + "%APPDATA%\\.minecraft\\libraries<br>"
                + "%APPDATA%\\.minecraft\\assets<br><br>"
                + "4. Paste them on XP at:<br>"
                + "C:\\Documents and Settings\\&lt;User&gt;\\Application Data\\.minecraft\\versions<br>"
                + "C:\\Documents and Settings\\&lt;User&gt;\\Application Data\\.minecraft\\libraries<br>"
                + "C:\\Documents and Settings\\&lt;User&gt;\\Application Data\\.minecraft\\assets</p>"
                + "<p><b>Loose jar warning:</b><br>"
                + "A loose .minecraft\\versions\\" + escape(selectedVersion()) + ".jar is not enough. "
                + "MCLauncherRevival expects .minecraft\\versions\\" + escape(selectedVersion()) + "\\"
                + escape(selectedVersion()) + ".jar plus the matching JSON metadata and support folders.</p>"
                + "<p><font color='#ffff55'><b>Use only Minecraft files you own or otherwise have the right to use. "
                + "This project is unofficial and does not bypass ownership checks.</b></font></p>"
                + "</body></html>");
        appendLog("Opened XP version setup help for " + selectedVersion() + ".");
    }

    private void updateEraBadge() {
        EraTheme theme = EraTheme.resolve(selectedStyleMode(), selectedVersion());
        eraBadge.setText(theme.badgeText);
        eraBadge.setBackground(theme.badgeColor);
        eraBadge.setForeground(theme.badgeForeground);
        updateRedownloadVisibility();
    }

    private void updateRedownloadVisibility() {
        if (redownloadButton == null || versionBox == null) {
            return;
        }
        VersionReadiness readiness = selectedVersionReadiness();
        redownloadButton.setVisible(readiness.folderExists);
        if (versionStatusLabel != null) {
            versionStatusLabel.setText("Version files: " + readiness.label);
            versionStatusLabel.setForeground(readiness.color);
            versionStatusLabel.setToolTipText(readiness.tooltip);
        }
        versionBox.setToolTipText(readiness.tooltip);
    }

    private VersionReadiness selectedVersionReadiness() {
        String version = selectedVersion();
        java.io.File minecraftDir = TokenCache.minecraftDir();
        java.io.File versionDir = new java.io.File(new java.io.File(minecraftDir, "versions"), version);
        java.io.File jarFile = localVersionJar(versionDir, version);
        java.io.File jsonFile = localVersionJson(versionDir, version);
        java.io.File librariesDir = new java.io.File(minecraftDir, "libraries");
        java.io.File nativeDir = new java.io.File(versionDir, "natives");

        if (!versionDir.exists()) {
            return new VersionReadiness("not downloaded yet",
                    "Selected version will download on launch when downloads are available.",
                    new Color(90, 90, 90), false);
        }
        if (!jarFile.exists() && !jsonFile.exists()) {
            return new VersionReadiness("missing jar/json",
                    "The selected version folder exists, but the client jar and JSON metadata are missing.",
                    new Color(145, 70, 30), true);
        }
        if (!jarFile.exists()) {
            return new VersionReadiness("missing jar",
                    "The selected version folder is missing " + version + ".jar.",
                    new Color(145, 70, 30), true);
        }
        if (!jsonFile.exists()) {
            return new VersionReadiness("missing json",
                    "The selected version folder is missing " + version + ".json.",
                    new Color(145, 70, 30), true);
        }
        if (!hasAnyFile(librariesDir)) {
            return new VersionReadiness("needs libraries",
                    "The selected version has jar/json files, but the shared .minecraft libraries folder looks empty.",
                    new Color(145, 70, 30), true);
        }
        if (!hasAnyFile(nativeDir)) {
            return new VersionReadiness("needs natives",
                    "The selected version has jar/json files, but extracted LWJGL natives were not found yet.",
                    new Color(145, 70, 30), true);
        }
        return new VersionReadiness("ready",
                "Selected version has local jar, JSON metadata, libraries, and extracted natives.",
                new Color(45, 95, 45), true);
    }

    private static boolean hasAnyFile(java.io.File dir) {
        return hasAnyFile(dir, 0);
    }

    private static boolean hasAnyFile(java.io.File dir, int depth) {
        if (dir == null || !dir.exists() || depth > 8) {
            return false;
        }
        java.io.File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                return true;
            }
            if (files[i].isDirectory() && hasAnyFile(files[i], depth + 1)) {
                return true;
            }
        }
        return false;
    }

    private static final class VersionReadiness {
        final String label;
        final String tooltip;
        final Color color;
        final boolean folderExists;

        VersionReadiness(String label, String tooltip, Color color, boolean folderExists) {
            this.label = label;
            this.tooltip = tooltip;
            this.color = color;
            this.folderExists = folderExists;
        }
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
        java.io.File versionDir;
        try {
            versionDir = selectedVersionFolderForRedownload(version);
        } catch (IOException e) {
            showError(e);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete and re-download only this selected version?\n\n" + version + "\n\nYour saves and auth tokens will not be touched.",
                "Force Redownload",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            deleteRecursive(versionDir);
            status("Deleted selected version folder for re-download: " + version);
            appendLog("Next launch will re-download " + version + ".");
            updateRedownloadVisibility();
        } catch (IOException e) {
            showError(e);
        }
    }

    private java.io.File selectedVersionFolderForRedownload(String version) throws IOException {
        if (version == null || version.trim().length() == 0) {
            throw new IOException("No selected version to redownload.");
        }
        String clean = version.trim();
        if (clean.indexOf('/') >= 0 || clean.indexOf('\\') >= 0
                || clean.indexOf(':') >= 0 || clean.indexOf("..") >= 0) {
            throw new IOException("Invalid version name for redownload: " + clean);
        }
        java.io.File root = new java.io.File(TokenCache.minecraftDir(), "versions").getCanonicalFile();
        java.io.File target = new java.io.File(root, clean).getCanonicalFile();
        if (target.equals(root) || !root.equals(target.getParentFile())) {
            throw new IOException("Refusing to delete outside versions folder: " + target.getAbsolutePath());
        }
        if (!target.exists()) {
            throw new IOException("Selected version folder was not found: " + clean);
        }
        return target;
    }

    private void deleteRecursive(java.io.File file) throws IOException {
        if (file == null || !file.exists()) {
            return;
        }
        java.io.File root = new java.io.File(TokenCache.minecraftDir(), "versions").getCanonicalFile();
        java.io.File target = file.getCanonicalFile();
        String rootPath = root.getPath();
        String targetPath = target.getPath();
        if (target.equals(root) || !targetPath.startsWith(rootPath + java.io.File.separator)) {
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
        String javaCommand = windowsOs() ? "javaw" : "java";
        return javaCommand + " -Xmx" + memory + " -Djava.library.path=<selected version natives> -cp <libraries + " + selectedVersion() + ".jar> <main class> <safe auth/session args hidden>\n"
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
            String decoded = new String(decodeBase64(encoded), "UTF-8");
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

    private static byte[] decodeBase64(String value) throws IOException {
        String compact = value.replaceAll("\\s", "");
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int[] quad = new int[4];
        int count = 0;
        for (int i = 0; i < compact.length(); i++) {
            char c = compact.charAt(i);
            int decoded = c == '=' ? -2 : base64Value(c);
            if (decoded < -2) {
                throw new IOException("Invalid Base64 character in skin texture data.");
            }
            quad[count++] = decoded;
            if (count == 4) {
                writeBase64Quad(quad, out);
                count = 0;
            }
        }
        if (count > 0) {
            while (count < 4) {
                quad[count++] = -2;
            }
            writeBase64Quad(quad, out);
        }
        return out.toByteArray();
    }

    private static int base64Value(char c) {
        if (c >= 'A' && c <= 'Z') {
            return c - 'A';
        }
        if (c >= 'a' && c <= 'z') {
            return c - 'a' + 26;
        }
        if (c >= '0' && c <= '9') {
            return c - '0' + 52;
        }
        if (c == '+' || c == '-') {
            return 62;
        }
        if (c == '/' || c == '_') {
            return 63;
        }
        return -3;
    }

    private static void writeBase64Quad(int[] quad, java.io.ByteArrayOutputStream out) throws IOException {
        if (quad[0] < 0 || quad[1] < 0) {
            throw new IOException("Invalid Base64 padding in skin texture data.");
        }
        out.write((quad[0] << 2) | (quad[1] >> 4));
        if (quad[2] == -2) {
            return;
        }
        if (quad[2] < 0) {
            throw new IOException("Invalid Base64 padding in skin texture data.");
        }
        out.write(((quad[1] & 15) << 4) | (quad[2] >> 2));
        if (quad[3] == -2) {
            return;
        }
        if (quad[3] < 0) {
            throw new IOException("Invalid Base64 padding in skin texture data.");
        }
        out.write(((quad[2] & 3) << 6) | quad[3]);
    }

    private String selectedVersion() {
        Object value = versionBox.isEditable() ? versionBox.getEditor().getItem() : versionBox.getSelectedItem();
        String text = value == null ? "" : value.toString().trim();
        return text.length() == 0 ? BetaLauncher.DEFAULT_VERSION : text;
    }

    private String selectedMemoryMegabytes() {
        Object value = memoryBox.getSelectedItem();
        String text = value == null ? "" : value.toString();
        return BetaLauncher.memoryPreview(text);
    }

    private String selectedStyleMode() {
        Object value = styleBox.getSelectedItem();
        String text = value == null ? "Auto" : value.toString().trim();
        return text.length() == 0 ? "Auto" : text;
    }

    private static java.io.File localVersionJar(java.io.File versionDir, String id) {
        java.io.File exact = new java.io.File(versionDir, id + ".jar");
        if (exact.exists()) {
            return exact;
        }
        return singleFileWithExtension(versionDir, ".jar");
    }

    private static java.io.File localVersionJson(java.io.File versionDir, String id) {
        java.io.File exact = new java.io.File(versionDir, id + ".json");
        if (exact.exists()) {
            return exact;
        }
        return singleFileWithExtension(versionDir, ".json");
    }

    private static java.io.File singleFileWithExtension(java.io.File dir, String extension) {
        if (dir == null || !dir.isDirectory()) {
            return null;
        }
        java.io.File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        java.io.File found = null;
        String lowerExtension = extension.toLowerCase(java.util.Locale.ENGLISH);
        for (int i = 0; i < files.length; i++) {
            java.io.File file = files[i];
            if (file == null || !file.isFile()) {
                continue;
            }
            String name = file.getName().toLowerCase(java.util.Locale.ENGLISH);
            if (!name.endsWith(lowerExtension)) {
                continue;
            }
            if (found != null) {
                return null;
            }
            found = file;
        }
        return found;
    }
    private int loadLocalVersions() {
        java.io.File versionsDir = new java.io.File(TokenCache.minecraftDir(), "versions");
        if (!versionsDir.exists() || !versionsDir.isDirectory()) {
            return 0;
        }
        java.io.File[] folders = versionsDir.listFiles();
        if (folders == null) {
            return 0;
        }
        java.util.ArrayList<String> found = new java.util.ArrayList<String>();
        for (int i = 0; i < folders.length; i++) {
            java.io.File folder = folders[i];
            if (folder == null || !folder.isDirectory()) {
                continue;
            }
            String id = folder.getName();
            java.io.File jar = localVersionJar(folder, id);
            java.io.File json = localVersionJson(folder, id);
            if (jar != null && jar.exists() && json != null && json.exists()) {
                found.add(id);
            }
        }
        if (found.size() == 0) {
            return 0;
        }
        java.util.Collections.sort(found, new java.util.Comparator<String>() {
            public int compare(String left, String right) {
                return left.compareToIgnoreCase(right);
            }
        });
        String selected = settings.get("version", selectedVersion());
        versionBox.removeAllItems();
        for (int i = 0; i < found.size(); i++) {
            versionBox.addItem(found.get(i));
        }
        if (!found.contains(BetaLauncher.DEFAULT_VERSION)) {
            versionBox.addItem(BetaLauncher.DEFAULT_VERSION);
        }
        versionBox.setSelectedItem(selected);
        allVersions = new java.util.ArrayList<String>(found);
        appendLog("Loaded " + found.size() + " local version(s) from " + versionsDir.getAbsolutePath() + ". Folders with a single jar/json pair are accepted even if filenames differ from the folder name.");
        updateRedownloadVisibility();
        return found.size();
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
        String message = friendlyErrorMessage(ex);
        status("Launcher error");
        appendLog("ERROR: " + message);
        splashLabel.setAnimationActive(false);
        setNewsHtml(errorNews(message));
    }

    private String friendlyErrorMessage(Exception ex) {
        if (xpCompatibilityMode() && BetaLauncher.isXpHttpsFailure(ex)) {
            return BetaLauncher.xpVersionFilesMessage();
        }
        return ex.getMessage() == null ? ex.toString() : ex.getMessage();
    }

    private void setNewsHtml(String html) {
        if (html != null && html.equals(lastNewsHtml)) {
            return;
        }
        lastNewsHtml = html == null ? "" : html;
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
        return htmlStart("#e8e8e8", "#aaaaff", "Verdana,Arial,sans-serif", 11, 24)
                + "<table width='100%' cellpadding='0' cellspacing='0'><tr>"
                + "<td><font size='+3'><b>Minecraft News</b></font></td>"
                + "<td align='right'></td>"
                + "</tr></table>"
                + "<br><br>"

                + "<p><a href='https://www.minecraft.net/'><b>MCLauncherRevival Alpha Released</b></a></p>"
                + "<p>Surprise! The old launcher window has stumbled out of a 2011 hard drive and learned a new trick: "
                + "Microsoft OAuth, Xbox Live, XSTS, Minecraft services, and profile lookup, all without asking for a raw password.</p>"
                + "<p>+ Added browser-based Microsoft login<br>"
                + "+ Added local token caching with a Forget Login button<br>"
                + "+ Added offline singleplayer for rainy afternoons<br>"
                + "+ Changed the news panel to look like it still believes Tumblr is powering half the internet</p>"
                + "<p>The update is available in this folder, enjoy!</p>"
                + "<p>Happy mining from the MCLauncherRevival team.</p>"

                + "<br><p><a href='https://www.minecraft.net/'><b>Minecraft Beta 1.8 and older - The Archive Update</b></a></p>"
                + "<p>New mobs and new places to put your precious items:</p>"
                + "<p>+ Added old-school terrain vibes<br>"
                + "+ Added a version selector for Beta 1.8.x down through the earliest classic builds<br>"
                + "+ Added official Mojang client download metadata<br>"
                + "+ Added LWJGL natives handled as platform files<br>"
                + "+ Added the sacred pre-hunger gameplay loop<br>"
                + "+ Removed Herobrine from the OAuth redirect, probably</p>"

                + "<br><p><a href='https://www.minecraft.net/'><b>Humble Blockjam Bundle</b></a></p>"
                + "<table cellpadding='6' cellspacing='0' bgcolor='#0f0f0f' style='border:1px solid #555555'><tr>"
                + "<td><img src='" + blockImageUrl() + "' width='72' height='72'></td>"
                + "<td><font color='#eeeeee'><b>HUMBLE BLOCKJAM BUNDLE</b><br>"
                + "A nostalgic launcher panel for classic offline tinkering and careful testing.</font></td>"
                + "</tr></table>"

                + "<br><hr color='#333333'>"
                + "<p><font color='#888888'>MCLauncherRevival Alpha. "
                + "Unofficial project. Not affiliated with Mojang or Microsoft.</font></p>"
                + "</body></html>";
    }

    private static String sidebarHtml(EraTheme theme) {
        if ("alpha".equals(theme.id)) {
            return alphaSidebarHtml(theme);
        }
        if ("infdev".equals(theme.id)) {
            return infdevSidebarHtml(theme);
        }
        if ("classic".equals(theme.id)) {
            return classicSidebarHtml(theme);
        }
        if ("preclassic".equals(theme.id)) {
            return preclassicSidebarHtml(theme);
        }
        return betaSidebarHtml(theme);
    }

    private static String betaSidebarHtml(EraTheme theme) {
        return sidebarStart(theme)
                + "<font size='+2'><b>Official<br>links:</b></font><br><br>"
                + sidebarLink("https://www.minecraft.net/", "Minecraft.net")
                + sidebarLink("https://www.minecraft.net/en-us/realms", "Minecraft Realms")
                + sidebarLink("https://www.facebook.com/minecraft/", "Minecraft on Facebook")
                + sidebarLink("https://minecraftshop.com/", "Merchandise")
                + "<br>"
                + sidebarLink("https://bugs.mojang.com/", "Bug tracker")
                + sidebarLink("https://help.minecraft.net/", "Account Support")
                + "<br>"
                + sidebarLink("https://twitter.com/Mojang", "Mojang on Twitter")
                + sidebarLink("https://twitter.com/MojangSupport", "Support on Twitter")
                + "<br>"
                + "<font size='+1'><b>Try our other<br>games!</b></font><br><br>"
                + centeredGameLogos(150, 44, 130, 38)
                + "<br><font size='+1'><b>Community<br>links:</b></font><br>"
                + sidebarLink("https://www.minecraftforum.net/", "Minecraft Forums")
                + sidebarLink("https://minecraft.wiki/", "Minecraft Wiki")
                + "<br><hr color='" + theme.ruleHex + "'>"
                + "<font color='" + theme.mutedHex + "'>Modern updates.<br>Old vibes.<br>Still fun to live in.</font>"
                + "</body></html>";
    }

    private static String alphaSidebarHtml(EraTheme theme) {
        return sidebarStart(theme)
                + "<font size='+2'><b>Minecraft<br>links:</b></font><br><br>"
                + sidebarLink("https://www.minecraft.net/", "Minecraft.net")
                + sidebarLink("https://help.minecraft.net/", "Account help")
                + sidebarLink("https://minecraft.wiki/", "Minecraft Wiki")
                + "<br><br>"
                + "<font size='+1'><b>Other bits!</b></font><br><br>"
                + centeredGameLogos(126, 37, 112, 33)
                + "<br><hr color='" + theme.ruleHex + "'>"
                + "<font color='" + theme.mutedHex + "'>Alpha board:<br>login first.<br>news nearby.</font>"
                + "</body></html>";
    }

    private static String infdevSidebarHtml(EraTheme theme) {
        return sidebarStart(theme)
                + "<font size='+1'><b>Devlog:</b></font><br><br>"
                + "<font color='" + theme.textHex + "'>world gen<br>terrain tests<br>client jars<br>local files</font><br><br>"
                + sidebarLink("https://minecraft.wiki/w/Java_Edition_Infdev", "Infdev notes")
                + "<br><hr color='" + theme.ruleHex + "'>"
                + "<font color='" + theme.mutedHex + "'>Experimental:<br>rough launcher.<br>rougher worlds.</font>"
                + "</body></html>";
    }

    private static String classicSidebarHtml(EraTheme theme) {
        return sidebarStart(theme)
                + "<font size='+1'><b>Links:</b></font><br><br>"
                + sidebarLink("https://www.minecraft.net/", "Minecraft.net")
                + sidebarLink("https://minecraft.wiki/w/Java_Edition_Classic", "Classic Wiki")
                + "<br><hr color='" + theme.ruleHex + "'>"
                + "<font color='" + theme.mutedHex + "'>Classic:<br>simple frame.<br>tiny chrome.</font>"
                + "</body></html>";
    }

    private static String preclassicSidebarHtml(EraTheme theme) {
        return sidebarStart(theme)
                + "<font size='+1'><b>Proto:</b></font><br><br>"
                + "<font color='" + theme.textHex + "'>blocks<br>mouse<br>keyboard<br>test jar</font><br><br>"
                + sidebarLink("https://minecraft.wiki/w/Java_Edition_pre-Classic", "Pre-Classic")
                + "<br><hr color='" + theme.ruleHex + "'>"
                + "<font color='" + theme.mutedHex + "'>No shop.<br>No feed.<br>Just blocks.</font>"
                + "</body></html>";
    }

    private static String sidebarStart(EraTheme theme) {
        return htmlStart(theme.textHex, theme.linkHex, theme.fontFamily, theme.sidebarFontSize, theme.sidebarMargin);
    }

    private static String sidebarLink(String href, String text) {
        return "<a href='" + href + "'>" + text + "</a><br>";
    }

    private static String centeredGameLogos(int scrollsWidth, int scrollsHeight, int cobaltWidth, int cobaltHeight) {
        return "<center><a href='https://www.minecraft.net/en-us/article/scrolls-now-free'><img src='"
                + scrollsLogoUrl() + "' width='" + scrollsWidth + "' height='" + scrollsHeight
                + "' border='0'></a><br>"
                + "<a href='https://playcobalt.com/'><img src='" + cobaltLogoUrl() + "' width='"
                + cobaltWidth + "' height='" + cobaltHeight + "' border='0'></a></center>";
    }
    private String logPage() {
        java.io.File launchLog = new java.io.File(new java.io.File(new java.io.File(TokenCache.minecraftDir(), "launcher_revive"), "logs"), "last-launch.log");
        return htmlStart("#e8e8e8", "#aaaaff", "Verdana,Arial,sans-serif", 11, 24)
                + "<font size='+3'><b>Launcher Log</b></font><br><br>"
                + "<p><font color='#999999'>Current launcher session messages. Game output is also written to disk after Minecraft starts.</font></p>"
                + xpModeNoteHtml()
                + macOsNoteHtml()
                + openGlFailureNoteHtml(launchLog)
                + "<p><b>Selected version:</b> " + escape(selectedVersion()) + "<br>"
                + "<b>Launcher style:</b> " + escape(selectedStyleMode()) + " -> " + escape(activeTheme.displayName) + "<br>"
                + "<b>Java runtime:</b> " + escape(javaRuntimeSummary()) + "<br>"
                + "<b>Java safety:</b> " + escape(javaSafetySummary()) + "<br>"
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

    private static String openGlFailureNoteHtml(java.io.File launchLog) {
        String text = readSmallLogLower(launchLog);
        if (text.indexOf("pixel format not accelerated") < 0
                && text.indexOf("failed to find an accelerated opengl mode") < 0
                && text.indexOf("no opengl context found") < 0) {
            return "";
        }
        return "<table width='100%' cellpadding='8' cellspacing='0' bgcolor='#1b1208' style='border:1px solid #6a3a22'><tr><td>"
                + "<b>OpenGL/graphics driver note:</b><br>"
                + "Minecraft started, but the graphics/OpenGL layer did not expose accelerated OpenGL. "
                + "On Windows XP this is usually a graphics driver issue. On macOS/Linux this may be old "
                + "LWJGL/OpenGL/native compatibility. This is a driver/OpenGL issue, "
                + "not a Microsoft login or launcher file issue."
                + "</td></tr></table><br>";
    }

    private static String readSmallLogLower(java.io.File file) {
        if (file == null || !file.exists()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        java.io.BufferedReader reader = null;
        try {
            reader = new java.io.BufferedReader(new java.io.FileReader(file));
            char[] buffer = new char[4096];
            int read;
            int total = 0;
            while ((read = reader.read(buffer)) != -1 && total < 65536) {
                out.append(buffer, 0, read);
                total += read;
            }
        } catch (Exception ignored) {
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
        }
        return out.toString().toLowerCase(java.util.Locale.ENGLISH);
    }

    private String profilePage() {
        String mode = currentProfile == null ? "No Microsoft profile loaded for this session" : "Logged in as " + currentProfile.name;
        String uuid = currentProfile == null ? "(not loaded)" : currentProfile.uuid;
        String tokenState = tokenCache.hasRefreshToken() ? "OAuth refresh token cached" : "No cached Microsoft refresh token";
        VersionReadiness readiness = selectedVersionReadiness();
        String minecraftDir = TokenCache.minecraftDir().getAbsolutePath();
        String savesDir = new java.io.File(TokenCache.minecraftDir(), "saves").getAbsolutePath();
        String modsDir = new java.io.File(TokenCache.minecraftDir(), "mods").getAbsolutePath();
        String texturePacksDir = new java.io.File(TokenCache.minecraftDir(), "texturepacks").getAbsolutePath();
        String backupsDir = new java.io.File(new java.io.File(TokenCache.minecraftDir(), "launcher_revive"), "backups").getAbsolutePath();
        String selectedVersionDir = new java.io.File(new java.io.File(TokenCache.minecraftDir(), "versions"), selectedVersion()).getAbsolutePath();
        String lastPlayed = settings.get("last.version", "(never)") + " as " + settings.get("last.name", "(nobody)");
        return htmlStart("#e8e8e8", "#aaaaff", "Verdana,Arial,sans-serif", 11, 24)
                + "<font size='+3'><b>Profile Editor</b></font><br><br>"
                + "<p><font color='#999999'>Classic launcher-style profile controls, focused on the settings this revived launcher actually uses.</font></p>"
                + xpModeNoteHtml()
                + macOsNoteHtml()
                + "<table cellpadding='6' cellspacing='0' bgcolor='#0d0d0d' style='border:1px solid #444444'>"
                + "<tr><td><b>Offline name</b></td><td>" + escape(offlineName.getText()) + "</td></tr>"
                + "<tr><td><b>Selected version</b></td><td>" + escape(selectedVersion()) + "</td></tr>"
                + "<tr><td><b>Version files</b></td><td>" + escape(readiness.label) + "</td></tr>"
                + "<tr><td><b>Memory preset</b></td><td>" + escape(String.valueOf(memoryBox.getSelectedItem())) + "</td></tr>"
                + "<tr><td><b>Low-end mode</b></td><td>" + (lowEndModeBox.isSelected() ? "On, Potato 256MB" : "Off") + "</td></tr>"
                + "<tr><td><b>Style mode</b></td><td>" + escape(selectedStyleMode()) + "</td></tr>"
                + "<tr><td><b>Resolved layout</b></td><td>" + escape(activeTheme.displayName) + "</td></tr>"
                + "<tr><td><b>Splash animation</b></td><td>" + ("notes".equals(activeTab) ? "Active on Update Notes" : "Paused off Update Notes") + "</td></tr>"
                + "<tr><td><b>Java runtime</b></td><td>" + escape(javaRuntimeSummary()) + "</td></tr>"
                + "<tr><td><b>Java safety</b></td><td>" + escape(javaSafetySummary()) + "</td></tr>"
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
                + "<a href='launcher:first-run-guide'>Open first-run guide</a><br>"
                + "<a href='launcher:xp-version-guide'>XP version setup help</a><br>"
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
                + "<p><font color='#888888'>This keeps the old Profile Editor tab feeling, but keeps unsupported complex profile features out of scope for this revival.</font></p>"
                + "</body></html>";
    }

    private static String xpModeNoteHtml() {
        if (!xpCompatibilityMode()) {
            return "";
        }
        return "<table width='100%' cellpadding='8' cellspacing='0' bgcolor='#1b1408' style='border:1px solid #5a4a22'><tr><td>"
                + "<b>Windows XP offline/classic note:</b><br>"
                + "+ Microsoft login is disabled/best-effort on XP.<br>"
                + "+ Fresh downloads may fail because XP/Java 7 cannot reliably connect to modern HTTPS services.<br>"
                + "+ Offline play works best with pre-cached .minecraft versions, libraries, and assets copied from a newer PC."
                + "</td></tr></table><br>";
    }

    private static String macOsNoteHtml() {
        if (!macOs()) {
            return "";
        }
        return "<table width='100%' cellpadding='8' cellspacing='0' bgcolor='#101826' style='border:1px solid #33506d'><tr><td>"
                + "<b>macOS experimental note:</b><br>"
                + "macOS support is experimental. If Minecraft opens as a blank window, the launcher likely started "
                + "the process successfully but the old client may be failing in LWJGL/OpenGL/native loading. "
                + "Check last-launch.log."
                + "</td></tr></table><br>";
    }

    private boolean confirmMacLaunch() {
        int choice = JOptionPane.showOptionDialog(
                this,
                "MCLauncherRevival can open on macOS, but launching old Beta/Alpha Minecraft clients is experimental. "
                        + "Some versions may open a blank window, fail to render, or hang because of old "
                        + "LWJGL/OpenGL/Java native compatibility. If the game does not load, check the Launcher Log "
                        + "tab and the last-launch.log file.",
                "macOS compatibility warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new Object[] { "Continue", "Cancel" },
                "Cancel");
        return choice == JOptionPane.YES_OPTION;
    }

    private static String htmlStart(EraTheme theme) {
        return htmlStart(theme.textHex, theme.linkHex, theme.linkBackHex, theme.linkEdgeHex,
                theme.fontFamily, theme.sidebarFontSize, theme.sidebarMargin);
    }

    private static String htmlStart(String textHex, String linkHex, String fontFamily, int fontSize, int margin) {
        return htmlStart(textHex, linkHex, "#111226", "#5c5fae", fontFamily, fontSize, margin);
    }

    private static String htmlStart(String textHex, String linkHex, String linkBackHex, String linkEdgeHex,
            String fontFamily, int fontSize, int margin) {
        return "<html><head><style type='text/css'>"
                + "a { color:" + linkHex + "; background-color:" + linkBackHex
                + "; text-decoration:underline; border-bottom:1px solid " + linkEdgeHex + "; }"
                + "</style></head><body text='" + textHex + "' link='" + linkHex + "' vlink='" + linkHex
                + "' style='font-family:" + fontFamily + ";font-size:" + fontSize
                + "px;margin:" + margin + "px;background-color:transparent'>";
    }
    private static String errorNews(String message) {
        return htmlStart("#eeeeee", "#aaaaff", "Verdana,Arial,sans-serif", 11, 24)
                + "<font size='+3'><b>Launcher error</b></font><br><br>"
                + "<p><font color='#ff9999'>" + escape(message) + "</font></p>"
                + "<p>Offline singleplayer mode is still available from the bottom bar.</p>"
                + "<hr color='#333333'>"
                + "<p><font color='#888888'>Tip: if online login gets weird, use Forget Login and try again.</font></p>"
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
                        setNewsHtml(htmlStart("#e8e8e8", "#aaaaff", "Verdana,Arial,sans-serif", 11, 24)
                                + "<font size='+3'><b>Microsoft Login</b></font><br><br>"
                                + "<p>" + escape(message) + "</p>"
                                + "<p>The launcher opens Microsoft OAuth in your default browser. "
                                + "Your password stays on Microsoft's website. "
                                + "MCLauncherRevival only receives the tokens Microsoft returns after sign-in.</p>"
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
                        JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE,
                                JOptionPane.OK_CANCEL_OPTION);
                        pane.setWantsInput(true);
                        javax.swing.JDialog dialog = pane.createDialog(MinecraftLauncher.this, title);
                        dialog.setAlwaysOnTop(true);
                        dialog.setVisible(true);
                        Object value = pane.getInputValue();
                        if (value != null && value != JOptionPane.UNINITIALIZED_VALUE) {
                            result[0] = String.valueOf(value);
                        }
                    }
                });
            } catch (Exception e) {
                return null;
            }
            return result[0];
        }

        public int choose(final String title, final String message, final String[] options) {
            final int[] result = new int[] { -1 };
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        result[0] = JOptionPane.showOptionDialog(
                                MinecraftLauncher.this,
                                message,
                                title,
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                options,
                                options == null || options.length == 0 ? null : options[0]);
                    }
                });
            } catch (Exception e) {
                return -1;
            }
            return result[0];
        }
    }

    private static final class TabLabel extends JLabel {
        private boolean active;
        private EraTheme theme = EraTheme.beta();

        TabLabel(String text, boolean selected) {
            super(text);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setActive(selected);
        }

        void setActive(boolean selected) {
            active = selected;
            applyStyle();
        }

        void setTheme(EraTheme next) {
            theme = next == null ? EraTheme.beta() : next;
            applyStyle();
        }

        private void applyStyle() {
            setOpaque(true);
            setFont(new Font(theme.controlFont, Font.PLAIN, 11));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, theme.borderColor),
                    new EmptyBorder(5, 7, 5, 7)));
            setBackground(active ? theme.tabActive : theme.tabInactive);
            setForeground(theme.tabForeground);
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

    private static final class SplashLabel extends JLabel {
        private EraTheme theme = EraTheme.beta();
        private double phase;
        private final Timer timer;

        SplashLabel() {
            super("");
            setOpaque(false);
            setForeground(new Color(255, 255, 85));
            setFont(new Font("Dialog", Font.BOLD, 15));
            setBorder(new EmptyBorder(0, 0, 0, 0));
            timer = new Timer(55, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    phase += 0.16;
                    repaint();
                }
            });
        }

        void setSplash(String splash) {
            setText(splash == null ? "" : splash);
            setToolTipText(getText());
        }

        void setTheme(EraTheme next) {
            theme = next == null ? EraTheme.beta() : next;
            setForeground(theme.splashColor);
            setFont(new Font(theme.splashFont, Font.BOLD, theme.splashSize));
        }

        void setAnimationActive(boolean active) {
            if (active && getText().length() > 0) {
                if (!timer.isRunning()) {
                    timer.start();
                }
            } else if (timer.isRunning()) {
                timer.stop();
            }
        }

        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(size.width + 34, size.height + 18);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                double bob = Math.sin(phase) * 3.0;
                double scale = 1.0 + Math.sin(phase * 0.85) * 0.055;
                double rotate = Math.sin(phase * 0.65) * 0.08;
                g2.translate(getWidth() / 2, getHeight() / 2 + bob);
                g2.rotate(rotate);
                g2.scale(scale, scale);
                g2.translate(-getWidth() / 2, -getHeight() / 2);
                g2.setColor(new Color(40, 35, 0, 155));
                g2.drawString(getText(), 18, getHeight() / 2 + 6);
                g2.setColor(theme.splashColor);
                g2.drawString(getText(), 16, getHeight() / 2 + 4);
            } finally {
                g2.dispose();
            }
        }
    }

    private static final class EraTheme {
        final String id;
        final String displayName;
        final String badgeText;
        final Color badgeColor;
        final Color badgeForeground;
        final Color overlayColor;
        final Color borderColor;
        final Color tabActive;
        final Color tabInactive;
        final Color tabForeground;
        final Color splashColor;
        final String textHex;
        final String linkHex;
        final String linkBackHex;
        final String linkEdgeHex;
        final String mutedHex;
        final String ruleHex;
        final String fontFamily;
        final String controlFont;
        final String splashFont;
        final String sidebarTitle;
        final String gamesTitle;
        final String sidebarNote;
        final String texturePath;
        final String windowTitle;
        final String updateTabTitle;
        final String logTabTitle;
        final String profileTabTitle;
        final int sidebarWidth;
        final int sidebarFontSize;
        final int sidebarMargin;
        final int splashSize;
        final int tileScale;

        EraTheme(String id, String displayName, String badgeText, Color badgeColor, Color badgeForeground,
                Color overlayColor, Color borderColor, Color tabActive, Color tabInactive, Color tabForeground,
                Color splashColor, String textHex, String linkHex, String linkBackHex, String linkEdgeHex, String mutedHex, String ruleHex,
                String fontFamily, String controlFont, String splashFont, String sidebarTitle, String gamesTitle,
                String sidebarNote, String texturePath, String windowTitle, String updateTabTitle,
                String logTabTitle, String profileTabTitle, int sidebarWidth, int sidebarFontSize,
                int sidebarMargin, int splashSize, int tileScale) {
            this.id = id;
            this.displayName = displayName;
            this.badgeText = badgeText;
            this.badgeColor = badgeColor;
            this.badgeForeground = badgeForeground;
            this.overlayColor = overlayColor;
            this.borderColor = borderColor;
            this.tabActive = tabActive;
            this.tabInactive = tabInactive;
            this.tabForeground = tabForeground;
            this.splashColor = splashColor;
            this.textHex = textHex;
            this.linkHex = linkHex;
            this.linkBackHex = linkBackHex;
            this.linkEdgeHex = linkEdgeHex;
            this.mutedHex = mutedHex;
            this.ruleHex = ruleHex;
            this.fontFamily = fontFamily;
            this.controlFont = controlFont;
            this.splashFont = splashFont;
            this.sidebarTitle = sidebarTitle;
            this.gamesTitle = gamesTitle;
            this.sidebarNote = sidebarNote;
            this.texturePath = texturePath;
            this.windowTitle = windowTitle;
            this.updateTabTitle = updateTabTitle;
            this.logTabTitle = logTabTitle;
            this.profileTabTitle = profileTabTitle;
            this.sidebarWidth = sidebarWidth;
            this.sidebarFontSize = sidebarFontSize;
            this.sidebarMargin = sidebarMargin;
            this.splashSize = splashSize;
            this.tileScale = tileScale;
        }

        static EraTheme resolve(String mode, String version) {
            String cleanMode = mode == null ? "auto" : mode.trim().toLowerCase(java.util.Locale.ENGLISH);
            if ("beta".equals(cleanMode)) {
                return beta();
            }
            if ("alpha".equals(cleanMode)) {
                return alpha();
            }
            if ("infdev".equals(cleanMode)) {
                return infdev();
            }
            if ("classic".equals(cleanMode)) {
                return classic();
            }
            if ("pre-classic".equals(cleanMode) || "preclassic".equals(cleanMode)) {
                return preclassic();
            }
            return forVersion(version);
        }

        static EraTheme forVersion(String version) {
            String clean = version == null ? "" : version.trim().toLowerCase(java.util.Locale.ENGLISH);
            if (clean.startsWith("b")) {
                return beta();
            }
            if (clean.startsWith("a")) {
                return alpha();
            }
            if (clean.startsWith("inf")) {
                return infdev();
            }
            if (clean.startsWith("c")) {
                return classic();
            }
            if (clean.startsWith("rd")) {
                return preclassic();
            }
            return beta();
        }

        static EraTheme beta() {
            return new EraTheme("beta", "Beta news launcher", "beta", new Color(70, 95, 150), Color.WHITE,
                    new Color(0, 0, 0, 145), new Color(110, 110, 110),
                    Color.WHITE, new Color(230, 230, 230), Color.BLACK, new Color(255, 255, 85),
                    "#e8e8e8", "#aaaaff", "#111226", "#5c5fae", "#888888", "#333333",
                    "Verdana,Arial,sans-serif", "Dialog", "Dialog", "Official links:", "Try our other games!",
                    "Modern twist:<br>OAuth backstage.<br>Old vibes up front.", "/net/minecraft/themes/beta.png",
                    "Minecraft Launcher 1.6.89-j Revival", "Update Notes", "Launcher Log", "Profile Editor",
                    210, 10, 18, 15, 2);
        }

        static EraTheme alpha() {
            return new EraTheme("alpha", "Alpha compact login board", "alpha", new Color(88, 130, 70), Color.WHITE,
                    new Color(24, 15, 7, 118), new Color(96, 78, 45),
                    new Color(222, 215, 196), new Color(198, 185, 150), Color.BLACK, new Color(255, 230, 80),
                    "#f1ead2", "#c6e5ff", "#24180a", "#7892a6", "#b6a985", "#5b4a2a",
                    "Verdana,Arial,sans-serif", "Dialog", "Dialog", "Minecraft links:", "Other bits!",
                    "Alpha board:<br>login first.<br>news nearby.", "/net/minecraft/themes/alpha.png",
                    "Minecraft Launcher Alpha", "News", "Console", "Profile",
                    164, 10, 16, 15, 3);
        }

        static EraTheme infdev() {
            return new EraTheme("infdev", "Indev/Infdev prototype panel", "indev", new Color(135, 96, 45), Color.WHITE,
                    new Color(8, 18, 14, 110), new Color(82, 95, 68),
                    new Color(205, 218, 188), new Color(172, 188, 150), Color.BLACK, new Color(255, 245, 120),
                    "#e0f0d0", "#d8ff9a", "#102010", "#6f8a45", "#9fb08d", "#445238",
                    "Monospaced,Verdana,sans-serif", "Dialog", "Monospaced", "Build links:", "Experiments:",
                    "Prototype:<br>rough panels.<br>raw notes.", "/net/minecraft/themes/infdev.png",
                    "Minecraft Indev Launcher", "Indev Notes", "Console", "Profile",
                    158, 10, 12, 14, 3);
        }

        static EraTheme classic() {
            return new EraTheme("classic", "Classic minimal launcher", "classic", new Color(105, 105, 105), Color.WHITE,
                    new Color(0, 0, 0, 96), new Color(125, 125, 125),
                    new Color(235, 235, 235), new Color(205, 205, 205), Color.BLACK, new Color(255, 255, 85),
                    "#eeeeee", "#99ccff", "#101820", "#5f7c99", "#aaaaaa", "#555555",
                    "Arial,Verdana,sans-serif", "Dialog", "Dialog", "Links:", "Blocks:",
                    "Classic:<br>simple frame.<br>tiny chrome.", "/net/minecraft/themes/classic.png",
                    "Minecraft Launcher Classic", "News", "Console", "Login",
                    126, 10, 10, 14, 4);
        }

        static EraTheme preclassic() {
            return new EraTheme("preclassic", "Pre-Classic stripped prototype", "rd", new Color(120, 70, 70), Color.WHITE,
                    new Color(18, 12, 12, 86), new Color(110, 84, 84),
                    new Color(228, 218, 218), new Color(196, 180, 180), Color.BLACK, new Color(255, 235, 110),
                    "#f0e6e6", "#ffb8a8", "#261112", "#a66c62", "#bba0a0", "#5a3f3f",
                    "Monospaced,Verdana,sans-serif", "Dialog", "Monospaced", "Proto:", "None:",
                    "Pre-classic:<br>blocks only.<br>tiny UI.", "/net/minecraft/themes/preclassic.png",
                    "Minecraft Launcher 0.1", "Blocks", "Console", "Login",
                    118, 9, 9, 13, 4);
        }
    }

    private static final class DarkNewsPanel extends JPanel {
        private Image texture = loadImage("/net/minecraft/themes/beta.png");
        private EraTheme theme = EraTheme.beta();
        private BufferedImage cachedTile;

        void setTheme(EraTheme next) {
            theme = next == null ? EraTheme.beta() : next;
            Image loaded = loadImage(theme.texturePath);
            texture = loaded == null ? loadImage("/net/minecraft/dirt.png") : loaded;
            cachedTile = null;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (texture != null) {
                BufferedImage tile = scaledTile();
                int tileW = tile.getWidth();
                int tileH = tile.getHeight();
                for (int x = 0; x < getWidth(); x += tileW) {
                    for (int y = 0; y < getHeight(); y += tileH) {
                        g.drawImage(tile, x, y, this);
                    }
                }
            } else {
                g.setColor(new Color(30, 30, 30));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setColor(theme.overlayColor);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        private BufferedImage scaledTile() {
            int scale = Math.max(1, theme.tileScale);
            int width = Math.max(1, texture.getWidth(this) * scale);
            int height = Math.max(1, texture.getHeight(this) * scale);
            if (cachedTile != null && cachedTile.getWidth() == width && cachedTile.getHeight() == height) {
                return cachedTile;
            }
            cachedTile = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = cachedTile.createGraphics();
            try {
                g2.drawImage(texture, 0, 0, width, height, this);
            } finally {
                g2.dispose();
            }
            return cachedTile;
        }
    }
}



