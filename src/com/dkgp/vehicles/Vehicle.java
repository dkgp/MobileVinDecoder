package com.dkgp.vehicles;

public class Vehicle {

	private String make;
	private String model;
	private String year;
	private String vin;
	private String dealerPhotoId;

	
	public String getMake()
	{
	    return this.make;
	}
	public void setMake(String value)
	{
	     this.make = value;
	}
	
	public String getModel()
	{
	    return this.model;
	}
	public void setModel(String value)
	{
	     this.model = value;
	}
	
	public String getYear()
	{
	    return this.year;
	}
	public void setYear(String value)
	{
	     this.year = value;
	}
	public String getVIN()
	{
	    return this.vin;
	}
	public void setVIN(String value)
	{
	     this.vin = value;
	}

	public String getDealerPhotoId()
	{
	    return this.dealerPhotoId;
	}
	public void setDealerPhotoId(String value)
	{
	     this.dealerPhotoId = value;
	}
}
