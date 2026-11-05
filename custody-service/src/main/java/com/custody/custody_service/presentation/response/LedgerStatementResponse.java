package com.custody.custody_service.presentation.response;

import java.util.List;

public class LedgerStatementResponse {

    private final List<LedgerEntryResponse> entries;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public LedgerStatementResponse(List<LedgerEntryResponse> entries, int page, int size,
                                     long totalElements, int totalPages) {
        this.entries = entries;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<LedgerEntryResponse> getEntries() { return entries; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
}
