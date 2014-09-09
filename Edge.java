package project1;

import java.util.*;
import org.jgrapht.graph.*;

public class Edge extends DefaultEdge {

	private String name = null;

	public Edge(){

	}

	public Edge(String name){
		this.name = name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return this.name;
	}
}