package integrative.task.structures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;

public class MinHeap<T> {
    private ArrayList<T> heap;
    private Comparator<T> comparator;

    public MinHeap(Comparator<T> comparator) {
        this.heap = new ArrayList<>();
        this.comparator = comparator;
    }

    public void add(T element) {
        heap.add(element);
        siftUp(heap.size() - 1);
    }

    public T poll() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }

        T min = heap.get(0);
        T last = heap.remove(heap.size() - 1);

        if (!isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }

        return min;
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return heap.get(0);
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }

    public void clear() {
        heap.clear();
    }

    private void siftUp(int index) {
        T element = heap.get(index);
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            T parent = heap.get(parentIndex);

            if (comparator.compare(element, parent) >= 0) {
                break;
            }

            heap.set(index, parent);
            index = parentIndex;
        }
        heap.set(index, element);
    }

    private void siftDown(int index) {
        int size = heap.size();
        T element = heap.get(index);

        while (true) {
            int leftChildIndex = 2 * index + 1;
            int rightChildIndex = 2 * index + 2;
            int smallestIndex = index;

            if (leftChildIndex < size && comparator.compare(heap.get(leftChildIndex), heap.get(smallestIndex)) < 0) {
                smallestIndex = leftChildIndex;
            }

            if (rightChildIndex < size && comparator.compare(heap.get(rightChildIndex), heap.get(smallestIndex)) < 0) {
                smallestIndex = rightChildIndex;
            }

            if (smallestIndex == index) {
                break;
            }

            heap.set(index, heap.get(smallestIndex));
            index = smallestIndex;
        }

        heap.set(index, element);
    }
}
