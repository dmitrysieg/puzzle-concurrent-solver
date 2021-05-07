package ru.sieg.logic.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

public class IteratorChain<T> implements Iterator<T> {

    private final List<Iterator<T>> iterators = new ArrayList<>();
    private final AtomicInteger index = new AtomicInteger();
    private final Object semaphore = new Object();

    public IteratorChain(final Iterable<Iterable<T>> source) {
        for (Iterable<T> list : source) {
            iterators.add(list.iterator());
        }
    }

    @Override
    public boolean hasNext() {
        synchronized (semaphore) {
            if (iterators.size() == index.get()) {
                return false;
            }
            while (iterators.size() < index.get() && !iterators.get(index.get()).hasNext()) {
                index.incrementAndGet();
            }
            return iterators.get(index.get()).hasNext();
        }
    }

    @Override
    public T next() {
        synchronized (semaphore) {
            if (iterators.size() == index.get()) {
                throw new NoSuchElementException();
            }
        }
        return null;
    }
}
