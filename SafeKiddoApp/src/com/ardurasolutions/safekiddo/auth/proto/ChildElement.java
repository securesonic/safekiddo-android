package com.ardurasolutions.safekiddo.auth.proto;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ChildElement {

	private Long id;
	private String name, uuid;
	
	public static ChildElement fromJsonElement(JsonObject data) {
		ChildElement res = new ChildElement();
		res.setId(data.get("id").getAsLong());
		res.setUuid(data.get("uuid").getAsString());
		res.setName(data.get("name").getAsString());
		return res;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return "{id: " + id +", name: " + name + ", uuid: " + uuid + "}";
	}
	
	public String toJSON() {
		return new Gson().toJson(this);
	}
	
	public static ChildElement fromJSON(String s) {
		return new Gson().fromJson(s, ChildElement.class);
	}

}
