package com.example.wheatherstation_uah.data;

public class City 
{
    private int id;
    private String name;
 
    // constructors
	public City()
	{
		this.id = 0;
		this.name = null;
	}
	public City(int id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public int getId() 
	{
		return id;
	}

	public void setId(int id) 
	{
		this.id = id;
	}

	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}
 }
