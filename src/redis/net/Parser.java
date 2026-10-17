package redis.net;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static String[] parse(String line) {
        List<String> args = new ArrayList<>();
        int i = 0;
        int n = line.length();
        while (i < n) {
            char ch = line.charAt(i);
            if (ch == ' ') { // 参数之间的空格，连续多个也只算分隔
                i++;
                continue;
            }

            StringBuilder arg = new StringBuilder();
            if (ch == '"' || ch == '\'') {
                i = readQuoted(line, i, arg);
                // 右引号后面只能是空格或行尾，否则视为引号不配对
                if (i < n && line.charAt(i) != ' ') {
                    throw new IllegalArgumentException("unbalanced quotes");
                }
            } else {
                while (i < n && line.charAt(i) != ' ') {
                    arg.append(line.charAt(i++));
                }
            }
            args.add(arg.toString());
        }
        return args.toArray(new String[0]);
    }

    // 从左引号 start 开始读到右引号，内容写入 arg，返回右引号之后的位置
    private static int readQuoted(String line, int start, StringBuilder arg) {
        char quote = line.charAt(start);
        int i = start + 1;
        while (i < line.length()) {
            char ch = line.charAt(i++);
            if (ch == quote) return i;
            if (quote == '"' && ch == '\\' && i < line.length()) {
                arg.append(unescape(line.charAt(i++)));
            } else {
                arg.append(ch);
            }
        }
        // 走到行尾还没遇到右引号（含 "abc\ 这种反斜杠吃掉右引号的情况）
        throw new IllegalArgumentException("unbalanced quotes");
    }

    private static char unescape(char ch) {
        switch (ch) {
            case 'n': return '\n';
            case 't': return '\t';
            default:  return ch; // \" \\ 以及未定义的 \x 都保留字符本身
        }
    }
}
