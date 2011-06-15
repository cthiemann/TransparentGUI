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
import processing.core.PApplet;
import java.awt.event.MouseEvent;

public class TWindow extends TContainer {
  protected boolean fragile = false;  // if this is true, the window will be removed from the rootContainer if a mouse click occured that did not hit this window
  protected boolean movable = false;
  
  protected TWindow() { super(null); }  // this constructor is only for use by TransparentGUI
  public TWindow(TransparentGUI gui) { this(gui, new TBorderLayout()); }
  public TWindow(TransparentGUI gui, TLayoutManager layout) {
    super(gui);
    setLayout(layout);
    setMargin(0);
    setPadding(0);
    setFocusable(false);
    capturesMouse = true;
  }
  
  public boolean isFocused() { return gui.getFocusedWindow() == this; }
  public boolean isActive() { return gui.getActiveWindow() == this; }
  
  public boolean isFragile() { return fragile; }
  public void setFragile(boolean b) { fragile = b; }
  
  public boolean isMovable() { return movable; }
  public void setMovable(boolean b) { movable = b; }

  protected float pmouseX, pmouseY;
  protected Rectangle pbounds;
  public void handleMouseEvent(MouseEvent e) {
    super.handleMouseEvent(e);
    if (gui.componentMouseClicked != this) return;
    switch (e.getID()) {
      case MouseEvent.MOUSE_PRESSED:
        pmouseX = gui.app.mouseX; pmouseY = gui.app.mouseY;
        pbounds = new Rectangle(bounds);
        break;
      case MouseEvent.MOUSE_DRAGGED:
        if (!movable) break;
        bounds.x = pbounds.x + gui.app.mouseX - pmouseX;
        bounds.y = pbounds.y + gui.app.mouseY - pmouseY;
        bounds.x = PApplet.max(0, PApplet.min(gui.app.width - bounds.width, bounds.x));
        bounds.y = PApplet.max(0, PApplet.min(gui.app.height - bounds.height, bounds.y));
        invalidate(); break;
    }
  }
}