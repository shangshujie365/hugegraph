// Copyright 2017 HugeGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.baidu.hugegraph.graphdb.attribute;

import com.baidu.hugegraph.core.attribute.Cmp;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.baidu.hugegraph.core.attribute.Text.*;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */

public class TextTest {

    @Test
    public void testContains() {
        String text = "This world is full of 1funny surprises! A Full Yes";
        //Contains
        assertTrue(CONTAINS.test(text, "world"));
        assertTrue(CONTAINS.test(text, "wOrLD"));
        assertFalse(CONTAINS.test(text, "worl"));

        assertTrue(CONTAINS.test(text, "this"));
        assertTrue(CONTAINS.test(text, "yes"));
        assertFalse(CONTAINS.test(text, "funny"));

        assertFalse(CONTAINS.test(text, "a"));
        assertFalse(CONTAINS.test(text, "A"));

        assertTrue(CONTAINS.test(text, "surprises"));
        assertTrue(CONTAINS.test(text, "FULL"));

        assertTrue(CONTAINS.test(text, "full surprises"));
        assertTrue(CONTAINS.test(text, "full,surprises,world"));
        assertFalse(CONTAINS.test(text, "full bunny"));
        assertTrue(CONTAINS.test(text, "a world"));



        //Prefix
        assertTrue(CONTAINS_PREFIX.test(text, "worl"));
        assertTrue(CONTAINS_PREFIX.test(text, "wORl"));
        assertTrue(CONTAINS_PREFIX.test(text, "ye"));
        assertTrue(CONTAINS_PREFIX.test(text, "Y"));

        assertFalse(CONTAINS_PREFIX.test(text, "fo"));
        assertFalse(CONTAINS_PREFIX.test(text, "of 1f"));
        assertFalse(CONTAINS_PREFIX.test(text, "ses"));


        //Regex
        assertTrue(CONTAINS_REGEX.test(text, "fu[l]+"));
        assertTrue(CONTAINS_REGEX.test(text, "wor[ld]{1,2}"));
        assertTrue(CONTAINS_REGEX.test(text, "\\dfu\\w*"));

        assertFalse(CONTAINS_REGEX.test(text, "fo"));
        assertFalse(CONTAINS_REGEX.test(text, "wor[l]+"));
        assertFalse(CONTAINS_REGEX.test(text, "wor[ld]{3,5}"));


        String name = "fully funny";
        //Cmp
        assertTrue(Cmp.EQUAL.test(name.toString(), name));
        assertFalse(Cmp.NOT_EQUAL.test(name, name));
        assertFalse(Cmp.EQUAL.test("fullly funny", name));
        assertTrue(Cmp.NOT_EQUAL.test("fullly funny", name));

        //Prefix
        assertTrue(PREFIX.test(name, "fully"));
        assertTrue(PREFIX.test(name, "ful"));
        assertTrue(PREFIX.test(name, "fully fu"));
        assertFalse(PREFIX.test(name, "fun"));

        //REGEX
        assertTrue(REGEX.test(name, "(fu[ln]*y) (fu[ln]*y)"));
        assertFalse(REGEX.test(name, "(fu[l]*y) (fu[l]*y)"));
        assertTrue(REGEX.test(name, "(fu[l]*y) .*"));

    }

}
