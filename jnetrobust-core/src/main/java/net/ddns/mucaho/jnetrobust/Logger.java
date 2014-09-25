package net.ddns.mucaho.jnetrobust;

/**
 * @jnetrobust.api
 */
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
    public abstract void log(String description, Object... params);

    public static Logger getConsoleLogger(final String name) {
        return new Logger() {
            @Override
            public void log(String description, Object... params) {
                System.out.print("[" + name + "]: " + description + "\t");
                for (Object param: params)
                    System.out.print(param + "\t");
                System.out.println();
            }
        };
    }
}
