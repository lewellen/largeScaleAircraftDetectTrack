package org.lewellen.lsadt;

public interface Predicate<T> {
	boolean Allow(T t);
}
