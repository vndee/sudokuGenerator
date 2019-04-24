import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class SudokuGenerator {
    static String puzzleFileName;
    static String solutionFileName;
    static int numberOfHoles;

    static int[][][] latinSquare;
    static int nSquare;
    static int[][] square;
    static int[][] maskRow, maskCol;

    static int solutionMat[][];
    static int mask[][];
    static int puzzleMat[][];
    static int visited[][];

    static boolean is_ok = false;
    static int[][] puzzleList;
    static int nPuzzleList = 0;
    static int[][] finalPuzzle;

    static final int[] numberOfSquare = {0, 1, 2, 12, 576, 161280};

    /**
     * Convert 1D position to 2D coordinates in a matrix
     * @param x 1D position
     * @param n size of matrix
     * @return extracted coordinates
     */
    static int[] extractCoord(int x, int n) {
        int[] ans = new int[2];
        ans[0] = x / n;
        ans[1] = x % n;
        return ans;
    }

    /**
     * Bruteforce method for generate all latin square size n
     * @param pos current backtracking depth
     * @param n size of latin square
     */
    static void backtrack(int pos, int n) {
        int[] coord = extractCoord(pos, n);
        int x = coord[0], y = coord[1];

        for (int candidate = 0; candidate < n; candidate++) {
            if (maskRow[x][candidate] == 0 && maskCol[y][candidate] == 0) {
                square[x][y] = candidate;

                if (pos == n * n - 1) {
                    for (int i = 0; i < n; i++)
                        for (int j = 0; j < n; j++)
                            latinSquare[nSquare][i][j] = square[i][j];
                    nSquare++;
                } else {
                    maskRow[x][candidate] = 1;
                    maskCol[y][candidate] = 1;
                    backtrack(pos + 1, n);
                    maskRow[x][candidate] = 0;
                    maskCol[y][candidate] = 0;
                }
            }
        }
    }

    /**
     * Latin square builder
     * @param n size of latin square
     */
    static void latinSquareBuilder(int n) {
        latinSquare = new int[numberOfSquare[n]][n][n];
        maskRow = new int[n][n];
        maskCol = new int[n][n];
        square = new int[n][n];

        backtrack(0, n);
    }

    /**
     * Check whether a block is valid or not
     * @param block sudoku block
     * @param n size of sudoku block
     * @param startX start x
     * @param startY start y
     * @return boolean
     */
    static boolean isValidBlock(int[][] block, int n, int startX, int startY) {
        boolean[] vis = new boolean[n*n + 1];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                if (vis[block[startX + i][startY + j]] == true) {
                    return false;
                } else {
                    vis[block[startX + i][startY + j]] = true;
                }
            }

        return true;
    }

    /**
     * check whether a sudoku solution is valid or not
     * @param solution sudoku solution
     * @param n size of sudoku square
     * @return boolean
     */
    static boolean isValidSolution(int[][] solution, int n) {
        int[][] row = new int[n][n + 1];
        int[][] col = new int[n][n + 1];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                int mask = solution[i][j];
                if (row[i][mask] == 1 || col[j][mask] == 1)
                    return false;

                row[i][mask] = 1;
                col[j][mask] = 1;
            }


        int m = (int) Math.sqrt(n);
        for (int i = 0; i < n; i += m)
            for (int j = 0; j < n; j += m) {
                if (isValidBlock(solution, m, i, j) == false)
                    return false;
            }

        return true;
    }

    /**
     * Put latin square into each solution block
     * @param arr latin square
     * @param n size of solution block
     * @param startX start x
     * @param startY start y
     */
    static void put(int[][] arr, int n, int startX, int startY) {
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                solutionMat[startX + i][startY + j] = arr[i][j];
    }

    /**
     * Swap 2 row of matrix
     * @param source source row
     * @param target target row
     * @param n size of matrix
     */
    static void swapRow(int source, int target, int n) {
        int[] tempArr = new int[n];

        for (int i = 0; i < n; i++)
            tempArr[i] = solutionMat[source][i];

        for (int i = 0; i < n; i++)
            solutionMat[source][i] = solutionMat[target][i];

        for (int i = 0; i < n; i++)
            solutionMat[target][i] = tempArr[i];
    }

    /**
     * Swap 2 column of a matrix
     * @param source source column
     * @param target target column
     * @param n size of matrix
     */
    static void swapCol(int source, int target, int n) {
        int[] tempArr = new int[n];

        for (int i = 0; i < n; i++)
            tempArr[i] = solutionMat[i][source];

        for (int i = 0; i < n; i++)
            solutionMat[i][source] = solutionMat[i][target];

        for (int i = 0; i < n; i++)
            solutionMat[i][target] = tempArr[i];
    }

    /**
     * Sudoku solver - brute force method
     * @param pos current backtracking depth
     * @param n size of sudoku square
     * @return number of solution
     */
    static int sudokuSolver(int pos, int n) {
        int x = puzzleList[pos][0];
        int y = puzzleList[pos][1];
        int ans = 0;

        for (int candidate = 1; candidate <= n; candidate++) {
            if (maskRow[x][candidate] == 0 && maskCol[y][candidate] == 0) {
                puzzleMat[x][y] = candidate;

                if (pos == nPuzzleList - 1) {
                    if (isValidSolution(puzzleMat, n)) {
                        ans += 1;
                    }
                } else {
                    maskRow[x][candidate] = 1;
                    maskCol[y][candidate] = 1;

                    ans += sudokuSolver(pos + 1, n);

                    maskRow[x][candidate] = 0;
                    maskCol[y][candidate] = 0;
                }
            }
        }

        return ans;
    }

    /**
     * Check whether a solution is unique or not
     * @param n size of sudoku square
     * @return boolean
     */
    static boolean isUniqueSolution(int n) {
        return sudokuSolver(0, n) == 1;
    }

    /**
     * Try and error method for generating puzzle lead to unique solution
     * @param diggedHoles number of Holes was digged
     * @param n size of puzzle square
     */
    static void tryAndError(int diggedHoles, int n) {
        if (is_ok == true) return;
        int temp = 0;

        for (int x = 0; x < n; x++)
            for (int y = 0; y < n; y++) {
                if (visited[x][y] == 0) {
                    temp = puzzleMat[x][y];
                    puzzleMat[x][y] = 0;

                    puzzleList[nPuzzleList][0] = x;
                    puzzleList[nPuzzleList][1] = y;
                    nPuzzleList++;

                    maskRow[x][solutionMat[x][y]] = 0;
                    maskCol[y][solutionMat[x][y]] = 0;

                    if (diggedHoles + 1 == numberOfHoles) {
                        if (isUniqueSolution(n)) {
                            is_ok = true;
                            for (int i = 0; i < n; i++)
                                for (int j = 0; j < n; j++)
                                    finalPuzzle[i][j] = puzzleMat[i][j];
                            for (int i = 0; i < nPuzzleList; i++)
                                finalPuzzle[puzzleList[i][0]][puzzleList[i][1]] = 0;
                            return;
                        }
                    } else {
                        visited[x][y] = 1;
                        tryAndError(diggedHoles + 1, n);
                        visited[x][y] = 0;
                    }

                    nPuzzleList--;
                    maskRow[x][solutionMat[x][y]] = 1;
                    maskCol[y][solutionMat[x][y]] = 1;
                    puzzleMat[x][y] = temp;
                }
            }
    }

    /**
     * Random method for generating puzzle. This one much faster than
     * brute-force method
     * @param n size of puzzle square
     */
    static void randomizeGenerator(int n) {
        int sz = n * n;
        int[] crd = new int[sz];

        for (int i = 0; i < sz; i++)
            crd[i] = i;

        Random rand = new Random();
        Vector<Integer> vec = new Vector<Integer>();

        int nH = numberOfHoles;
        while (nH > 0) {
            int x = rand.nextInt(sz);
            vec.add(crd[x]);

            // Delete x from coordinate list
            sz--;
            for (int i = x; i < sz; i++)
                crd[i] = crd[i + 1];
            nH--;
        }

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                puzzleMat[i][j] = solutionMat[i][j];

        for (int i = 0; i < vec.size(); i++) {
            int[] coord = extractCoord(vec.get(i), n);
            int x = coord[0], y = coord[1];
            puzzleMat[x][y] = 0;
        }
    }

    /**
     * Digging hole
     * @param n size of puzzle
     */
    static void diggingHole(int n) {
        visited = new int[n][n];
        puzzleMat = new int[n][n];
        finalPuzzle = new int[n][n];
        maskRow = new int[n][n + 1];
        maskCol = new int[n][n + 1];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                puzzleMat[i][j] = solutionMat[i][j];
                maskRow[i][j] = 1;
                maskCol[i][j] = 1;
            }

        puzzleList = new int[n*n][2];
        nPuzzleList = 0;

        randomizeGenerator(n);
    }

    /**
     * Sudoku generator
     * @param targetSize size of puzzle
     */
    static void sudokuGenerator(int targetSize) {
        int n = (int) Math.sqrt(targetSize);
        int[][][] base10Converted = new int[numberOfSquare[n]][n][n];

        latinSquareBuilder(n);

        for (int i = 1; i <= targetSize; i++) {
            int[] coord = extractCoord(i - 1, n);
            int mask = latinSquare[0][coord[0]][coord[1]];

            for (int x = 0; x < n; x++) {
                for (int y = 0; y < n; y++) {
                    base10Converted[i - 1][x][y] = mask * n + latinSquare[i][x][y] + 1;
                }
            }
        }

        solutionMat = new int[targetSize][targetSize];

        int index = 0;
        for (int i = 0; i < targetSize; i += n)
            for (int j = 0; j < targetSize; j += n) {
                put(base10Converted[index++], n, i, j);
            }

        swapRow(1, 3, targetSize);
        swapRow(2, 6, targetSize);
        swapRow(5, 7, targetSize);

        // printArr(solutionMat, targetSize, targetSize);
        // System.err.println("Valid solution = " + isValidSolution(solutionMat, targetSize));

        diggingHole(targetSize);
        // System.err.println("------------------------------------------------\nPuzzle:\n");
        // printArr(puzzleMat, targetSize, targetSize);
    }

    /**
     * Print array - for DEBUG purpose
     * @param arr input array
     * @param n number of rows
     * @param m number of cols
     */
    static void printArr(int[][] arr, int n, int m) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                System.err.print(arr[i][j] + " ");
            }
            System.err.println();
        }
        System.err.println();
    }

    /**
     * Main process
     * @param args global arguments
     */
    public static void main(String[] args) {
        try {
            puzzleFileName = args[0];
            solutionFileName = args[1];
            numberOfHoles = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.out.println("Missing agrument!");
            return;
        }

        sudokuGenerator(9);

        try {
            BufferedWriter bwSol = new BufferedWriter(new FileWriter(solutionFileName));

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    bwSol.write(solutionMat[i][j] + " ");
                }
                bwSol.write("\n");
            }

            bwSol.close();

            BufferedWriter bwPuz = new BufferedWriter(new FileWriter(puzzleFileName));

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    bwPuz.write(puzzleMat[i][j] + " ");
                }
                bwPuz.write("\n");
            }

            bwPuz.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
