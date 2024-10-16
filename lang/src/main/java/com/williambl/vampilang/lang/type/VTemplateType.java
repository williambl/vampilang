package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.VEnvironment;

import java.util.stream.Stream;

public sealed interface VTemplateType extends VType permits VDynamicTemplateType {
    Stream<VType> bounds(VEnvironment env);
}
