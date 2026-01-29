package ru.landilf.apartmentpriceanalyzer;

import java.util.List;
import java.util.Map;

public class Apartment {
    private String id;
    private Long price_per_month;
    private Integer metro_nearest_time;
    private Double total_area;
    private Double floor;
    private String title; // Optional, might be missing in JSON but good to keep
    private String address; // Optional

    // Flags
    private Long has_bath_flg;
    private Long has_shower_flg;
    private Long has_internet_flg;
    private Long has_ac_flg;
    private Long has_room_furniture_flg;
    private Long has_kitchen_furniture_flg;
    private Long has_dishwasher_flg;
    private Long has_washer_flg;
    private Long has_tv_flg;
    private Long has_fridge_flg;
    private Long has_garbage_chute_flg;
    private Long has_concierge_flg;

    // Financials
    private Long utility_fixed_bill;
    private Long utility_usage_bill_flg;
    private Long utility_counters_extra_flg;
    private Double comission;
    private Long prepayment_months_cnt;
    private Long rent_term_months_cnt;

    // Counts
    private Long combined_bathrooms_cnt;
    private Long separate_bathrooms_cnt;
    private Long freight_elevators_cnt;
    private Long passenger_elevators_cnt;
    private Long balcony_cnt;
    private Long loggia_cnt;
    private Long entrances_cnt;

    // Categories (encoded)
    private Long repair_cat;
    private Long parking_cat;
    private Long heating_cat;
    private Long era_cat;

    // House Types
    private Long individual_project_flg;
    private Long house_type_monolithic_flg;
    private Long house_type_monolithic_brick_flg;
    private Long house_type_panel_flg;

    // Districts
    private Long district_central_flg;
    private Long district_frunzenskiy_flg;
    private Long district_kalininskiy_flg;
    private Long district_vasileostrovsky_flg;
    private Long district_krasnogvardeysky_flg;
    private Long district_krasnoselsky_flg;
    private Long district_moskovsky_flg;
    private Long district_nevsky_flg;
    private Long district_other_flg;
    private Long district_primorsky_flg;
    private Long district_vyborgsky_flg;
    // Add others if they appear in full dataset, but sticking to provided snippet

    // Legacy fields for compatibility if mixed data exists
    private Map<String, String> facts;
    private List<String> features;

    public Apartment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getPrice_per_month() { return price_per_month; }
    public void setPrice_per_month(Long price_per_month) { this.price_per_month = price_per_month; }

    public Integer getMetro_nearest_time() { return metro_nearest_time; }
    public void setMetro_nearest_time(Integer metro_nearest_time) { this.metro_nearest_time = metro_nearest_time; }

    public Double getTotal_area() { return total_area; }
    public void setTotal_area(Double total_area) { this.total_area = total_area; }

    public Double getFloor() { return floor; }
    public void setFloor(Double floor) { this.floor = floor; }

    public String getTitle() { 
        // Generate title if missing, based on area/rooms
        if (title == null || title.isEmpty()) {
            return "Квартира, " + (total_area != null ? total_area + " м²" : "? м²");
        }
        return title; 
    }
    public void setTitle(String title) { this.title = title; }

    public String getAddress() {
        if (address != null && !address.isEmpty()) return address;
        
        StringBuilder sb = new StringBuilder();
        sb.append("Санкт-Петербург");
        
        if (isTrue(district_central_flg)) sb.append(", Центральный р-н");
        else if (isTrue(district_frunzenskiy_flg)) sb.append(", Фрунзенский р-н");
        else if (isTrue(district_kalininskiy_flg)) sb.append(", Калининский р-н");
        else if (isTrue(district_krasnogvardeysky_flg)) sb.append(", Красногвардейский р-н");
        else if (isTrue(district_krasnoselsky_flg)) sb.append(", Красносельский р-н");
        else if (isTrue(district_moskovsky_flg)) sb.append(", Московский р-н");
        else if (isTrue(district_nevsky_flg)) sb.append(", Невский р-н");
        else if (isTrue(district_primorsky_flg)) sb.append(", Приморский р-н");
        else if (isTrue(district_vasileostrovsky_flg)) sb.append(", Василеостровский р-н");
        else if (isTrue(district_vyborgsky_flg)) sb.append(", Выборгский р-н");
        else if (isTrue(district_other_flg)) sb.append(", Другой район");
        
        return sb.toString();
    }
    
    private boolean isTrue(Long val) {
        return val != null && val == 1;
    }

    public void setAddress(String address) { this.address = address; }

    // Getters for flags
    public Long getHas_bath_flg() { return has_bath_flg; }
    public Long getHas_shower_flg() { return has_shower_flg; }
    public Long getHas_internet_flg() { return has_internet_flg; }
    public Long getHas_ac_flg() { return has_ac_flg; }
    public Long getHas_room_furniture_flg() { return has_room_furniture_flg; }
    public Long getHas_kitchen_furniture_flg() { return has_kitchen_furniture_flg; }
    public Long getHas_dishwasher_flg() { return has_dishwasher_flg; }
    public Long getHas_washer_flg() { return has_washer_flg; }
    public Long getHas_tv_flg() { return has_tv_flg; }
    public Long getHas_fridge_flg() { return has_fridge_flg; }
    public Long getHas_garbage_chute_flg() { return has_garbage_chute_flg; }
    public Long getHas_concierge_flg() { return has_concierge_flg; }

    // Financials
    public Long getUtility_fixed_bill() { return utility_fixed_bill; }
    public Double getComission() { return comission; }
    public Long getPrepayment_months_cnt() { return prepayment_months_cnt; }
    public Long getRent_term_months_cnt() { return rent_term_months_cnt; }

    // Cats
    public Long getRepair_cat() { return repair_cat; }
    public Long getParking_cat() { return parking_cat; }
    public Long getHeating_cat() { return heating_cat; }
    
    // Additional Getters
    public Long getUtility_usage_bill_flg() { return utility_usage_bill_flg; }
    public Long getUtility_counters_extra_flg() { return utility_counters_extra_flg; }
    public Long getCombined_bathrooms_cnt() { return combined_bathrooms_cnt; }
    public Long getSeparate_bathrooms_cnt() { return separate_bathrooms_cnt; }
    public Long getFreight_elevators_cnt() { return freight_elevators_cnt; }
    public Long getPassenger_elevators_cnt() { return passenger_elevators_cnt; }
    public Long getBalcony_cnt() { return balcony_cnt; }
    public Long getLoggia_cnt() { return loggia_cnt; }
    public Long getEntrances_cnt() { return entrances_cnt; }
    public Long getEra_cat() { return era_cat; }
    public Long getIndividual_project_flg() { return individual_project_flg; }
    public Long getHouse_type_monolithic_flg() { return house_type_monolithic_flg; }
    public Long getHouse_type_monolithic_brick_flg() { return house_type_monolithic_brick_flg; }
    public Long getHouse_type_panel_flg() { return house_type_panel_flg; }

    // Legacy support
    public Map<String, String> getFacts() { return facts; }
    public List<String> getFeatures() { return features; }
}
