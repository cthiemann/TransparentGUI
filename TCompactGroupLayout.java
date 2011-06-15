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

public class TCompactGroupLayout extends Object implements TLayoutManager {
  public static final String STRETCH = "Stretch";  // components with this hint may be enlarged to fit an oversized compact group
  
  public float addPadding = 0;
  
  public TCompactGroupLayout() { this(0); }
  public TCompactGroupLayout(float addPadding) { this.addPadding = addPadding; }
  
  protected void styleComponents(TContainer target) {
    // do not call setMargin, setPadding etc here, because they will re-invalidate its component's layout
    float r = 8;
    target.padding = new TComponent.Spacing(0);
    target.setBackgroundColor(target.gui.style.getBackgroundColorForCompactGroups());
    TComponent first = null, last = null;
    for (int i = 0; i < target.getComponentCount(); i++) {
      TComponent comp = target.getComponent(i);
      if (!comp.isVisible()) continue;
      if (first == null) first = comp;
      comp.margin = new TComponent.Spacing(0);
      comp.padding = new TComponent.Spacing(addPadding, 5);
      comp.borderRadius = new TComponent.BorderRadius(0);
      last = target.getComponent(i);
    }
    if (first == null) return;  // this container has no visible components
    first.padding.left += 5;
    first.borderRadius.topleft = r;
    first.borderRadius.bottomleft = r;
    last.padding.right += 5;
    last.borderRadius.topright = r;
    last.borderRadius.bottomright = r;
    target.borderRadius = new TComponent.BorderRadius(5);
  }
  
  public TComponent.Dimension preferredLayoutSize(TContainer target) {
    return minimumLayoutSize(target);
  }

  public TComponent.Dimension minimumLayoutSize(TContainer target) {
    styleComponents(target);
    float width = 0, height = 0;
    for (int i = 0; i < target.getComponentCount(); i++) {
      if (!target.getComponent(i).isVisible()) continue;
      TComponent.Dimension d = target.getComponent(i).getPreferredSize();
      width += d.width;
      height = PApplet.max(height, d.height);
    }
    return new TComponent.Dimension(width, height);
  }

  public TComponent.Dimension maximumLayoutSize(TContainer target) {
    return new TComponent.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }

  public void layoutContainer(TContainer target) {
    float left = 0, width, height = target.getHeight(), prefWidth = 0, stretchCount = 0;
    for (int i = 0; i < target.getComponentCount(); i++)
      if (target.getComponent(i).isVisible()) {
        prefWidth += target.getComponent(i).getPreferredSize().width;
        if (STRETCH.equals(target.getComponent(i).getLayoutHint())) stretchCount++; }
    float addToStretch = (stretchCount == 0) ? 0 : (target.getWidth() - prefWidth)/stretchCount;
    for (int i = 0; i < target.getComponentCount(); i++) {
      if (!target.getComponent(i).isVisible()) continue;
      width = target.getComponent(i).getPreferredSize().width;
      if (STRETCH.equals(target.getComponent(i).getLayoutHint())) width += addToStretch;
      target.getComponent(i).setBounds(left, 0, width, height);
      left += width;
    }  // FIXME: what happens if the components don't fit?
  }
}