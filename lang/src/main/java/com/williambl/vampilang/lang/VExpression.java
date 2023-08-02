package com.williambl.vampilang.lang;

import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public sealed interface VExpression {
    public static VExpression functionApplication(VFunctionDefinition function, VExpression... inputs) {
        return new FunctionApplication(function, null, Arrays.asList(inputs));
    }

    public static VExpression value(VType type, Object value) {
        return new Value(new VValue(type, value));
    }

    public record FunctionApplication(VFunctionDefinition function, @Nullable VFunctionSignature resolvedSignature, List<VExpression> inputs) implements VExpression {

        @Override
        public VExpression resolveTypes() {
            var resolvedInputs = this.inputs.stream().map(VExpression::resolveTypes).toList();
            var resolvedFunctionSignature = this.function.signature().uniquise().resolveTypes(resolvedInputs.stream().map(VExpression::type).toList());
            return new FunctionApplication(this.function, resolvedFunctionSignature, resolvedInputs);
        }

        @Override
        public VType type() {
            return this.resolvedSignature == null ? this.function.signature().outputType() : this.resolvedSignature.outputType();
        }

        @Override
        public String toString(EvaluationContext ctx) {
            var builder = new StringBuilder();
            builder.append("(function ");
            builder.append(this.function.name());
            builder.append(" ");
            for (var input : this.inputs) {
                builder.append(input.toString(ctx));
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
                return this.resolveTypes().evaluate(ctx);
            }

            return this.function.function().apply(ctx, this.resolvedSignature, this.inputs.stream().map(expr -> expr.evaluate(ctx)).toList());
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
        public VExpression resolveTypes() {
            return this;
        }
        @Override
        public VType type() {
            return this.value.type();
        }

        @Override
        public String toString(EvaluationContext ctx) {
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
    VExpression resolveTypes();
    VType type();
    VValue evaluate(EvaluationContext ctx);
    String toString(EvaluationContext ctx);
}
