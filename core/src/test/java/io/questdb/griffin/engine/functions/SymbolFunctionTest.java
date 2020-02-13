/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2020 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.griffin.engine.functions;

import io.questdb.cairo.sql.Record;
import io.questdb.cairo.sql.SymbolTableSource;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.std.str.StringSink;
import io.questdb.test.tools.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class SymbolFunctionTest {
    // assert that all type casts that are not possible will throw exception

    private static final SymbolFunction function = new SymbolFunction(25) {
        @Override
        public CharSequence getSymbol(Record rec) {
            return "XYZ";
        }

        @Override
        public CharSequence valueOf(int symbolKey) {
            return "XYZ";
        }

        @Override
        public int getInt(Record rec) {
            return 0;
        }

        @Override
        public boolean isSymbolTableStatic() {
            return false;
        }

        @Override
        public void init(SymbolTableSource symbolTableSource, SqlExecutionContext executionContext) {
        }
    };

    @Test(expected = UnsupportedOperationException.class)
    public void testGetBin() {
        function.getBin(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetBinLen() {
        function.getBinLen(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetBool() {
        function.getBool(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetByte() {
        function.getByte(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDate() {
        function.getDate(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDouble() {
        function.getDouble(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetFloat() {
        function.getFloat(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetLong() {
        function.getLong(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMetadata() {
        function.getMetadata();
    }

    @Test
    public void testGetStr() {
        Assert.assertEquals("XYZ", function.getStr(null));
    }

    @Test
    public void testGetStrB() {
        Assert.assertEquals("XYZ", function.getStrB(null));
    }

    @Test
    public void testGetStrSink() {
        StringSink sink = new StringSink();
        function.getStr(null, sink);
        TestUtils.assertEquals("XYZ", sink);
    }

    @Test
    public void testGetPosition() {
        Assert.assertEquals(25, function.getPosition());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetRecordCursorFactory() {
        function.getRecordCursorFactory();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetShort() {
        function.getShort(null);
    }

    public void testGetStrLen() {
        Assert.assertEquals(3, function.getStrLen(null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetTimestamp() {
        function.getTimestamp(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testChar() {
        function.getChar(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLong256() {
        function.getLong256(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLong256A() {
        function.getLong256A(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLong256B() {
        function.getLong256B(null);
    }
}