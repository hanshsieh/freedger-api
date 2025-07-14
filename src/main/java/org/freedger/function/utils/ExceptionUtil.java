package org.freedger.function.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
    public static String getPrettyStackTrace(Throwable e) {
        final var stringWriter = new StringWriter();
        final var printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}

