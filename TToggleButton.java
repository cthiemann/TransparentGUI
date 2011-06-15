/*
 * Copyright 2011 Christian Thiemann <christian@spato.net>
 * Developed at Northwestern University <http://rocs.northwestern.edu>
 *
 * This file is part of TransparentGUI, a GUI library for Processing.
 *
 * TransparentGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TransparentGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TransparentGUI.  If not, see <http://www.gnu.org/licenses/>.
 */

package tGUI;
import processing.core.PGraphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class TToggleButton extends TButton {
  protected boolean selected = false;
  protected TButtonGroup group = null;
  
  public TToggleButton(TransparentGUI gui) { this(gui, "", null); }
  public TToggleButton(TransparentGUI gui, String s) { this(gui, s, null); }
  public TToggleButton(TransparentGUI gui, String s, TButtonGroup g) { super(gui, s); setButtonGroup(g); }
  
  public boolean isSelected() { return selected; }
  public void setSelected(boolean b) { selected = b; }
  
  public TButtonGroup getButtonGroup() { return group; }
  public void setButtonGroup(TButtonGroup g) { group = g;
    if ((group != null) && (group.getSelected() == null)) group.setSelected(this); }
  
  public void handleKeyEvent(KeyEvent e) {
    if ((group == null) || (group.getSelected() != this))
      super.handleKeyEvent(e);  // only look at the event if it has a chance to do anything
  }

  public void handleMouseClicked() {
    if (group == null) {
      selected = !selected;
      super.handleMouseClicked();
    } else if (group.getSelected() != this) {
      group.setSelected(this);
      super.handleMouseClicked();
    }
  }
  
  public Color getForegroundColor() { return gui.style.getForegroundColor(this, selected); }  // FIXME
}