
/**
* Describes a set of transformations that need to take place to make
* one RavensObject (from) like another RavensObject (to)
* Used to implement Framing
*/
public class Transformation {

	//From Objec to Object
	RavensObject from;
	RavensObject to;

	//list of different transformations
	//physical transformations
	public boolean unchanged = false;
	public boolean changedShape = false;

	//if an object was added or deleted
	public boolean deleted = false;
	public boolean added = false;

	public boolean shrunk = false;
	public boolean expaned = false;
	public boolean fillChanged = false;
	public boolean flippedVertically = false;
	public boolean flippedHorizontally = false;
	public boolean rotated = false;

	//positional transformations
	public boolean movedAbove = false;
	public boolean movedBelow = false;
	public boolean movedInside = false;
	public boolean movedOutside = false;
	public boolean movedLeftOf = false;
	public boolean movedRightOf = false;
	public boolean movedBelow = false;

	//the total score of this transformationc
	public double score;
	//overall transformation type
	public String transformationType = "";

	public List<String> fillTransformations = new ArrayList<String>();
	public int degreesRotated = 0;

	List<String> positionalAttributes = null; 
	List<String> physicalAttributes = null;
	Map<String, Integer> shapeSizeMapping = null;

	public Transformation(RavensObject from, RavensObject to){
		this.from = from;
		this.to = to;
		
		List<String> positionalAttributes = new ArrayList<String>(Arrays.asList(new String[]{"inside", "above", "overlaps", "left-of"}));
		List<String> physicalAttributes = new ArrayList<String>(Arrays.asList(new String[]{"shape", "fill", "size", "angle", "vertical-flip"}));
			
		shapeSizeMapping = new HashMap<String, Integer>();
        shapeSizeMapping.put("small", 1);
        shapeSizeMapping.put("medium", 2);
        shapeSizeMapping.put("large", 3);

		//compute transformation
		computeTransformation();
	}

	private void computeTransformation(){

        if(from == null){
            transformation.added = true;
            return transformation;
        }

        if(to == null){
            transformation.removed = true;
            return transformation;
        }

        //get names of attributes in list for easy lookup
        Map<String, RavensAttribute> fromAttributes = attributeToMap(from.getAttributes());
        Map<String, RavensAttribute> toAttributes = attributeToMap(to.getAttributes());

        //compute positional attributes 
        for(int i=0; i<positionalAttributes.size(); i++){
        	String attribute = positionalAttributes.get(i);
        	switch(attribute){
        		case "above" :
        			if(fromAttributes.containsKey(attribute) && !toAttributes.containsKey(attribute)){
        				//no longer above (move below)
        				movedBelow = true;
        			}
        			if(!fromAttributes.containsKey(attribute) && toAttributes.containsKey(attribute)){
        				//wasn't above and no is (move above)
        				movedAbove = true;
        			}
        			break;
        		case "left-of" :
        			if(fromAttributes.containsKey(attribute) && !toAttributes.containsKey(attribute)){
        				//was left-of and now isn't (moved right)
        				movedRightOf = true;
        			}
        			if(!fromAttributes.containsKey(attribute) && toAttributes.containsKey(attribute)){
        				//wasn't left-of and now is (moved left)
        				movedLeftOf = true;
        			}
        			break;
        		case "inside" :
        			if(fromAttributes.containsKey(attribute) && !toAttributes.containsKey(attribute)){
        				//was inside and moved (outside)
        				movedOutside = true;
        			}
        			if(!fromAttributes.containsKey(attribute) && toAttributes.containsKey(attribute)){
        				movedInside = true;
        			}
        			break;
        		default :
        			break;

        	}
        }
        
        //compute physical attribute
        for(int i=0; i<physicalAttributes.size(); i++){
        	String attribute = physicalAttributes.get(i);
        	switch(attribute){
        		case "shape":
        			if(!fromAttributes.get(attribute).getValue().equals(toAttributes.get(attribute).getValue())){
        				changedShape = true;
        			}
        			break;
        		case "fill":
        			if(fromAttributes.get(attribute).getValue().equals(toAttributes.get(attribute).getValue())){
        				//fill didn't change
        				fillTransformations.add("unchanged");
        			} else {
        				fillChanged = true;

                        List<String> fromFills = new ArrayList(Arrays.asList(fromAttributes.get(attribute).getValue().split(",")));
                        List<String> toFills = new ArrayList(Arrays.asList(toAttributes.get(attribute).getValue().split(",")));
                        //fills that were removed
                        for(int j=0; j<fromFills.size(); j++){
                        	if(!toFills.contains(fromFills.get(j))){
                        		fillTransformations.add("removed:"+fromFills.get(j));
                        	}
                        }
                        //fills that were added
                        for(int j=0; j<toFills.size(); j++){
                        	if(!fromFills.contains(toFills.get(j))){
                        		fillTransformations.add("added:"+toFills.get(j));
                        	}
                        }
        			}
        			break;
        		case "size":
        			int sizeChange = calculateSizeChange(fromAttributes.get(attribute).getValue(), toAttributes.get(attribute).getValue());
        			if(sizeChange < 0){
        				shrunk = true;
        			}else if(sizeChange > 0){
        				expaned = true;
        			} else {
        				//size didn't change
        				//understand if neither shrunk or expanded that it didn't change in size
        			}
        			break;
        		case "angle":
        			if(fromAttributes.get(attribute) != null){
        				if(toAttributes.get(attribute) != null){
        					//an angle exists for both objects
        					if(!fromAttributes.get(attribute).getValue().equals(toAttributes.get(attribute).getValue())){
        						rotated = true;
        						degreesRotated = Math.abs(Integer.parseInt(toAttributes.get(attribute).getValue()) - Integer.parseInt(fromAttributes.get(attribute).getValue()));
        					}
        				} else {
        					//angle existed and now it doesn't
        					if(Integer.parseInt(fromAttributes.get(attribute).getValue()) != 0){
        						rotated = true;
        						degreesRotated = Integer.parseInt(fromAttributes.get(attribute).getValue();
        					}
        				}
        			} else {
        				if(toAttributes.get(attribute) != null){
        					//understand that if there wasn't an angle before and now there is a rotation has occurred
        					if(Integer.parseInt(toAttributes.get(attribute).getValue()) != 0){
        						rotated = true;
        						degreesRotated = Integer.parseInt(toAttributes.get(attribute).getValue());
        					}
        				}
        			}
        			//also account for flips
        			break;
        	}
        }

    }

    /**
    * Put Raven's attributes into a map
    * Name - RavensAttribute
    *
    */
    private Map<String, RavensAttribute> attributeToMap(ArrayList<RavensAttribute> ravensAttributes){
        Map<String, RavensAttribute> ravensAttributeMap = new HashMap<String, RavensAttribute>();
        for(int i=0; i<ravensAttributes.size(); i++){
            ravensAttributeMap.put(ravensAttributes.get(i).getName(), ravensAttributes.get(i));
        }
        return ravensAttributeMap;
    }

    private int calculateSizeChange(String a, String b){
        int retval = UNKNOW_SIZE;
        if(shapeSizeMapping.containsKey(a) && shapeSizeMapping.containsKey(b)){
            retval = shapeSizeMapping.get(b) - shapeSizeMapping.get(a);
        } 
        return retval;
    }
}