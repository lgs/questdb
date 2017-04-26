package com.questdb.json;

import com.questdb.misc.Chars;
import com.questdb.misc.Unsafe;
import com.questdb.std.IntStack;
import com.questdb.std.Mutable;
import com.questdb.test.tools.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonLexerTest {

    private final JsonLexer parser = new JsonLexer();
    private final JsonAssemblingListener listener = new JsonAssemblingListener();

    @Before
    public void setUp() throws Exception {
        parser.clear();
        listener.clear();
    }

    @Test
    public void testArrayObjArray() throws Exception {
        assertThat("[{\"A\":[\"122\",\"133\"],\"x\":\"y\"},\"134\",\"abc\"]", "[\n" +
                "{\"A\":[122, 133], \"x\": \"y\"}, 134  , \"abc\"\n" +
                "]");
    }

    @Test
    public void testDanglingArrayEnd() throws Exception {
        assertError("Dangling ]", 8, "[1,2,3]]");
    }

    @Test
    public void testDanglingComma() throws Exception {
        assertError("Attribute name expected", 12, "{\"x\": \"abc\",}");
    }

    @Test
    public void testDanglingObjectEnd() throws Exception {
        assertError("Dangling }", 8, "[1,2,3]}");
    }

    @Test
    public void testEmptyArray() throws Exception {
        assertThat("[]", "[]");
    }

    @Test
    public void testEmptyObject() throws Exception {
        assertThat("{}", "{}");
    }

    @Test
    public void testExponent() throws Exception {
        assertThat("[\"-1.34E4\",\"3\"]", "[-1.34E4,3]");
    }

    @Test
    public void testIncorrectArrayStart() throws Exception {
        assertError("[ is not expected here", 3, "[1[]]");
    }

    @Test
    public void testInvalidObjectNesting() throws Exception {
        assertError("{ is not expected here", 11, "{\"a\":\"x\", {}}");
    }

    @Test
    public void testMisplacedArrayEnd() throws Exception {
        assertError("] is not expected here. You have non-terminated object", 18, "{\"a\":1, \"b\": 15.2]}");
    }

    @Test
    public void testMisplacedColon() throws Exception {
        assertError("Misplaced ':'", 9, "{\"a\":\"x\":}");
    }

    @Test
    public void testMisplacedQuote() throws Exception {
        assertError("Unexpected quote '\"'", 9, "{\"a\":\"1\"\", \"b\": 15.2}");
    }

    @Test
    public void testMisplacesObjectEnd() throws Exception {
        assertError("} is not expected here. You have non-terminated array", 7, "[1,2,3}");
    }

    @Test
    public void testMissingArrayValue() throws Exception {
        assertError("Unexpected comma", 2, "[,]");
    }

    @Test
    public void testMissingAttributeValue() throws Exception {
        assertError("Attribute value expected", 6, "{\"x\": }");
    }

    @Test
    public void testNestedObjNestedArray() throws Exception {
        assertThat("{\"x\":{\"y\":[[\"1\",\"2\",\"3\"],[\"5\",\"2\",\"3\"],[\"0\",\"1\"]],\"a\":\"b\"}}", "{\"x\": { \"y\": [[1,2,3], [5,2,3], [0,1]], \"a\":\"b\"}}");
    }

    @Test
    public void testNestedObjects() throws Exception {
        assertThat("{\"abc\":{\"x\":\"123\"},\"val\":\"000\"}", "{\"abc\": {\"x\":\"123\"}, \"val\": \"000\"}");
    }

    @Test
    public void testQuoteEscape() throws Exception {
        assertThat("{\"x\":\"a\\\"bc\"}", "{\"x\": \"a\\\"bc\"}");
    }

    @Test
    public void testSimpleJson() throws Exception {
        assertThat("{\"abc\":\"123\"}", "{\"abc\": \"123\"}");
    }

    @Test
    public void testUnclosedQuote() throws Exception {
        assertError("Unexpected symbol", 11, "{\"a\":\"1, \"b\": 15.2}");
    }

    @Test
    public void testUnquotedNumbers() throws Exception {
        assertThat("[{\"A\":\"122\"},\"134\",\"abc\"]", "[\n" +
                "{\"A\":122}, 134  , \"abc\"\n" +
                "]");
    }

    @Test
    public void testWrongQuote() throws Exception {
        assertError("Unexpected symbol", 10, "{\"x\": \"a\"bc\",}");
    }

    private void assertError(String expected, int expectedPosition, String input) throws JsonException {
        int len = input.length();
        long address = Unsafe.malloc(len);
        try {
            Chars.strcpy(input, len, address);
            try {
                parser.parse(address, len, listener);
                Assert.fail();
            } catch (JsonException e) {
                Assert.assertEquals(expected, e.getMessage());
                Assert.assertEquals(expectedPosition, e.getPosition());
            }
        } finally {
            Unsafe.free(address, len);
        }
    }

    private void assertThat(String expected, String input) throws JsonException {
        int len = input.length();
        long address = Unsafe.malloc(len);
        try {
            Chars.strcpy(input, len, address);
            parser.parse(address, len, listener);
            TestUtils.assertEquals(expected, listener.value());
        } finally {
            Unsafe.free(address, len);
        }
    }

    private static class JsonAssemblingListener implements JsonListener, Mutable {
        private final StringBuffer buffer = new StringBuffer();
        private final IntStack itemCountStack = new IntStack();
        private int itemCount = 0;

        @Override
        public void clear() {
            buffer.setLength(0);
            itemCount = 0;
            itemCountStack.clear();
        }

        @Override
        public void onEvent(int code, CharSequence tag) {
            switch (code) {
                case JsonLexer.EVT_OBJ_START:
                    if (itemCount++ > 0) {
                        buffer.append(',');
                    }
                    buffer.append('{');
                    itemCountStack.push(itemCount);
                    itemCount = 0;
                    break;
                case JsonLexer.EVT_OBJ_END:
                    buffer.append('}');
                    itemCount = itemCountStack.pop();
                    break;
                case JsonLexer.EVT_ARRAY_START:
                    if (itemCount++ > 0) {
                        buffer.append(',');
                    }
                    buffer.append('[');
                    itemCountStack.push(itemCount);
                    itemCount = 0;
                    break;
                case JsonLexer.EVT_ARRAY_END:
                    itemCount = itemCountStack.pop();
                    buffer.append(']');
                    break;
                case JsonLexer.EVT_NAME:
                    if (itemCount > 0) {
                        buffer.append(',');
                    }
                    buffer.append('"');
                    buffer.append(tag);
                    buffer.append('"');
                    buffer.append(':');
                    break;
                case JsonLexer.EVT_VALUE:
                    buffer.append('"');
                    buffer.append(tag);
                    buffer.append('"');
                    itemCount++;
                    break;
                case JsonLexer.EVT_ARRAY_VALUE:
                    if (itemCount++ > 0) {
                        buffer.append(',');
                    }
                    buffer.append('"');
                    buffer.append(tag);
                    buffer.append('"');
                    break;
                default:
                    break;
            }
        }

        public CharSequence value() {
            return buffer;
        }
    }
}