/* Simple sudoku generator
    How to use:
    main/main.exe arg 1: path of directory to output puzzles - example:{ puzzles/ }, arg 2: number of puzzles - example:{ 1000 }, arg 3: percentage of incorrect puzzles (0 - 100) - example:{ 10 }, 
    arg 4:debug mode if incorrect files should be labled or not true to turn on debug mode and false to turn it off - example: { true }.

    
    example execution (windows): ./main.exe puzzles/ 100 10 true
*/
#include <fstream>
#include <iostream>
#include <vector>
#include <set>
#include "Sudoku.h"
#include <string>
#include "stdlib.h"

using namespace std;

void puzzleOut(Sudoku& puz, int num, bool debugmode, string outpath){
    string debugstring = "";

    if(puz.hasErrors() && debugmode){
        debugstring = "_ERROR";
    }

    string filename = "puzzle_" + to_string(num) + debugstring + ".txt";

    ofstream out(outpath + filename);

    for(int row = 0; row < 9; row++){
        string line = "";
        for(int col = 0; col < 9; col++){
            if(col < 8){
                line += to_string(puz.getSlot(col, row)) + " ";
            }
            else{
                line += to_string(puz.getSlot(col, row)) + "\n";
            }

        }
        out << line;
    }


}

int main(int argc, char* args[]){
    //initiate rand
    srand(time(0));
    //srand(100);
    //Set up execution variables

    if (argc != 5){
        cout << "usage: program output_path number_of_puzzles error_rate debug_toggle" << endl;
        return -1;
    }

    int numPuzzles = atoi(args[2]);
    string outpath = args[1];
    int errorPercent = atoi(args[3]);
    if( errorPercent < 0 || errorPercent > 100){
        cout << "error_rate must be within 0 - 100" << endl;
        return -1;
    }
    string debugarg = args[4];
    bool debugmode = false;
    if(debugarg == "true"){
        debugmode = true;
    }
    else if(debugarg == "false"){
        debugmode = false;
    }
    else{
        cout << "usage: program output_path number_of_puzzles error_rate debug_toggle" << endl;
        return -1;
    }


    //Debug parameters
    /*
    string outpath = "puzzles/";
    int numPuzzles = 100;
    int errorPercent = 100;
    bool debugmode = true;
    */
    for(int puzzle = 0; puzzle < numPuzzles; puzzle++){
        //This heap allocation is completly unneccessary as it goes out of scope at each loop cycle, but I don't care.
        Sudoku* currsudoku = new Sudoku(errorPercent);

        puzzleOut(*currsudoku, puzzle, debugmode, outpath);

        delete currsudoku;
    }


}