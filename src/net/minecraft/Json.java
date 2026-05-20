package net.minecraft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Json {
    private final String text;
    private int pos;

    private Json(String text) {
        this.text = text == null ? "" : text;
    }

    static Object parse(String text) throws IOException {
        Json parser = new Json(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (!parser.end()) {
            throw new IOException("Unexpected JSON trailing data at " + parser.pos);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> object(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    @SuppressWarnings("unchecked")
    static List<Object> array(Object value) {
        return value instanceof List ? (List<Object>) value : null;
    }

    static String string(Map<String, Object> object, String key) {
        if (object == null) {
            return null;
        }
        Object value = object.get(key);
        return value == null ? null : String.valueOf(value);
    }

    static long number(Map<String, Object> object, String key, long fallback) {
        if (object == null) {
            return fallback;
        }
        Object value = object.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }

    static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder out = new StringBuilder(value.length() + 16);
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\b':
                    out.append("\\b");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 32) {
                        String hex = Integer.toHexString(c);
                        out.append("\\u");
                        for (int j = hex.length(); j < 4; j++) {
                            out.append('0');
                        }
                        out.append(hex);
                    } else {
                        out.append(c);
                    }
            }
        }
        out.append('"');
        return out.toString();
    }

    private Object readValue() throws IOException {
        skipWhitespace();
        if (end()) {
            throw new IOException("Unexpected end of JSON");
        }
        char c = peek();
        if (c == '"') {
            return readString();
        }
        if (c == '{') {
            return readObject();
        }
        if (c == '[') {
            return readArray();
        }
        if (c == 't') {
            expect("true");
            return Boolean.TRUE;
        }
        if (c == 'f') {
            expect("false");
            return Boolean.FALSE;
        }
        if (c == 'n') {
            expect("null");
            return null;
        }
        if (c == '-' || (c >= '0' && c <= '9')) {
            return readNumber();
        }
        throw new IOException("Unexpected JSON character '" + c + "' at " + pos);
    }

    private Map<String, Object> readObject() throws IOException {
        LinkedHashMap<String, Object> object = new LinkedHashMap<String, Object>();
        expect('{');
        skipWhitespace();
        if (tryRead('}')) {
            return object;
        }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            object.put(key, readValue());
            skipWhitespace();
            if (tryRead('}')) {
                return object;
            }
            expect(',');
        }
    }

    private List<Object> readArray() throws IOException {
        ArrayList<Object> array = new ArrayList<Object>();
        expect('[');
        skipWhitespace();
        if (tryRead(']')) {
            return array;
        }
        while (true) {
            array.add(readValue());
            skipWhitespace();
            if (tryRead(']')) {
                return array;
            }
            expect(',');
        }
    }

    private String readString() throws IOException {
        expect('"');
        StringBuilder out = new StringBuilder();
        while (!end()) {
            char c = next();
            if (c == '"') {
                return out.toString();
            }
            if (c == '\\') {
                if (end()) {
                    throw new IOException("Unfinished JSON escape");
                }
                char escaped = next();
                switch (escaped) {
                    case '"':
                    case '\\':
                    case '/':
                        out.append(escaped);
                        break;
                    case 'b':
                        out.append('\b');
                        break;
                    case 'f':
                        out.append('\f');
                        break;
                    case 'n':
                        out.append('\n');
                        break;
                    case 'r':
                        out.append('\r');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    case 'u':
                        out.append((char) Integer.parseInt(readChars(4), 16));
                        break;
                    default:
                        throw new IOException("Bad JSON escape \\" + escaped + " at " + pos);
                }
            } else {
                out.append(c);
            }
        }
        throw new IOException("Unfinished JSON string");
    }

    private Number readNumber() throws IOException {
        int start = pos;
        if (peek() == '-') {
            pos++;
        }
        while (!end() && Character.isDigit(peek())) {
            pos++;
        }
        boolean decimal = false;
        if (!end() && peek() == '.') {
            decimal = true;
            pos++;
            while (!end() && Character.isDigit(peek())) {
                pos++;
            }
        }
        if (!end() && (peek() == 'e' || peek() == 'E')) {
            decimal = true;
            pos++;
            if (!end() && (peek() == '+' || peek() == '-')) {
                pos++;
            }
            while (!end() && Character.isDigit(peek())) {
                pos++;
            }
        }
        String number = text.substring(start, pos);
        try {
            return decimal ? Double.valueOf(number) : Long.valueOf(number);
        } catch (NumberFormatException e) {
            throw new IOException("Bad JSON number " + number);
        }
    }

    private void skipWhitespace() {
        while (!end()) {
            char c = peek();
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                pos++;
            } else {
                return;
            }
        }
    }

    private boolean tryRead(char c) {
        if (!end() && peek() == c) {
            pos++;
            return true;
        }
        return false;
    }

    private void expect(char c) throws IOException {
        if (end() || next() != c) {
            throw new IOException("Expected '" + c + "' at " + pos);
        }
    }

    private void expect(String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            expect(value.charAt(i));
        }
    }

    private String readChars(int count) throws IOException {
        if (pos + count > text.length()) {
            throw new IOException("Unexpected end of JSON escape");
        }
        String value = text.substring(pos, pos + count);
        pos += count;
        return value;
    }

    private char next() {
        return text.charAt(pos++);
    }

    private char peek() {
        return text.charAt(pos);
    }

    private boolean end() {
        return pos >= text.length();
    }
}
