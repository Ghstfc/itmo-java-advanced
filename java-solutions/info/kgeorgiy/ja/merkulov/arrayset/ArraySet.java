package info.kgeorgiy.ja.merkulov.arrayset;

import java.util.*;

@SuppressWarnings({"unchecked", "unused"})
public class ArraySet<E extends Comparable<? super E>> extends AbstractSet<E> implements SortedSet<E> {

    private final List<E> elements;
    private final Comparator<E> comparator;

    public ArraySet() {
        elements = new ArrayList<>();
        comparator = null;
    }

    public ArraySet(Collection<E> collection) {
        elements = new ArrayList<>(collection);
        Collections.sort(elements);
        comparator = null;
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
        if (index < 0)
            index = -(index + 1);
        return index;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        int left = border(fromElement);
        int right = border(toElement);
        if (comparator != null && comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("");
        }
        return new TreeSet<>(elements.subList(left, right));
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int right = border(toElement);
        return new TreeSet<>(elements.subList(0, right));
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int left = border(fromElement);
        return new TreeSet<>(elements.subList(left, elements.size()));
    }

    @Override
    public E first() {
        if (elements.isEmpty())
            throw new NoSuchElementException("ArraySet is empty");
        return elements.get(0);
    }

    @Override
    public E last() {
        if (elements.isEmpty())
            throw new NoSuchElementException("ArraySet is empty");
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
    public boolean contains(Object o) {
        return Collections.binarySearch(elements, (E) o, comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }
}