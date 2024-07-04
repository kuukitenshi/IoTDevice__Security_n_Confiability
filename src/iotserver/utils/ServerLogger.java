package iotserver.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a logger for the server.
 */
public class ServerLogger {

    /**
     * Constructor of the class
     */
    private ServerLogger() {
        throw new UnsupportedOperationException("Cannot create instance of " + getClass().getName());
    }

    /**
     * Method that returns a logger with the given name.
     * 
     * @param name the name of the logger
     * @return the logger
     */
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LoggerFormatter());
        logger.addHandler(handler);
        return logger;
    }

    /**
     * Private class that represents a formatter for the logger.
     */
    private static class LoggerFormatter extends Formatter {

        private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mmm:ss");
        private static final String MESSAGE_FORMAT = "[%s] (%s): %s\n";

        @Override
        public String format(LogRecord logRecord) {
            String formattedDate = DATE_FORMATTER.format(new Date(logRecord.getMillis()));
            return String.format(MESSAGE_FORMAT, formattedDate, logRecord.getLoggerName(), logRecord.getMessage());
        }
    }
}
