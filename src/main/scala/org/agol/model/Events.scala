package org.agol.model

import akka.actor._
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Await
import akka.pattern.ask

/**
 * Contains all Events for Message-Exchanges between different Actors
 * 
 * Contains some convenience functions for sending specific Messages to an Actor and waiting for its reply
 */
object Events {

  sealed case class Event

  /** 
   * Runs the game (after init phase - puts cells into 'running'-Mode)
   */
  case object Run extends Event

  /**
   * Pauses the game (after game has started to run - puts cells into 'initializing'-mode)
   */
  case object Pause extends Event

  /**
   * Shuts managed Actors down (aka stops Actors via its supervisor)
   */
  case object ShutDown extends Event
  
  /**
   * Initially resets a cell to be dead
   */
  case object ResetDead extends Event
  
  /**
   * Initially resets a cell to be alive
   */
  case object ResetAlive extends Event
  
  /**
   * Initially resets a cell with its neighbor cells (on which the cell's state depends on)
   */
  case class ResetNeighbors( neighborCells :List[ActorRef] ) extends Event
  
  /**
   * Adds a listener to a cell (for getting informed by state changes of a cell)
   */
  case class AddListener( listener :ActorRef )
  
  /**
   * Asks a cell for its current neighbors which are alive
   */
  case class NeighborsAlive( count :Int ) extends Event
  
  /**
   * Asks a cell for its current state
   */
  case object GetState extends Event
  
  /**
   * Asks a cell for its position within a cell matrix
   */
  case object GetPosition extends Event
  
  /**
   * Answer to a request for a cells position
   */
  case class Position( row :Int, column :Int )
  
  
  /**
   * Common base class for cell state
   */
  abstract trait CellState extends Event{
    def isAlive : Boolean
  }
  
  /**
   * Informs a cell of the death of one of its neighbors
   */
  case class Dead( cell :Cell ) extends CellState{
    override def isAlive = false
  }
  
  /**
   * Informs a cell of the (re)birth of one of its neighbors
   */
  case class Alive( cell :Cell ) extends CellState{
    override def isAlive = true
  }
   
  /**
   * Asks a CellDisplay about its (Swing) panel (should go into a separate Event object for collecting the 'interface' of Actor 'CellDisplay'
   */
  case object GetPanel extends Event
  
   /**
   * Asks a CellDisplay about its (Swing) panel (should go into a separate Event object for collecting the 'interface' of Actor 'CellDisplay'
   */ 
  case object GetCells extends Event
  
  // some convenience-Methods for asking an Actor and waiting for its reply

  implicit val timeout = Timeout( 5.seconds )
  
  def askForCells[T]( actor :ActorRef )( implicit m :Manifest[T] ) = askFor( GetCells, actor )
	
  def askForPanel[T]( actor :ActorRef )( implicit m :Manifest[T] ) = askFor( GetPanel, actor )
  
  def askForState[T]( actor :ActorRef )( implicit m :Manifest[T] ) =  askFor( GetState, actor ) 
  
  def askForPosition[T]( actor :ActorRef )( implicit m :Manifest[T] ) =  askFor( GetPosition, actor )
  
  def askFor[T]( event :Event, actor :ActorRef )( implicit m :Manifest[T] ) = Await.result( ask( actor, event ).mapTo[T], timeout.duration )
}