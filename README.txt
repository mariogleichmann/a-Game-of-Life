Asynchronous Game of Life - a sample for the Typesafe Stack (http://typesafe.com/stack).

-------------------------------------------------------
Description
-------------------------------------------------------
Asynchronous Game of Life is an implementation of Conway's famous Game of Life. 
In contrary to its synchronous counterpart, where the state of each cell is calculated in discrete steps (the state of all cells within Generation n is a discrete function over the state of all cells in Generation n-1), this implemention uses Akka to simulate a more realistic behaviour by allowing each cell to act asynchronously.

-------------------------------------------------------
Technical Setup
-------------------------------------------------------
This is an Akka 2.0 sample project using Scala and SBT.

The provided View / Display is based on Swing, but could be easily exchanged because of a strict decoupling of model (the 'Matrix', consisting of cells) and View (the Matrix- and Cell-Displays).

To run and test it use SBT invoke: 'sbt run'

-------------------------------------------------------
License
-------------------------------------------------------
Apache 2.0 (please see also the Typesafe CLA at http://typesafe.com/contribute/current-cla)

-------------------------------------------------------
Architecture
-------------------------------------------------------
The implementation follows the classic MVC pattern:

(A) The Modell consists of ...

(1) the so called Cell-Matrix, which acts as an aggregate for all cells. 
It represents a Matrix of <row> x <column> single cells  (each acting as an independent Actor) and acts as a supervising Actor for all those single cell Actors
The following structure holds:
- A Matrix is split into <row> rows (so called CellRows), while each row contains <column> cells
- Each cell is interconnected with its neighbor cells

(2) the single cells (typically located within a Matrix of cells)
- each cell features a unique position (given by a <row>- and a <column>-Number within a Matrix)
- each cell has a state - that is either 'Dead' or 'Alive'
- each cell is typically 'connected' with its neighbor cells (for which the cell is interested in their state changes)
- each cell will accept a number of listeners, which are also interested in the cells state changes
 
A cell can act in two different modes:
- Within the 'initializing'-mode, the cells context and state gets initialized without changing its state upon it 
 (so without actively re-firing state changes based on reported state changes of its neighbor cells)
- Within the 'running'-mode, the cell will react to state changes, typically reported from its neighbor cells 
  and will fire own state changes upon it (while capturing the rules on which circumstances the cell is going 
  to die or gets reborn again, based on the current state of its neighbor cells)
 
A cell can switch between its two modes by sending an appropriate Message:
- Run   : initializing -> running
- Pause : running -> initializing

(B) The View consists of ...

(1) The Matrix-Display - a Swing-specific Display for representing the state and state changes for a bunch of single cells, typically for a given Matrix of a certain dimension
The following structure holds:
- features a Swing-Panel which consists of a number of CellDisplays for each given cell
- each CellDisplay reflects the current state of a cell, independently of all other CellDisplays

Acts as a supervising Actor for all those single CellDisplay Actors

(2) a bunch of Cell-Displays - a Swing-specific Display for representing the state and state changes of a single cell
- registers itself as a listener (interested in state changes) at the underlying cell
- consists of a simple Swing-Panel which reflects the state of the underlying cell (by different colors)
- listens to mouse clicks on the Panel in order to change the cells state 
  (typically only effective if the cell is in 'initialization'-mode)
  
(C) The Controller represents a Swing-specific entry point into the game (aka 'the Main Controller')

- creates a specific Matrix for a bunch of cells (which dimension is defined by ROWS and COLUMNS)
- creates an appropriate Swing-based Display, displaying the state changes for the cells of the given Matrix
 
Provides a Swing-based Frame for the game 
- in which the MatrixDisplay is embedded
- which provides controls for switching between modes (initializing the cells state, running the Matrix) and finally ending the game