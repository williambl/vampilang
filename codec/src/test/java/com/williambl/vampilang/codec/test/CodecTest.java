package com.williambl.vampilang.codec.test;

import com.google.common.collect.Sets;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import com.williambl.vampilang.codec.*;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VTemplateType;
import com.williambl.vampilang.lang.type.VType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CodecTest {
    @Test
    public void testDeserialiseSimpleExpression() {
        var typeA = VType.create();
        var typeB = VType.create();
        var typeAOrB = VType.createTemplate(typeA, typeB);
        var codecRegistry = new VTypeCodecRegistryImpl();
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        var aOrBValueCodec = new ValueCodec(typeAOrB, codecRegistry);
        var res = aOrBValueCodec.decode(JsonOps.INSTANCE, JsonParser.parseString("3"));
        Assertions.assertTrue(res.result().isPresent());
        var resExpr = res.result().get().getFirst();
        Assertions.assertEquals(3, resExpr.evaluate(new EvaluationContext()).value());
    }

    @Test
    public void testDeserialiseFunctionApplication() {
        var typeA = VType.create();
        var typeB = VType.create();
        var boolType = VType.create();
        var typeAOrB = VType.createTemplate(typeA, typeB);
        var codecRegistry = new VTypeCodecRegistryImpl();
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", typeAOrB, "b", typeAOrB), typeAOrB), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        codecRegistry.codecs.put(boolType, Codec.BOOL);
        codecRegistry.functions.put("if-else", ifElseFunction);
        {
            var codec = codecRegistry.expressionCodecForType(typeAOrB, new EvaluationContext.Spec());
            var res = codec.decode(JsonOps.INSTANCE, JsonParser.parseString("{\"function\": \"if-else\", \"value\": {\"predicate\": true, \"a\": 3, \"b\": 5}}"));
            Assertions.assertTrue(res.result().isPresent());
            var resExpr = res.result().get().getFirst();
            Assertions.assertEquals(3, resExpr.evaluate(new EvaluationContext()).value());
        }
        {
            var codec = codecRegistry.expressionCodecForType(typeB, new EvaluationContext.Spec());
            var res = codec.decode(JsonOps.INSTANCE, JsonParser.parseString("{\"function\": \"if-else\", \"value\": {\"predicate\": false, \"a\": \"aaa\", \"b\": \"bbb\"}}"));
            Assertions.assertTrue(res.result().isPresent());
            var resExpr = res.result().get().getFirst();
            Assertions.assertEquals("bbb", resExpr.evaluate(new EvaluationContext()).value());
        }
    }

    @Test
    public void testDeserialiseSimpleExpressionWithParameterisedType() {
        var typeA = VType.create();
        var typeB = VType.create();
        var typeAOrB = VType.createTemplate(typeA, typeB);
        var bareListType = VType.create();
        var listType = VType.createParameterised(bareListType, typeAOrB);
        var aListType = listType.with(0, typeA);
        var codecRegistry = new VTypeCodecRegistryImpl();
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        codecRegistry.codecs.put(aListType, Codec.INT.listOf());
        var listValueCodec = new ValueCodec(listType, codecRegistry);
        var res = listValueCodec.decode(JsonOps.INSTANCE, JsonParser.parseString("[3, 4, 5]"));
        Assertions.assertTrue(res.result().isPresent());
        var resExpr = res.result().get().getFirst();
        Assertions.assertEquals(List.of(3, 4, 5), resExpr.evaluate(new EvaluationContext()).value());
    }

    @Test
    public void testDeserialiseSimpleExpressionWithParameterisedTypeWithDynamicCodec() {
        var typeA = VType.create();
        var typeB = VType.create();
        var typeAOrB = VType.createTemplate(typeA, typeB);
        var bareListType = VType.create();
        var listType = VType.createParameterised(bareListType, typeAOrB);
        var codecRegistry = new VTypeCodecRegistryImpl();
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        codecRegistry.parameterisedTypeCodecs.put(bareListType, t -> codecRegistry.rawCodecForType(t.parameters.get(0)).listOf());
        var listValueCodec = new ValueCodec(listType, codecRegistry);
        {
            var res = listValueCodec.decode(JsonOps.INSTANCE, JsonParser.parseString("[3, 4, 5]"));
            Assertions.assertTrue(res.result().isPresent());
            var resExpr = res.result().get().getFirst();
            Assertions.assertEquals(List.of(3, 4, 5), resExpr.evaluate(new EvaluationContext()).value());
        }
        {
            var res = listValueCodec.decode(JsonOps.INSTANCE, JsonParser.parseString("[\"hi\", \"test\"]"));
            Assertions.assertTrue(res.result().isPresent());
            var resExpr = res.result().get().getFirst();
            Assertions.assertEquals(List.of("hi", "test"), resExpr.evaluate(new EvaluationContext()).value());
        }
    }

    @Test
    public void testSerialiseFunctionApplication() {
        var typeA = VType.create();
        var typeB = VType.create();
        var boolType = VType.create();
        var typeAOrB = VType.createTemplate(typeA, typeB);
        var codecRegistry = new VTypeCodecRegistryImpl();
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", typeAOrB, "b", typeAOrB), typeAOrB), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        codecRegistry.codecs.put(boolType, Codec.BOOL);
        codecRegistry.functions.put("if-else", ifElseFunction);
        {
            var codec = codecRegistry.expressionCodecForType(typeAOrB, new EvaluationContext.Spec());
            var res = codec.encodeStart(JsonOps.INSTANCE, VExpression.functionApplication(ifElseFunction, Map.of(
                    "predicate", VExpression.value(boolType, true),
                    "a", VExpression.value(typeB, ":)"),
                    "b", VExpression.value(typeB, ":(")
            )));
            Assertions.assertTrue(res.result().isPresent());
            var resJson = res.result().get();
            Assertions.assertEquals(JsonParser.parseString("{\"function\": \"if-else\", \"value\": {\"predicate\": true, \"a\": \":)\", \"b\": \":(\"}}"), resJson);
        }
    }

    @Test
    public void testSerialiseFunctionApplicationWithParameterisedTypes() {
        var typeA = VType.create();
        var typeB = VType.create();
        var boolType = VType.create();
        var typeAOrB = VType.createTemplate(typeA, typeB);
        var bareListType = VType.create();
        var listType = VType.createParameterised(bareListType, typeAOrB);
        var aListType = listType.with(0, typeA);
        var bListType = listType.with(0, typeB);
        var codecRegistry = new VTypeCodecRegistryImpl();
        var getFunction = new VFunctionDefinition("get", new VFunctionSignature(Map.of("index", typeA, "a", listType), typeAOrB), (ctx, sig, a) -> new VValue(sig.outputType(), ((List<?>) a.get("a").value()).get((Integer) a.get("index").value())));
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        codecRegistry.codecs.put(boolType, Codec.BOOL);
        codecRegistry.codecs.put(aListType, Codec.INT.listOf());
        codecRegistry.codecs.put(bListType, Codec.STRING.listOf());
        codecRegistry.functions.put("get", getFunction);
        {
            var codec = codecRegistry.expressionCodecForType(typeAOrB, new EvaluationContext.Spec());
            var res = codec.encodeStart(JsonOps.INSTANCE, VExpression.functionApplication(getFunction, Map.of(
                    "index", VExpression.value(typeA, 0),
                    "a", VExpression.value(bListType, List.of(":)"))
            )));
            Assertions.assertTrue(res.result().isPresent());
            var resJson = res.result().get();
            Assertions.assertEquals(JsonParser.parseString("{\"function\": \"get\", \"value\": {\"index\": 0, \"a\": [\":)\"]}}"), resJson);
        }
    }

    @Test
    public void testDeserialiseVariableReference() {
        var typeA = VType.create();
        var typeB = VType.create();
        var typeAOrB = VType.createTemplate(typeA, typeB);
        var codecRegistry = new VTypeCodecRegistryImpl();
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        {
            var spec = new EvaluationContext.Spec(Map.of("my_var", typeA));
            var codec = codecRegistry.expressionCodecForType(typeAOrB, spec);
            var res = codec.decode(JsonOps.INSTANCE, JsonParser.parseString("{\"var\": \"my_var\"}"));
            Assertions.assertTrue(res.result().isPresent());
            var resExpr = res.result().get().getFirst();
            Assertions.assertEquals(3, resExpr.evaluate(EvaluationContext.builder(spec).addVariable("my_var", new VValue(typeA, 3)).build()).value());
        }
        {
            var spec = new EvaluationContext.Spec(Map.of("my_var", typeB));
            var codec = codecRegistry.expressionCodecForType(typeAOrB, spec);
            var res = codec.decode(JsonOps.INSTANCE, JsonParser.parseString("{\"var\": \"my_var\"}"));
            Assertions.assertTrue(res.result().isPresent());
            var resExpr = res.result().get().getFirst();
            Assertions.assertEquals("bbb", resExpr.evaluate(EvaluationContext.builder(spec).addVariable("my_var", new VValue(typeB, "bbb")).build()).value());
        }
    }

    @Test
    public void testSerialiseVariableReference() {
        var typeA = VType.create();
        var typeB = VType.create();
        var typeAOrB = VType.createTemplate(typeA, typeB);
        var codecRegistry = new VTypeCodecRegistryImpl();
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        {
            var codec = codecRegistry.expressionCodecForType(typeAOrB, new EvaluationContext.Spec());
            var res = codec.encodeStart(JsonOps.INSTANCE, VExpression.variable("my_var"));
            Assertions.assertTrue(res.result().isPresent());
            var resJson = res.result().get();
            Assertions.assertEquals(JsonParser.parseString("{\"var\": \"my_var\"}"), resJson);
        }
    }

    @Test
    public void testDeserialiseFunctionApplicationWithVariableReference() {
        var typeA = VType.create();
        var typeB = VType.create();
        var boolType = VType.create();
        var typeAOrB = VType.createTemplate(typeA, typeB);
        var codecRegistry = new VTypeCodecRegistryImpl();
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", typeAOrB, "b", typeAOrB), typeAOrB), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        codecRegistry.codecs.put(typeA, Codec.INT);
        codecRegistry.codecs.put(typeB, Codec.STRING);
        codecRegistry.codecs.put(boolType, Codec.BOOL);
        codecRegistry.functions.put("if-else", ifElseFunction);
        {
            var spec = new EvaluationContext.Spec(Map.of("my_var", boolType));
            var codec = codecRegistry.expressionCodecForType(typeAOrB, spec);
            var res = codec.decode(JsonOps.INSTANCE, JsonParser.parseString("{\"function\": \"if-else\", \"value\": {\"predicate\": {\"var\": \"my_var\"}, \"a\": 3, \"b\": 5}}"));
            Assertions.assertTrue(res.result().isPresent());
            var resExpr = res.result().get().getFirst();
            Assertions.assertEquals(3, resExpr.resolveTypes(spec).evaluate(EvaluationContext.builder(spec).addVariable("my_var", new VValue(boolType, true)).build()).value());
        }
    }

    private static class VTypeCodecRegistryImpl implements VTypeCodecRegistry {
        private final Map<VType, Codec<?>> codecs = new HashMap<>();
        private final Map<VType, Function<VParameterisedType, Codec<?>>> parameterisedTypeCodecs = new HashMap<>();
        private final Map<String, VFunctionDefinition> functions = new HashMap<>();

        @Override
        public Codec<?> rawCodecForType(VType type) {
            var res = this.codecs.get(type);
            if (res == null && type instanceof VParameterisedType paramed && this.parameterisedTypeCodecs.containsKey(paramed.bareType)) {
                var codec = this.parameterisedTypeCodecs.get(paramed.bareType).apply(paramed);
                this.codecs.put(paramed, codec);
                return codec;
            }

            return res;
        }

        @Override
        public Map<VType, Codec<?>> codecsMatching(VType type) {
            return this.allTypesMatching(type).stream().map(t -> Optional.ofNullable(this.rawCodecForType(t)).map(v -> Map.entry(t, v))).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));
        }

        public Set<VType> allTypesMatching(VType type) {
            Set<VType> set = new HashSet<>();
            if (type instanceof VTemplateType template) {
                if (template.bounds == null) {
                    set.addAll(this.codecs.keySet());
                } else {
                    set.addAll(template.bounds.stream().map(this::allTypesMatching).flatMap(Set::stream).toList());
                }
            } else if (type instanceof VParameterisedType paramed) {
                List<Set<VType>> typesMatchingEachParam = new ArrayList<>();
                for (int i = 0; i < paramed.parameters.size(); i++) {
                    typesMatchingEachParam.add(this.allTypesMatching(paramed.parameters.get(i)));
                }
                Sets.cartesianProduct(typesMatchingEachParam).forEach(assignment -> set.add(paramed.with(assignment)));
            } else {
                set.add(type);
            }

            return set;
        }

        @Override
        public Codec<VExpression> expressionCodecForType(VType type, EvaluationContext.Spec spec) {
            return new VExpressionCodec(
                    //TODO cache
                    new ValueCodec(type, this),
                    new KeyDispatchCodec<>( //TODO holy shit
                            "function",
                            Codec.STRING,
                            (VExpression.FunctionApplication f) -> DataResult.success(f.function().name()),
                            k -> Optional.ofNullable(this.functions.get(k))
                                    .map(f -> new FunctionApplicationCodec(f, this, spec))
                                    .map(DataResult::success)
                                    .orElseGet(() -> DataResult.error(() -> "No function found")))
                            .codec()
                            .comapFlatMap(f -> type.contains(((VExpression.FunctionApplication) f.resolveTypes(spec)).resolvedSignature().outputType())
                                    ? DataResult.success(f)
                                    : DataResult.error(() -> "Unmatched type"),
                                    Function.identity()),
                    VariableRefCodec.CODEC);
        }
    }
}
