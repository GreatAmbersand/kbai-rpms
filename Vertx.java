package project1;

import java.util.*;

public class Vertx {

	String name;
	Set<String> attributes;

	public Vertx(){}

	public Vertx(String name){
		this.name = name;
	}

	public Vertx(Set<String> attributes){
		this.attributes = attributes;
	}

	public Vertx(String name, Set<String> attributes){
		this.name = name;
		this.attributes = attributes;
	}

	private Set<String> getAttributes(){
		return this.attributes;
	}

	private String getName(){
		return this.name;
	}

	public boolean equals(Object a){
		if(a instanceof Vertx){
			Vertx v = (Vertx)a;
			if(v.getName().equals(this.name)){
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}