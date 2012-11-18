package org.agol.model

import akka.actor._ 
import akka.actor.Actor._
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.actor.Props
import Events._

/**
 * Represents a Matrix of <row> x <column> single cells  (each acting as an independent Actor)
 * and acts as a supervising Actor for all those single cell Actors
 * 
 * The following structure holds:
 * 
 * - A Matrix is split into <row> rows (so called CellRows), while each row contains <column> cells
 * - Each cell is interconnected with its neighbor cells
 */
class CellMatrix( rows :Int, columns :Int ) extends Actor with Traversable[ActorRef] {
   
    var cellRows :List[CellRow] = null
    
    lazy val cells :List[ActorRef] = cellRows.foldRight( Nil :List[ActorRef] )( ( cellrow, acc ) => cellrow.cells ::: acc )
    
    override def preStart() = {
      cellRows = makeCellRows( rows, columns )
      interconnect( this )
    }
    
    /**
     * Processes all incoming messages (- delegates them to the underlying cells for most of them)
     * (see Events for a description of the single Messages)
     */
    def receive :Receive = {	  
	  
	  case Run      =>  foreach( cell => cell ! Run )
	    
	  case Pause    =>  foreach( cell => cell ! Pause )
	  
	  case ShutDown =>  foreach( cell => context.stop( cell ) )
	   
	  case GetCells =>  sender ! cells	  
    }
    
    /**
     * Delivers the nth CellRow within the Matrix 
     */
	def apply( n: Int ) = cellRows( n )
	
	/**
	 * Applies the given Function f to every cell within the Matrix (see Traversable)
	 */
	override def foreach[U]( f: ActorRef => U ) {		
		cellRows.foreach( row => row.foreach( cell => f( cell ) ) )
	}	
	
	/**
	 * Factory-Method
	 * Creates <rows> CellRows, each containing <columns> cells
	 */
	def makeCellRows( rows :Int, columns :Int ) : List[CellRow] = {		
		( for( rowNum <- 0 to rows - 1 ) yield CellRow( rowNum, columns ) ) toList
	}	
	
	/**
	 * Interconnects each cell within the given Matrix with all its neighbor cells
	 */
	def interconnect( cellMatrix :CellMatrix ){		
		cellMatrix.foreach( cell => connect( cell, cellMatrix ) )
	}
		
	/**
	 * Connects the given cell with all its neighbor cells within the given Matrix
	 */
	def connect( cell :ActorRef, cellMatrix :CellMatrix ){				
		cell ! ResetNeighbors( neighborsOf( askFor[Position]( GetPosition, cell ), cellMatrix ) )
	}
	
	/**
	 * Gives all Neighbor cells for the given Position within the given Matrix
	 */
	def neighborsOf( p :Position, cellMatrix :CellMatrix ) = 
	  west( p, cellMatrix ) :: east( p, cellMatrix ) ::  north( p, cellMatrix ) :: south( p, cellMatrix ) ::
	  northWest( p, cellMatrix ) :: northEast( p, cellMatrix ) :: southWest( p, cellMatrix ) :: southEast( p, cellMatrix ) :: Nil
	  	
	def west( p :Position, cellMatrix :CellMatrix ) = cellMatrix( p.row ) ( ( p.column + columns - 1 ) % columns )
	def east( p :Position, cellMatrix :CellMatrix ) = cellMatrix( p.row ) ( ( p.column + 1) % columns )
	def northWest( p :Position, cellMatrix :CellMatrix ) = cellMatrix(  ( p.row + rows - 1) % rows ) ( ( p.column + columns - 1) % columns )
	def north( p :Position, cellMatrix :CellMatrix ) = cellMatrix( ( p.row + rows - 1) % rows ) ( p.column )
	def northEast( p :Position, cellMatrix :CellMatrix ) = cellMatrix( ( p.row + rows - 1) % rows ) ( ( p.column + 1 ) % columns )
	def southWest( p :Position, cellMatrix :CellMatrix ) = cellMatrix( ( p.row + 1) % rows ) ( ( p.column + columns - 1 ) % columns )
	def south( p :Position, cellMatrix :CellMatrix ) = cellMatrix( ( p.row + 1) % rows ) ( p.column )
	def southEast( p :Position, cellMatrix :CellMatrix ) = cellMatrix( ( p.row + 1) % rows ) ( ( p.column + 1) % columns )	
}

/**
 * Represents a single row within a Matrix
 * 
 * Consists of a List of cells (each acting as an independent Actor)
 */
class CellRow( val cells : List[ActorRef] ) extends Traversable[ActorRef]{
	
	/**
	 * Delivers the nth cell within the row
	 */
	def apply( n :Int ) = cells( n )
	
	/**
	 * Applies the given Function f to each cell within the row (see Traversable)
	 */
	override def foreach[U]( f: ActorRef => U ) {		
		cells.foreach( cell => f( cell ) )
	}
}

/**
 * CellRow Compagnion - acts as a Factory for creating all rows of a Matrix
 */
object CellRow{
	
  /**
   * Creates <row> CellRows, each containing <columns> cells (acting as independent Actors)
   */
  def apply( row :Int, columns :Int )( implicit ctx :ActorContext ) : CellRow = {	
    new CellRow( ( for ( column <- 0 to columns - 1 ) yield ctx.actorOf( Props( new Cell( row, column ) ) ) ) toList )		
  }
}