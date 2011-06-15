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

public class TFlowLayout extends Object implements TLayoutManager {
  public static final int ALIGN_LEFT = PApplet.LEFT;
  public static final int ALIGN_CENTER = PApplet.CENTER;
  public static final int ALIGN_RIGHT = PApplet.RIGHT;
  public int align = ALIGN_LEFT; // FIXME: implement alignment in layoutContainer
  
  public TFlowLayout() {}
  public TFlowLayout(int align) { this.align = align; }
  
  public TComponent.Dimension preferredLayoutSize(TContainer target) {
    float width = 0, height = 0, spacing = target.getPaddingLeft();
    TComponent[] components = target.getComponents();
    for (int i = 0; i < components.length; i++) {
      if (!components[i].isVisible()) continue;
      TComponent.Dimension d = components[i].getPreferredSize();
      width += d.width + PApplet.max(spacing, components[i].getMarginLeft());  // we need components width plus the larger of the spacing the last component (or target) requested or this component's left margin
      height = PApplet.max(height, d.height + PApplet.max(components[i].getMarginTop(), target.getPaddingTop())
                                            + PApplet.max(components[i].getMarginBottom(), target.getPaddingBottom()));
      spacing = components[i].getMarginRight();  // tell next component we want this spacing to it
    }
    width += PApplet.max(spacing, target.getPaddingRight());  // honor last component's and target's spacing requests
    return new TComponent.Dimension(width, height);
  }

  public TComponent.Dimension minimumLayoutSize(TContainer target) {
    float width = 0, height = 0;
    TComponent[] components = target.getComponents();
    for (int i = 0; i < components.length; i++) {
      if (!components[i].isVisible()) continue;
      TComponent.Dimension d = components[i].getMinimumSize();
      width += d.width;
      height = PApplet.max(height, d.height);
    }
    return new TComponent.Dimension(width, height);
  }

  public TComponent.Dimension maximumLayoutSize(TContainer target) {
    return new TComponent.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }

  protected void alignComponents(TContainer target, int i0, int i1,
                                 float excessWidth, float rowHeight, float spacingTop) {
    for (int i = i0; i < i1; i++) {
      TComponent comp = target.getComponent(i);
      comp.setLocation(comp.getX(), comp.getY() + spacingTop + (rowHeight - comp.getHeight())/2);
    }
  }

  public void layoutContainer(TContainer target) {
    int rowStart = 0;
    float left = 0, top = 0, rowHeight = 0;
    float spacingTop = target.getPaddingTop(), spacingLeft = target.getPaddingLeft(), spacingBottom = 0;
    TComponent[] components = target.getComponents();
    for (int i = 0; i < components.length; i++) {
      if (!components[i].isVisible()) continue;
      TComponent.Dimension d = components[i].getPreferredSize();
      if (d.width > target.getWidth() - left) {
        alignComponents(target, rowStart, i,
                        target.getWidth() - left - PApplet.max(target.getPaddingRight(), components[i].getMarginRight()),
                        rowHeight, spacingTop);
        top += spacingTop + rowHeight;
        spacingTop = spacingBottom;
        spacingLeft = target.getPaddingLeft();
        spacingBottom = 0;
        left = 0;
        rowStart = i;
        rowHeight = 0;
      }
      left += PApplet.max(spacingLeft, components[i].getMarginLeft());
      spacingTop = PApplet.max(spacingTop, components[i].getMarginTop());  // FIXME: this could raise the spacingTop unncessarily if the component is very small and will be vertically aligned centered
      components[i].setBounds(left, top, d.width, d.height);
      left += d.width;
      spacingBottom = PApplet.max(spacingBottom, d.height + components[i].getMarginBottom() - rowHeight);
      rowHeight = PApplet.max(rowHeight, d.height);
    }
    alignComponents(target, rowStart, components.length,
                    target.getWidth() - left - target.getPaddingRight(),  // FIXME: ignoring last component's margin right
                    rowHeight, spacingTop);
  }
}