package net.causw.domain.model.util;

public class PatternUtil {

    public static String toAntPath(String pattern) {
        return pattern.replaceAll("\\{[^/]+}", "*");
    }
}
