package org.agol.display

import akka.actor._ 
import akka.actor.Actor._
import scala.swing._
import org.agol.model.Events._
import org.agol.model.CellMatrix
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

/**
 * A Swing-specific Display for representing the state and state changes for a bunch of single cells, 
 * typically for a given Matrix of a certain dimension
 * 
 * The following structure holds:
 * 
 * - features a Swing-Panel which consists of a number of CellDisplays for each given cell
 * - each CellDisplay reflects the current state of a cell, independently of all other CellDisplays
 * 
 * Acts as a supervising Actor for all those single CellDisplay Actors
 */
class MatrixDisplay( cells :List[ActorRef], rows :Int, columns :Int ) extends Actor {     
  
    val cellDisplays : Traversable[ActorRef] = cells.map( cell => displayFor( cell ) )
    
	lazy val panel = new GridPanel( rows, columns ) { 
      
      cellDisplays.foreach( cellDisplay => contents += askForPanel( cellDisplay ) )
	}
	
    /**
     * Processes all incoming messages
     * (see Events for a description of the single Messages)
     */    	
	def receive :Receive = {	  
	  
	  case ShutDown => cellDisplays.foreach( display => context.stop( display ) )
	  
	  case GetPanel => sender ! panel	    
    }	
    

	/**
	 * Factory-Method
	 * Creates a CellDisplay for a given cell
	 */
	def displayFor( cell :ActorRef ) : ActorRef = {
		
	  val display = context.actorOf( Props( new CellDisplay( cell ) ) )
		
	  cell ! AddListener( display )
		
	  display
	}
	
}
