#ifndef _SUDOKU_H
#define _SUDOKU_H
#include <set>
#include <vector>

using namespace std;
class Sudoku {
        public:
            Sudoku(int errorate);
            int getSlot(int col, int row);
            const bool hasErrors();


        private:
            int errors;
            int errorrate;

            vector<set<int>> rowset;
            vector<set<int>> colset;
            vector<set<int>> gridset;
            vector<vector<int>> board;

            void GenerateBoard();
            int GridHelper(int row, int col);

};

#endif