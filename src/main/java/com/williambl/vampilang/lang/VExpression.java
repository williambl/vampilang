package com.williambl.vampilang.lang;

import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
            var resolvedFunctionSignature = this.function.signature().resolveTypes(resolvedInputs.stream().map(VExpression::type).toList());
            return new FunctionApplication(this.function, resolvedFunctionSignature, resolvedInputs);
        }
        @Override
        public VType type() {
            return this.resolvedSignature == null ? this.function.signature().outputType() : this.resolvedSignature.outputType();
        }

        @Override
        public String toString(Map<VType, String> typeNames) {
            var builder = new StringBuilder();
            builder.append("(function ");
            builder.append(this.function.name());
            builder.append(" ");
            for (var input : this.inputs) {
                builder.append(input.toString(typeNames));
                builder.append(" ");
            }
            builder.append(": ");
            builder.append((this.resolvedSignature == null ? this.function.signature().uniquise() : this.resolvedSignature).toString(typeNames));
            builder.append(")");
            return builder.toString();
        }

        @Override
        public VValue evaluate() {
            if (this.resolvedSignature == null) {
                return this.resolveTypes().evaluate();
            }

            return this.function.function().apply(this.resolvedSignature, this.inputs.stream().map(VExpression::evaluate).toList());
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
        public String toString(Map<VType, String> typeNames) {
            return "(value %s : %s)".formatted(this.value.value(), typeNames.computeIfAbsent(this.type(), $ -> "type#"+Integer.toString(new Random().nextInt(0, 500), 16)));
        }

        @Override
        public VValue evaluate() {
            return this.value();
        }
    }
    VExpression resolveTypes();
    VType type();
    VValue evaluate();

    String toString(Map<VType, String> typeNames);
}
