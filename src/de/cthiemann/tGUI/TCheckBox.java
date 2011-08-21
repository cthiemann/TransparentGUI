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
import java.awt.Color;
import java.awt.event.MouseEvent;

public class TCheckBox extends TToggleButton {
  public TCheckBox(TransparentGUI gui) { this(gui, "", null); }
  public TCheckBox(TransparentGUI gui, String s) { this(gui, s, null); }
  public TCheckBox(TransparentGUI gui, String s, TButtonGroup g) {
    super(gui, s, g); setAlignment(ALIGN_LEFT); }

  public TComponent.Dimension getPreferredSize() {
    TComponent.Dimension d = super.getPreferredSize();
    d.width += gui.app.g.textDescent()/2 + 5 + gui.app.g.textAscent() - 15;
    return d;
  }

  public TComponent.Dimension getMinimumSize() {
    TComponent.Dimension d = super.getMinimumSize();
    d.width += gui.app.g.textDescent()/2 + 5 + gui.app.g.textAscent();
    return d;
  }

  public Color getForegroundColor() { return gui.style.getForegroundColor(this); }  // FIXME

  // FIXME: this needs to be reworked
  public void draw(PGraphics g) {
    // draw label
    g.textFont(getFont());
    float boxpad = g.textAscent() + g.textWidth("  ");
    padding.left += boxpad;
    super.draw(g);
    padding.left -= boxpad;
    // draw checkbox
    float x = bounds.x + padding.left;
    float y = bounds.y + bounds.height - padding.bottom - g.textDescent();
    float h = g.textAscent() + g.textDescent();
    if (bounds.height > h) y -= (bounds.height - h)/2;
    g.stroke(getForeground());
    g.noFill();
    if (group == null) {  // draw a rectangular check box with a cross
      g.rectMode(g.CORNER);
      g.rect(x + 1, y - g.textAscent() + 2, g.textAscent() - 2, g.textAscent() - 2);
      if (selected) {
        g.line(x + 3, y - g.textAscent() + 4, x + g.textAscent() - 3, y - 2);
        g.line(x + 3, y - 2, x + g.textAscent() - 3, y - g.textAscent() + 4);
      }
    } else {  // draw a round box with a disc
      float d = g.textAscent() - 3;
      x += 2 + d/2;
      y = bounds.height/2 + 1;
      g.ellipse(x, y, d, d);
      if (selected) {
        g.fill(getForeground());
        g.ellipse(x, y, d/3, d/3);
      }
    }
  }
}