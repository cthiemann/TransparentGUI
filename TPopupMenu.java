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

public class TPopupMenu extends TWindow {
  
  public class Item extends TButton {
    public Item(TransparentGUI gui, String text) { super(gui, text); setMargin(0); setAlignment(ALIGN_LEFT); }
    public void handleMouseClicked() { super.handleMouseClicked(); TPopupMenu.this.handleMouseClickOn(this); }
  }
  
  protected class Separator extends Item {
    public Separator(TransparentGUI gui, String text) { super(gui, text); setEnabled(false); setAlignment(ALIGN_CENTER); }
    public Separator(TransparentGUI gui) { this(gui, ""); }
    public TComponent.Dimension getPreferredSize() {
      TComponent.Dimension d = super.getPreferredSize();
      d.width += 20; if (text.length() == 0) d.height -= 5;
      return d;
    }
    public void draw(PGraphics g) {
      super.draw(g);
      g.textFont(getFont());
      g.stroke(getForeground()); g.noFill();
      //
      float x0 = bounds.x + padding.left;
      float x1 = bounds.x + bounds.width - padding.right;
      float x2 = bounds.x + bounds.width/2 - g.textWidth(text)/2 - 5;
      float x3 = bounds.x + bounds.width/2 + g.textWidth(text)/2 + 5;
      float y = bounds.y + bounds.height - padding.bottom - g.textDescent();
      float h = g.textAscent() + g.textDescent();
      if (bounds.height - padding.top - padding.bottom > h)
        y -= (bounds.height - padding.top - padding.bottom - h)/2;
      y -= g.textAscent()/3;
      //
      if (text.length() == 0) {
        g.line(bounds.x + padding.left, bounds.y + bounds.height/2 - 1,
               bounds.x + bounds.width - padding.right, bounds.y + bounds.height/2 - 1);
      } else {
        g.line(x0, y, x2, y); g.line(x3, y, x1, y);
      }
    }
  }
  
  TPopupMenu(TransparentGUI gui) {
    super(gui); fragile = true;
    setPadding(5, 1);
    setBorder(2); setBorderColor(new java.awt.Color(200, 0, 0, 200));
  }
  
  public void add(Item item) { super.add(item, TBorderLayout.NORTH); }
  public void add(String text) { add(text, text); }
  public void add(String text, String actionCommand) { 
    Item item = new Item(gui, text);
    item.setActionCommand(actionCommand);
    add(item);
  }
  public void addSeparator() { addSeparator(""); }
  public void addSeparator(String text) { add(new Separator(gui, text)); }
  
  public Item getItem(String actionCommand) {
    if (actionCommand == null) return null;
    for (int i = 0; i < components.size(); i++)
      if (actionCommand.equals(((Item)components.get(i)).getActionCommand()))
        return (Item)components.get(i);
    return null;
  }
  
  public void setEnabled(String actionCommand, boolean b) {
    Item item = getItem(actionCommand);
    if (item != null) item.setEnabled(b);
  }
  
  public void show(TComponent origin, int x, int y) {
    setSize(getPreferredSize());
    validate();
    TComponent.Point p = new TComponent.Point(origin.getX() + x, origin.getY() + y);
    if (p.x + bounds.width + margin.right > gui.app.width) p.x -= bounds.width;
    if (p.y + bounds.height + margin.bottom > gui.app.height) p.y -= bounds.height;
    setLocation(p);
    gui.add(this);
  }
  
  public void handleMouseClickOn(Item item) {
    gui.remove(this);
  }
  
}
