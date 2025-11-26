#ifndef _SUDOKU_H
#define _SUDOKU_H
#include <set>
#include <vector>

using namespace std;
class Sudoku {
        public:
            //functions
            Sudoku(int errorate);
            int getSlot(int col, int row);
            const bool hasErrors();


        private:
            int errors;
            int errorrate;
            //sudoku checklists
            vector<set<int>> rowset;
            vector<set<int>> colset;
            vector<set<int>> gridset;
            //THE BOARD
            vector<vector<int>> board;
            //Private functions
            void GenerateBoard();
            int GridHelper(int row, int col);

};

#endif