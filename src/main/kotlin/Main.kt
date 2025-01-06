package org.example

import java.util.*
import kotlin.random.Random

data class Dim2(val x: Int, val y: Int)
enum class CellState {
    UNEXPLORED, EXPLORED, MARKED
}
class Cell(val mine: Boolean, val neighbors: Int, var cellState: CellState) {
    fun getNotation(reveal: Boolean = false) = when(cellState) {
            CellState.UNEXPLORED -> if (mine && reveal) "X" else "."
            CellState.MARKED -> if (mine && reveal) "X" else "*"
            CellState.EXPLORED -> if (neighbors == 0) "/" else neighbors.toString()
        }
}

val neighborIncrs = listOf(Dim2(-1, -1), Dim2(-1, 0), Dim2(-1, 1),
    Dim2(0, -1), Dim2(0, 1),
    Dim2(1, -1), Dim2(1, 0), Dim2(1, 1))

fun getNeighbors(size: Dim2, loc: Dim2): List<Dim2> =
    neighborIncrs.map { (xincr, yincr) ->
        Dim2(loc.x + xincr, loc.y + yincr)
    } .filter { (i,j) ->
        i >= 0 && i < size.x && j >= 0 && j < size.y
    }
fun countNeighbors(mineLocations: List<Dim2>, size: Dim2, loc: Dim2):Int {
    if (mineLocations.contains(loc)) return 0
    return getNeighbors(size, loc).count { mineLocations.contains(it) }
}

fun setup(size: Dim2, mineLocations: List<Dim2>) = Array(size.x) { row ->
        Array(size.y) { col -> Cell(
            mineLocations.contains(Dim2(row, col)),
            countNeighbors(mineLocations, size, Dim2(row, col)),
            CellState.UNEXPLORED)
        }
    }

fun generateMineLocations(size: Dim2, numMines: Int) = List(numMines) {
    val i = Random.nextInt(0, size.x)
    val j = Random.nextInt(0, size.y)
    Dim2(i, j)
}

fun display(board: Array<Array<Cell>>, reveal:Boolean = false) {
    println(" |" + (1..9).joinToString(separator = " ") + " |")
    println("-|" + "--".repeat(9) + "|")
    for (i in board.indices) {
        println("${i+1}|" + board[i].joinToString(" ") { it.getNotation(reveal)} + " |")
    }
    println("-|" + "--".repeat(9) + "|")
    println(" |" + "  ".repeat(9) + "|")
}

fun hasWon(board: Array<Array<Cell>>):Boolean {
    val size = Dim2(board.size, board[0].size)
    val numMines = board.sumOf { row -> row.count { it.mine } }
    val explored = board.sumOf { row -> row.count { it.cellState == CellState.EXPLORED } }
    // If all cells except mines are explored, then player has won
    if (numMines == size.x * size.y - explored) return true

    // If all mines are marked correctly then player has won
    val markedIsMine = board.sumOf { row -> row.count { it.mine && it.cellState == CellState.MARKED } }
    val markedIsNotMine = board.sumOf { row -> row.count { !it.mine && it.cellState == CellState.MARKED } }
    return numMines == markedIsMine && markedIsNotMine == 0
}

fun markCell(board: Array<Array<Cell>>, loc: Dim2) =
    if (board[loc.x][loc.y].cellState == CellState.EXPLORED) {
        println("Cell is already explored")
        false
    } else {
        board[loc.x][loc.y].cellState = if (board[loc.x][loc.y].cellState == CellState.MARKED)
            CellState.UNEXPLORED else CellState.MARKED
        true
    }

fun exploreCell(board: Array<Array<Cell>>, loc: Dim2): Boolean =
    if (board[loc.x][loc.y].cellState == CellState.EXPLORED) {
        false
    } else {
        board[loc.x][loc.y].cellState = CellState.EXPLORED
        if (board[loc.x][loc.y].neighbors == 0) {
            val size = Dim2(board.size, board[0].size)
            getNeighbors(size, loc).forEach { exploreCell(board, it) }
        }
        true
    }
fun main() {
    val scanner = Scanner(System.`in`)
    val size = Dim2(9, 9)

    println("How many mines do you want on the field?")
    val numMines = scanner.nextInt()
    val mineLocations = generateMineLocations(size, numMines)

    val board = setup(size, mineLocations)
    var endGame:Boolean
    do {
        display(board)
        var didToggle: Boolean
        var lost = false
        do {
            println("Set/unset mines marks or claim a cell as free:")
            val x = scanner.nextInt() - 1
            val y = scanner.nextInt() - 1
            val loc = Dim2(x,y)

            val command = scanner.next()
            didToggle = when(command) {
                "mine" -> markCell(board, loc)
                "free" -> {
                    if (board[loc.x][loc.y].mine) {
                        lost = true // User stepped on a mine
                        break
                    }
                    exploreCell(board, loc)
                }
                else -> {
                    println("Invalid Command $command");
                    false
                }
            }
        } while(!didToggle)
        endGame = lost || hasWon(board)
    } while(!endGame)
    display(board, true)
    if (hasWon(board)) {
        println("Congratulations! You found all the mines!")
    } else {
        println("You stepped on a mine and failed!")
    }
}