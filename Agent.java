package project1;

import org.jgrapht.*;
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
        String retVal = "0";

        //get the problem figures here so as to not call problem.getFigures() over and over
        HashMap<String, RavensFigure> figs = problem.getFigures();

        //handle each type of problem a little differently.
        String problemType = problem.getProblemType();
        if(problemType.equals("2x1")){

            //inverse the matrix from 2x1 to 1x2 to deal with horizontal axis easier
            Multigraph[][] ruleMatrix = new Multigraph[1][2];
            Multigraph[][] relationshipMatrix = new Multigraph[1][2];

            //A is to B as C is to #            
            for(RavensFigure fig : problem.getFigures().values()){

                String name = fig.getName();

                //create graph for figure
                //these don't include relationships so there shouldn't be multiple permutations
                Multigraph graphedFigure = createGraphForFigure(fig);

                //if name is a letter, place in its matrix
                    int[] coordinates = twoBy1_mapping.get(name);
                    if(name.equals("A") || name.equals("B")){
                        //place in rule matrix
                        ruleMatrix[coordinates[0]][coordinates[1]] = graphedFigure;
                    } else {
                        //place in relationship matrix
                        relationshipMatrix[coordinates[0]][coordinates[1]] = graphedFigure;
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
        return retVal;
    }

    private Multigraph createGraphForFigure(RavensFigure figure){

        ClassBasedEdgeFactory edgeFactory  = new ClassBasedEdgeFactory(Edge.class);

        Multigraph<RavensObject, Edge> retval = new Multigraph<RavensObject, Edge>(edgeFactory);

        for(RavensObject figObj : figure.getObjects()){
            //add a new vector if the vector doesn't already exist
            //Vertex v = new Vertex(figObj.getName());
            retval.addVertex(figObj);
        }

        Set<RavensObject> verticies = retval.vertexSet();
        Iterator<RavensObject> it = verticies.iterator();
        while(it.hasNext()){
            RavensObject ro = it.next();
            for(RavensAttribute attr : ro.getAttributes()){
                //add a relationship edge
                if(intraGraphRelationships.contains(attr.getName())){
                    Iterator<RavensObject> vit = verticies.iterator();
                    RavensObject target = null;
                    while(vit.hasNext()){
                        target = vit.next();
                        if(target.getName().equals(attr.getValue())){
                            break;
                        }
                    }
                    if (target != null) {
                        Edge edge = retval.addEdge(ro, target);
                        edge.setName(attr.getName());
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
    private void generateRelationships(Multigraph one, Multigraph two){

    }

    private Set<Multigraph> vectorCombinations(Multigraph one, Multigraph two){
        //Set one.vertexSet();
        return null;
    }

    private void printGraph(Multigraph<RavensObject, Edge> mg){
        //print graph to make sure we made it properly
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        Set<Edge> edges = mg.edgeSet();
        Iterator<Edge> edgeIt = edges.iterator();
        while(edgeIt.hasNext()){
            Edge edge = edgeIt.next();
            //RavensObject from = edge
            sb.append(edge.toString()+"\n");
        }
        System.out.println(sb.toString());
    }
}
