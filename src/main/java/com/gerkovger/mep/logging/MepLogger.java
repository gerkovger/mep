package com.gerkovger.mep.logging;

import com.gerkovger.mep.config.Config;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Objects;

import static com.gerkovger.mep.logging.Colors.*;

public enum MepLogger {

    INSTANCE;

    enum LogLevel {
        ERROR(0, RED), WARN(1, YELLOW), INFO(2, GREEN), DEBUG(3, BRIGHT_CYAN);
        final int level;
        final String paddedName;
        final int padLength = Config.INSTANCE.isShowMPlayerOut() ? 7 : 5;
        LogLevel(int ll, String color) {
            paddedName = "[" + color + " ".repeat(padLength - name().length()) + name() + RESET + "]: ";
            level = ll;
        }
    }

    private final Config config = Config.INSTANCE;

    private PrintStream printStream = System.out;

    private int logLevel = LogLevel.INFO.level;

    MepLogger() {
        setLogLevel(config.getLogLevel());
    }

    public void setLogLevel(String logLevelStr) {
        logLevelStr = logLevelStr.toUpperCase();
        for (LogLevel value : LogLevel.values()) {
            if (Objects.equals(logLevelStr, value.name())) {
                logLevel = value.level;
                return;
            }
        }
        logLevel = LogLevel.INFO.level;
    }

    public void setPrintStream(PrintStream ps) {
        this.printStream = ps;
    }

    public void error(String msg, Object... params) {
        print(LogLevel.ERROR, msg, params);
    }

    public void warn(String msg, Object... params) {
        print(LogLevel.WARN, msg, params);
    }

    public void info(String msg, Object... params) {
        print(LogLevel.INFO, msg, params);
    }

    public void debug(String msg, Object... params) {
        print(LogLevel.DEBUG, msg, params);
    }

    private void print(LogLevel withLevel, String msg, Object... params) {
        var caller = config.isDetailedLogs() ?
                colorizeStackTrace(Thread.currentThread().getStackTrace()[3]) + ": " :
                "";
        if (withLevel.level <= logLevel) {
            printStream.println(withLevel.paddedName + caller + parse(msg, params));
        }
    }

    private String parse(String msg, Object... params) {
        int iMsg = 0;
        int iPar = 0;
        StringBuilder sb = new StringBuilder();
        while (iMsg < msg.length()) {
            if (iMsg < msg.length() - 1
                    && msg.charAt(iMsg) == '{'
                    && msg.charAt(iMsg + 1) == '}') {
                if (iPar < params.length)
                    sb.append(paramToString(params[iPar++]));
                else sb.append("?");
                iMsg+=2;
            } else {
                sb.append(msg.charAt(iMsg++));
            }
        }
        return sb.toString();
    }

    private static String paramToString(Object o) {
        return switch (o) {
            case null -> "null";
            case Throwable t -> parseThrowable(t);
            case Path p -> parsePath(p);
            default -> o.toString();
        };
    }

    private static String parseThrowable(Throwable t) {
        var sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String parsePath(Path p) {
        return Config.INSTANCE.isCompactFileNames() ?
                p.getFileName().toString() :
                p.toAbsolutePath().toString();
    }

    private static String colorizeStackTrace(StackTraceElement ste) {
        var s = ste.toString();
        int i = s.indexOf("(");
        int j = s.indexOf(")");
        return s.substring(0, i + 1) + BLUE + s.substring(i + 1, j) + ")" + RESET;
    }

}
