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
import processing.core.PGraphics;
import java.util.Vector;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class TChoice extends TComponent {
  protected String actionCmdPrefix = "";
  protected Vector<Object> items = new Vector<Object>();
  protected int selected = -1;
  protected Menu menu = null;
  protected boolean compact = true;  // if true, the TChoice will only be as wide as the currently selected item
  protected Renderer renderer = null;
  protected TComponent.Rectangle obounds = null;
  protected String strEmpty = "\u2014 empty \u2014";  // displayed in choice if item list is empty
  protected String strNoSelection = "\u2014 click to select \u2014";  // displayed if no item is selected but allowNone is false
  protected String strNone = "\u2014 none \u2014";  // displayed if no item (null) is selected and allowNone is true
  protected boolean allowNone = false;  // if true, TChoice will offer to select "none" in the popup menu

  protected char hotKeyCharCycle = 0;
  protected char[] hotKeyCharItems = null;

  public static abstract class Renderer {
    public boolean getEnabled(Object o) { return true; }
    public String getActionCommand(Object o) { return o.toString(); }
    public abstract TComponent.Dimension getPreferredSize(TChoice c, Object o, boolean inMenu);
    public abstract void draw(TChoice c, PGraphics g, Object o, TComponent.Rectangle bounds, boolean inMenu);
  }

  public static class StringRenderer extends Renderer {
    public String getString(Object o, boolean inMenu) { return o.toString(); }
    public TComponent.Dimension getPreferredSize(TChoice c, Object o, boolean inMenu) {
      c.gui.app.g.textFont(c.getFont());
      return new TComponent.Dimension(
        c.gui.app.g.textWidth(getString(o, inMenu)),
        c.gui.app.g.textAscent() + 1.5f*c.gui.app.g.textDescent());
    }
    public void draw(TChoice c, PGraphics g, Object o, TComponent.Rectangle bounds, boolean inMenu) {
      g.noStroke();
      g.textFont(c.getFont());
      g.fill(c.getForeground());
      g.textAlign(g.LEFT, g.BASELINE);
      float x = bounds.x;
      float y = bounds.y + bounds.height - g.textDescent();
      float h = g.textAscent() + g.textDescent();
      if (bounds.height > h) y -= (bounds.height - h)/2;
      g.text(getString(o, inMenu), x, y);
    }
  }

  protected class Menu extends TWindow {

    protected class Item extends TComponent {
      protected Object o;
      protected TComponent.Rectangle obounds;
      protected Renderer renderer;
      public Item(TransparentGUI gui, Object o) {
        super(gui); this.o = o; setMargin(0); clickable = true;
        this.renderer = TChoice.this.renderer; }
      public TComponent.Dimension getMinimumSize() {
        return renderer.getPreferredSize(TChoice.this, o, true); }
      public void validate() { super.validate(); obounds = getBounds();
        obounds.x += padding.left; obounds.y += padding.top;
        obounds.width -= padding.left + padding.right; obounds.height -= padding.top + padding.bottom;
        setEnabled(renderer.getEnabled(o)); }
      public void handleMouseClicked() { Menu.this.handleMouseClickOn(this); }
      public void draw(PGraphics g) { super.draw(g); renderer.draw(TChoice.this, g, o, obounds, true); }
    }

    protected class NullItem extends Item {
      public NullItem(TransparentGUI gui) { super(gui, strNone); renderer = new StringRenderer(); }
      public void draw(PGraphics p) {
        java.awt.Color c = TChoice.this.isForegroundSet() ? TChoice.this.getForegroundColor() : null;;
        TChoice.this.setForeground((getForeground() & 0x00ffffff) | (127 << 24));
        super.draw(p);
        TChoice.this.setForegroundColor(c);
      }
    }

    Menu(TransparentGUI gui) { super(gui); fragile = true; }
    public void validate() {
      if (valid) return;
      removeAll();
      if (allowNone)
        add(new NullItem(gui), TBorderLayout.NORTH);
      for (int i = 0; i < items.size(); i++)
        add(new Item(gui, items.get(i)), TBorderLayout.NORTH);
      TComponent.Point p = TChoice.this.getLocationOnScreen();
      if (!items.isEmpty() || allowNone) {
        p.x -= getComponent(0).getPaddingLeft() - TChoice.this.getPaddingLeft();
        p.y -= getComponent(0).getPaddingTop() - TChoice.this.getPaddingTop(); }
      TComponent.Dimension d = getPreferredSize();
      setBounds(p.x, p.y, d.width, d.height);
      super.validate();
      for (int i = 0; i < selected; i++)
        p.y -= getComponent(i).getHeight();
      p.y = PApplet.max(0, PApplet.min(gui.getHeight() - d.height, p.y));
      p.x = PApplet.max(0, PApplet.min(gui.getWidth() - d.width, p.x));
      setLocation(p);
    }
    public void handleMouseClickOn(Item item) {
      TChoice.this.selectAndNotify(components.indexOf(item) - (allowNone ? 1 : 0));
      gui.remove(this);
    }
  }

  public TChoice(TransparentGUI gui) { this(gui, ""); }
  public TChoice(TransparentGUI gui, String actionCmdPrefix) {
    super(gui);
    this.actionCmdPrefix = actionCmdPrefix;
    menu = new Menu(gui);
    setRenderer(new StringRenderer());
    clickable = true;
  }

  public String getActionCommandPrefix() { return actionCmdPrefix; }
  public void setActionCommandPrefix(String s) { actionCmdPrefix = s; }

  public boolean isCompact() { return compact; }
  public void setCompact(boolean b) { if (compact != b) { compact = b; invalidate(); } }

  public Renderer getRenderer() { return renderer; }
  public void setRenderer(Renderer r) { renderer = r; }

  public String getEmptyString() { return strEmpty; }
  public void setEmptyString(String str) { this.strEmpty = str; }

  public String getNoSelectionString() { return strNoSelection; }
  public void setNoSelectionString(String str) { this.strNoSelection = str; }

  public String getNoneString() { return strNone; }
  public void setNoneString(String str) { this.strNone = str; }

  public boolean allowsNone() { return allowNone; }
  public void setAllowNone(boolean b) { allowNone = b; }

  public int getItemCount() { return items.size(); }
  public Object getItem(int index) { return items.get(index); }

  public void add(Object[] items) { if (items != null) for (int i = 0; i < items.length; i++) add(items[i]); }
  public void add(Object item) { items.add(item); invalidate(); }
  public void insert(Object item, int index) { items.add(index, item); invalidate(); }

  public void remove(Object item) { remove(items.indexOf(item)); }
  public void remove(int index) {
    if (selected == index) selected = Math.max(0, selected - 1);
    items.remove(index);
    if (items.isEmpty()) selected = -1;
    invalidate();
  }
  public void removeAll() { items.clear(); selected = -1; invalidate(); }

  public Object getSelectedItem() { return (selected != -1) ? items.get(selected) : null; }
  public int getSelectedIndex() { return selected; }

  public boolean select(Object item) { return select(items.indexOf(item)); }
  public boolean select(int index) {
    if (selected == index) return false;
    selected = index;
    if (items.isEmpty()) selected = -1;
    if (compact) invalidate();
    return true;
  }
  protected void selectAndNotify(int index) {
    if (select(index)) {
      gui.fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
        actionCmdPrefix + ((selected == -1) ? "@@none@@" : renderer.getActionCommand(items.get(selected)))));
      bgAlpha = 1.f;  // give visual feedback of the action
    }
  }

  public Dimension getMinimumSize() {
    TComponent.Dimension d = (selected > -1)
      ? renderer.getPreferredSize(this, items.get(selected), false) : new TComponent.Dimension(0, 0);
    if (!compact)  // find maximum item width
      for (int i = 0; i < items.size(); i++)
        d.width = PApplet.max(d.width, renderer.getPreferredSize(this, items.get(i), false).width);
    // ensure the empty/no-selection string can be displayed
    if ((selected == -1) || !compact) {
      gui.app.g.textFont(getFont());
      d.width = PApplet.max(d.width,
        gui.app.g.textWidth((items.size() == 0) ? strEmpty : (allowNone ? strNone : strNoSelection)));
      d.height = PApplet.max(d.height, gui.app.g.textAscent() + 1.5f*gui.app.g.textDescent());
    }
    return d;
  }

  public void validate() { super.validate(); obounds = getBounds();
    obounds.x += padding.left; obounds.y += padding.top;
    obounds.width -= padding.left + padding.right; obounds.height -= padding.top + padding.bottom; }
  public void invalidate() { super.invalidate(); gui.remove(menu); menu.invalidate(); }
  public void invalidateMenu() { menu.invalidate(); }

  public void setHotKeyChar(char c) { gui.unregisterFromKeyEvents(this); hotKeyCharCycle = c;
    if ((hotKeyCharCycle > 0) || (hotKeyCharItems != null)) gui.registerForKeyEvents(this); }
  public void setShortcutChars(char[] c) { gui.unregisterFromKeyEvents(this); hotKeyCharItems = c;
    if ((hotKeyCharCycle > 0) || (hotKeyCharItems != null)) gui.registerForKeyEvents(this); }

  public void handleKeyEvent(KeyEvent e) {
    super.handleKeyEvent(e);
    if (e.isConsumed() || (e.getID() != KeyEvent.KEY_PRESSED)) return;
    if ((hotKeyCharCycle > 0) && (e.getKeyChar() == hotKeyCharCycle) && (items.size() > 0))
      selectAndNotify((selected + 1) % items.size());
    else if (hotKeyCharItems != null)
      for (int i = 0; i < Math.min(hotKeyCharItems.length, items.size()); i++)
        if (e.getKeyChar() == hotKeyCharItems[i])
          selectAndNotify(i);
  }

  public void handleMouseClicked() { if (!items.isEmpty()) gui.add(menu); }

  public void draw(PGraphics g) {
    super.draw(g);
    if (selected > -1)
      renderer.draw(this, g, items.get(selected), obounds, false);
    else {
      g.noStroke();
      g.textFont(getFont());
      g.fill(getForeground(), 127);
      g.textAlign(g.LEFT, g.BASELINE);
      float y = bounds.y + bounds.height - g.textDescent();
      float h = g.textAscent() + g.textDescent();
      if (bounds.height > h) y -= (bounds.height - h)/2;
      g.text(items.isEmpty() ? strEmpty : (allowNone ? strNone : strNoSelection),
        bounds.x + padding.left, y);
    }
  }
}