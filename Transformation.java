package project2;

import java.util.*;
/**
* Describes a set of transformations that need to take place to make
* one RavensObject (from) like another RavensObject (to)
* Used to implement Framing
*/
public class Transformation {

	//From Objec to Object
	public RavensObject from;
	public RavensObject to;

	//list of different transformations
	//physical transformations
	public boolean unchanged = true;
	public boolean changedShape = false;
    public String shapeChangedTo = null;

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


    public boolean[] metrics = null; 

	//the total score of this transformationc
	public double score = 0.0;
	//overall transformation type
	public String transformationType = "";

    int sizeChange = 0;

	public List<String> fillTransformations = new ArrayList<String>();
	public int degreesRotated = 0;

	List<String> positionalAttributes = null; 
	List<String> physicalAttributes = null;
	Map<String, Integer> shapeSizeMapping = null;

	public Transformation(RavensObject from, RavensObject to){
		this.from = from;
		this.to = to;
		
		positionalAttributes = new ArrayList<String>(Arrays.asList(new String[]{"inside", "above", "overlaps", "left-of"}));
		physicalAttributes = new ArrayList<String>(Arrays.asList(new String[]{"shape", "fill", "size", "angle", "vertical-flip"}));
			
		shapeSizeMapping = new HashMap<String, Integer>();
        shapeSizeMapping.put("small", 1);
        shapeSizeMapping.put("medium", 2);
        shapeSizeMapping.put("large", 3);

		//compute transformation
        try{
            computeTransformation();
        } catch (Exception e){
            System.out.println("Unable to create transformation");
            e.printStackTrace();
        }

        try{
            score();
        } catch (Exception e){
            System.out.println("Unable to score transformation");
            e.printStackTrace();
        }
	}

