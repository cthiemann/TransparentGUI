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
import java.util.Vector;

public class TButtonGroup extends Object {
  protected TToggleButton selected = null;

  public TButtonGroup() { this(null); }
  public TButtonGroup(TToggleButton[] buttons) {
    if (buttons != null)
      for (int i = 0; i < buttons.length; i++)
        buttons[i].setButtonGroup(this);
  }

  public TToggleButton getSelected() { return selected; }
  public void setSelected(TToggleButton b) {
    if ((selected == b) || (b.group != this)) return;
    if (selected != null) selected.setSelected(false);
    selected = b;
    selected.setSelected(true);
  }
}