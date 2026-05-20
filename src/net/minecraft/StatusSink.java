package net.minecraft;

interface StatusSink {
    void status(String message);

    String ask(String title, String message);
}
