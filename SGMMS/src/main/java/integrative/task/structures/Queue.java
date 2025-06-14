package integrative.task.structures;

import java.util.NoSuchElementException;

public class Queue<T> {
    private LinkedList<T> elements;

    public Queue() {
        this.elements = new LinkedList<>();
    }

    public void add(T element) {
        elements.addLast(element);
    }

    public T poll() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elements.removeFirst();
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return elements.getFirst();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int size() {
        return elements.size();
    }

    public void clear() {
        elements.clear();
    }
}
