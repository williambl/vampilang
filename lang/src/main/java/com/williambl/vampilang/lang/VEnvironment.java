package com.williambl.vampilang.lang;

import com.williambl.vampilang.lang.type.VType;

import java.util.Map;

public interface VEnvironment {
    <T> T evaluate(VExpression expr);
    void registerType(String name, VType type);
    Map<String, VType> allTypes();
}
