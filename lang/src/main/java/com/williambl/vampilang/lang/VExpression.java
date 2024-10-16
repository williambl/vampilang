package com.williambl.vampilang.lang;

import com.mojang.serialization.DataResult;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.ConstructableVType;
import com.williambl.vampilang.lang.type.LambdaVType;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface VExpression {
    public static VExpression functionApplication(VFunctionDefinition function, Map<String, VExpression> inputs) {
        return new FunctionApplication(function, null, inputs);
    }

    public static VExpression app(VFunctionDefinition function, Map<String, VExpression> inputs) {
        return functionApplication(function, inputs);
    }

    public static VExpression value(VType type, Object value) {
        return new Value(new VValue(type, value));
    }

    public static VExpression val(VType type, Object value) {
        return value(type, value);
    }

    public static VExpression variable(String name) {
        return new VariableRef(name, null);
    }

    public static VExpression var(String name) {
        return variable(name);
    }

    static VExpression object(String typeName, Map<String, VExpression> properties) {
        return new ObjectConstruction(typeName, properties, null);
    }

    static VExpression list(List<VExpression> entries) {
        return new ListConstruction(null, entries);
    }

    static VExpression list(VExpression... entries) {
        return new ListConstruction(null, List.of(entries));
    }

    static VExpression list(VParameterisedType type) {
        return new ListConstruction(type, List.of());
    }

    static VExpression lambda(LambdaVType type, VExpression expr) {
        return new Lambda(type, expr);
    }

    public record FunctionApplication(VFunctionDefinition function, @Nullable VFunctionSignature resolvedSignature, Map<String, VExpression> inputs) implements VExpression {

        @Override
        public DataResult<VExpression> resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            var resolvedInputsRes = bubbleUp(this.inputs.entrySet().stream()
                    .map(kv -> kv.getValue().resolveTypes(env, spec).map(v -> Map.entry(kv.getKey(), v)))).map(s ->
                    s.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            return resolvedInputsRes.flatMap(resolvedInputs -> {
                var resolvedFunctionSignature = this.function.signature()
                        .uniquise()
                        .resolveTypes(env, resolvedInputs.entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, kv -> kv.getValue().type())));
                return resolvedFunctionSignature.map(sig -> new FunctionApplication(this.function, sig, resolvedInputs));
            });
        }

        @Override
        public VType type() {
            return this.resolvedSignature == null ? this.function.signature().outputType() : this.resolvedSignature.outputType();
        }

        @Override
        public String toString(TypeNamer ctx) {
            var builder = new StringBuilder();
            builder.append("(function ");
            builder.append(this.function.name());
            builder.append(" ");
            var inputs = new ArrayList<>(this.inputs.entrySet());
            inputs.sort(Map.Entry.comparingByKey());
            for (var input : inputs) {
                builder.append(input.getKey());
                builder.append(" = ");
                builder.append(input.getValue().toString(ctx));
                builder.append(" ");
            }
            builder.append(": ");
            builder.append((this.resolvedSignature == null ? this.function.signature().uniquise() : this.resolvedSignature).toString(ctx));
            builder.append(")");
            return builder.toString();
        }

        @Override
        public VValue evaluate(EvaluationContext ctx) {
            if (this.resolvedSignature == null) {
                throw new UnsupportedOperationException("Cannot evaluate unresolved expression!");
            }

            Map<String, VValue> evaluatedInputs = new HashMap<>();
            return this.function.function().apply(ctx, this.resolvedSignature, s -> evaluatedInputs.computeIfAbsent(s, k -> this.inputs.get(k).evaluate(ctx)));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            FunctionApplication that = (FunctionApplication) o;
            return Objects.equals(this.function, that.function) && Objects.equals(this.resolvedSignature, that.resolvedSignature) && Objects.equals(this.inputs, that.inputs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.function, this.resolvedSignature, this.inputs);
        }
    }
    public record Value(VValue value) implements VExpression {

        @Override
        public DataResult<VExpression> resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            return DataResult.success(this);
        }
        @Override
        public VType type() {
            return this.value.type();
        }

        @Override
        public String toString(TypeNamer ctx) {
            return "(value %s : %s)".formatted(this.value.value(), this.type().toString(ctx));
        }

        @Override
        public VValue evaluate(EvaluationContext ctx) {
            return this.value();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            Value value1 = (Value) o;
            return Objects.equals(this.value, value1.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.value);
        }
    }

    public record VariableRef(String name, @Nullable VType resolvedType) implements VExpression {

        @Override
        public DataResult<VExpression> resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            return Optional.ofNullable(spec.nullableTypeOf(this.name))
                    .map(type -> DataResult.success((VExpression) new VariableRef(this.name, type)))
                    .orElse(DataResult.error(() -> "No variable with name "+this.name));
        }

        @Override
        public VType type() {
            return this.resolvedType == null ? VType.createTemplate() : this.resolvedType;
        }

        @Override
        public VValue evaluate(EvaluationContext ctx) {
            return ctx.getVariable(this.name, this.type());
        }

        @Override
        public String toString(TypeNamer ctx) {
            return "(variable %s : %s)".formatted(this.name, this.type().toString(ctx));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            VariableRef that = (VariableRef) o;
            return Objects.equals(this.name, that.name) && Objects.equals(this.resolvedType, that.resolvedType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.resolvedType);
        }
    }

    public record ObjectConstruction(String typeName, Map<String, VExpression> properties, @Nullable ConstructableVType<?> resolvedType) implements VExpression {

        @Override
        public DataResult<VExpression> resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            var resolvedPropertiesRes = bubbleUp(this.properties.entrySet().stream()
                    .map(kv -> kv.getValue().resolveTypes(env, spec).map(v -> Map.entry(kv.getKey(), v)))).map(s ->
                    s.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            return resolvedPropertiesRes.flatMap(resolvedProperties -> {
                var resultType = env.getType(this.typeName);
                if (!(resultType instanceof ConstructableVType<?> constructableVType)) {
                    return DataResult.error(() -> "Type %s must be constructable to be used in an object literal expression".formatted(this.typeName));
                }
                var expectedPropertyTypes = constructableVType.propertyTypes;
                for (var key : expectedPropertyTypes.keySet()) {
                    var expected = expectedPropertyTypes.get(key);
                    var actual = resolvedProperties.get(key);
                    if (actual == null || !expected.contains(actual.type(), env)) {
                        return DataResult.error(() -> "Argument %s should be of type %s!".formatted(key, expected));
                    }
                }
                return DataResult.success(new ObjectConstruction(this.typeName, resolvedProperties, constructableVType));
            });
        }

        @Override
        public VType type() {
            if (this.resolvedType == null) {
                throw new NoSuchElementException("Type not yet resolved");
            }

            return this.resolvedType;
        }

        @Override
        public VValue evaluate(EvaluationContext ctx) {
            if (this.resolvedType == null) {
                throw new UnsupportedOperationException("Cannot evaluate unresolved expression!");
            }

            var propertiesValues = this.properties.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    kv -> kv.getValue().evaluate(ctx)));

            var result = this.resolvedType.constructor.apply(propertiesValues);

            return VValue.value(this.resolvedType, result, ctx.env());
        }

        @Override
        public String toString(TypeNamer ctx) {
            var builder = new StringBuilder();
            builder.append("(object ");
            var inputs = new ArrayList<>(this.properties.entrySet());
            inputs.sort(Map.Entry.comparingByKey());
            for (var input : inputs) {
                builder.append(input.getKey());
                builder.append(" = ");
                builder.append(input.getValue().toString(ctx));
                builder.append(" ");
            }
            builder.append(": ");
            builder.append(this.typeName);
            builder.append(")");
            return builder.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            ObjectConstruction that = (ObjectConstruction) o;
            return Objects.equals(this.typeName, that.typeName) && Objects.equals(this.properties, that.properties) && Objects.equals(this.resolvedType, that.resolvedType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.typeName, this.properties, this.resolvedType);
        }
    }

    public record ListConstruction(@Nullable VParameterisedType resolvedType, List<VExpression> entries) implements VExpression {
        @Override
        public DataResult<VExpression> resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            var listType = env.listType();
            if (listType == null) {
                return DataResult.error(() -> "No list type in environment!");
            }

            if (this.entries.isEmpty()) {
                return this.resolvedType != null
                        ? (this.resolvedType.bareType.equals(listType.bareType)
                                ? DataResult.success(this)
                                : DataResult.error(() -> "List has non-list resolved type! (type: %s. list<> type: %s)".formatted(this.resolvedType, listType)))
                        : DataResult.error(() -> "Empty list has no set type!"); //todo should this just return plain list type?
            }

            return VExpression.bubbleUp(this.entries.stream().map(e -> e.resolveTypes(env, spec))).map(Stream::toList).flatMap(resolvedEntries -> {
                if (resolvedEntries.isEmpty()) {
                    return DataResult.success(new ListConstruction(listType, resolvedEntries));
                }
                VType mostSpecificCommonSupertype = null;
                var allTypes = env.allTypes().values();
                supertypes: for (var possibleSupertype : allTypes) {
                    for (var entry : resolvedEntries) {
                        if (!possibleSupertype.contains(entry.type(), env)) {
                            continue supertypes;
                        }
                    }
                    if (mostSpecificCommonSupertype == null || mostSpecificCommonSupertype.contains(possibleSupertype, env)) {
                        mostSpecificCommonSupertype = possibleSupertype;
                    }
                }
                if (mostSpecificCommonSupertype == null) {
                    String err = "No common supertype found for types [%s] in list!".formatted(resolvedEntries.stream().map(VExpression::type).map(Object::toString).collect(Collectors.joining(", ")));
                    return DataResult.error(() -> err);
                }

                return DataResult.success(new ListConstruction(listType.with(0, mostSpecificCommonSupertype), resolvedEntries));
            });
        }

        @Override
        public VType type() {
            if (this.resolvedType == null) {
                throw new NoSuchElementException("Type not yet resolved");
            }

            return this.resolvedType;
        }

        @Override
        public VValue evaluate(EvaluationContext ctx) {
            var evaluatedEntries = this.entries.stream().map(e -> e.evaluate(ctx)).toList();
            return VValue.value(this.resolvedType(), evaluatedEntries, ctx.env());
        }

        @Override
        public String toString(TypeNamer ctx) {
            var builder = new StringBuilder();
            builder.append("(list ");
            for (var entry : this.entries) {
                builder.append(entry.toString(ctx));
                builder.append(" ");
            }
            builder.append(": ");
            builder.append(this.resolvedType == null ? "?" : this.resolvedType.toString(ctx));
            builder.append(")");
            return builder.toString();
        }
    }

    public record Lambda(LambdaVType type, VExpression expr) implements VExpression {

        @Override
        public DataResult<VExpression> resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            return this.expr.resolveTypes(env, spec.merge(this.type.specToMerge())).map(expr -> new Lambda(this.type, expr));
        }

        @Override
        public String toString(TypeNamer ctx) {
            return "(lambda %s : %s)".formatted(this.expr.toString(ctx), this.type.toString(ctx));
        }

        @Override
        public VValue evaluate(EvaluationContext ctx) {
            return VValue.value(this.type, this.expr, ctx.env());
        }
    }

    DataResult<VExpression> resolveTypes(VEnvironment env, EvaluationContext.Spec spec);
    VType type();
    VValue evaluate(EvaluationContext ctx);
    String toString(TypeNamer ctx);

    private static <T> DataResult<Stream<T>> bubbleUp(Stream<DataResult<T>> stream) {
        var streamContents = new ArrayList<T>();
        var errors = new ArrayList<String>();
        stream.forEach(res -> {
            if (res.result().isPresent()) {
                streamContents.add(res.result().get());
            } else {
                errors.add(res.error().map(DataResult.PartialResult::message).orElse("?"));
            }
        });
        return errors.isEmpty() ? DataResult.success(streamContents.stream()) : DataResult.error(() -> String.join("\n and ", errors));
    }
}
