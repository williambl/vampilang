package com.williambl.vampilang.stdlib;

import com.google.common.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.type.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StandardVTypes {
    public static final TypedVType<Double> NUMBER = VType.create(TypeToken.of(Double.class));
    public static final TypedVType<String> STRING = VType.create(TypeToken.of(String.class));
    public static final TypedVType<Boolean> BOOLEAN = VType.create(TypeToken.of(Boolean.class));
    public static final VTopTemplateType TEMPLATE_ANY = VType.createTopTemplate();
    private static final SimpleVType RAW_LIST = VType.create();
    public static final VParameterisedType LIST = VType.createParameterised(RAW_LIST, TEMPLATE_ANY);
    private static final SimpleVType RAW_MATCH_CASE = VType.create();
    public static final VParameterisedType MATCH_CASE = VType.createParameterised(RAW_MATCH_CASE, TEMPLATE_ANY, TEMPLATE_ANY.uniquise(new HashMap<>()));
    private static final SimpleVType RAW_OPTIONAL = VType.create();
    public static final VParameterisedType OPTIONAL = VType.createParameterised(RAW_OPTIONAL, TEMPLATE_ANY);
    private static final SimpleVType RAW_OPTIONAL_MAPPING = VType.create();
    public static final LambdaVType OPTIONAL_MAPPING = VType.createLambda(RAW_OPTIONAL_MAPPING, TEMPLATE_ANY.uniquise(new HashMap<>()), List.of(TEMPLATE_ANY.uniquise(new HashMap<>())), p -> new EvaluationContext.Spec(Map.of("unwrapped_optional", p.parameters.get(1))));

    public static void register(VEnvironment env) {
        env.registerType("number", NUMBER, Codec.DOUBLE);
        env.registerType("string", STRING, Codec.STRING);
        env.registerType("boolean", BOOLEAN, Codec.BOOL);
        env.registerType("list", RAW_LIST);
        env.registerType("match_case", RAW_MATCH_CASE); //TODO match cases need to be constructable (how also make them parameterised? probably make VParameterisedType and ConstructableVType interfaces)
        //noinspection unchecked
        env.registerCodecForParameterisedType(RAW_MATCH_CASE, paramed -> RecordCodecBuilder.<Map.Entry<Object, Object>>create(instance -> instance.group(
                ((Codec<Object>) env.rawCodecForType(paramed.parameters.get(0))).fieldOf("when").forGetter(Map.Entry::getKey),
                ((Codec<Object>) env.rawCodecForType(paramed.parameters.get(1))).fieldOf("then").forGetter(Map.Entry::getValue)
        ).apply(instance, Map::entry)));
        env.registerType("optional", RAW_OPTIONAL);
    }
}
