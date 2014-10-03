package project1;

import java.util.*;
import java.lang.*;

/**
 * Your Agent for solving Raven's Progressive Matrices. You MUST modify this
 * file.
 * 
 * You may also create and submit new files in addition to modifying this file.
 * 
 * Make sure your file retains methods with the signatures:
 * public Agent()
 * public char Solve(RavensProblem problem)
 * 
 * These methods will be necessary for the project's main method to run.
 * 
 */
public class Agent {
    /**
     * The default constructor for your Agent. Make sure to execute any
     * processing necessary before your Agent starts solving problems here.
     * 
     * Do not add any variables to this signature; they will not be used by
     * main().
     * 
     */

    //TODO: may need to keep a list of the problems that we solve to reweight in the future
    //could be a hashmap of problem : matrix< of graphs

    //mappings
    private HashMap<String, int[]> twoBy1_mapping;
    private HashMap<String, int[]> twoBy2_mapping;
    private HashMap<String, int[]> threeBy3_mapping;
    private HashMap<String, Integer> shapeSizeMapping;
    private HashMap<String, Integer> transformationWeights;

    private final int UNKNOW_SIZE = -1000;

    private ArrayList<String> intraGraphRelationships;

    public Agent() {

        //initialzie mapping
        //2x1 problem mapping to 1x2
        //array 1 [[A, B]]
        //array 2 [[C, #]]
        twoBy1_mapping = new HashMap<String, int[]>();
        twoBy1_mapping.put("A", new int[]{0, 0});
        twoBy1_mapping.put("B", new int[]{0, 1});
        //overlap of C with A and # with B here. Will be two separate arrays.
        twoBy1_mapping.put("C", new int[]{0, 0});
        twoBy1_mapping.put("#", new int[]{0, 1});

        /*
        [
            [A, B]
            [C, #]
        ]
        */
        twoBy2_mapping = new HashMap<String, int[]>();
        twoBy2_mapping.put("A", new int[]{0, 0});
        twoBy2_mapping.put("B", new int[]{0, 1});
        twoBy2_mapping.put("C", new int[]{1, 0});
        twoBy2_mapping.put("#", new int[]{1, 1});

        /*
        [
            [A, B, C]
            [D, E, F]
            [G, H, #]
        ]
        */
        threeBy3_mapping = new HashMap<String, int[]>();
        threeBy3_mapping.put("A", new int[]{0, 0});
        threeBy3_mapping.put("B", new int[]{0, 1});
        threeBy3_mapping.put("C", new int[]{0, 2});
        threeBy3_mapping.put("D", new int[]{1, 0});
        threeBy3_mapping.put("E", new int[]{1, 1});
        threeBy3_mapping.put("F", new int[]{1, 2});
        threeBy3_mapping.put("G", new int[]{2, 0});
        threeBy3_mapping.put("H", new int[]{2, 1});
        threeBy3_mapping.put("#", new int[]{2, 2});

        transformationWeights = new HashMap<String, Integer>();
        transformationWeights.put("unchanged", 5);
        //TODO: how often does reflected get used
        transformationWeights.put("reflected", 4);
        transformationWeights.put("rotated", 3);
        transformationWeights.put("scaled", 2);
        transformationWeights.put("deleted", 1);
        transformationWeights.put("changedShape", 0);

        

        //initialzie weights of types of edges here
    }
    /**
     * The primary method for solving incoming Raven's Progressive Matrices.
     * For each problem, your Agent's Solve() method will be called. At the
     * conclusion of Solve(), your Agent should return a String representing its
     * answer to the question: "1", "2", "3", "4", "5", or "6". These Strings
     * are also the Names of the individual RavensFigures, obtained through
     * RavensFigure.getName().
     * 
     * In addition to returning your answer at the end of the method, your Agent
     * may also call problem.checkAnswer(String givenAnswer). The parameter
     * passed to checkAnswer should be your Agent's current guess for the
     * problem; checkAnswer will return the correct answer to the problem. This
     * allows your Agent to check its answer. Note, however, that after your
     * agent has called checkAnswer, it will *not* be able to change its answer.
     * checkAnswer is used to allow your Agent to learn from its incorrect
     * answers; however, your Agent cannot change the answer to a question it
     * has already answered.
     * 
     * If your Agent calls checkAnswer during execution of Solve, the answer it
     * returns will be ignored; otherwise, the answer returned at the end of
     * Solve will be taken as your Agent's answer to this problem.
     * 
     * @param problem the RavensProblem your agent should solve
     * @return your Agent's answer to this problem
     */
    public String Solve(RavensProblem problem) {

        //default answer to 1
        //this should only stay unchanged if we don't recognize the problemType
        String retVal = "1";
        try {
            //get the problem figures here so as to not call problem.getFigures() over and over
            HashMap<String, RavensFigure> figs = problem.getFigures();

            //handle each type of problem a little differently.
            String problemType = problem.getProblemType();
            if(problemType.equals("2x1")){

                int answer = 1;

                List<Transformation> ruleTransformations = correlateRavensFigures(problem.getFigures().get("A"), problem.getFigures().get("B"));
                List<List<Transformation>> candidateTransformations = new ArrayList<List<Transformation>>();
                for(int i=1; i<= 6; i++){
                    candidateTransformations.add(correlateRavensFigures(problem.getFigures().get("C"), problem.getFigures().get(""+i)));    
                }

                double highScore = 0.0;
                for(int i=0; i<candidateTransformations.size(); i++){
                    System.out.println();
                    System.out.println("------------");
                    System.out.println("Scoring "+ruleTransformations+" against "+candidateTransformations.get(i));
                    double score = scoreTransformationSimilarity(ruleTransformations, candidateTransformations.get(i), (i+1)+"");
                    System.out.println("score : "+score);
                    if(score > highScore){
                        System.out.println("score : "+score+" higher than current high score : "+highScore);
                        highScore = score;
                        answer = i+1;
                        System.out.println("setting answer to "+answer);
                    }
                }
                System.out.println("returning : "+answer);
                return answer+"";
                /*RavensFigure figA = figs.get("A");
                String figAObjectString = getFigureObjectString(figA);
                
                RavensFigure figB = figs.get("B");
                String figBObjectString = getFigureObjectString(figB);

                RavensFigure figC = figs.get("C");
                String figCObjectString = getFigureObjectString(figC);

                ArrayList<String> permutationsOfA = permute("", figAObjectString, new ArrayList<String>());
                ArrayList<String> permutationsOfC = permute("", figCObjectString, new ArrayList<String>());

                Map<String, String> answerObjectStrings = new HashMap<String, String>();
                for(Map.Entry<String, RavensFigure> figEntry : figs.entrySet()){
                    try{
                        Integer.parseInt(figEntry.getKey());

                        System.out.println("Adding "+figEntry.getKey()+" to answer set");

                        String tempObjectString = getFigureObjectString(figEntry.getValue());
                        answerObjectStrings.put(figEntry.getKey(), tempObjectString);

                    } catch(Exception e){}
                }

                double bestScore = 0.0;
                for(int i = 0; i<permutationsOfA.size(); i++){
                    String permutation = permutationsOfA.get(i);
                    Map<String, ArrayList<String>> permAtoB = computeTransformations(permutation, figBObjectString, figA, figB);
                    //double score = scoreTransformations(interGraphRelationships);
                    for(int j = 0; j<permutationsOfC.size(); j++){
                        String permutationOfC = permutationsOfC.get(j);
                        for(Map.Entry<String, String> answerEntry : answerObjectStrings.entrySet()){
                            String answerObjectString = answerEntry.getValue();
                            Map<String, ArrayList<String>> cToDRelationships = computeTransformations(permutationsOfC.get(j), answerObjectString, figC, figs.get(answerEntry.getKey()));
                            double score = scoreTransformations(permutation, permAtoB, permutationOfC, cToDRelationships);
                            System.out.println(""+permutation+" -> "+figBObjectString+" : "+permutationOfC+" -> ("+answerEntry.getKey()+")"+answerObjectString+" scored "+score);
                            if(score > bestScore){
                                bestScore = score;
                                System.out.println(""+score+" > "+bestScore+". switching current answer to "+answerEntry.getKey());
                                retVal = answerEntry.getKey();
                            }
                            //double scoreTransformations(cToDRelationships);
                        }
                    }
                }

                for(int i=0; i<permuationsOfA.size(); i++){
                    String aPermutation = permuationsOfA.get(i);
                }*/

            } else if(problemType.equals("2x2")){
                //use system of scorring correlation instead of permuations because they become very expensive 
                //deal with vertical and horizontal axes

                //String dimensions = problemType.split("x")

            } else if (problemType.equals("3x3")) {
                //deal with vertical, horizontal, and diagonal axes

                //String dimensions = problemType.split("x")
            }
            String answer = problem.checkAnswer(retVal);
            //store answer somewhere to check after re-weighting
            if(retVal.equals(answer)){
                //perform meta-reasoning
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("returning "+retVal);
        return retVal;
    }

    /**
    * Extract the highest similarity score between two Lists of transformations
    */
    private double scoreTransformationSimilarity(List<Transformation> ruleTransformations, List<Transformation> candidate, String answerNubmer){
        
        //overall similarity between two sets of transformations
        double similarity = 0;

        int positiveRuleTransformations = 0;
        for(int i=0; i<ruleTransformations.size(); i++){
            if(ruleTransformations.get(i).score != 0){
                positiveRuleTransformations++;
            }
        }
        //count up the transformations that have scores
        int positiveCandidateTransformations = 0;
        for(int i=0; i<candidate.size(); i++){
            if(candidate.get(i).score != 0){
                positiveCandidateTransformations++;
            }
        }
        System.out.println("# rule transformations : "+positiveRuleTransformations);
        System.out.println("# candidate transformations : "+positiveCandidateTransformations);
        //same number of positive transformations give it a high correlation
        if(positiveRuleTransformations == positiveCandidateTransformations){
            System.out.println("same number of positive transformations +2");
            similarity += 2;
        }

        for(int i=0; i< ruleTransformations.size(); i++){
            //similarity between current rule transformation and each of the candidate transformations
            double compareSimilarity = 0;
            Transformation ab = ruleTransformations.get(i);
            //hold which transformations we've already calculated
            List<Transformation> alreadyMappedTransformation = new ArrayList<Transformation>();
            //compares transformations in order they were created (not necessarily the highest scoring way)
            Transformation map = null;
            String mapLog = "";
            for(int j=0; j< candidate.size(); j++){
                
                double currentSimilarity = 0.0;

                Transformation c_ = candidate.get(j);

                if(alreadyMappedTransformation.contains(c_)){
                    continue;
                }

                StringBuffer similarityLog = new StringBuffer(); 

                //System.out.println("((A to B) "+ruleTransformations.get(i)+") against ((C to "+answerNubmer+")) "+candidate.get(j));

                //added or delted
                if(ab.added && c_.added){
                    //shaped was added in both transformations
                    //System.out.println("added in both transformations +3");
                    similarityLog.append("added in both transformations +3\n");
                    //similarity += 3;
                    currentSimilarity += 3;
                }
                if(ab.deleted && c_.deleted){
                    //shape was deleted from both
                    //System.out.println("deleted in both transformations +3");
                    similarityLog.append("added in both transformations +3\n"); 
                    //similarity += 3;
                    currentSimilarity += 3;
                }

                //shape
                if(!ab.changedShape && !c_.changedShape){
                    if(!ab.deleted && !c_.deleted){
                        //either both changed shape or did not
                        //similarity += 2;    
                        currentSimilarity += 2;
                        //System.out.println("Both didn't change shape +2");
                        similarityLog.append("Both didn't change shape +2\n");
                        //check if that shapes are the same
                        String abToShape = "";
                        String c_ToShape = "";
                        for(int k = 0; k<ab.to.getAttributes().size(); k++){
                            if(ab.to.getAttributes().get(k).getName().equals("shape")){
                                abToShape = ab.from.getAttributes().get(k).getValue();
                                //System.out.println("set ab shape to "+abFromShape);
                            }
                        }
                        for(int k = 0; k<c_.to.getAttributes().size(); k++){
                            if(c_.to.getAttributes().get(k).getName().equals("shape")){
                                c_ToShape = c_.to.getAttributes().get(k).getValue();
                                //System.out.println("set c_ shape to "+c_FromShape);
                            }
                        }
                        if(!abToShape.equals("") && !c_ToShape.equals("")){
                            if(abToShape.equals(c_ToShape)){
                                similarityLog.append("same shape between frames +2");
                                currentSimilarity += 2;
                            }
                        }
                    }
                }
                
                if(ab.changedShape && c_.changedShape){
                    //either both changed shape or did not
                    similarity += 2;    
                    System.out.println("Both changed shape");
                }

                //size
                //|| ((!ab.shrunk && !c_.shrunk) || (!ab.expaned && !c_.expaned)))
                if((ab.shrunk && c_.shrunk) || (ab.expaned && c_.expaned))  {
                    //similarity += 2;
                    currentSimilarity += 2;
                    similarityLog.append("Shrunk or expaned +2\n");
                    //System.out.println("Shrunk or expaned +2");
                    if(ab.sizeChange == c_.sizeChange){
                        similarityLog.append("same size change +1\n");
                        //System.out.println("same size change +1");
                        currentSimilarity++;
                    }
                }

                //check for flips
                boolean alreadyFlipped = false;
                if(ab.flippedHorizontally && c_.flippedHorizontally){
                    //System.out.println("flippedHorizontally +2");
                    similarityLog.append("flippedHorizontally +2\n");
                    currentSimilarity += 2;
                    alreadyFlipped = true;
                }

                if((ab.flippedVertically && c_.flippedVertically)){
                    //System.out.println("flippedVertically +2");
                    similarityLog.append("flippedVertically +2\n");
                    currentSimilarity += 2;   
                    alreadyFlipped = true;
                }
                //rotated
                if((ab.rotated && c_.rotated) && !alreadyFlipped){
                    //similarity++;
                    currentSimilarity++;
                    //System.out.println("both rotated +1");
                    similarityLog.append("both rotated +1\n");
                    if(ab.degreesRotated == c_.degreesRotated && ab.degreesRotated != 0){
                        //System.out.println("rotated same # of degress +1");
                        similarityLog.append("rotated same # of degress +1\n");
                        //similarity++;
                        currentSimilarity++;
                    }
                }

                //Fill matches
                if(ab.fillChanged && c_.fillChanged){
                    //System.out.println("both fills changed +1");
                    similarityLog.append("both fills changed +1\n");
                    //similarity++;
                    currentSimilarity++;
                    //check which fills were changed
                    List<String> abFills = ab.fillTransformations;
                    List<String> c_Fills = c_.fillTransformations;

                    for(int k=0; k<c_Fills.size(); k++){
                        if(abFills.contains(c_Fills.get(k))){
                            //similarity++;
                            //System.out.println("contains same fill +1");
                            similarityLog.append("contains same fill +1\n");
                            currentSimilarity++;
                        } else {
                            //System.out.println("removing similarity -1. Missing "+c_Fills.get(k));
                            similarityLog.append("removing similarity -1. Missing "+c_Fills.get(k)+"\n");
                            //similarity--;
                            currentSimilarity--;
                        }
                    }
                }
                if(currentSimilarity > compareSimilarity){
                    compareSimilarity = currentSimilarity;
                    map = c_;
                    //System.out.println("setting transformation");
                    mapLog = similarityLog.toString();
                }
            }
            
            System.out.println("((A to B) "+ruleTransformations.get(i)+") against ((C to "+answerNubmer+")) "+map);
            if(!mapLog.equals("")){
                System.out.println(mapLog);
            } else {
                System.out.println("no mapping.");
            }
            System.out.println("similarity "+similarity+" + "+compareSimilarity);

            similarity += compareSimilarity;

            alreadyMappedTransformation.add(map);

        }
        return similarity;
    }


    private ArrayList<String> permute(String prefix, String left, ArrayList<String> placeHolder){
        if(left.length() == 0){
            placeHolder.add(prefix);
        } else {
            for(int i = 0; i<left.length(); i++){
                permute(prefix + left.charAt(i), left.substring(0, i) + left.substring(i+1), placeHolder);
            }
        }
        return placeHolder;
    }

    private String getFigureObjectString(RavensFigure figure){
        StringBuffer sb = new StringBuffer();
        for(RavensObject figObj : figure.getObjects()){
            //TODO: REVISE IF OBJECT NAMES BECOME MORE THAN 1 CHARACTER
            sb.append(figObj.getName());
        }
        return sb.toString();
    }

    private Map<String, ArrayList<String>> computeTransformations(String permutation, String relateTo, RavensFigure figA, RavensFigure figB){
        //could be ArrayList<ArrayList<String>>
        //the object name isn't necessary if we are testing all permutations
        Map<String, ArrayList<String>> relationships = new HashMap<String, ArrayList<String>>();
        //put objects into map for easy lookup
        Map<String, RavensObject> figAObjects = objectsToMap(figA.getObjects());
        Map<String, RavensObject> figBObjects = objectsToMap(figB.getObjects());
        //TODO:Make sure to check the size of permuatation and relateTo
        char[] aObjNames = permutation.toCharArray();
        char[] bObjNames = relateTo.toCharArray();
        System.out.println("objects in A "+aObjNames.length+" objInB "+bObjNames.length);
        for(int i=0; i<aObjNames.length; i++){
            
            ArrayList<String> transformations = new ArrayList<String>();
            
            if(i >= bObjNames.length){
                System.out.println(aObjNames[i]+" deleted");
                transformations.add("deleted");
            } else {
                System.out.println("finding transformations between "+aObjNames[i]+" and "+bObjNames[i]);
                //find transformations
                RavensObject objInA = figAObjects.get(String.valueOf(aObjNames[i]));
                RavensObject objInB = figBObjects.get(String.valueOf(bObjNames[i]));
                
                Map<String, RavensAttribute> objAAttrs = attributeToMap(objInA.getAttributes());
                Map<String, RavensAttribute> objBAttrs = attributeToMap(objInB.getAttributes());

                for(Map.Entry<String, RavensAttribute> attribute : objAAttrs.entrySet()){
                    String key = attribute.getKey();
                    String val = attribute.getValue().getValue();

                    //TODO:Check for attributes in B and not A
                    //TODO:Separate positional transformations from physical transformations
                    if(objBAttrs.get(key) != null){
                        //analyze attribute and create relationship
                        String valForB = objBAttrs.get(key).getValue();
                        switch(key){
                            case "shape":
                                if(!val.equals(valForB)){
                                    transformations.add("changedShape:"+valForB);
                                }
                                break;
                            case "fill":
                                //TODO:Make a Set. Should be unique values.
                                List<String> aFills = new ArrayList(Arrays.asList(val.split(",")));

                                if(valForB != null){
                                    List<String> bFills = new ArrayList(Arrays.asList(valForB.split(",")));
                                    for(String aFill : aFills){
                                        if(!(bFills.contains(aFill))){
                                            transformations.add("fill:removed:"+aFill);
                                        }
                                    }
                                    for(String bFill : bFills){
                                        if(!(aFills.contains(bFill))){
                                            transformations.add("fill:added:"+bFill);
                                        }
                                    }
                                } else {
                                    for(String aFill : aFills){
                                        transformations.add("fill:added:"+aFill);
                                    }
                                }
                                break;
                            case "size":
                                if(valForB != null){
                                    val = val.trim().toLowerCase();
                                    valForB = valForB.trim().toLowerCase();
                                    /*int sizeChange = calculateSizeChange(val, valForB);
                                    if(sizeChange != 0 && sizeChange != UNKNOW_SIZE){
                                        transformations.add("scaled:"+sizeChange);
                                    } */
                                }
                                break;
                            case "inside":
                                break;
                            case "above":
                                break;
                            case "overlaps":
                                break;
                            case "angle":
                                int angleA = Integer.parseInt(val);
                                int angleB = Integer.parseInt(valForB);
                                int angleRotated = 0;
                                if(angleA != angleB){
                                    //this allows for negative rotations
                                    angleRotated = angleB - angleA;
                                } else {
                                    angleRotated = 0;
                                }
                                transformations.add("rotated:"+angleRotated);
                                break;
                            case "left-of":
                                break;
                            case "vertical-flip":
                                if(!val.equals(valForB)){
                                    transformations.add("reflected:vertical");
                                }
                                break;
                            default:
                                break;
                        }
                    } else {
                        //attribute in A not in B
                        System.out.println("Attribute "+key+" in object "+objInA.getName()+" in first graph but not in second object "+objInB.getName());
                        transformations.add(""+key+":removed");
                    }
                }
                
                for(Map.Entry<String, RavensAttribute> attribute : objBAttrs.entrySet()){
                    String key = attribute.getKey();
                    String value = attribute.getValue().getValue();
                    if(intraGraphRelationships.contains(key)){
                        if(objAAttrs.get(key) == null){
                            transformations.add(key+":added");
                        }
                    }
                }
            }
            if(transformations.size() == 0){
                //this is what we want to see
                transformations.add("unchanged");
            }
            relationships.put(String.valueOf(aObjNames[i]), transformations);
        }
        if(permutation.length() < relateTo.length()){
            //TODO:an object was added to the frame
            //find a way to come up with a random key
        }
        return relationships;
    }

    private Map<String, RavensObject> objectsToMap(ArrayList<RavensObject> ravensObjects){
        Map<String, RavensObject> ravensObjectsMap = new HashMap<String, RavensObject>();
        for(int i=0; i<ravensObjects.size(); i++){
            ravensObjectsMap.put(ravensObjects.get(i).getName(), ravensObjects.get(i));
        }
        return ravensObjectsMap;
    }

    private Map<String, RavensAttribute> attributeToMap(ArrayList<RavensAttribute> ravensAttributes){
        Map<String, RavensAttribute> ravensAttributeMap = new HashMap<String, RavensAttribute>();
        for(int i=0; i<ravensAttributes.size(); i++){
            ravensAttributeMap.put(ravensAttributes.get(i).getName(), ravensAttributes.get(i));
        }
        return ravensAttributeMap;
    }

    private List<Transformation> correlateRavensFigures(RavensFigure fig1, RavensFigure fig2){
        System.out.println("");
        System.out.println("Correlating figure "+fig1.getName()+" to "+fig2.getName());
        //retval
        List<Transformation> transformations = new ArrayList<Transformation>();
        //to keep track of which objects have already been mapped between fig1 and fig2
        List<RavensObject> alreadyCorrelated = new ArrayList<RavensObject>();

        for(int i=0; i<fig1.getObjects().size(); i++){
            RavensObject from = fig1.getObjects().get(i);
            //return the most closely matched object (understand it as the objec to correlate to)
            System.out.println("Mapping "+from.getName());
            RavensObject to = findCorrelatedObject(from, fig2, alreadyCorrelated);
            if(to != null){
                alreadyCorrelated.add(to);
            }
            //calculate the transformations that need to take place
            System.out.println("Creating transfomation object");
            Transformation currentTransformation = new Transformation(from, to);
            transformations.add(currentTransformation);
            System.out.println(currentTransformation);
            /*if(from != null){
                System.out.print(""+from.getName());
            } else {
                System.out.print("null ");
            }

            System.out.print(" ---> ");
            if(to != null){
                System.out.print(""+to.getName());
            } else {
                System.out.print(" null ");
            }*/
            //System.out.println(""+from.getName()+" --> "+to.getName());
        }

        //added in second figure
        for(int i=0; i<fig2.getObjects().size(); i++){
            RavensObject objectInTwo = fig2.getObjects().get(i);
            if(!alreadyCorrelated.contains(objectInTwo)){
                transformations.add(new Transformation(null, objectInTwo));
                System.out.println("Object added "+objectInTwo.getName()+" in second figure ");
            }
        }
        System.out.println("Returning "+transformations.size()+" transformations.");
        return transformations;
    }

    /**
    * Find the most closely correlated object (that hasn't already been mapped)
    * in contained in fig2 by weighting
    *
    */
    private RavensObject findCorrelatedObject(RavensObject obj, RavensFigure fig2, List<RavensObject>alreadyCorrelated){
        
        RavensObject chosenToObject = null;
        double bestCorrelationScore = 0.0;

        for(int i=0; i< fig2.getObjects().size(); i++){

            RavensObject toObject = fig2.getObjects().get(i);

            //move to next object if we've already mapped this one
            if(alreadyCorrelated.contains(toObject)){
                continue;
            }

            double currentCorrelation = 0.0;
            boolean shape_match = false;
            for(int j=0; j < obj.getAttributes().size(); j++) {
                for(int k=0; k < toObject.getAttributes().size(); k++) {
            
                    /*** PRODUCTION RULES FOR CORRELATING OBJECTS ***/          
                    //Shapes are the same
                    if (obj.getAttributes().get(j).getName().equals("shape") && toObject.getAttributes().get(k).getName().equals("shape") &&
                            obj.getAttributes().get(j).getValue().equals(toObject.getAttributes().get(k).getValue())) {    
                        System.out.println(obj.getName()+" shape matched "+toObject.getName());
                        //static value for now
                        currentCorrelation += 3;
                        shape_match = true;
                    }
                    
                    //Check other attributes if shape matches
                    if(shape_match) {
                        
                        //Sizes are the same
                        if (obj.getAttributes().get(j).getName().equals("size") && toObject.getAttributes().get(k).getName().equals("size") &&
                                        obj.getAttributes().get(j).getValue().equals(toObject.getAttributes().get(k).getValue())){
                            currentCorrelation += 2;  
                        } 
                        
                        //Fills are the same
                        if (obj.getAttributes().get(j).getName().equals("fill") && toObject.getAttributes().get(k).getName().equals("fill") &&
                                        obj.getAttributes().get(j).getValue().equals(toObject.getAttributes().get(k).getValue())){
                            currentCorrelation += 1;  
                        }
                    }
                }
            }
            //if not already mapped, map it
            if(bestCorrelationScore <= currentCorrelation) {
                bestCorrelationScore = currentCorrelation;
                chosenToObject = toObject;
            }
        //end object loop
        }

        if(chosenToObject != null) System.out.println(obj.getName()+" correlated to " + chosenToObject.getName() + " : value = " + bestCorrelationScore);

        return chosenToObject;
    }

    /**
    * Score the relationships described in the list
    * 
    */
    private double scoreTransformations(ArrayList<String> transformations){
        double score = 0.0;
        for(String transformation : transformations){
            String transformationName = null;
            if(transformation.contains(":")){
                String[] transformationData = transformation.split(":");
                if(transformationData.length >= 1){
                    transformationName = transformationData[0];
                }
            }
            if(transformationName != null){
                if(transformationWeights.containsKey(transformationName)){
                    score += transformationWeights.get(transformationName);
                }
            }
        }
        return score;
    }

    //private 
    /**
    * Generate the 
    *
    */
    private double scoreTransformations(String fig1, Map<String, ArrayList<String>> fig1Map, String fig2, Map<String, ArrayList<String>> fig2Map){
        double score = 0;
        char[] fig1Objs = fig1.toCharArray();
        int objCountIn1 = fig1Objs.length;

        char[] fig2Objs = fig2.toCharArray();
        int objCountIn2 = fig2Objs.length;

        for(Map.Entry<String, ArrayList<String>> entry : fig1Map.entrySet()){
            for(String s : entry.getValue()){
                System.out.println(""+entry.getKey()+" ---- "+s+" ---- >");
            }
        }

        for(Map.Entry<String, ArrayList<String>> entry : fig2Map.entrySet()){
            for(String s : entry.getValue()){
                System.out.println(""+entry.getKey()+" ---- "+s+" ---- >");
            }
        }

        if(objCountIn1 != objCountIn2){
            System.out.println(""+objCountIn1+" objects in figure 1 and "+objCountIn2+" in fig 2");
            System.out.println("Unhandled differnce in number of objects");
        }

        for(int i = 0; i<fig1Objs.length; i++){
            System.out.println("Getting relationships for "+fig1Objs[i]);
            ArrayList<String> f1Relationships = fig1Map.get(String.valueOf(fig1Objs[i]));

            ArrayList<String> f2Relationships = null;
            if(i < fig2Objs.length){
                System.out.println(String.valueOf(fig2Objs[i]));
                System.out.println(fig2Map.get(String.valueOf(fig2Objs[i])));
                //create new array list so that we can modify w/o reprocaution
                f2Relationships = new ArrayList<String>(fig2Map.get(String.valueOf(fig2Objs[i])));
            }
            if(f1Relationships != null){
                int normalizationFactor = f1Relationships.size();
                if(f2Relationships != null){
                    for(String rIn1 : f1Relationships){
                        String rIn1Name = null;
                        if(rIn1.contains(":")){
                            String[] rs = rIn1.split(":");
                            rIn1Name = rs[0];
                        } else {
                            rIn1Name = rIn1;
                        }
                        boolean matchfound = false;
                        for(String rIn2 : f2Relationships){
                            //ArrayList<String> matches = 
                            if(rIn2.contains(rIn1Name)){
                                System.out.println("Found a match!! "+rIn1+" vs. "+rIn2);
                                //TODO: Refine match to account for more details (how well do the relationships match)
                                //TODO: make sure that if we match more than one we take the best match
                                score += 1.0 / normalizationFactor;
                                matchfound = true;
                                //remove the relationship so we don't match it agains
                                f2Relationships.remove(rIn2);
                                //break so we don't multi match.
                                break;
                            }
                        }
                        if(!matchfound){
                            System.out.println("Missing corresponding "+rIn1+" relationship");
                        }
                    }
                    
                    //there are unmatched object transformations
                    if(f2Relationships.size() > 0){
                        System.out.println("Extra relationships in fig2");
                        for(String s : f2Relationships){
                            System.out.println(s);
                        }
                        int extraTransformFactor = normalizationFactor + f2Relationships.size();
                        score -= (1.0/extraTransformFactor);
                    }
                } else {
                    //there were less figures in the next corresponding representation
                    //score += (transformationWeights.get("deleted")/objCountIn1);
                    System.out.println("Unhandled differnce in number of objects ----");
                }

            } else {
                System.out.println("no relationships for "+fig1Objs[i]);
                for(Map.Entry<String, ArrayList<String>> entry : fig1Map.entrySet()){
                    for(String s : entry.getValue()){
                        System.out.println(""+entry.getKey()+" ---- "+s+" ---- >");
                    }
                }
            }
        }

        return score;
    }
}
