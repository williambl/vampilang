package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VExpression;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class LambdaVType extends VParameterisedType {
    public final EvaluationContext.Spec specToMerge;

    LambdaVType(VType bareType, VType resultType, EvaluationContext.Spec specToMerge) {
        super(bareType, List.of(resultType), (p, o) -> o instanceof VExpression x && Objects.equals(p.parameters.get(0), x.type()));
        this.specToMerge = specToMerge;
    }

    @Override
    public VType uniquise(HashMap<VType, VType> uniquisedTemplates) {
        for (var type : this.parameters) {
            if (!uniquisedTemplates.containsKey(type)) {
                uniquisedTemplates.put(type, type.uniquise(uniquisedTemplates));
            }
        }
        return new LambdaVType(this.bareType, uniquisedTemplates.get(this.parameters.get(0)), this.specToMerge);
    }
}
