
package edu.columbia.cs.sdarts.util;


/**
 * An <code>int</code>-specific stack, designed to reduce casting overhead.
 * It is used inside a lot of the SAX parsers to keep track of state.
 * It does not use the Java Collections API.
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class IntStack {
  private int[]   stack;
  private int     index;
  private double  growthFactor;

  /**
   * Default constructor. Construct with a growth factor of 0.5
   */
  public IntStack () {
    this (0.5);
  }

  /**
   * Create and <code>IntStack</code> and assign some growth factor to it.
   * When an <code>IntStack</code> runs out of storage, the growth factor
   * is multiplied by the current stack size. The product is added to the
   * current size, and the sum is the new stack size.
   * @param growthFactor the growth factor
   */
  public IntStack (double growthFactor) {
    this.growthFactor = growthFactor;
    index = -1;
    stack = new int [10];
  }

  /**
   * Return whether the stack is empty
   * @return whether the stack is empty
   */
  public boolean empty () {
    return (index == -1);
  }

  /**
   * Clears the contents of the stack
   */
  public void clear () {
    index = -1;
  }

  /**
   * Returns the <code>int</code> at the top of the stack, or -1 if the
   * stack is empty.
   * @return the <code>int</code> at the top of the stack, or -1 if the
   * stack is empty. (DANGER - be careful of storing negative values in
   * the stack)
   */
  public int peek () {
    return peek(0);
  }

  /**
   * Returns the <code>int</code> at <i>distanceFromTop</i> distance from the
   * top of the stack, or if that distance goes below the stack. Calling
   * <code>peek(0)</code> is the equivalent of calling <code>peek()</code>.
   * @return the <code>int</code> at the top of the stack, or -1 if the
   * stack is empty. (DANGER - be careful of storing negative values in
   * the stack)
   */
  public int peek (int distanceFromTop) {
    int location = index - distanceFromTop;
    if (location < 0) {
      return -1;
    }
    else {
      return stack[location];
    }
  }

  /**
   * Pops the top <code>int</code> off the stack and returns it
   * @return the popped <code>int</code>
   */
  public int pop () {
    int retVal = stack[index];
    stack[index] = 0;
    index--;
    return retVal;
  }

  /**
   * Pushes a new <code>int</code> onto the stack
   * @param item the new <code>int</code> to push
   * @return the <code>int</code> that was pushed
   */
  public int push (int item) {
    int len = stack.length;
    if (++index == len) {
      int[] stack2 = new int[len + (int) Math.floor (len * growthFactor)];
      System.arraycopy (stack, 0, stack2, 0, len);
      stack = stack2;
    }

    stack[index] = item;
    return item;
  }

  /**
   * Returns the 1-based position where an <code>int</code> is on this stack.
   * If the <code>int</code> occurs as an item in this stack, this method
   * returns the distance from the top of the stack of the occurrence nearest
   * the top of the stack; the topmost item on the stack is considered to be
   * at distance 1. The <code>==</code> sign is used to compare the
   * <code>int</code> to the items in this stack.
   * @param item the <code>int</code> being searched for
   * @return its 1-based distance from the top of the stack, or -1 if it
   * is not present.
   */
  public int search (int item) {
    for (int i = index ; i >= 0 ; i--) {
      if (stack[i] == item) {
        return (i - index + 1);
      }
    }
    return -1;
  }
}