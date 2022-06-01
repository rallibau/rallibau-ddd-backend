package com.rallibau.shared.infrastructure.persistence.memory;

import com.rallibau.shared.domain.DateValueObject;
import com.rallibau.shared.domain.Identifier;
import com.rallibau.shared.domain.StringValueObject;
import com.rallibau.shared.domain.AggregateRoot;
import com.rallibau.shared.domain.IntegerValueObject;
import com.rallibau.shared.domain.criteria.Criteria;
import com.rallibau.shared.domain.criteria.Filter;
import com.rallibau.shared.domain.criteria.FilterDateValue;
import com.rallibau.shared.domain.criteria.FilterIntegerValue;
import com.rallibau.shared.domain.criteria.FilterOperator;
import com.rallibau.shared.domain.criteria.FilterStringValue;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class MemoryRepository<T extends AggregateRoot, J extends Identifier> {
    protected final Class<T> aggregateClass;
    private final List<T> store;
    private final HashMap<FilterOperator, Function<Filter, Predicate<T>>> predicateTransformers =
            new HashMap<FilterOperator, Function<Filter, Predicate<T>>>() {{
                put(FilterOperator.EQUAL, MemoryRepository.this::equalsPredicateTransformer);
                put(FilterOperator.NOT_EQUAL, MemoryRepository.this::notEqualsPredicateTransformer);
                put(FilterOperator.GT, MemoryRepository.this::greaterThanPredicateTransformer);
                put(FilterOperator.LT, MemoryRepository.this::lowerThanPredicateTransformer);
                put(FilterOperator.CONTAINS, MemoryRepository.this::containsPredicateTransformer);
                put(FilterOperator.NOT_CONTAINS, MemoryRepository.this::notContainsPredicateTransformer);
            }};

    public MemoryRepository(Class<T> aggregateClass) {
        this.aggregateClass = aggregateClass;
        store = new ArrayList<>();

    }

    protected void persist(T entity) {
        store.add(entity);
    }

    protected Optional<T> byId(Identifier id) {
        return store.stream().filter(aggregateObject -> id.value().equals("aggregateObject.")).findFirst();
    }

    protected List<T> byCriteria(Criteria criteria) {
        List<T> result = new ArrayList<>(store);
        for (Filter filter : criteria.filters().filters()) {
            List<T> storeAux = new ArrayList<>();
            List<T> resultAux = new ArrayList<>(result);
            store.stream().filter(predicateTransformers.get(filter.operator()).apply(filter)).forEach(storeAux::add);
            resultAux.stream().filter(task -> !storeAux.contains(task)).forEach(result::remove);
        }

        return result;
    }

    public Integer countByCriteria(Criteria criteria) {
        return store.size();
    }

    public BigInteger getNextKey(String sequenceName) {
        return new BigInteger(32, new Random() {
            int seed = 0;

            @Override
            protected int next(int bits) {
                seed = ((22695477 * seed) + 1) & 2147483647; // Values shamelessly stolen from Wikipedia
                return seed;
            }
        });
    }

    protected List<T> all() {
        return store;
    }

    private Predicate<T> equalsPredicateTransformer(Filter filter) {
        return task -> getValue(filter, task).isPresent() && getValue(filter, task).get().equals(filter.value().value());
    }

    private Predicate<T> notEqualsPredicateTransformer(Filter filter) {
        return task -> getValue(filter, task).isPresent() && !getValue(filter, task).get().equals(filter.value().value());
    }

    private Predicate<T> greaterThanPredicateTransformer(Filter filter) {
        if (filter.value() instanceof FilterStringValue) {
            return task -> getValue(filter, task).isPresent() && getValue(filter, task).get().compareTo((String) filter.value().value()) > 0;
        }
        if (filter.value() instanceof FilterDateValue) {
            return task -> getValueDate(filter, task).isPresent() && getValueDate(filter, task).get().compareTo((Date) filter.value().value()) > 0;
        }
        if (filter.value() instanceof FilterIntegerValue) {
            return task -> getValueInteger(filter, task).isPresent() && getValueInteger(filter, task).get().compareTo((Integer) filter.value().value()) > 0;
        }
        throw new UnsupportedOperationException();
    }

    private Predicate<T> lowerThanPredicateTransformer(Filter filter) {
        if (filter.value() instanceof FilterStringValue) {
            return task -> getValue(filter, task).isPresent() && getValue(filter, task).get().compareTo((String)filter.value().value()) < 0;
        }
        if (filter.value() instanceof FilterDateValue) {
            return task -> getValueDate(filter, task).isPresent() && getValueDate(filter, task).get().compareTo((Date)filter.value().value()) < 0;
        }
        if (filter.value() instanceof FilterIntegerValue) {
            return task -> getValueInteger(filter, task).isPresent() && getValueInteger(filter, task).get().compareTo((Integer)filter.value().value()) < 0;
        }
        throw new UnsupportedOperationException();
    }

    private Predicate<T> containsPredicateTransformer(Filter filter) {
        if (filter.value() instanceof FilterStringValue) {
            return task -> getValue(filter, task).isPresent() && getValue(filter, task).get().contains((String)filter.value().value());
        }
        throw new UnsupportedOperationException();
    }

    private Predicate<T> notContainsPredicateTransformer(Filter filter) {
        if (filter.value() instanceof FilterStringValue) {
            return task -> getValue(filter, task).isPresent() && !getValue(filter, task).get().contains((String)filter.value().value());
        }

        throw new UnsupportedOperationException();
    }

    private Optional<String> getValue(Filter filter, T task) {
        try {
            Method method = task.getClass().getMethod(filter.field().value());
            Object object = method.invoke(task);
            return Optional.of(((StringValueObject) object).value());

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Date> getValueDate(Filter filter, T task) {
        try {
            Method method = task.getClass().getMethod(filter.field().value());
            Object object = method.invoke(task);
            return Optional.of(((DateValueObject) object).value());

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Integer> getValueInteger(Filter filter, T task) {
        try {
            Method method = task.getClass().getMethod(filter.field().value());
            Object object = method.invoke(task);
            return Optional.of(((IntegerValueObject) object).value());

        } catch (Exception e) {
            return Optional.empty();
        }
    }


}
