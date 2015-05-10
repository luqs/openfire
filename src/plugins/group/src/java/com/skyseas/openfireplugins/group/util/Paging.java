package com.skyseas.openfireplugins.group.util;

import java.util.Collections;
import java.util.List;

/**
* Created by apple on 14-9-13.
*/
public class Paging<T> {
    private List<T> items = Collections.emptyList();
    private int offset;
    private int limit;
    private int count;


    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
