package ru.landilf.apartmentpriceanalyzer;

import java.util.List;
import java.util.Map;

public class Apartment {
    private String id;
    private String title;
    private Long price_per_month;
    private String address;
    private Features features;
    private List<String> facts;

    public Apartment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getPrice_per_month() { return price_per_month; }
    public void setPrice_per_month(Long price_per_month) { this.price_per_month = price_per_month; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Features getFeatures() { return features; }
    public void setFeatures(Features features) { this.features = features; }

    public List<String> getFacts() { return facts; }
    public void setFacts(List<String> facts) { this.facts = facts; }

    public static class Features {
        private String hcs_price;
        private Long deposit;
        private Double comission;
        private Integer metro_cnt;
        private Integer metro_nearest_time;
        private Integer prepayment_months_cnt;
        private Integer rent_term_months_cnt;
        private Double total_area;
        private Double living_area;
        private Double kitchen_area;
        private Integer floor_number;
        private Integer total_floors_cnt;
        private String layout_cat;
        private String repair_cat;
        private String house_type_cat;
        private String parking_cat;
        private String heating_cat;
        private String ceiling_height;
        private String construction_year;
        private String construction_series;
        private Integer combined_bathrooms_cnt;
        private Integer separate_bathrooms_cnt;
        private Integer passenger_elevators_cnt;
        private Integer freight_elevators_cnt;
        private String balcony_loggia_cnt;
        private Integer entrances_cnt;
        private String entrance_info;

        public Features() {}

        public String getHcs_price() { return hcs_price; }
        public void setHcs_price(String hcs_price) { this.hcs_price = hcs_price; }

        public Long getDeposit() { return deposit; }
        public void setDeposit(Long deposit) { this.deposit = deposit; }

        public Double getComission() { return comission; }
        public void setComission(Double comission) { this.comission = comission; }

        public Integer getMetro_cnt() { return metro_cnt; }
        public void setMetro_cnt(Integer metro_cnt) { this.metro_cnt = metro_cnt; }

        public Integer getMetro_nearest_time() { return metro_nearest_time; }
        public void setMetro_nearest_time(Integer metro_nearest_time) { this.metro_nearest_time = metro_nearest_time; }

        public Integer getPrepayment_months_cnt() { return prepayment_months_cnt; }
        public void setPrepayment_months_cnt(Integer prepayment_months_cnt) { this.prepayment_months_cnt = prepayment_months_cnt; }

        public Integer getRent_term_months_cnt() { return rent_term_months_cnt; }
        public void setRent_term_months_cnt(Integer rent_term_months_cnt) { this.rent_term_months_cnt = rent_term_months_cnt; }

        public Double getTotal_area() { return total_area; }
        public void setTotal_area(Double total_area) { this.total_area = total_area; }

        public Double getLiving_area() { return living_area; }
        public void setLiving_area(Double living_area) { this.living_area = living_area; }

        public Double getKitchen_area() { return kitchen_area; }
        public void setKitchen_area(Double kitchen_area) { this.kitchen_area = kitchen_area; }

        public Integer getFloor_number() { return floor_number; }
        public void setFloor_number(Integer floor_number) { this.floor_number = floor_number; }

        public Integer getTotal_floors_cnt() { return total_floors_cnt; }
        public void setTotal_floors_cnt(Integer total_floors_cnt) { this.total_floors_cnt = total_floors_cnt; }

        public String getLayout_cat() { return layout_cat; }
        public void setLayout_cat(String layout_cat) { this.layout_cat = layout_cat; }

        public String getRepair_cat() { return repair_cat; }
        public void setRepair_cat(String repair_cat) { this.repair_cat = repair_cat; }

        public String getHouse_type_cat() { return house_type_cat; }
        public void setHouse_type_cat(String house_type_cat) { this.house_type_cat = house_type_cat; }

        public String getParking_cat() { return parking_cat; }
        public void setParking_cat(String parking_cat) { this.parking_cat = parking_cat; }

        public String getHeating_cat() { return heating_cat; }
        public void setHeating_cat(String heating_cat) { this.heating_cat = heating_cat; }

        public String getCeiling_height() { return ceiling_height; }
        public void setCeiling_height(String ceiling_height) { this.ceiling_height = ceiling_height; }

        public String getConstruction_year() { return construction_year; }
        public void setConstruction_year(String construction_year) { this.construction_year = construction_year; }

        public String getConstruction_series() { return construction_series; }
        public void setConstruction_series(String construction_series) { this.construction_series = construction_series; }

        public Integer getCombined_bathrooms_cnt() { return combined_bathrooms_cnt; }
        public void setCombined_bathrooms_cnt(Integer combined_bathrooms_cnt) { this.combined_bathrooms_cnt = combined_bathrooms_cnt; }

        public Integer getSeparate_bathrooms_cnt() { return separate_bathrooms_cnt; }
        public void setSeparate_bathrooms_cnt(Integer separate_bathrooms_cnt) { this.separate_bathrooms_cnt = separate_bathrooms_cnt; }

        public Integer getPassenger_elevators_cnt() { return passenger_elevators_cnt; }
        public void setPassenger_elevators_cnt(Integer passenger_elevators_cnt) { this.passenger_elevators_cnt = passenger_elevators_cnt; }

        public Integer getFreight_elevators_cnt() { return freight_elevators_cnt; }
        public void setFreight_elevators_cnt(Integer freight_elevators_cnt) { this.freight_elevators_cnt = freight_elevators_cnt; }

        public String getBalcony_loggia_cnt() { return balcony_loggia_cnt; }
        public void setBalcony_loggia_cnt(String balcony_loggia_cnt) { this.balcony_loggia_cnt = balcony_loggia_cnt; }

        public Integer getEntrances_cnt() { return entrances_cnt; }
        public void setEntrances_cnt(Integer entrances_cnt) { this.entrances_cnt = entrances_cnt; }

        public String getEntrance_info() { return entrance_info; }
        public void setEntrance_info(String entrance_info) { this.entrance_info = entrance_info; }
    }
}
