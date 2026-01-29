package ru.landilf.apartmentpriceanalyzer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class FilterSettings implements Serializable {
    private Integer minPrice;
    private Integer maxPrice;
    private Integer minArea;
    private Integer maxArea;
    private Set<Integer> roomCounts = new HashSet<>(); // 0 for Studio, 1, 2, 3, 4 (for 4+)

    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }

    public Integer getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }

    public Integer getMinArea() { return minArea; }
    public void setMinArea(Integer minArea) { this.minArea = minArea; }

    public Integer getMaxArea() { return maxArea; }
    public void setMaxArea(Integer maxArea) { this.maxArea = maxArea; }

    public Set<Integer> getRoomCounts() { return roomCounts; }
    public void setRoomCounts(Set<Integer> roomCounts) { this.roomCounts = roomCounts; }
    
    public boolean isActive() {
        return minPrice != null || maxPrice != null || minArea != null || maxArea != null || !roomCounts.isEmpty();
    }
}
