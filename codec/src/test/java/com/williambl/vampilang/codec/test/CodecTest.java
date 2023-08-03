package com.williambl.vampilang.codec.test;

import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import com.williambl.vampilang.codec.FunctionApplicationCodec;
import com.williambl.vampilang.codec.VTypeCodecRegistry;
import com.williambl.vampilang.codec.ValueCodec;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VTemplateType;
import com.williambl.vampilang.lang.type.VType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

public class CodecTest {
    @Test
    public void test() {
        var typeA = new VType();
        var typeB = new VType();
        var typeAOrB = new VTemplateType(Set.of(typeA, typeB));
        var codecRegistry = new VTypeCodecRegistryImpl();
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        var aOrBValueCodec = new ValueCodec(typeAOrB, codecRegistry);
        var res = aOrBValueCodec.decode(JsonOps.INSTANCE, JsonParser.parseString("3"));
        Assertions.assertTrue(res.result().isPresent());
        var resExpr = res.result().get().getFirst();
        Assertions.assertEquals(resExpr.evaluate(new EvaluationContext()).value(), 3);
    }

    @Test
    public void testWithFunction() {
        var typeA = new VType();
        var typeB = new VType();
        var boolType = new VType();
        var typeAOrB = new VTemplateType(Set.of(typeA, typeB));
        var codecRegistry = new VTypeCodecRegistryImpl();
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", typeAOrB, "b", typeAOrB), typeAOrB), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        codecRegistry.codecs.put(boolType, Codec.BOOL);
        codecRegistry.functions.put("if-else", ifElseFunction);
        {
            var codec = codecRegistry.expressionCodecForType(typeAOrB);
            var res = codec.decode(JsonOps.INSTANCE, JsonParser.parseString("{\"function\": \"if-else\", \"value\": {\"predicate\": true, \"a\": 3, \"b\": 5}}"));
            Assertions.assertTrue(res.result().isPresent());
            var resExpr = res.result().get().getFirst();
            Assertions.assertEquals(resExpr.evaluate(new EvaluationContext()).value(), 3);
        }
        {
            var codec = codecRegistry.expressionCodecForType(typeB);
            var res = codec.decode(JsonOps.INSTANCE, JsonParser.parseString("{\"function\": \"if-else\", \"value\": {\"predicate\": false, \"a\": \"aaa\", \"b\": \"bbb\"}}"));
            Assertions.assertTrue(res.result().isPresent());
            var resExpr = res.result().get().getFirst();
            Assertions.assertEquals(resExpr.evaluate(new EvaluationContext()).value(), "bbb");
        }
    }

    private static class VTypeCodecRegistryImpl implements VTypeCodecRegistry {
        private final Map<VType, Codec<?>> codecs = new HashMap<>();
        private final Map<String, VFunctionDefinition> functions = new HashMap<>();
        //TODO create our own dispatch codec that works like the mc registry ones (or just.. use the mc registry one)
        private final Codec<VExpression.FunctionApplication> functionCodec = new KeyDispatchCodec<>("function", Codec.STRING, (VExpression.FunctionApplication f) -> DataResult.success(f.function().name()), k -> Optional.ofNullable(this.functions.get(k)).map(f -> new FunctionApplicationCodec(f, this)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No function found"))).codec();

        @Override
        public Codec<?> rawCodecForType(VType type) {
            return this.codecs.get(type);
        }

        @Override
        public Map<VType, Codec<?>> allCodecs() {
            return Map.copyOf(this.codecs);
        }

        @Override
        public Codec<VExpression> expressionCodecForType(VType type) {
            return Codec.either(
                    new ValueCodec(type, this),
                            this.functionCodec.comapFlatMap(f -> type.contains(((VExpression.FunctionApplication) f.resolveTypes()).resolvedSignature().outputType()) ? DataResult.success(f) : DataResult.error(() -> "Unmatched type"),
                                    Function.identity()))
                    .xmap(e -> e.map(v -> v, v -> (VExpression) v), e -> e instanceof VExpression.Value value ? Either.left(value) : Either.right((VExpression.FunctionApplication) e));
        }
    }
}
