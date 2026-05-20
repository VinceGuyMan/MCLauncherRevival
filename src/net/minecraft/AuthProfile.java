package net.minecraft;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.UUID;

final class AuthProfile {
    final String name;
    final String uuid;
    final String accessToken;
    final boolean online;

    private AuthProfile(String name, String uuid, String accessToken, boolean online) {
        this.name = cleanName(name);
        this.uuid = stripDashes(uuid);
        this.accessToken = accessToken == null ? "0" : accessToken;
        this.online = online;
    }

    static AuthProfile online(String name, String uuid, String accessToken) {
        return new AuthProfile(name, uuid, accessToken, true);
    }

    static AuthProfile offline(String name) {
        String safeName = cleanName(name);
        UUID offlineUuid;
        try {
            offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + safeName).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + safeName).getBytes());
        }
        return new AuthProfile(safeName, offlineUuid.toString(), "0", false);
    }

    String sessionId() {
        if (!online) {
            return "-";
        }
        return "token:" + accessToken + ":" + uuid;
    }

    private static String cleanName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.length() == 0) {
            value = "Player";
        }
        value = value.replaceAll("[^A-Za-z0-9_]", "_");
        if (value.length() > 16) {
            value = value.substring(0, 16);
        }
        return value;
    }

    private static String stripDashes(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").toLowerCase(Locale.ENGLISH);
    }
}
