package com.custody.custody_service.application.dto;

import java.util.List;

public class LedgerStatementOutput {

    private final List<LedgerEntryOutput> entries;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public LedgerStatementOutput(List<LedgerEntryOutput> entries, int page, int size,
                                  long totalElements, int totalPages) {
        this.entries = entries;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<LedgerEntryOutput> getEntries() { return entries; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
}
