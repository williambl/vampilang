package com.williambl.vampilang.lang;

import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
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

    public record FunctionApplication(VFunctionDefinition function, @Nullable VFunctionSignature resolvedSignature, Map<String, VExpression> inputs) implements VExpression {

        @Override
        public VExpression resolveTypes(EvaluationContext.Spec spec) {
            var resolvedInputs = this.inputs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> kv.getValue().resolveTypes(spec)));
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
                return this.resolveTypes(new EvaluationContext.Spec()).evaluate(ctx);
            }

            return this.function.function().apply(ctx, this.resolvedSignature, this.inputs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> kv.getValue().evaluate(ctx))));
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
        public VExpression resolveTypes(EvaluationContext.Spec spec) {
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
        public VExpression resolveTypes(EvaluationContext.Spec spec) {
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

    VExpression resolveTypes(EvaluationContext.Spec spec);
    VType type();
    VValue evaluate(EvaluationContext ctx);
    String toString(TypeNamer ctx);
}
