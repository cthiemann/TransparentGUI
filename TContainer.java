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
import java.util.Vector;

public class TContainer extends TComponent {
  protected Vector<TComponent> components = new Vector<TComponent>();
  protected TLayoutManager layout = null;

  public TContainer(TransparentGUI gui) {
    super(gui);
    setMargin(0);
    setPadding(0);
    capturesMouse = false;
  }

  public int getComponentCount() { return components.size(); }
  public TComponent getComponent(int i) { return components.get(i); }
  public TComponent[] getComponents() {
    TComponent[] comps = new TComponent[components.size()];
    for (int i = 0; i < comps.length; i++)
      comps[i] = components.get(i);
    return comps;
  }

  public void add(TComponent comp) { add(comp, null, -1); }
  public void add(TComponent comp, Object hint) { add(comp, hint, -1); }
  public void add(TComponent comp, int index) { add(comp, null, index); }
  public void add(TComponent comp, Object hint, int index) {
    if (comp.parent != null) comp.parent.remove(comp);
    comp.parent = this;
    comp.hint = hint;
    if (index == -1) components.add(comp); else components.add(index, comp);
    comp.invalidate();  // this will also invalidate this container
  }
  public void remove(int index) { remove(components.get(index)); }
  public void remove(TComponent comp) {
    components.remove(comp);
    comp.invalidate();  // this will also invalidate this container
    comp.parent = null;
    comp.hint = null;
  }
  public void removeAll() { while (components.size() > 0) remove(0); }

  public TLayoutManager getLayout() { return layout; }
  public void setLayout(TLayoutManager layout) { this.layout = layout; invalidate(); }
  public void doLayout() { if (layout != null) layout.layoutContainer(this); }
  public void validate() {
    if (valid) return;  // nothing to do here
    doLayout();
    for (int i = 0; i < components.size(); i++)
      if (components.get(i).isVisible())
        components.get(i).validate();
    valid = true;
  }
  public void invalidateAll() {
    super.invalidate();
    for (int i = 0; i < components.size(); i++)
      if (components.get(i) instanceof TContainer)
        ((TContainer)components.get(i)).invalidateAll();
      else
        components.get(i).invalidate();
  }

  public TComponent.Dimension getPreferredSize() {
    return (layout != null) ? layout.preferredLayoutSize(this) : super.getPreferredSize(); }
  public TComponent.Dimension getMinimumSize() {
    return (layout != null) ? layout.minimumLayoutSize(this) : super.getMinimumSize(); }
  public TComponent.Dimension getMaximumSize() {
    return (layout != null) ? layout.maximumLayoutSize(this) : super.getMaximumSize(); }

  public TComponent getComponentAt(float x, float y) {
    // query child components, front-most (last added) child first
    for (int i = components.size() - 1; i >= 0; i--) {
      if (!components.get(i).isVisible()) continue;
      TComponent result = components.get(i).getComponentAt(x - bounds.x, y - bounds.y);
      if (result != null)
        return result;
    }
    // return this if contains(x, y), null otherwise
    return super.getComponentAt(x, y);
  }

  public void transferFocus() { gui.requestFocus(getFocusableComponentAfter(this)); }
  public void transferFocusBackward() { gui.requestFocus(getFocusableComponentBefore(this)); }

  TComponent getFocusableComponentAfter(TComponent comp) {
    int index = components.indexOf(comp);  // if comp == this, we will start with checking components[0]
    for (int i = index + 1; i < components.size(); i++) {
      TComponent c = components.get(i);
      if (c instanceof TContainer)
        c = ((TContainer)c).getFirstFocusableComponent();
      if ((c != null) && c.isShowing() && c.isEnabled() && c.isFocusable())
        return c;
    }
    if (this instanceof TWindow)
      return getFirstFocusableComponent();
    if (parent != null)
      return parent.getFocusableComponentAfter(this);
    return null;
  }

  TComponent getFocusableComponentBefore(TComponent comp) {
    int index = components.indexOf(comp);
    for (int i = index - 1; i >= 0; i--) {
      TComponent c = components.get(i);
      if (c instanceof TContainer)
        c = ((TContainer)c).getLastFocusableComponent();
      if ((c != null) && c.isShowing() && c.isEnabled() && c.isFocusable())
        return c;
    }
    if ((index != -1) && isShowing() && isEnabled() && isFocusable())
      return this;  // comp is a child of ours, but there was no suitable child before comp, so try us
    if (this instanceof TWindow)
      return getLastFocusableComponent();
    if (parent != null)
      return parent.getFocusableComponentBefore(this);
    return null;
  }

  TComponent getFirstFocusableComponent() {
    if (isShowing() && isEnabled() && isFocusable())
      return this;
    for (int i = 0; i < components.size(); i++) {
      TComponent c = components.get(i);
      if (c instanceof TContainer)
        c = ((TContainer)c).getFirstFocusableComponent();
      if ((c != null) && c.isShowing() && c.isEnabled() && c.isFocusable())
        return c;
    }
    return null;
  }

  TComponent getLastFocusableComponent() {
    for (int i = components.size() - 1; i >= 0; i--) {
      TComponent c = components.get(i);
      if (c instanceof TContainer)
        c = ((TContainer)c).getLastFocusableComponent();
      if ((c != null) && c.isShowing() && c.isEnabled() && c.isFocusable())
        return c;
    }
    if (isShowing() && isEnabled() && isFocusable())
      return this;
    return null;
  }

  TComponent getDefaultFocusableComponent() { return getFirstFocusableComponent(); }

  public void draw(PGraphics g) {
    if (!valid) validate();
    drawBackground(g);
    g.pushMatrix();
    g.translate(bounds.x, bounds.y);
    for (int i = 0; i < components.size(); i++)
      if (components.get(i).isVisible())
        components.get(i).draw(g);
    g.popMatrix();
    drawBorder(g);
    //drawLayout(g);
  }
}