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
import processing.core.PFont;
import processing.core.PGraphics;
import java.util.Vector;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class TButton extends TLabel {
  protected String command = null;
  protected int hotKeyCode = 0;
  protected int hotKeyMods = 0;

  public TButton(TransparentGUI gui) { this(gui, ""); }
  public TButton(TransparentGUI gui, String s) {
    super(gui, s); clickable = capturesMouse = true; setFocusable(true); setAlignment(ALIGN_CENTER); }
  
  public String getActionCommand() { return (command != null) ? command : text; }
  public void setActionCommand(String s) { command = s; }
  
  public void setHotKey(int c) { setHotKey(c, 0); }
  public void setHotKey(int c, int mods) {
    gui.unregisterFromKeyEvents(this);
    hotKeyCode = c;
    hotKeyMods = mods;
    if (c > 0) gui.registerForKeyEvents(this);
  }
  
  public void handleKeyEvent(KeyEvent e) {
    super.handleKeyEvent(e);
    if (e.isConsumed() || (e.getID() != KeyEvent.KEY_PRESSED)) return;
    if ((e.getKeyCode() == hotKeyCode) && (e.getModifiers() == hotKeyMods)) {
      handleMouseClicked();
      e.consume();
    }
  }
  
  public void handleMouseClicked() {
    gui.fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand()));
    bgAlpha = 1.0f;  // give instant visual feedback (also when the hot key was hit)
  }
}