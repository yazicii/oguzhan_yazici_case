package org.insider.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Log4j 2 appender: Collects thread-based log lines during the test.
 * Retrieved by the listener via getAndClearForCurrentThread() and added to the Allure report.
 *
 * Registered programmatically via {@link #install()} (packages attribute not required).
 */
public class AllureLogAppender extends AbstractAppender {

    private static final String APPENDER_NAME = "AllureLog";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private static final ConcurrentMap<Long, StringBuilder> THREAD_BUFFERS = new ConcurrentHashMap<>();
    private static volatile boolean installed = false;

    private AllureLogAppender() {
        super(APPENDER_NAME, null, PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
        long threadId = Thread.currentThread().getId();
        StringBuilder sb = THREAD_BUFFERS.computeIfAbsent(threadId, k -> new StringBuilder());
        String time = TIME_FMT.format(Instant.ofEpochMilli(event.getTimeMillis()));
        sb.append(time)
          .append(" [").append(event.getLevel().name()).append("] ")
          .append(event.getLoggerName()).append(" - ")
          .append(event.getMessage().getFormattedMessage());
        if (event.getThrown() != null) {
            sb.append(" ").append(event.getThrown().getMessage());
        }
        sb.append("\n");
    }

    /**
     * Returns the collected log content for the current thread and clears the buffer.
     */
    public static String getAndClearForCurrentThread() {
        long threadId = Thread.currentThread().getId();
        StringBuilder sb = THREAD_BUFFERS.remove(threadId);
        return sb == null ? "" : sb.toString();
    }

    /**
     * Programmatically adds AllureLogAppender to the org.insider logger.
     * Only set up once even if called multiple times.
     */
    public static synchronized void install() {
        if (installed) return;

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        AllureLogAppender appender = new AllureLogAppender();
        appender.start();
        config.addAppender(appender);

        LoggerConfig loggerConfig = config.getLoggerConfig("org.insider");
        if (loggerConfig.getName().equals("org.insider")) {
            loggerConfig.addAppender(appender, null, null);
        } else {
            LoggerConfig insiderConfig = new LoggerConfig("org.insider",
                    org.apache.logging.log4j.Level.DEBUG, false);
            insiderConfig.addAppender(appender, null, null);
            config.addLogger("org.insider", insiderConfig);
        }
        ctx.updateLoggers();
        installed = true;
    }
}