	private boolean computeTransformation(){

        if(from == null){
            added = true;
            unchanged = false;
            return false;
        }

        if(to == null){
            deleted = true;
            unchanged = false;
            return false;
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
                        //log the shape it changed to as well (may need it for in depth processing)
                        shapeChangedTo = toAttributes.get(attribute).getValue();
        			}
        			break;
        		case "fill":
                    if(fromAttributes.get(attribute) != null && toAttributes.get(attribute) != null){
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
                    }
        			break;
        		case "size":
                    if(fromAttributes != null && toAttributes != null){
                        if(fromAttributes.get(attribute) != null && toAttributes.get(attribute) != null){
                            sizeChange = calculateSizeChange(fromAttributes.get(attribute).getValue(), toAttributes.get(attribute).getValue());
                            if(sizeChange < 0){
                                shrunk = true;
                            }else if(sizeChange > 0){
                                expaned = true;
                            } else {
                                //size didn't change
                                //understand if neither shrunk or expanded that it didn't change in size
                            }
                        }
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
                            //need angle for both object to determin horizontal flip
                            /*if(Integer.parseInt(fromAttributes.get(attribute).getValue()) == 90 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 270){
                                flippedHorizontally = true;
                            }
                            if(Integer.parseInt(fromAttributes.get(attribute).getValue()) == 270 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 90){
                                flippedHorizontally = true;
                            }*/
        				} else {
        					//angle existed and now it doesn't
        					if(Integer.parseInt(fromAttributes.get(attribute).getValue()) != 0){
        						rotated = true;
        						degreesRotated = Integer.parseInt(fromAttributes.get(attribute).getValue());
        					}
        				}
                        //one angle will do for vertical flip
                        if(Integer.parseInt(fromAttributes.get(attribute).getValue()) == 0 && (Integer.parseInt(toAttributes.get(attribute).getValue()) == 180)){
                            flippedVertically = true;
                        }
                        if(Integer.parseInt(fromAttributes.get(attribute).getValue()) == 180 && (toAttributes.get(attribute) == null || Integer.parseInt(toAttributes.get(attribute).getValue()) == 0)){
                            flippedVertically = true;
                        }
        			} else {
        				if(toAttributes.get(attribute) != null){
        					//understand that if there wasn't an angle before and now there is a rotation has occurred
        					if(Integer.parseInt(toAttributes.get(attribute).getValue()) != 0){
        						rotated = true;
        						degreesRotated = Integer.parseInt(toAttributes.get(attribute).getValue());
        					}
                            //if there is 180 angle in the new object and the last one is null understand as vertical flip
                            if(Integer.parseInt(toAttributes.get(attribute).getValue()) == 180){
                                flippedVertically = true;
                            }
        				}
        			}
                    //work with shapes that don't change during a rotation : circle, square
                    //both must have an angle. 
                    if(fromAttributes.get(attribute) != null && toAttributes.get(attribute) != null){
                        String fromShape = fromAttributes.get("shape").getValue();
                        String toShape = toAttributes.get("shape").getValue();
                        /*if((fromShape.equals("square") && toShape.equals("square")) || (fromShape.equals("cirlce") && toShape.equals("circle"))) {
                            //could always be flipped to get the same image
                            flippedVertically = true;
                            flippedHorizontally = true;
                        }*/
                        if(fromShape.contains("triangle") && toShape.contains("triangle")){
                            System.out.println("found "+fromShape);
                            if(Integer.parseInt(fromAttributes.get(attribute).getValue()) == 0 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 0){
                                flippedHorizontally = true;
                            }
                            if((Integer.parseInt(fromAttributes.get(attribute).getValue()) == 90 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 180)
                                || (Integer.parseInt(fromAttributes.get(attribute).getValue()) == 180 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 90)
                                || (Integer.parseInt(fromAttributes.get(attribute).getValue()) == 270 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 0)
                                || (Integer.parseInt(fromAttributes.get(attribute).getValue()) == 0 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 270)) {
                                    System.out.println("Flipping vertically");
                                    flippedVertically = true;
                                }
                            if((Integer.parseInt(fromAttributes.get(attribute).getValue()) == 90 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 0) 
                                || (Integer.parseInt(fromAttributes.get(attribute).getValue()) == 0 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 90)
                                || (Integer.parseInt(fromAttributes.get(attribute).getValue()) == 180 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 270)
                                || (Integer.parseInt(fromAttributes.get(attribute).getValue()) == 270 && Integer.parseInt(toAttributes.get(attribute).getValue()) == 180)){
                                System.out.println("Flipping horizontally");
                                flippedHorizontally = true;
                            }
                        }
                    }
        			break;
        	}
        }

        metrics = new boolean[]{
            changedShape,
            deleted, 
            added, 
            shrunk, 
            expaned, 
            fillChanged, 
            flippedVertically, 
            flippedHorizontally,
            rotated,
            movedAbove,
            movedBelow,
            movedInside,
            movedOutside,
            movedLeftOf,
            movedRightOf
        };
        for(int i=0; i<metrics.length; i++){
            if(metrics[i]){
                unchanged = false;
            }
        }
        return true;
    }

    private void score(){
    //list of different transformations
    //physical transformations
    if(unchanged)
        score += 5;

    if(shrunk)
        score += 3;
    if(expaned)
        score += 3;

    if(fillChanged)
        score += 2;
    //if an object was added or deleted
    if(deleted)
        score += 1;

    if(added)
        score += 1;

    if(flippedVertically)
        score += 1;

    if(flippedHorizontally)
        score += 1;

    if(rotated)
        score += 1;

    if(changedShape)
        score += 0.5;

    //positional transformations
    if(movedAbove)
        score += 2;

    if(movedBelow)
        score += 2;

    if(movedInside)
        score += 2;

    if(movedOutside)
        score += 2;

    if(movedLeftOf)
        score += 2;

    if(movedRightOf)
        score += 2;

    if(movedBelow)
        score += 2;

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
        //int retval = UNKNOW_SIZE;
        int retval = 0;
        if(shapeSizeMapping.containsKey(a) && shapeSizeMapping.containsKey(b)){
            retval = shapeSizeMapping.get(b) - shapeSizeMapping.get(a);
        } 
        return retval;
    }

    public String toString(){
        String retval = "";
        if(from != null){
            retval += from.getName();
        } else {
            retval += " null ";
        }

        retval += " ---> ";
        if(to != null){
            retval += to.getName();
        } else {
            retval +=" null ";
        }
        return retval;
    }

    //don't want to override equals here. We use it for other things
    public boolean sameAs(Transformation test){
        boolean retval = true;
        if(this.metrics != null && test.metrics != null){
            for(int i=0; i<metrics.length; i++){
                if(this.metrics[i] != test.metrics[i]){
                    retval = false;
                }
            }
        } else {
            System.out.println("metrics boolean[] is null");
            retval = false;
        }
        return retval;
    }
}