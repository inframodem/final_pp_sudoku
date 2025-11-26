/*Shortest Path: Does Breadth first Traversal on a weighted graph
11/20/2025 Alexander Peterson
 */

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
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
    public static void main(String[] args) {
	// validate arguments.
	if ( args.length < 3 ) {
            System.out.println("Usage: filename source destination");
            System.exit(-1);
	}

	// start Sparks and read a given input file
	String inputFolder = args[0];
    String outputFolder = args[1];
        SparkConf conf = new SparkConf( ).setAppName( "Spark Sudoku Solver: Alex's Implementation" );
        JavaSparkContext jsc = new JavaSparkContext( conf );
	JavaPairRDD<String, String> files = jsc.wholeTextFiles(inputFolder);

    // define two global variables
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
		String[] lines = file._2.split("\n");

        int[][] board = new int[9][9];
        
        int crow = 0;
        for(String line : lines){
            if(crow >= 9){
                break;
            }
            String[] tokens = line.split(" ");
            if(tokens.length < 9){
                return new Tuple2<String,String>("Error", filename);
            }
            for(int i = 0; i < 9; i++){
                board[crow][i] = Integer.parseInt(tokens[i]);
            }
            crow++;
        }

        HashSet<Integer>[] rowset = new HashSet[9];
        HashSet<Integer>[] colset = new HashSet[9];
        HashSet<Integer>[] squareset = new HashSet[9];

        String boardState = "Solvable";
        for(int row = 0; row < 9; row++){
            for(int col = 0; col < 9; col++){
                int slotvalue = board[row][col];
                if(rowset[row].contains(slotvalue)){
                    boardState = "Unsolvable";
                    break;
                }
                if(colset[col].contains(slotvalue)){
                    boardState = "Unsolvable";
                    break;
                }
                int squareslot = GridHelper(row, col);
                if(squareset[squareslot].contains(slotvalue)){
                    boardState = "Unsolvable";
                    break;
                }

                rowset[row].add(slotvalue);
                colset[col].add(slotvalue);
                squareset[squareslot].add(slotvalue);

            }
            if(boardState.equals("Unsolvable")){
                break;
            }
        }
		
		// return each node's information
		return new Tuple2<>( boardState, filename );
	});

	// just for debugging
	System.err.println( "Initial Network:" );
	JavaPairRDD<String, Iterable<String>> filesgrouped = fileMatchs.groupByKey();
    filesgrouped.collect();

    
    JavaRDD<String> matchkeys = filesgrouped.keys();

    try(PrintWriter fout = new PrintWriter(outputFolder)){
        matchkeys.foreach(key -> {
        fout.println(key + ":");

        List<Iterable<String>> result = filesgrouped.lookup(key);
        if(!result.isEmpty()){
            Iterable<String> filelist = result.get(0);
            for(String file : filelist){
                fout.println(file);
            }
            fout.println();

        }
    });

    }catch(Exception e){
        System.err.println("Error Occurred Exporting Output File!");
    }


    // stop the timer
    long endTime = System.currentTimeMillis();
    System.err.println("Elapsed Time: " + (endTime - startTime));
    jsc.stop();

    }
}
