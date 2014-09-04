package project1;

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

    public Agent() {
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
            //deal with horizontal axis only
            
            //A is to B as C is to #
            RavensFigure figA = figs.get("A");

            //create two 2x1 matrices, 1 for the rule the other for the answer
            String dimensions = problemType.split("x")

            //inverse the matrix from 2x1 to 1x2 to deal with horizontal axis easier
            Graph[][] mainMatrix = new Graph[1][2]
            
            for(RavensObject robj : )

        } else if(problemType.equals("2x2")){
            //deal with vertical and horizontal axes

        } else if (problemType.equals("3x3")) {
            //deal with vertical, horizontal, and diagonal axes
        }
        String answer = problem.checkAnswer(retVal);
        //store answer somewhere to check after re-weighting
        if(retVal.equals(answer)){
            //perform meta-reasoning
        }
        return retVal;
    }
}
