package org.gbif.pipelines.ws.geocode;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * Models the response of the {@link GeocodeService}.
 */
public class GeocodeResponse implements Serializable {

  private static final long serialVersionUID = -9137655613118727430L;

  private String id;
  private String type;
  private String source;
  @SerializedName("title")
  private String countryName;
  private String isoCountryCode2Digit;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public String getIsoCountryCode2Digit() {
    return isoCountryCode2Digit;
  }

  public void setIsoCountryCode2Digit(String isoCountryCode2Digit) {
    this.isoCountryCode2Digit = isoCountryCode2Digit;
  }
}