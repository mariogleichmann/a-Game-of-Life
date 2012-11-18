package org.agol

import scala.swing._
import org.agol.model.CellMatrix
import org.agol.display.MatrixDisplay
import akka.actor._ 
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import org.agol.model.Events._

/**
 * Represents a Swing-specific entry point into the game (aka 'the Main Controller')
 * 
 * - creates a specific Matrix for a bunch of cells (which dimension is defined by ROWS and COLUMNS)
 * - creates an appropriate Swing-based Display, displaying the state changes for the cells of the given Matrix
 * 
 * Provides a Swing-based Frame for the game 
 * 
 * - in which the MatrixDisplay is embedded
 * - which provides controls for switching between modes (initializing the cells state, running the Matrix) 
 *   and finally ending the game
 */
object Game extends SimpleSwingApplication{
 
  val ROWS = 60
  val COLUMNS = 60
	
  val system = ActorSystem("GameOfLife")
	
  var cellMatrix :ActorRef = null	  
  var matrixDisplay :ActorRef = null
  
  /**
   * Starting the game
   */
  override def startup( args: Array[String] ) {
  
    cellMatrix = system.actorOf( Props( new CellMatrix( ROWS, COLUMNS ) ), name = "cellMatrix")
	matrixDisplay = system.actorOf( Props( new MatrixDisplay( askForCells( cellMatrix ), ROWS, COLUMNS ) ), name = "matrixDisplay")
	  
    super.startup( args )
  }

  /**
   * Defining the games visual Frame and its controls
   */
  override def top = new MainFrame {

    title = "Game of Life"
	    
    resizable = true
	
    contents = new BoxPanel( Orientation.Vertical ){
	  
      contents += askForPanel( matrixDisplay )
      contents += Button( "Run" ){ cellMatrix ! Run }
      contents += Button( "Pause" ){ cellMatrix ! Pause }
    }
  }   

  /**
   * Callback, on ending the game
   */
  override def shutdown() {
	  
    matrixDisplay ! ShutDown
    cellMatrix ! ShutDown
    
  }		
}