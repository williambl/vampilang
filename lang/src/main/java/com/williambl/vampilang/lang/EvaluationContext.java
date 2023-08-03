package com.williambl.vampilang.lang;

import com.williambl.vampilang.lang.type.VType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class EvaluationContext {
    private final @Unmodifiable Map<String, VValue> variables;

    public EvaluationContext(@Unmodifiable Map<String, VValue> variables) {
        this.variables = variables;
    }

    public EvaluationContext() {
        this(Map.of());
    }

    public VValue getVariable(String name, VType type) {
        var variable = this.variables.get(name);
        if (variable == null || !type.contains(variable.type())) {
            throw new NoSuchElementException("No such variable of name %s and type %s".formatted(name, type));
        }

        return variable;
    }

    public EvaluationContext with(String name, VValue variable) {
        var newVars = new HashMap<>(this.variables);
        newVars.put(name, variable);
        return new EvaluationContext(newVars);
    }

    public static Builder builder(Spec spec) {
        return new Builder(spec);
    }

    public static class Spec {
        private final @Unmodifiable Map<String, VType> variableTypes;

        public Spec(@Unmodifiable Map<String, VType> variableTypes) {
            this.variableTypes = variableTypes;
        }

        public Spec() {
            this(Map.of());
        }

        public VType typeOf(String variableName) {
            var res = this.variableTypes.get(variableName);
            if (res == null) {
                throw new NoSuchElementException("No variable with name "+variableName);
            }

            return res;
        }
    }

    public static class Builder {
        private final Map<String, VValue> variables = new HashMap<>();
        private final Spec spec;

        private Builder(Spec spec) {
            this.spec = spec;
        }

        public Builder addVariable(String name, VValue variable) {
            this.variables.put(name, variable);
            return this;
        }

        public EvaluationContext build() {
            for (var variable : this.spec.variableTypes.entrySet()) {
                var value = this.variables.get(variable.getKey());
                if (value == null || !variable.getValue().contains(value.type())) {
                    throw new IllegalStateException("Evaluation Context missing variable %s of type %s".formatted(variable.getKey(), variable.getValue()));
                }
            }

            return new EvaluationContext(Map.copyOf(this.variables));
        }
    }
}
