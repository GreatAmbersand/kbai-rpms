package project4;

import java.util.*;
import java.lang.*;

public class Pattern {

	//type will only be set if there is an actual pattern
	String type;

	//for patterns of shapes
	List<String> shapesSeen;
	//TODO: Implement Later
	//List<String> rotations;
	//List<String> fills;

	Transformation first;
	Transformation second;

	public Pattern(Transformation first, Transformation second){
		this.first = first;
		this.second = second;
	}

	public void fillShapesSeen(){
		RavensObject from = first.from;
		RavensObject to = first.to;

		RavensObject[] objects = new RavensObject[]{first.from, first.to, second.from, second.to};
		for(RavensObject object : objects){
			for(RavensAttribute ra : object.getAttributes()){
				if(ra.getName().equals("shape")){
					if(shapesSeen == null){
						shapesSeen = new ArrayList<String>();
					}
					if(!shapesSeen.contains(ra.getValue())){
						shapesSeen.add(ra.getValue());
					}
					break;
				}
			}
		}
	}

	public static List<Pattern> findPatterns(List<Transformation> first, List<Transformation> second){
		
		List<Pattern> patterns = new ArrayList<Pattern>();

        Transformation[] firstArray = first.toArray(new Transformation[0]);
        Transformation[] secondArray = second.toArray(new Transformation[0]);

        //changed shape
        Set<Transformation> alreadyMatchedShape = new HashSet<Transformation>();
        for(Transformation t : firstArray){
            if(t.changedShape){
                for(Transformation st : secondArray){
                    if(st.changedShape && !alreadyMatchedShape.contains(st)) {
                        //could have a pattern here
                        alreadyMatchedShape.add(st);
                        Pattern p = new Pattern(t, st);
                        p.type = "changedShape";
                        p.fillShapesSeen();
                        patterns.add(p);
                        break;
                    }
                }
            }
        }
        return patterns;
	}

	public static List<List<Pattern>> findMatchingPatterns(List<Pattern> first, List<Pattern> second){
		
		List<List<Pattern>> matchingPatterns = new ArrayList<List<Pattern>>();

		Set<Pattern> alreadyMatchedPattern = new HashSet<Pattern>();

		for(Pattern firstP : first){
			for(Pattern secondP : second){
				if(!alreadyMatchedPattern.contains(secondP)){
					if(secondP.type.equals(firstP.type)){
						if(firstP.type.equals("changedShape")){
							//check if all the shapes are the same? Is that useful
							boolean allShapesMatched = true;
							for(String shape : firstP.shapesSeen){
								if(!secondP.shapesSeen.contains(shape)){
									//discrepancy between the shapes seen... not a matching pattern
									allShapesMatched = false;
								}
							}
							if(allShapesMatched){
								alreadyMatchedPattern.add(secondP);
								List<Pattern> matching = new ArrayList<Pattern>();
								matching.add(firstP);
								matching.add(secondP);
								matchingPatterns.add(matching);
							}
						}
					}
				}
			}
		}
		return matchingPatterns;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Pattern type ");
		sb.append(type);
		sb.append("\n");
		if(type.equals("changedShape")){
			//print out list of shapes seen
			for(int i =0; i<shapesSeen.size(); i++){
				if(i==shapesSeen.size()-1){
					sb.append(shapesSeen.get(i));
				}
				else {
					sb.append(shapesSeen.get(i));
					sb.append(", ");
				}

			}
		}
		return sb.toString();
	}
}