package com.williambl.vampilang.lang;

import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.ConstructableVType;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public sealed interface VExpression {
    public static VExpression functionApplication(VFunctionDefinition function, Map<String, VExpression> inputs) {
        return new FunctionApplication(function, null, inputs);
    }

    public static VExpression value(VType type, Object value) {
        return new Value(new VValue(type, value));
    }

    public static VExpression variable(String name) {
        return new VariableRef(name, null);
    }

    static VExpression object(String typeName, Map<String, VExpression> properties) {
        return new ObjectConstruction(typeName, properties, null);
    }

    static VExpression list(List<VExpression> entries) {
        return new ListConstruction(null, entries);
    }

    public record FunctionApplication(VFunctionDefinition function, @Nullable VFunctionSignature resolvedSignature, Map<String, VExpression> inputs) implements VExpression {

        @Override
        public VExpression resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            var resolvedInputs = this.inputs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> kv.getValue().resolveTypes(env, spec)));
            var resolvedFunctionSignature = this.function.signature().uniquise().resolveTypes(resolvedInputs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> kv.getValue().type())));
            return new FunctionApplication(this.function, resolvedFunctionSignature, resolvedInputs);
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
        public VExpression resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            return this;
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
        public VExpression resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            return new VariableRef(this.name, spec.typeOf(this.name));
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
        public VExpression resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            var resolvedProperties = this.properties.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> kv.getValue().resolveTypes(env, spec)));
            var resultType = env.getType(this.typeName);
            if (!(resultType instanceof ConstructableVType<?> constructableVType)) {
                throw new IllegalArgumentException("Type %s must be constructable to be used in an object literal expression".formatted(this.typeName));
            }
            var expectedPropertyTypes = constructableVType.propertyTypes;
            for (var key : expectedPropertyTypes.keySet()) {
                var expected = expectedPropertyTypes.get(key);
                var actual = resolvedProperties.get(key);
                if (actual == null || !expected.contains(actual.type())) {
                    throw new IllegalArgumentException("Argument %s should be of type %s!".formatted(key, expected));
                }
            }
            return new ObjectConstruction(this.typeName, resolvedProperties, constructableVType);
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

            return new VValue(this.resolvedType, result);
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
        public VExpression resolveTypes(VEnvironment env, EvaluationContext.Spec spec) {
            var resolvedEntries = this.entries.stream().map(e -> e.resolveTypes(env, spec)).toList();
            var listType = env.listType();
            if (listType == null) {
                throw new IllegalStateException("No list type in environment!");
            }
            var entryType = resolvedEntries.size() > 0 ? resolvedEntries.get(0).type() : null;
            if (entryType != null) {
                if (!resolvedEntries.stream().allMatch(e -> entryType.contains(e.type()))) { //TODO this is horrible, find the most general type of all of them instead
                    throw new IllegalStateException("Not all entries match type of first entry!");
                }

                return new ListConstruction(listType.with(0, entryType), resolvedEntries);
            }

            return new ListConstruction(listType, resolvedEntries);
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
            return new VValue(this.resolvedType(), evaluatedEntries);
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

    VExpression resolveTypes(VEnvironment env, EvaluationContext.Spec spec);
    VType type();
    VValue evaluate(EvaluationContext ctx);
    String toString(TypeNamer ctx);
}
