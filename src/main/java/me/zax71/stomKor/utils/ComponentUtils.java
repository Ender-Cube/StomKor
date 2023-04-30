package me.zax71.stomKor.utils;

// Credit: https://www.spigotmc.org/threads/free-code-sending-perfectly-centered-chat-message.95872/

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public final class ComponentUtils {

    private ComponentUtils() {
        //not called
    }

    private final static int CENTER_PX = 154;

    /**
     * Changes a number of milliseconds to the HMMSS.SSS format
     * @param milliseconds The number of milliseconds
     * @return HMMSSS.SSS formatted String
     */
    public static String toHumanReadableTime(Long milliseconds) {
        if (milliseconds < 60000) {
            return zeroPrefix(milliseconds / 1000.0) + "s";

        } else if (milliseconds < 60000*60) {
            return zeroPrefix(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
                    + ":"
                    + zeroPrefix(milliseconds / 1000.0 - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)))
                    + "s";

        } else {
            return zeroPrefix(TimeUnit.MILLISECONDS.toHours(milliseconds))
                    + ":"
                    + zeroPrefix(TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)))
                    + ":"
                    + zeroPrefix(milliseconds / 1000.0 - (TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds))))
                    + "s";
        }

    }

    private static String zeroPrefix(Double number) {
        if (number < 10) {
            return "0" + new DecimalFormat("#.###").format(number);
        } else {
            return new DecimalFormat("#.###").format(number);
        }
    }

    private static String zeroPrefix(Long number) {
        if (number < 10) {
            return "0" + number;
        } else {
            return String.valueOf(number);
        }
    }

    /**
     * Centers a component
     * @param component The component to be centered
     * @return The component prefixed with spaces
     */
    // Yes, I know it is very scuffed
    public static @NotNull Component centerComponent(final @NotNull Component component) {
        return Component.text(spacePrefix(LegacyComponentSerializer.legacySection().serialize(component)))
                .append(component);
    }

    public static @NotNull String spacePrefix(final @NotNull String legacyTextMessage) {
        return spacePrefix(measureLegacyText(legacyTextMessage));
    }

    private static int measureLegacyText(final @NotNull String legacyTextMessage) {
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (final char c : legacyTextMessage.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                final DefaultFontInfo info = DefaultFontInfo.forCharacter(c);
                messagePxSize += info.length(isBold);
                messagePxSize++;
            }
        }
        return messagePxSize;
    }

    private static @NotNull String spacePrefix(final int messagePxSize) {
        final int halvedMessageSize = messagePxSize / 2;
        final int toCompensate = CENTER_PX - halvedMessageSize;
        final int spaceLength = DefaultFontInfo.SPACE.length() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb.toString();
    }

    public enum DefaultFontInfo {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private final char character;
        private final int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public static DefaultFontInfo forCharacter(char c) {
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                if (dFI.character() == c) {
                    return dFI;
                }
            }
            return DefaultFontInfo.DEFAULT;
        }

        public char character() {
            return this.character;
        }

        public int length() {
            return this.length;
        }

        public int boldLength() {
            if (this == DefaultFontInfo.SPACE) {
                return this.length();
            }
            return this.length + 1;
        }

        public int length(final boolean bold) {
            return bold ? this.boldLength() : this.length();
        }
    }
}
