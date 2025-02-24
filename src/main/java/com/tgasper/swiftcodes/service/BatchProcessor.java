package com.tgasper.swiftcodes.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class BatchProcessor<T> {
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private final List<T> batch;
    private final int batchSize;

    public BatchProcessor() {
        this(DEFAULT_BATCH_SIZE);
    }

    public BatchProcessor(int batchSize) {
        this.batchSize = batchSize;
        this.batch = new ArrayList<>(batchSize);
    }

    public void add(T item, Consumer<List<T>> flushAction) {
        batch.add(item);
        if (batch.size() >= batchSize) {
            flush(flushAction);
        }
    }

    public void flush(Consumer<List<T>> flushAction) {
        if (!batch.isEmpty()) {
            flushAction.accept(new ArrayList<>(batch));
            batch.clear();
        }
    }
} 