package com.example.digitalwalletsystem.dto;

import java.math.BigDecimal;

public class CategoryStatDto {

    private String category;
    private BigDecimal total;
    private long count;

    public CategoryStatDto() {}

    public CategoryStatDto(String category, BigDecimal total, long count) {
        this.category = category;
        this.total = total;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}