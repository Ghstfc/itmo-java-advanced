package info.kgeorgiy.ja.merkulov.arrayset;

import java.util.*;

public class ArraySet<E extends Comparable<? super E>> extends AbstractSet<E> implements SortedSet<E> {

    private final List<E> elements;
    private final Comparator<E> comparator;

    public ArraySet() {
        this(new ArrayList<>(), null);
    }

    public ArraySet(Collection<E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<E> collection, Comparator<E> comparator) {
        this.elements = new ArrayList<>();
        this.comparator = comparator;
        TreeSet<E> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        elements.addAll(treeSet);
    }


    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    private int border(E element) {
        int index = Collections.binarySearch(elements, element, comparator);
        return index < 0 ? -(index + 1) : index;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        int left = border(fromElement);
        int right = border(toElement);
        if (left == right)
            throw new IllegalArgumentException("");
        return new ArraySet<>(elements.subList(left, right));
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int right = border(toElement);
        return new ArraySet<>(elements.subList(0, right));
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int left = border(fromElement);
        return new ArraySet<>(elements.subList(left, elements.size()));
    }

    @Override
    public E first() {
        if (elements.isEmpty()) {
            throw new NoSuchElementException("ArraySet is empty");
        }
        return elements.get(0);
    }

    @Override
    public E last() {
        if (elements.isEmpty()) {
            throw new NoSuchElementException("ArraySet is empty");
        }
        return elements.get(elements.size() - 1);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(elements, (E) o, comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }
}
