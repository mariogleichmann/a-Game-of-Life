package org.agol.display

import scala.swing._
import scala.swing.event._
import javax.swing.border.LineBorder
import java.awt.Color
import akka.actor._ 
import org.agol.model.Events._
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

/**
 * A Swing-specific Display for representing the state and state changes of a single cell
 * 
 * - registers itself as a listener (interested in state changes) at the underlying cell
 * - consists of a simple Swing-Panel which reflects the state of the underlying cell (by different colors)
 * 
 * - listens to mouse clicks on the Panel in order to change the cells state 
 *   (typically only effective if the cell is in 'initialization'-mode)
 */
class CellDisplay( cell :ActorRef ) extends Actor {
  
  val COLOR_OF_DEAD = Color.GRAY
  val COLOR_OF_LIFE = Color.GREEN
  
  val panel = new FlowPanel {

    border = LineBorder.createBlackLineBorder
    
    background = COLOR_OF_DEAD
               			  	
    listenTo( mouse.clicks )
		  	
    reactions += {
		  	  
      case e: MouseClicked => {              
		  	 	  	  	    
        val oldState :CellState = askForState( cell )
        
        cell ! ( if( oldState.isAlive ) ResetDead else ResetAlive )
                
        background = if( oldState.isAlive ) COLOR_OF_DEAD else COLOR_OF_LIFE
      }
    }
  }	
	
  /**
   * Processes all incoming messages
   * (see Events for a description of the single Messages)
   */   
  def receive = {
 	  
 	  case Dead( _ )  =>  panel.background = COLOR_OF_DEAD
 	  
 	  case Alive( _ ) =>  panel.background = COLOR_OF_LIFE
 	  
 	  case GetPanel   =>  sender ! panel
  }
  
}