/*Shortest Path: Does Breadth first Traversal on a weighted graph
11/20/2025 Alexander Peterson
 */

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.util.LongAccumulator;
import scala.Tuple2;


import java.io.PrintWriter;
import java.util.*;

public class SudokuValidator {

    private static int GridHelper(int row, int col){
        int srow = row / 3;
        int scol = col / 3;
        return (srow * 3) + scol;
    }

    private static Tuple2<Integer, Integer> posIncrementHelper(Tuple2<Integer, Integer> curPos){
        int row = curPos._1;
        int col = curPos._2;

      if(col + 1 > 8) {  // Moving past last column
            if(row + 1 > 8) {  // And already on last row
                row = -1;
                col = -1;
            } else {
                col = 0;
                row += 1;
            }
        }
        else{
            col += 1;
        }
        return new Tuple2<Integer,Integer>(row, col);
    }
    public static void main(String[] args) {
	// validate arguments.
	if ( args.length < 2 ) {
            System.out.println("Usage: inputfolder outputfile");
            System.exit(-1);
	}

	// start Sparks and read a given input file
	String inputFolder = args[0];
    String outputFolder = args[1];
        SparkConf conf = new SparkConf( ).setAppName( "Spark Sudoku Solver: Alex's Implementation" );
        JavaSparkContext jsc = new JavaSparkContext( conf );
	JavaPairRDD<String, String> files = jsc.wholeTextFiles(inputFolder);

    // define one global variables
	LongAccumulator active = jsc.sc( ).longAccumulator( );            // # active nodes
    files.collect();
    System.err.println( "Starting Number of Nodes: " + active);
    System.err.println( "Number of Puzzles: " + files.count());

	// now start a timer
	System.err.println( "Timer got started." );
	long startTime = System.currentTimeMillis();

	// create the initial network information
	JavaPairRDD<String, String> fileMatchs = files.mapToPair( file -> {
		// identify each node name
        String filename = file._1;
		String[] lines = file._2.split("\\R");
        //System.err.println( "Working on file: " + filename );
        int[][] board = new int[9][9];
        //System.err.println( "Number of Lines: " + lines.length );
        int crow = 0;
        for(String line : lines){
            if(crow >= 9){
                break;
            }
            //System.err.println( "Current Line: " + line );
            String[] tokens = line.split(" ");
            //System.err.println( "Number of Tokens: " + lines.length );
            if(tokens.length < 9){
                return new Tuple2<String,String>("Error", filename);
            }
            for(int i = 0; i < 9; i++){
                //System.err.println( "Current Tokens " + i +": " + tokens[i] );
                board[crow][i] = Integer.parseInt(tokens[i]);
                
            }
            crow++;
        }
        //Set up Row, Column, and Square trackers
        HashSet<Integer>[] rowset = new HashSet[9];
        HashSet<Integer>[] colset = new HashSet[9];
        HashSet<Integer>[] squareset = new HashSet[9];
        for (int i = 0; i < 9; i++) {
            rowset[i] = new HashSet<>();
            colset[i] = new HashSet<>();
            squareset[i] = new HashSet<>();
        }
        //Main loop checks if current checked slot already exists in a row, column, and square
        String boardState = "Solvable";
        for(int row = 0; row < 9; row++){
            for(int col = 0; col < 9; col++){
                //Row check
                int slotvalue = board[row][col];
                int squareslot = GridHelper(row, col);
                if(rowset[row].contains(slotvalue) || colset[col].contains(slotvalue) || squareset[squareslot].contains(slotvalue)){
                    boardState = "Unsolvable";
                    break;
                }
                //Add it if still Solvable
                if(slotvalue != 0){
                    rowset[row].add(slotvalue);
                    colset[col].add(slotvalue);
                    squareset[squareslot].add(slotvalue);
                }
            }
            if(boardState.equals("Unsolvable")){
                break;
            }
        }
		//System.err.println( filename + " is " + boardState);
		// return each node's information
        if(boardState.equals("Unsolvable")){
            return new Tuple2<>( boardState, filename );
        }
        //Solving Portion
        Stack<Tuple2<Integer,Tuple2<Integer, Integer>>> trackStack = new Stack<>();
        Tuple2<Integer, Integer> currentPosTuple2 = new Tuple2<>(0,0);
        int currentNumber = 1;
        boolean isRunning = true;

        System.err.println("Starting solver for: " + filename);
        while(isRunning){
            //Get Board Position
            int row = currentPosTuple2._1;
            int col = currentPosTuple2._2;
            //Board is solved
            if(row < 0 && col < 0){
                isRunning = false;
                break;
            }
            //Already filled Slot before solving, skip
            if(board[row][col] != 0){

                currentPosTuple2 = posIncrementHelper(currentPosTuple2); 
                //System.err.println(filename + ": Skiping to " + currentPosTuple2._1 + " " + currentPosTuple2._2);
                currentNumber = 1;  // Reset when moving forward
                continue;
            }

            boolean foundValid = false;

            for(int i = currentNumber; i <= 9; i++){
                int squareslot = GridHelper(row, col);
                if(!rowset[row].contains(i) && !colset[col].contains(i) && !squareset[squareslot].contains(i)){

                    currentNumber = 1;
                    Tuple2<Integer,Tuple2<Integer, Integer>> stackElement = new Tuple2<>(i,currentPosTuple2);
                    rowset[row].add(i);
                    colset[col].add(i);
                    squareset[squareslot].add(i);
                    board[row][col] = i;
                    trackStack.push(stackElement);
                    //System.err.println(filename + ": Adding "+ i + " to " + currentPosTuple2._1 + " " + currentPosTuple2._2);
                    currentPosTuple2 = posIncrementHelper(currentPosTuple2);

                    //System.err.println(filename + ": Moving to " + currentPosTuple2._1 + " " + currentPosTuple2._2);
                    foundValid = true;
                    break;
                }
            }
            //If a valid number was found, move to the next slot
            if(foundValid){
                continue;
            }
            // No more moves to backtrack - puzzle is unsolvable
            if(trackStack.isEmpty()){

                boardState = "Unsolvable";
                isRunning = false;
                break;
            }

            //Failed to find valid number so backtracking
            Tuple2<Integer,Tuple2<Integer, Integer>> stackElement = trackStack.pop();
            currentPosTuple2 = stackElement._2;
            int lastTriedNumber = stackElement._1;
            //System.err.println(filename + ": Backtracking with "+ lastTriedNumber + " to " + currentPosTuple2._1 + " " + currentPosTuple2._2);
            row = currentPosTuple2._1;
            col = currentPosTuple2._2;
            int squareslot = GridHelper(row, col);
            board[row][col] = 0;
            rowset[row].remove(lastTriedNumber);
            colset[col].remove(lastTriedNumber);
            squareset[squareslot].remove(lastTriedNumber);
            currentNumber = lastTriedNumber + 1;

        }

        if(boardState.equals("Unsolvable")){
            return new Tuple2<>( boardState, filename );
        }

        filename += " ";
        for(int row = 0; row < 9; row++){
            for(int col = 0; col < 9; col++){
                filename += board[row][col];
            }
        }
        return new Tuple2<>( boardState, filename );
	});

    JavaPairRDD<String, Iterable<String>> filesgrouped = fileMatchs.groupByKey();
    List<Tuple2<String, Iterable<String>>> grouplist = filesgrouped.collect();

	// just for debugging
	System.err.println( "Finished Solving Puzzles" );
    System.err.println( "Number of keys:" + grouplist.size());

    try(PrintWriter fout = new PrintWriter(outputFolder)){
        for(Tuple2<String, Iterable<String>> pair : grouplist){
            String ckey = pair._1;
            Iterable<String> filelist = pair._2;
            fout.println(ckey + ":");
            for(String file : filelist){
                fout.println(file);
                
            }
            fout.println();

        }
        System.err.println("Successfully Wrote File!");
    }catch(Exception e){
        System.err.println("Error Occurred Exporting Output File!");
    }

    // stop the timer
    long endTime = System.currentTimeMillis();
    System.err.println("Elapsed Time: " + (endTime - startTime));
    jsc.stop();

    }
}
