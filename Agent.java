package project1;

import org.jgrapht.*;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.*;
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

        intraGraphRelationships = new ArrayList<String>(Arrays.asList(new String[]{"inside", "above", "overlaps", "left-of"}));

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

                //inverse the matrix from 2x1 to 1x2 to deal with horizontal axis easier
                DirectedMultigraph[][] ruleMatrix = new DirectedMultigraph[1][2];
                DirectedMultigraph[][] relationshipMatrix = new DirectedMultigraph[1][2];

                ArrayList<DirectedMultigraph> answerGraphs = new ArrayList<DirectedMultigraph>();
                //A is to B as C is to #            
                for(RavensFigure fig : problem.getFigures().values()){

                    String name = fig.getName();

                    //create graph for figure
                    //these don't include relationships so there shouldn't be multiple permutations
                    DirectedMultigraph graphedFigure = createGraphForFigure(fig);
                    printGraph(name, graphedFigure);
                    
                    char[] chars = name.toCharArray();
                    if(chars.length == 1 && Character.isLetter(chars[0])){
                        int[] coordinates = twoBy1_mapping.get(name);
                        if(name.equals("A") || name.equals("B")){ 
                            //place in rule matrix
                            ruleMatrix[coordinates[0]][coordinates[1]] = graphedFigure;
                        } else if(name.equals("C")){
                            //place in relationship matrix
                            relationshipMatrix[coordinates[0]][coordinates[1]] = graphedFigure;
                        }
                    } else {
                        //place graphedFigure in set of possible answers
                        answerGraphs.add(graphedFigure);
                    }
                }

                //count the number of relationship permutations for the rule graphs

                //for(int i = 0; i<permutations; i++){}
                    //deal with horizontal axis only
                    int rowLength = ruleMatrix[0].length;
                    //each row
                    for(int i = 0; i<ruleMatrix.length; i++){
                        for(int j=0; j<rowLength; j += 2){
                            //ge nerate relationship with vertex mapping provided by next permutation
                            generateRelationships(ruleMatrix[i][j], ruleMatrix[i][j+1]);
                        }
                    }
                //}

            } else if(problemType.equals("2x2")){
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
        return retVal;
    }

    private DirectedMultigraph createGraphForFigure(RavensFigure figure){

        ClassBasedEdgeFactory edgeFactory  = new ClassBasedEdgeFactory(Edge.class);

        DirectedMultigraph<RavensObject, Edge> retval = new DirectedMultigraph<RavensObject, Edge>(edgeFactory);

        for(RavensObject figObj : figure.getObjects()){
            //add a new vector if the vector doesn't already exist
            //Vertex v = new Vertex(figObj.getName());
            retval.addVertex(figObj);
        }

        Set<RavensObject> verticies = retval.vertexSet();
        Iterator<RavensObject> it = verticies.iterator();
        while(it.hasNext()){
            RavensObject object = it.next();

            for(RavensAttribute attr : object.getAttributes()){
                //add a relationship edge
                if(intraGraphRelationships.contains(attr.getName())){
                    System.out.println("mapping "+attr.getName()+" as intragraph relationship");
                    System.out.println("looking for object[s] "+attr.getValue());
                    
                    String[] objects = attr.getValue().split(",");

                    for(int i=0; i<objects.length; i++){

                        Iterator<RavensObject> vit = verticies.iterator();
                        RavensObject target = null;
                        while(vit.hasNext()){
                            target = vit.next();
                            if(target.getName().equals(objects[i])){
                                System.out.println("found object "+target.getName());
                                break;
                            }
                        }

                        if (target != null && target.getName().equals(objects[i])) {
                            
                            Edge edge = new Edge(attr.getName());

                            if(retval.addEdge(object, target, edge)){
                                System.out.println(object.getName()+"---->"+target.getName());
                            } else {
                                System.out.println("Edge "+object.getName()+"---->"+target.getName()+" already exists");
                            }

                        }

                    }
                }
            }
        }
        return retval;
    }

    /**
    *   Generate Transformation Relationships between two graphs
    *   could return a set of edges between verticies in each graph
    *   could return a new graph that combines the two using the new edges
    *   could take a graph reference and modify it while returning if there are more edge generation permutations
    *   
    */
    private void generateRelationships(DirectedMultigraph one, DirectedMultigraph two){

    }

    private Set<DirectedMultigraph> vectorCombinations(DirectedMultigraph one, DirectedMultigraph two){
        //Set one.vertexSet();
        return null;
    }

    private void printGraph(String name, DirectedMultigraph<RavensObject, Edge> mg){
        //print graph to make sure we made it properly
        StringBuffer sb = new StringBuffer();
        sb.append("\n"+name+"\n");
        //Set<Edge> edges = mg.edgeSet();
        /*Iterator<Edge> edgeIt = edges.iterator();
        while(edgeIt.hasNext()){
            Edge edge = edgeIt.next();
            
            RavensObject source  = mg.getEdgeSource(edge);
            RavensObject target = mg.getEdgeTarget(edge);

            //RavensObject from = edge
            sb.append(""+source.getName()+"-----"+edge.getName()+"----->"+target.getName()+"\n");

        }*/
        Set<RavensObject> verticies = mg.vertexSet();
        Iterator<RavensObject> vertIt = verticies.iterator();
        while(vertIt.hasNext()){
            RavensObject vertex = vertIt.next();
            Set<Edge> edges = mg.edgesOf(vertex);
            if(edges.size() > 0){
                Iterator<Edge> edgeIt = edges.iterator();
                while(edgeIt.hasNext()){
                    Edge edge = edgeIt.next();

                    RavensObject target = mg.getEdgeTarget(edge);
                    //RavensObject from = edge
                    sb.append(""+vertex.getName()+"-----"+edge.getName()+"----->"+target.getName()+"\n");
                }
            } else {
                sb.append(""+vertex.getName()+"\n");
            }
        }
        System.out.println(sb.toString());
    }
}
