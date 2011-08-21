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

package de.cthiemann.tGUI;
import processing.core.PApplet;
import processing.core.PGraphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public class TSlider extends TComponent {
  protected int value = 0, min = 0, max = 100;
  protected float prefWidth = 0;
  protected float knobSize = 8;
  protected String command = null;

  public TSlider(TransparentGUI gui) { this(gui, null); }
  public TSlider(TransparentGUI gui, String cmd) { super(gui); clickable = true; setActionCommand(cmd); }

  public String getActionCommand() { return (command != null) ? command : "TSlider"; }
  public void setActionCommand(String s) { command = s; }

  public int getValue() { return value; }
  public void setValue(int value) { this.value = value; }
  public int getMinValue() { return min; }
  public void setMinValue(int min) { this.min = min; }
  public int getMaxValue() { return max; }
  public void setMaxValue(int max) { this.max = max; }
  public void setValueBounds(int min, int max) { this.min = min; this.max = max; }

  public float getPreferredWidth() { return prefWidth; }
  public void setPreferredWidth(float width) { prefWidth = width; invalidate(); }

  public TComponent.Dimension getMinimumSize() { return new TComponent.Dimension(prefWidth + knobSize, knobSize); }

  public void handleMouseEvent(MouseEvent e) {
    super.handleMouseEvent(e);
    if (gui.componentMouseClicked == this) {
      int oldvalue = value;
      float width = bounds.width - padding.left - padding.right - knobSize;
      value = min + PApplet.round((e.getX() - getLocationOnScreen().x - padding.left - knobSize/2f)/width*(max - min));
      value = PApplet.max(min, PApplet.min(max, value));
      if (value != oldvalue)
        gui.fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand() + "##valueChanged"));
    }
  }

  public void draw(PGraphics g) {
    super.draw(g);
    g.pushStyle();
    g.noFill();
    g.stroke(getForeground());
    g.strokeWeight((gui.componentMouseClicked == this) ? 1 : .25f);
    float width = bounds.width - padding.left - padding.right - knobSize;
    float y = bounds.y + padding.top + (bounds.height - padding.top - padding.bottom)/2;
    g.line(bounds.x + padding.left + knobSize/2, y,
           bounds.x + padding.left + (bounds.width - padding.left - padding.right) - knobSize/2f, y);
    g.popStyle();
    g.noStroke();
    g.fill(getForeground());
    float r = (gui.componentMouseClicked == this) ? knobSize : .7f*knobSize;
    float knobOffset = (value - min)*width/(max - min);
    g.ellipse(bounds.x + padding.left + knobSize/2f + knobOffset, y, r, r);
  }
}