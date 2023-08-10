package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VExpression;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class LambdaVType extends VParameterisedType {
    private final Function<LambdaVType, EvaluationContext.Spec> specFunction;

    LambdaVType(VType bareType, VType resultType, List<VType> paramedInputTypes, Function<LambdaVType, EvaluationContext.Spec> specFunction) {
        super(bareType, Stream.concat(Stream.of(resultType), paramedInputTypes.stream()).toList(), (p, o) -> o instanceof VExpression x && Objects.equals(p.parameters.get(0), x.type()));
        this.specFunction = specFunction;
    }

    LambdaVType(VType bareType, List<VType> params, Function<LambdaVType, EvaluationContext.Spec> specFunction) {
        super(bareType, params, (p, o) -> o instanceof VExpression x && Objects.equals(p.parameters.get(0), x.type()));
        this.specFunction = specFunction;
    }

    @Override
    public VType uniquise(HashMap<VType, VType> uniquisedTemplates) {
        for (var type : this.parameters) {
            if (!uniquisedTemplates.containsKey(type)) {
                uniquisedTemplates.put(type, type.uniquise(uniquisedTemplates));
            }
        }

        return new LambdaVType(this.bareType, this.parameters.stream().map(uniquisedTemplates::get).toList(), this.specFunction);
    }

    @Override
    public LambdaVType with(int index, VType type) {
        var newParams = new ArrayList<>(this.parameters);
        newParams.set(index, type);
        return new LambdaVType(this.bareType, newParams, this.specFunction);
    }

    @Override
    public LambdaVType with(List<VType> assignment) {
        return new LambdaVType(this.bareType, assignment, this.specFunction);
    }

    public EvaluationContext.Spec specToMerge() {
        return this.specFunction.apply(this);
    }
}
