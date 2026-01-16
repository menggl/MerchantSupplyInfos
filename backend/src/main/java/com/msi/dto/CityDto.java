package com.msi.dto;

public class CityDto {
  private String city_code;
  
  private String city_name;

  private Integer is_online;
  
  public CityDto() {}
  
  public CityDto(String city_code, String city_name) {
    this.city_code = city_code;
    this.city_name = city_name;
  }

  public CityDto(String city_code, String city_name, Integer is_online) {
    this.city_code = city_code;
    this.city_name = city_name;
    this.is_online = is_online;
  }
  
  public String getCity_code() { return city_code; }
  public void setCity_code(String city_code) { this.city_code = city_code; }
  
  public String getCity_name() { return city_name; }
  public void setCity_name(String city_name) { this.city_name = city_name; }

  public Integer getIs_online() { return is_online; }
  public void setIs_online(Integer is_online) { this.is_online = is_online; }
}
