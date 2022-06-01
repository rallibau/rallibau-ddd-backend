package com.rallibau.shared.domain.bus.query;

public class PaginationInfo {
    private final Integer limit;
    private final Integer offset;
    private final Long total;

    public PaginationInfo(Integer limit, Integer offset, Long total) {
        this.limit = limit;
        this.offset = offset;
        this.total = total;
    }

    public PaginationInfo() {
        this.limit = 0;
        this.offset = 0;
        this.total = 0L;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public Long getTotal() {
        return total;
    }

    public Long getTotalPage() {
        if(total.equals(0)){
            return 0L;
        }
        if(total <= limit){
            return 1L;
        }
        return Long.valueOf((long) Math.ceil(total/limit));
    }
}
