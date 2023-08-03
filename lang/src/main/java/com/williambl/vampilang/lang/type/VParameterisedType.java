package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.EvaluationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public final class VParameterisedType implements VType {
    public final VType bareType;
    public final List<VType> parameters;

    VParameterisedType(VType bareType, List<VType> parameters) {
        this.bareType = bareType;
        this.parameters = parameters;
    }

    @Override
    public VType uniquise(HashMap<VType, VType> uniquisedTemplates) {
        for (var type : this.parameters) {
            if (!uniquisedTemplates.containsKey(type)) {
                uniquisedTemplates.put(type, type.uniquise(uniquisedTemplates));
            }
        }
        return new VParameterisedType(this.bareType, this.parameters.stream().map(uniquisedTemplates::get).toList());
    }

    @Override
    public boolean contains(VType other) {
        return this.equals(other) || (other instanceof VParameterisedType paramed
                && paramed.bareType.equals(this.bareType)
                && (paramed.parameters.equals(this.parameters)
                || (paramed.parameters.size() == this.parameters.size() && checkBiPredicateOnLists(this.parameters, paramed.parameters, VType::contains))));
    }

    @Override
    public boolean accepts(Object value) {
        return false; //TODO
    }

    private static <A, B> boolean checkBiPredicateOnLists(List<A> a, List<B> b, BiPredicate<A, B> predicate) {
        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i++) {
            if (!predicate.test(a.get(i), b.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString(EvaluationContext ctx) {
        return this.bareType.toString(ctx) + (this.parameters == null ? "" : "<"+this.parameters.stream().map(b -> b.toString(ctx)).sorted().collect(Collectors.joining(","))+">");
    }

    public VParameterisedType with(int index, VType type) {
        var newParams = new ArrayList<>(this.parameters);
        newParams.set(index, type);
        return new VParameterisedType(this.bareType, newParams);
    }

    public VType with(List<VType> assignment) {
        return new VParameterisedType(this.bareType, assignment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        VParameterisedType that = (VParameterisedType) o;
        return Objects.equals(this.bareType, that.bareType) && Objects.equals(this.parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bareType, this.parameters);
    }
}
