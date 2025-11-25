#include <set>
#include <vector>
#include "Sudoku.h"
#include "stdlib.h"
#include "time.h"
#include "tuple"

int numOfSlots = 30;

Sudoku::Sudoku(int erate){
    errorrate = erate;
    rowset.resize(9);
    colset.resize(9);
    gridset.resize(9);
    board.resize(9);
    errors = 0;
    for(int i  = 0; i < 9; i++){
        board[i].resize(9);
    }

    GenerateBoard();
}

int Sudoku::getSlot(int col, int row){
    if(row > 8 || row < 0 || col > 8 || col < 0){
        return -1;
    }
    return board[row][col];
}

void Sudoku::GenerateBoard(){

    set<tuple<int, int>> positions;

    //Get Positions of numbers in sudoku puzzle
    for(int i = 0; i < numOfSlots; i++){
        int rowrand = (rand() % 9);
        int colrand = (rand() % 9);
        positions.insert(make_tuple(rowrand, colrand));
    }

    for(int currrow = 0; currrow < 9;  currrow++){
        for(int currcol = 0; currcol < 9;  currcol++){
            if(positions.find(make_tuple(currrow, currcol)) != positions.end()){
                //Get a random starting posisiton for the true random sudoku experience
                int randnum = (rand() % 9);
                for(int i = randnum; i <= (randnum + 8); i++){
                    //Get the truen number from 1 - 9
                    int truenum = (i % 9) + 1;
                    if(rowset[currrow].find(truenum) != rowset[currrow].end()){
                        continue;
                    }
                    if(colset[currcol].find(truenum) != colset[currcol].end()){
                        continue;
                    }
                    int currsquare = GridHelper(currrow, currcol);
                    if(gridset[currsquare].find(truenum) != gridset[currsquare].end()){
                        continue;
                    }

                    board[currrow][currcol] = truenum;

                    rowset[currrow].insert(truenum);
                    colset[currcol].insert(truenum);
                    gridset[currsquare].insert(truenum);

                    break;
                }
            }
            else {
                board[currrow][currcol] = 0;
            }
        }
    }
    int errorroll = (rand() % 100) + 1;
    
    if(errorrate <= 0 || errorroll > errorrate){
        return;
    }

    vector<tuple<int, int>> poslist(positions.begin(), positions.end());
    int finalNumSlots = poslist.size();
    int numerrors = (rand() % 5) + 1;

    for(int i = 0; i < numerrors; i++){
        int randslot = (rand() % (finalNumSlots)); 
        tuple<int, int> pos = poslist[randslot];
        int row = get<0>(pos);
        int col = get<1>(pos);
        for(int j = 1; j <= 9; j++){
            //For errors I'm not going to use a
            if(rowset[row].find(j) != rowset[row].end()){
                board[row][col] = j;
                errors++;
                break;
            }
            if(colset[col].find(i) != colset[col].end()){
                board[row][col] = j;
                errors++;
                break;
            }
            int currsquare = GridHelper(row, col);
            if(gridset[currsquare].find(i) != gridset[currsquare].end()){
                board[row][col] = j;
                errors++;
                break;
            }
        }
    }
}

const bool Sudoku::hasErrors(){
    return errors > 0;
}

int Sudoku::GridHelper(int row, int col){
    int squarerow = (row) / 3;
    int squarecol = (col) / 3;
    return  (squarerow * 3) + squarecol;
}