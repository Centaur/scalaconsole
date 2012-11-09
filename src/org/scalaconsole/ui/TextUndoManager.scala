package org.scalaconsole.ui

import javax.swing.undo.{UndoableEdit, CompoundEdit, UndoManager}
import java.lang.Boolean
import java.beans.PropertyChangeListener
import javax.swing.event.{UndoableEditEvent, SwingPropertyChangeSupport}

class TextUndoManager extends UndoManager {
  def addPropertyChangeListener(listener: PropertyChangeListener) {
    propChangeSupport.addPropertyChangeListener(listener)
  }
  def removePropertyChangeListener(listener: PropertyChangeListener) {
    propChangeSupport.removePropertyChangeListener(listener)
  }

  val propChangeSupport = new SwingPropertyChangeSupport(this)
  var compoundEdit = new StructuredEdit
  var firstModified = 0L
  var modificationMarker = editToBeUndone()


  override def die() {
    val undoable = canUndo
    super.die()
    firePropertyChangeEvent('Undo.name, undoable, canUndo)
  }


  override def discardAllEdits() {
    val undoable = canUndo
    val redoable = canRedo
    super.discardAllEdits()
    modificationMarker = editToBeUndone()
    firePropertyChangeEvent('Undo.name, undoable, canUndo)
    firePropertyChangeEvent('Redo.name, redoable, canRedo)
  }

  def hasChanged = modificationMarker != editToBeUndone()


  override def redo() {
    compoundEdit.end()
    if(firstModified == 0L) {
      firstModified = editToBeRedone().asInstanceOf[StructuredEdit].editedTime
    }
    val undoable = canUndo
    super.redo()
    firePropertyChangeEvent('Undo.name, undoable, canUndo)
  }


  override def redoTo(edit: UndoableEdit) {
    compoundEdit.end()
    if(firstModified == 0L) {
      firstModified = editToBeRedone().asInstanceOf[StructuredEdit].editedTime
    }
    val undoable = canUndo
    super.redoTo(edit)
    firePropertyChangeEvent('Undo.name, undoable, canUndo)
  }

  def reset() {
    if(modificationMarker != editToBeUndone()) {
      modificationMarker = editToBeUndone()
    }
  }


  override def trimEdits(from: Int, to: Int) {
    val undoable = canUndo
    val redoable = canRedo
    super.trimEdits(from, to)
    firePropertyChangeEvent('Undo.name, undoable, canUndo)
    firePropertyChangeEvent('Redo.name, redoable, canRedo)
  }

  override def undo() {
    compoundEdit.end()
    if(firstModified == editToBeUndone.asInstanceOf[StructuredEdit].editedTime) {
      firstModified = 0
    } else if (firstModified == 0) {
      firstModified = editToBeUndone().asInstanceOf[StructuredEdit].editedTime
    }
    val redoable = canRedo
    super.undo()
    firePropertyChangeEvent('Redo.name, redoable, canRedo)
  }


  override def undoableEditHappened(e: UndoableEditEvent) {
    val edit = e.getEdit
    val undoable = canUndo
    var editTime = System.currentTimeMillis()
    if(firstModified == 0 || editTime - compoundEdit.editedTime > 700){
      compoundEdit.end()
      compoundEdit = new StructuredEdit
    }
    compoundEdit.addEdit(edit)
    if(firstModified == 0) {
      firstModified = compoundEdit.editedTime
    }
    if(lastEdit() != compoundEdit) {
      addEdit(compoundEdit)
      firePropertyChangeEvent('Undo.name, undoable, canUndo)
    }

  }

  protected def firePropertyChangeEvent(name:String, oldValue:Boolean, newValue:Boolean) {
    propChangeSupport.firePropertyChange(name, oldValue, newValue)
  }

  class StructuredEdit extends CompoundEdit {
    var editedTime = 0L

    override def addEdit(anEdit: UndoableEdit) = {
      val result = super.addEdit(anEdit)
      if(result && editedTime == 0L){
        editedTime = System.currentTimeMillis
      }
      result
    }

    override def isInProgress = false

    override def canUndo = edits.size > 0
  }
}

