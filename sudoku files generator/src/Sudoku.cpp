#include <set>
#include <vector>
#include "Sudoku.h"
#include "stdlib.h"
#include "time.h"
#include "tuple"

int numOfSlots = 30;

Sudoku::Sudoku(int erate){
    //Initiate rand
    srand(time(NULL));
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
                
                for(int i = 1; i <= 9; i++){
                    if(rowset[currrow].find(i) != rowset[currrow].end()){
                        continue;
                    }
                    if(colset[currcol].find(i) != colset[currcol].end()){
                        continue;
                    }
                    int currsquare = GridHelper(currrow, currcol);
                    if(gridset[currsquare].find(i) != gridset[currsquare].end()){
                        continue;
                    }

                    board[currrow][currcol] = i;

                    rowset[currrow].insert(i);
                    colset[currcol].insert(i);
                    gridset[currsquare].insert(i);
                    errors++;
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
        int randslot = (rand() % (finalNumSlots + 1)); 
        tuple<int, int> pos = poslist[randslot];
        int row = get<0>(pos);
        int col = get<1>(pos);
        for(int j = 1; j <= 9; j++){
            if(rowset[row].find(j) != rowset[row].end()){
                board[row][col] = j;
                break;
            }
            if(colset[col].find(i) != colset[col].end()){
                board[row][col] = j;
                break;
            }
            int currsquare = GridHelper(row, col);
            if(gridset[currsquare].find(i) != gridset[currsquare].end()){
                board[row][col] = j;
                break;
            }
        }
    }
}

const bool Sudoku::hasErrors(){
    return errors > 0;
}

int Sudoku::GridHelper(int row, int col){
    int squarerow = (row - 1) / 3;
    int squarecol = (col - 1) / 3;
    return  (squarerow * 3) + squarecol;
}