package net.minecraft;

final class VersionNotes {
    private VersionNotes() {
    }

    static String page(String selectedVersion, boolean patchMode, boolean compact, String themeId) {
        String version = clean(selectedVersion);
        VersionNoteData.Note note = VersionNoteData.forVersion(version);
        Style style = Style.forTheme(themeId, note.family);
        boolean compactMode = patchMode || compact;

        StringBuilder html = new StringBuilder(8192);
        html.append(bodyStart(style));
        html.append(header(style, note, compactMode));
        html.append(eraLead(note));
        if (compactMode) {
            patchSection(html, note);
        } else {
            updateSection(html, note);
        }
        projectNote(html);
        html.append("</div></body></html>");
        return html.toString();
    }

    private static String bodyStart(Style style) {
        return "<html><head><style type='text/css'>"
                + "body { font-family: Arial, Helvetica, sans-serif; color: " + style.text + "; background: " + style.background + "; margin: 0; padding: 0; }"
                + "h1 { font-size: " + style.titleSize + "px; margin: 0 0 28px 0; color: " + style.heading + "; }"
                + "h2 { font-size: 16px; margin: 22px 0 8px 0; color: " + style.heading + "; }"
                + "h3 { font-size: 13px; margin: 18px 0 6px 0; color: " + style.heading + "; }"
                + "p { font-size: " + style.bodySize + "px; line-height: 1.34; margin: 0 0 13px 0; }"
                + "ul { margin: 3px 0 14px 18px; padding: 0; }"
                + "li { font-size: " + style.bodySize + "px; margin: 2px 0; }"
                + "a { color: " + style.link + "; font-weight: bold; text-decoration: underline; }"
                + ".wrap { padding: " + style.padding + "px; }"
                + ".splash { color: #ffff55; font-weight: bold; font-size: 14px; }"
                + ".muted { color: " + style.muted + "; }"
                + ".box { border: 1px solid " + style.border + "; padding: 9px; margin: 8px 0 16px 0; background: " + style.box + "; }"
                + ".small { font-size: 11px; color: " + style.muted + "; }"
                + "hr { border: 0; border-top: 1px solid " + style.border + "; margin: 18px 0; }"
                + "</style></head><body><div class='wrap'>";
    }

    private static String header(Style style, VersionNoteData.Note note, boolean compactMode) {
        return "<h1>" + escape(style.headingText) + "</h1>";
    }

    private static String eraLead(VersionNoteData.Note note) {
        StringBuilder html = new StringBuilder(1024);
        html.append("<h2><a href='").append(escape(note.primaryUrl())).append("'>").append(escape(note.title)).append("</a></h2>");
        html.append("<p><b>Selected version:</b> ").append(escape(note.versionId)).append("<br>");
        if (note.releaseDate.length() > 0) {
            html.append("<b>Release date:</b> ").append(escape(note.releaseDate)).append("<br>");
        }
        html.append("<b>Timeline family:</b> ").append(escape(note.family));
        html.append("</p>");
        return html.toString();
    }

    private static void updateSection(StringBuilder html, VersionNoteData.Note note) {
        html.append("<p>").append(escape(note.shortSummary)).append("</p>");
        html.append("<h3>Why this version matters</h3>");
        html.append("<p>").append(escape(note.whyItMatters)).append("</p>");
        if (note.knownQuirks.length() > 0) {
            html.append("<h3>Known quirks</h3>");
            bulletList(html, note.quirkItems());
        }
        if (note.launcherCommentary.length() > 0) {
            html.append("<p><b>Launcher note:</b> ").append(escape(note.launcherCommentary)).append("</p>");
        }
        sources(html, note);
        fallbackNotice(html, note);
    }

    private static void patchSection(StringBuilder html, VersionNoteData.Note note) {
        html.append("<p class='muted'>Compact patch-style notes for ").append(escape(note.versionId))
                .append(". These are curated for launcher readability, not copied as a full wiki dump.</p>");
        listBlock(html, "Added", note.addedItems());
        listBlock(html, "Changed", note.changedItems());
        listBlock(html, "Fixed", note.fixedItems());
        listBlock(html, "Removed", note.removedItems());
        listBlock(html, "Known quirks", note.quirkItems());
        sources(html, note);
        fallbackNotice(html, note);
    }

    private static void fallbackNotice(StringBuilder html, VersionNoteData.Note note) {
        if (!note.exact) {
            html.append("<div class='box'><b>Fallback note:</b> Exact historical notes for this build were not found. This panel is using a verified family summary rather than pretending the note is exact.</div>");
        }
    }

