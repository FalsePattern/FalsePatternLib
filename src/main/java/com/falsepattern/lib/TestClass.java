package com.falsepattern.lib;

import com.falsepattern.json.node.JsonNode;

public class TestClass {
    public static void test() {
        FalsePatternLib.libLog.info(JsonNode.parse("{\"test\":\"hi\"}").get("test").stringValue());
    }
}
