package org.agol.model

import akka.actor._
import Events._
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Future

/**
 * Represents a single cell (typically located within a Matrix of cells)
 * 
 * - each cell features a unique position (given by a <row>- and a <column>-Number within a Matrix)
 * - each cell has a state - that is either 'Dead' or 'Alive'
 * - each cell is typically 'connected' with its neighbor cells (for which the cell is interested in their state changes)
 * - each cell will accept a number of listeners, which are also interested in the cells state changes
 * 
 * A cell can act in two different modes:
 * 
 * - Within the 'initializing'-mode, the cells context and state gets initialized without changing its state upon it 
 *   (so without actively re-firing state changes based on reported state changes of its neighbor cells)
 *   
 * - Within the 'running'-mode, the cell will react to state changes, typically reported from its neighbor cells 
 *   and will fire own state changes upon it (while capturing the rules on which circumstances the cell is going 
 *   to die or gets reborn again, based on the current state of its neighbor cells)
 *   
 * A cell can switch between its two modes by sending an appropriate Message:
 * 
 * - Run   : initializing -> running
 * - Pause : running -> initializing
 */
class Cell( val row :Int, val column :Int ) extends Actor {
	
  import context._
  
  var state :CellState = Dead( this )
  
  var neighborCells :List[ActorRef] = Nil
  
  var neighborCellsAlive = 0;
  
  var listeners :List[ActorRef] = Nil

  /**
   * Processes all incoming messages within 'initializing'-mode
   * (see Events for a description of the single Messages)
   */
  def initializing :Receive = { 
	  
    case ResetDead => { 
      state = Dead( this )
      fireNewState
    }
	  
    case ResetAlive => {
      state = Alive( this )
      fireNewState
    }
	  
    case ResetNeighbors( cells ) => {
      neighborCells = cells
    }
    
    case Dead( cell )	=> {
      if( neighborCellsAlive > 0 ) neighborCellsAlive -= 1
    }
	   
    case Alive( cell ) => { 
      neighborCellsAlive += 1
    }
	     
    case AddListener( listener ) => {
      listeners = listener :: listeners
    }
	     
    case GetPosition => {
      sender ! Position( row, column )
    }
	   					   
    case GetState => {
      sender ! state
    }
	  
    case Run => {
      if( newState ) fireNewState 
      become( running ) 
    }	     
  }
  
  /**
   * Processes all incoming messages within 'running'-mode
   * (see Events for a description of the single Messages)
   */
  def running :Receive = {

    case Dead( cell )	=> {
      if( neighborCellsAlive > 0 ) neighborCellsAlive -= 1
      if( newState ) fireNewState 
    }
	   
    case Alive( cell ) => { 
      neighborCellsAlive += 1
      if( newState ) fireNewState 
    }
	   
    case Pause => {
      become( initializing )
    }
  }
  
  /**
   * Initialy receive messages in 'initializing'-mode
   */
  def receive = initializing
  
  /**
   * Shows if the cell has changed its state (based on a new calculation upon the last reported state of all neighbor cells)
   */
  def newState() : Boolean = {
	  
	  val oldState = state

	  state = if( neighborCellsAlive >= 3 && neighborCellsAlive <= 5 ) Alive( this ) else Dead( this )

	  println( "state of cell " + row + ", " + column + " : " + oldState + " -> " + state )

	  !( oldState.isAlive == state.isAlive )
  }
  
  /**
   * Fires a state change to all neighbor cells and all registered listeners
   */
  def fireNewState {
	   
	  listeners.foreach( _ ! state )
	  neighborCells.foreach( _ ! state )
  }
  
}