    private static void listBlock(StringBuilder html, String title, String[] items) {
        if (items.length == 0) {
            return;
        }
        html.append("<h3>").append(escape(title)).append("</h3>");
        bulletList(html, items);
    }

    private static void bulletList(StringBuilder html, String[] items) {
        if (items.length == 0) {
            return;
        }
        html.append("<ul>");
        for (int i = 0; i < items.length; i++) {
            String item = clean(items[i]);
            if (item.length() > 0) {
                html.append("<li>").append(escape(item)).append("</li>");
            }
        }
        html.append("</ul>");
    }

    private static void sources(StringBuilder html, VersionNoteData.Note note) {
        String[] items = note.sourceItems();
        if (items.length == 0) {
            return;
        }
        html.append("<h3>Sources</h3><p class='small'>");
        for (int i = 0; i < items.length; i++) {
            String item = clean(items[i]);
            int eq = item.indexOf('=');
            if (eq > 0 && eq + 1 < item.length()) {
                if (i > 0) {
                    html.append(" / ");
                }
                String label = item.substring(0, eq);
                String url = item.substring(eq + 1);
                html.append("<a href='").append(escape(url)).append("'>").append(escape(label)).append("</a>");
            }
        }
        html.append("</p>");
    }

    private static void projectNote(StringBuilder html) {
        html.append("<hr>");
        html.append("<h2><a href='https://github.com/VinceGuyMan/MCLauncherRevival'>MCLauncherRevival Historical Notes</a></h2>");
        html.append("<p>The launcher now uses static, version-aware notes researched from public historical sources. "
                + "The goal is to be useful inside a small old launcher panel without pretending to be a full wiki page.</p>");
        html.append("<p class='small'>MCLauncherRevival Alpha. Unofficial project. Vibe-Coded with Codex.</p>");
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String escape(String value) {
        value = clean(value);
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '&') {
                out.append("&amp;");
            } else if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '"') {
                out.append("&quot;");
            } else if (c == '\'') {
                out.append("&#39;");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static final class Style {
        final String headingText;
        final String background;
        final String text;
        final String heading;
        final String muted;
        final String link;
        final String border;
        final String box;
        final int padding;
        final int titleSize;
        final int bodySize;

        Style(String headingText, String background, String text, String heading, String muted,
              String link, String border, String box, int padding, int titleSize, int bodySize) {
            this.headingText = headingText;
            this.background = background;
            this.text = text;
            this.heading = heading;
            this.muted = muted;
            this.link = link;
            this.border = border;
            this.box = box;
            this.padding = padding;
            this.titleSize = titleSize;
            this.bodySize = bodySize;
        }

        static Style forTheme(String themeId, String family) {
            String theme = themeId == null ? "" : themeId.toLowerCase();
            String fam = family == null ? "" : family.toLowerCase();
            if (theme.indexOf("pre") >= 0 || fam.indexOf("pre-classic") >= 0) {
                return new Style("Minecraft Pre-Classic News", "#101010", "#eeeeee", "#ffffff", "#bbbbbb", "#3355ff", "#555555", "#181818", 18, 28, 12);
            }
            if (theme.indexOf("classic") >= 0 || fam.indexOf("classic") >= 0) {
                return new Style("Minecraft Classic News", "#151515", "#eeeeee", "#ffffff", "#c6c6c6", "#3355ff", "#666666", "#202020", 22, 30, 12);
            }
            if (theme.indexOf("inf") >= 0 || fam.indexOf("infdev") >= 0) {
                return new Style("Minecraft Indev News", "#111111", "#eeeeee", "#ffffff", "#c4c4c4", "#3355ff", "#666666", "#1d1d1d", 24, 32, 12);
            }
            if (theme.indexOf("alpha") >= 0 || fam.indexOf("alpha") >= 0) {
                return new Style("Minecraft Alpha News", "#120f0b", "#f1f1f1", "#ffffff", "#c8c8c8", "#3355ff", "#6b5c43", "#1b160f", 28, 34, 13);
            }
            if (theme.indexOf("beta") >= 0 || fam.indexOf("beta") >= 0) {
                return new Style("Minecraft Beta News", "#111111", "#f2f2f2", "#ffffff", "#bdbdbd", "#3355ff", "#777777", "#181818", 34, 38, 14);
            }
            return new Style("Minecraft News", "#111111", "#f2f2f2", "#ffffff", "#bdbdbd", "#3355ff", "#777777", "#181818", 34, 38, 14);
        }
    }
}
