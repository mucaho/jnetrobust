package net.ddns.mucaho.jnetrobust.util;

public interface Logger {
    public static enum LoggingEvent {
        SEND("Data sent"),
        RECEIVE("Data received"),
        ORDERED("Data received ordered"),
        UNORDERED("Data order was corrupted"),
        ACKED("Data was received at other end"),
        NOTACKED("Data was probably not received at other end");

        private final String description;
        private LoggingEvent(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
    public void log(String... texts);
}
