

package edu.columbia.cs.sdarts.util;

import java.util.LinkedList;

/**
 * An unsynchronized version of <code>java.util.Stack</code> that performs
 * better because it is unsynchronized. It is a subclass of
 * <code>java.util.LinkedList</code> and of course can be cast as a
 * <code>java.util.List</code>. It also is more forgiving of peeking and
 * popping an empty stack (returning <code>null</code> instead of throwing
 * an exception), and has a new peek that accepts distance from the top
 * of the stack.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class UnsynchStack extends LinkedList {
  /**
   * Create an empty stack
   */
   public UnsynchStack () {
     super ();
   }

   /**
    * @return whether the stack is empty
    */
   public boolean empty () {
     return isEmpty();
   }

   /**
    * Looks at the object at the top of this stack without removing it from the stack.
    * @return the object at the top of this stack
    * @exception EmptyStackException if this stack is empty.
    */
   public Object peek () {
     return (peek (0));
   }

   /**
    * Looks at the object at the specified distance from the
    * top of this stack without removing it from the stack.
    * @param distanceFromTop distance from the top to look at, or
    * <code>null</code> if this is an illegal distance or the stack
    * is empty
    * @return the object at the requested location
    */
   public Object peek (int distanceFromTop) {
     Object val = null;
     try {
      val = get (size() - 1 - distanceFromTop);
     }
     catch (Exception e) {}
     return val;
   }

   /**
    * Removes the object at the top of this stack and returns that
    * object as the value of this function.
    * @return The object at the top of this stack, or <code>null</code>
    * if the stack is empty.
    */
   public Object pop () {
     Object val = null;
     try {
      val = remove (size() - 1);
     }
     catch (Exception e) {}
     return val;
   }

   /**
    * Pushes an item onto the top of this stack.
    * @param item the object to push
    * @return the same object
    */
   public void push(Object item) {
     add (item);
     //return item;
   }

   /**
    * Returns the 1-based position where an object is on this stack. If the
    * object o occurs as an item in this stack, this method returns the
    * distance from the top of the stack of the occurrence nearest the top of the
    * stack; the topmost item on the stack is considered to be at distance 1.
    * The equals method is used to compare o to the items in this stack.
    * @param o  the desired object.
    * @return the 1-based position from the top of the stack where the object
    * is located; the return value -1 indicates that the object is not on the stack.
    */
   public int search (Object o) {
     int lastIndex = lastIndexOf (o);
     if (lastIndex != -1) {
       return (size() - lastIndex);
     }
     return -1;
   }
}