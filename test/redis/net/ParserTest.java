package redis.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parser.parse 的测试，规则见设计文档 §2.2 第 2、3 条。
 *
 * 读法：每个 @Test 方法是一个用例，JUnit 会逐个运行。
 * - assertArrayEquals(期望数组, 实际数组)：逐个元素比较，长度和内容都要一样
 * - assertThrows(异常类型.class, () -> 代码)：代码必须抛出这个类型的异常才算通过
 *
 * 注意 Java 字面量自身的转义：源码里的 "\\n" 是两个字符（反斜杠 + n），
 * 也就是用户在命令行里敲的样子；"\n" 才是一个真正的换行。
 */
class ParserTest {

    // 小工具：少写一层 new String[]{...}
    private static void assertParse(String line, String... expected) {
        assertArrayEquals(expected, Parser.parse(line));
    }

    // ---------- 按空格切分 ----------

    @Test
    void splitBySpace() {
        assertParse("SET name tom", "SET", "name", "tom");
    }

    @Test
    void singleWord() {
        assertParse("PING", "PING");
    }

    @Test
    void multipleSpacesCountAsOne() {
        assertParse("SET  name   tom", "SET", "name", "tom");
    }

    @Test
    void leadingAndTrailingSpacesIgnored() {
        assertParse("   SET name tom   ", "SET", "name", "tom");
    }

    @Test
    void keepsCase() {
        // 大小写原样保留，是否忽略大小写由 Dispatcher 决定，不是 Parser 的事
        assertParse("set Name TOM", "set", "Name", "TOM");
    }

    // ---------- 空行 ----------

    @Test
    void emptyLineReturnsEmptyArray() {
        assertEquals(0, Parser.parse("").length);
    }

    @Test
    void blankLineReturnsEmptyArray() {
        assertEquals(0, Parser.parse("     ").length);
    }

    // ---------- 双引号 ----------

    @Test
    void doubleQuotesKeepSpaces() {
        assertParse("SET msg \"hello world\"", "SET", "msg", "hello world");
    }

    @Test
    void emptyDoubleQuotesIsEmptyString() {
        // "" 是一个参数，内容为空串，不能丢
        assertParse("SET k \"\"", "SET", "k", "");
    }

    @Test
    void doubleQuotedFirstArg() {
        assertParse("\"SET\" k v", "SET", "k", "v");
    }

    // ---------- 双引号内的转义 ----------

    @Test
    void escapedDoubleQuote() {
        // 用户输入：SET k "a\"b"   →   值是 a"b
        assertParse("SET k \"a\\\"b\"", "SET", "k", "a\"b");
    }

    @Test
    void escapedBackslash() {
        // 用户输入：SET k "a\\b"   →   值是 a\b（一个反斜杠）
        assertParse("SET k \"a\\\\b\"", "SET", "k", "a\\b");
    }

    @Test
    void escapedNewline() {
        // 用户输入：SET k "a\nb"   →   值是 a、真换行、b
        assertParse("SET k \"a\\nb\"", "SET", "k", "a\nb");
    }

    @Test
    void escapedTab() {
        // 用户输入：SET k "a\tb"   →   值是 a、真制表符、b
        assertParse("SET k \"a\\tb\"", "SET", "k", "a\tb");
    }

    @Test
    void unknownEscapeKeepsChar() {
        // 未定义的转义 \x：去掉反斜杠，保留 x（和真 Redis 一致）
        assertParse("SET k \"a\\xb\"", "SET", "k", "axb");
    }

    // ---------- 单引号：原样，不转义 ----------

    @Test
    void singleQuotesKeepSpaces() {
        assertParse("SET msg 'hello world'", "SET", "msg", "hello world");
    }

    @Test
    void singleQuotesDoNotEscape() {
        // 用户输入：SET k 'a\nb'   →   值是 a、反斜杠、n、b 四个字符
        assertParse("SET k 'a\\nb'", "SET", "k", "a\\nb");
    }

    @Test
    void doubleQuoteInsideSingleQuotes() {
        // 单引号里的 " 只是普通字符
        assertParse("SET k 'it\"s'", "SET", "k", "it\"s");
    }

    @Test
    void emptySingleQuotesIsEmptyString() {
        assertParse("SET k ''", "SET", "k", "");
    }

    // ---------- 引号不配对 ----------

    @Test
    void unclosedDoubleQuoteThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Parser.parse("SET msg \"hello"));
    }

    @Test
    void unclosedSingleQuoteThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Parser.parse("SET msg 'hello"));
    }

    @Test
    void trailingBackslashInsideQuotesThrows() {
        // 用户输入：SET k "abc\   →   反斜杠把右引号"吃掉"了，引号没闭合
        assertThrows(IllegalArgumentException.class,
                () -> Parser.parse("SET k \"abc\\"));
    }

    @Test
    void closingQuoteMustBeFollowedBySpace() {
        // 用户输入：SET k "a"b   →   右引号后面紧跟字符，真 Redis 也报 unbalanced quotes
        assertThrows(IllegalArgumentException.class,
                () -> Parser.parse("SET k \"a\"b"));
    }
}
