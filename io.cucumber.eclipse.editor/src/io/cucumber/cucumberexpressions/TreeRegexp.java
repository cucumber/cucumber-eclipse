package io.cucumber.cucumberexpressions;

import static java.util.Collections.singleton;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * TreeRegexp represents matches as a tree of {@link Group}
 * reflecting the nested structure of capture groups in the original
 * regexp.
 */
//This is a copy of the TreeRegexp from cucumber to get parse regular expresions
final class TreeRegexp_ {
    private final Pattern pattern;
	private final GroupBuilder_ groupBuilder;

	TreeRegexp_(String regexp) {
        this(PatternCompilerProvider.getCompiler().compile(regexp, Pattern.UNICODE_CHARACTER_CLASS));
    }

	TreeRegexp_(Pattern pattern) {
        this.pattern = pattern;
        this.groupBuilder = createGroupBuilder(pattern);
    }

	private static GroupBuilder_ createGroupBuilder(Pattern pattern) {
        String source = pattern.pattern();
		GroupBuilder_ root = new GroupBuilder_(0, true);
		root.setEndIndex(source.length() - 1);
		Deque<GroupBuilder_> stack = new ArrayDeque<>(singleton(root));
        boolean escaping = false;
        boolean charClass = false;

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '[' && !escaping) {
                charClass = true;
            } else if (c == ']' && !escaping) {
                charClass = false;
            } else if (c == '(' && !escaping && !charClass) {
                boolean nonCapturing = isNonCapturingGroup(source, i);
				GroupBuilder_ groupBuilder = new GroupBuilder_(i, !nonCapturing);
                stack.push(groupBuilder);
            } else if (c == ')' && !escaping && !charClass) {
				GroupBuilder_ gb = stack.pop();
				gb.setEndIndex(i);
                if (gb.isCapturing()) {
					gb.setSource(source.substring(gb.getStartIndex() + 1, i));
                    stack.peek().add(gb);
                } else {
                    gb.moveChildrenTo(stack.peek());
                }
            }
            escaping = c == '\\' && !escaping;
        }
        return stack.pop();
    }

    private static boolean isNonCapturingGroup(String source, int i) {
        // Regex is valid. Bounds check not required.
        if (source.charAt(i+1) != '?') {
            // (X)
            return false;
        }
        if (source.charAt(i+2) != '<') {
            // (?:X)
            // (?idmsuxU-idmsuxU)
            // (?idmsux-idmsux:X)
            // (?=X)
            // (?!X)
            // (?>X)
            return true;
        }
        // (?<=X) or (?<!X) else (?<name>X)
        return source.charAt(i + 3) == '=' || source.charAt(i + 3) == '!';
    }

    Pattern pattern() {
        return pattern;
    }

    Group match(CharSequence s) {
        final Matcher matcher = pattern.matcher(s);
        if (!matcher.matches())
            return null;
        return groupBuilder.build(matcher, IntStream.rangeClosed(0, matcher.groupCount()).iterator());
    }

	public GroupBuilder_ getGroupBuilder() {
        return groupBuilder;
    }

}
