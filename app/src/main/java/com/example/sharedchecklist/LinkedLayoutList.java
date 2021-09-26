package com.example.sharedchecklist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LinkedLayoutList<T extends ViewGroup> extends LinearLayout {

    private ListNode<T> header;
    private int size;
    private IndexOutOfBoundsException IndexError;

    public LinkedLayoutList(Context context) {
        super(context);
        init(context);
    }

    public LinkedLayoutList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reminder_list_structure, this, true);

        header = null;
        size = 0;
    }

    public void insertBefore(T val, int index) throws IndexOutOfBoundsException {
        if (index >= size || index < 0) {
            throw (IndexError);
        }

        if (index == 0) {
            insertHead(val);
        } else {
            insertAfter(val, index-1);
        }
    }

    public void insertAfter(T val, int index) throws IndexOutOfBoundsException {
        if (index >= size || index < 0) {
            throw (IndexError);
        }

        if (isEmpty() && index == 0) {
            header = new ListNode<T>(val);
            size++;
            return;
        }

        if (index == 0) {
            ListNode<T> second = header.getNext();
            header.setNext(new ListNode<T>(val, second));
            size++;
        } else if (index != size-1){
            int i = 0;
            ListNode<T> current = header;
            while (i < index) {
                current = current.getNext();
                i++;
            }
            ListNode<T> next = current.getNext();
            current.setNext(new ListNode<T>(val, next));
            size++;
        } else {
            insertTail(val);
        }

    }

    public void insertHead(T val) {
        if (!isEmpty()) {
            ListNode<T> currentHead = new ListNode<T>(header.getData(), header.getNext());
            ListNode<T> newHead = new ListNode<T>(val, currentHead);
            this.header = newHead;
            size++;
        } else {
            header = new ListNode<T>(val);
            size++;
        }
    }

    public void insertTail(T val) {
        if (!isEmpty()) {
            last().setNext(new ListNode<T>(val));
            size++;
        } else {
            insertHead(val);
        }
    }

    public ListNode<T> first() {
        return header;
    }

    public ListNode<T> last() {
        int i = 0;
        ListNode<T> current = header;
        while (i < size && current.getNext() != null) {
            current = current.getNext();
            i++;
        }
        return current;
    }

    public int size() {
        return size;
    }

    public T get(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= size) {
            throw IndexError;
        }
        ListNode<T> current = header;
        int i = 0;
        while (i < index && current.getNext() != null) {
            i++;
            current = current.getNext();
        }
        return current.getData();
    }

    public void remove(int index) throws IndexOutOfBoundsException {
        if (index >= size || index < 0) {
            throw (IndexError);
        }

        if (index == 0) {
            ListNode<T> second = new ListNode<>(header.getNext().getData(), header.getNext().getNext());
            header = second;
            size--;
            refresh();
        } else {
            int i = 0;
            ListNode<T> current = header;
            while (i < index-1) {
                current = current.getNext();
                i++;
            }
            //remove current.next() node
            ListNode<T> newNext = current.getNext().getNext();
            current.setNext(new ListNode<T>(newNext.getData(), newNext.getNext()));
            size--;
        }
    }

    public void refresh() {
        removeAllViews();
        ListNode<T> current = header;
        for (int i = 0; i < size; i++) {
            addView(current.getData());
            current = current.getNext();
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        header = null;
        size = 0;
    }

    @Override
    public String toString() {
        String s = "";
        ListNode<T> current = header;
        for (int i = 0; i < size; i++) {
            s += current.getData().toString() + "[]";
            if (i != size-1) {
                s += " => ";
            }
            current = current.getNext();
        }
        return s;
    }

    public void addView(T v) {
        super.addView(v);
        insertTail(v);
    }

}
