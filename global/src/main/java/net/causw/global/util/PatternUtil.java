package net.causw.global.util;

public class PatternUtil {

    public static String toAntPath(String pattern) {
        return pattern.replaceAll("\\{[^/]+}", "*");
    }
}
