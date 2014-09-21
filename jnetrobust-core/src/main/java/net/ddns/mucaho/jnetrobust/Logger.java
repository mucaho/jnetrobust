package net.ddns.mucaho.jnetrobust;

public abstract class Logger {
    public static enum LoggingEvent {
        SEND("Data sent"),
        RECEIVE("Data received"),
        RETRANSMIT("Data retransmitted"),
        ORDERED("Data received ordered"),
        UNORDERED("Data received not ordered"),
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
    public abstract void log(String... texts);

    public static Logger getConsoleLogger() {
        return new Logger() {
            @Override
            public void log(String... texts) {
                for (String text : texts)
                    System.out.print(text + "\t");
                System.out.println();
            }
        };
    }
}
