package io.github.liam0999.civmod.chat;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ChatCoordinate(int x, @Nullable Integer y, int z, int textPosStart, int textPosEnd) {
    private static final int HORIZONTAL_LIMIT = 30_000_000;
    private static final int VERTICAL_LIMIT = 4096;

    private static final Pattern KEYED = Pattern.compile("\\[[^\\[\\]]{0,160}?\\bx\\s*:\\s*-?\\d+[^\\[\\]]{0,160}?\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEY_X = Pattern.compile("\\bx\\s*:\\s*(-?\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEY_Y = Pattern.compile("\\by\\s*:\\s*(-?\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEY_Z = Pattern.compile("\\bz\\s*:\\s*(-?\\d+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern BRACKETED = Pattern.compile("\\[\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*(?:,\\s*(-?\\d+)\\s*)?\\]");

    private static final Pattern BARE = Pattern.compile("(?<![\\d.\\-])(-?\\d+)\\s*,\\s*(-?\\d+)(?:\\s*,\\s*(-?\\d+))?(?![\\d.])(?!\\s*,\\s*-?\\d)");

    private static final Pattern SPACED = Pattern.compile("(?<![\\d.\\-])(-?\\d+)\\s+(-?\\d+)(?:\\s+(-?\\d+))?(?![\\d.])(?!\\s+-?\\d)");

    private static final int SPACED_MIN_MAGNITUDE = 10;

    public static List<ChatCoordinate> find(String text) {
        List<ChatCoordinate> found = new ArrayList<>();

        Matcher keyed = KEYED.matcher(text);
        while (keyed.find()) {
            String body = keyed.group();
            Matcher x = KEY_X.matcher(body);
            Matcher z = KEY_Z.matcher(body);
            if (!x.find() || !z.find()) {
                continue;
            }
            Matcher y = KEY_Y.matcher(body);
            Integer yValue = y.find() ? parse(y.group(1)) : null;
            add(found, parse(x.group(1)), yValue, parse(z.group(1)), keyed.start(), keyed.end());
        }

        Matcher bracketed = BRACKETED.matcher(text);
        while (bracketed.find()) {
            if (overlaps(found, bracketed.start(), bracketed.end())) {
                continue;
            }
            addOrdered(found, bracketed, bracketed.start(), bracketed.end());
        }

        Matcher bare = BARE.matcher(text);
        while (bare.find()) {
            if (overlaps(found, bare.start(), bare.end())) {
                continue;
            }
            addOrdered(found, bare, bare.start(), bare.end());
        }

        Matcher spaced = SPACED.matcher(text);
        while (spaced.find()) {
            if (overlaps(found, spaced.start(), spaced.end()) || !looksSpatial(spaced)) {
                continue;
            }
            addOrdered(found, spaced, spaced.start(), spaced.end());
        }

        found.sort(Comparator.comparingInt(ChatCoordinate::textPosStart));
        return found;
    }

    private static void addOrdered(List<ChatCoordinate> found, Matcher matcher, int start, int end) {
        Integer first = parse(matcher.group(1));
        Integer second = parse(matcher.group(2));
        Integer third = matcher.group(3) == null ? null : parse(matcher.group(3));

        if (first == null || second == null) {
            return;
        }

        if (third == null) {
            add(found, first, null, second, start, end);
        } else {
            add(found, first, second, third, start, end);
        }
    }

    private static void add(List<ChatCoordinate> found, @Nullable Integer x, @Nullable Integer y, @Nullable Integer z, int start, int end) {
        if (x == null || z == null) {
            return;
        }
        if (Math.abs(x) > HORIZONTAL_LIMIT || Math.abs(z) > HORIZONTAL_LIMIT) {
            return;
        }
        if (y != null && Math.abs(y) > VERTICAL_LIMIT) {
            return;
        }
        found.add(new ChatCoordinate(x, y, z, start, end));
    }

    private static boolean looksSpatial(Matcher matcher) {
        for (int group = 1; group <= matcher.groupCount(); group++) {
            Integer value = matcher.group(group) == null ? null : parse(matcher.group(group));
            if (value != null && Math.abs(value) >= SPACED_MIN_MAGNITUDE) {
                return true;
            }
        }
        return false;
    }

    private static boolean overlaps(List<ChatCoordinate> found, int start, int end) {
        for (ChatCoordinate coordinate : found) {
            if (start < coordinate.textPosEnd() && coordinate.textPosStart() < end) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable Integer parse(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String clipboardText() {
        return y == null ? x + " " + z : x + " " + y + " " + z;
    }
}
