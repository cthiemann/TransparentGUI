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

// FIXME: TBorderLayout does not honor component margin and padding

public class TBorderLayout extends Object implements TLayoutManager {
  public static final String NORTH = "North";
  public static final String SOUTH = "South";
  public static final String EAST = "East";
  public static final String WEST = "West";
  public static final String CENTER = "Center";

  protected boolean stretchCenter = true;

  public TBorderLayout() { this(true); }
  public TBorderLayout(boolean stretchCenter) { this.stretchCenter = stretchCenter; }

  public TComponent.Dimension preferredLayoutSize(TContainer target) {
    float width = 0, height = 0, addwidth = 0, addheight = 0;
    // The width and height variables are there to track the width of all EAST/WEST components and the height
    // of all NORTH/SOUTH components.  The addwidth and addheight track the additional space necessary to accomodate
    // every component.  I.e., assume the first component is NORTH.  Then height += comp.height because it sits at the top
    // and will take up that much space.  But also addwidth has to be enlarged to d.width if the latter is larger than the
    // former, because it wants to be that wide.  Now, if the second component is EAST, addheight has be at least comp.height,
    // because that EAST components wants to be that high.  But we can addwidth can be decreased by comp.width because this
    // is now accounted for in width.
    TComponent.Spacing s = target.getPadding();
    TComponent[] components = target.getComponents();
    TComponent center = null;
    String layoutHint = null;
    for (int i = 0; i < components.length; i++) {
      if (!components[i].isVisible()) continue;
      layoutHint = (String)components[i].getLayoutHint();
      TComponent.Dimension d = components[i].getPreferredSize();
      if (((layoutHint == null) || (CENTER.equals(layoutHint))) && (center == null)) {
        center = components[i];
      } else if (NORTH.equals(layoutHint)) {
        float h = d.height + PApplet.max(s.top, components[i].getMarginTop());
        height += h;
        addheight = PApplet.max(0, addheight - h);
        addwidth = PApplet.max(addwidth, d.width + PApplet.max(s.left, components[i].getMarginLeft())
                                                 + PApplet.max(s.right, components[i].getMarginRight()));
        s.top = components[i].getMarginBottom();
      } else if (SOUTH.equals(layoutHint)) {
        float h = d.height + PApplet.max(s.bottom, components[i].getMarginBottom());
        height += h;
        addheight = PApplet.max(0, addheight - h);
        addwidth = PApplet.max(addwidth, d.width + PApplet.max(s.left, components[i].getMarginLeft())
                                                 + PApplet.max(s.right, components[i].getMarginRight()));
        s.bottom = components[i].getMarginTop();
      } else if (EAST.equals(layoutHint)) {
        float w = d.width + PApplet.max(s.right, components[i].getMarginRight());
        width += w;
        addwidth = PApplet.max(0, addwidth - w);
        addheight = PApplet.max(addheight, d.height + PApplet.max(s.top, components[i].getMarginTop())
                                                    + PApplet.max(s.bottom, components[i].getMarginBottom()));
        s.right = components[i].getMarginLeft();
      } else if (WEST.equals(layoutHint)) {
        float w = d.width + PApplet.max(s.left, components[i].getMarginLeft());
        width += w;
        addwidth = PApplet.max(0, addwidth - w);
        addheight = PApplet.max(addheight, d.height + PApplet.max(s.top, components[i].getMarginTop())
                                                    + PApplet.max(s.bottom, components[i].getMarginBottom()));
        s.left = components[i].getMarginRight();
      }
    }
    if (center != null) {
      TComponent.Dimension d = center.getPreferredSize();
      d.width += PApplet.max(s.left, center.getMarginLeft());
      d.width += PApplet.max(s.right, center.getMarginRight());
      d.height += PApplet.max(s.top, center.getMarginTop());
      d.height += PApplet.max(s.bottom, center.getMarginBottom());
      addwidth = PApplet.max(addwidth, d.width);
      addheight = PApplet.max(addheight, d.height);
    } else {
      // If we don't have a center component that holds everything in place, check that the padding/margin
      // agains the "wall we last built towards" is honored.
      TComponent.Spacing p = target.getPadding();
      if (NORTH.equals(layoutHint)) {
        float h = PApplet.max(s.top, p.bottom);
        height += h; addheight = PApplet.max(0, addheight - h);
      } else if (SOUTH.equals(layoutHint)) {
        float h = PApplet.max(s.bottom, p.top);
        height += h; addheight = PApplet.max(0, addheight - h);
      } else if (EAST.equals(layoutHint)) {
        float w = PApplet.max(s.right, p.left);
        width += w; addwidth = PApplet.max(0, addwidth - w);
      } else if (WEST.equals(layoutHint)) {
        float w = PApplet.max(s.left, p.right);
        width += w; addwidth = PApplet.max(0, addwidth - w);
      }
    }
    return new TComponent.Dimension(width + addwidth, height + addheight);
  }

  public TComponent.Dimension minimumLayoutSize(TContainer target) {
    float width = 0, height = 0, addwidth = 0, addheight = 0;
    TComponent[] components = target.getComponents();
    TComponent center = null;
    for (int i = 0; i < components.length; i++) {
      if (!components[i].isVisible()) continue;
      TComponent.Dimension d = components[i].getMinimumSize();
      if (((components[i].getLayoutHint() == null) || (CENTER.equals(components[i].getLayoutHint()))) && (center == null))
        center = components[i];
      else if ((NORTH.equals(components[i].getLayoutHint())) || (SOUTH.equals(components[i].getLayoutHint()))) {
        height += d.height;
        addheight = PApplet.max(0, addheight - d.height);
        addwidth = PApplet.max(addwidth, d.width);
      } else if ((EAST.equals(components[i].getLayoutHint())) || (WEST.equals(components[i].getLayoutHint()))) {
        width += d.width;
        addwidth = PApplet.max(0, addwidth - d.width);
        addheight = PApplet.max(addheight, d.height);
      }
    }
    if (center != null) {
      TComponent.Dimension d = center.getMinimumSize();
      addwidth = PApplet.max(addwidth, d.width);
      addheight = PApplet.max(addheight, d.height);
    }
    return new TComponent.Dimension(width + addwidth, height + addheight);
  }

  public TComponent.Dimension maximumLayoutSize(TContainer target) {
    return new TComponent.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }

  public void layoutContainer(TContainer target) {
    float left = 0, right = target.getWidth(), top = 0, bottom = target.getHeight();
    TComponent.Spacing s = target.getPadding();
    TComponent[] components = target.getComponents();
    TComponent center = null;
    for (int i = 0; i < components.length; i++) {
      if (!components[i].isVisible()) continue;
      TComponent.Dimension d = components[i].getPreferredSize();
      if (((components[i].getLayoutHint() == null) || (CENTER.equals(components[i].getLayoutHint()))) && (center == null))
        center = components[i];
      else if (NORTH.equals(components[i].getLayoutHint())) {
        top += PApplet.max(s.top, components[i].getMarginTop());
        float cleft = left + PApplet.max(s.left, components[i].getMarginLeft());
        float cright = right - PApplet.max(s.right, components[i].getMarginRight());
        components[i].setBounds(cleft, top, cright - cleft, d.height);
        top += d.height;
        s.top = components[i].getMarginBottom();
      } else if (SOUTH.equals(components[i].getLayoutHint())) {
        bottom -= PApplet.max(s.bottom, components[i].getMarginBottom());
        float cleft = left + PApplet.max(s.left, components[i].getMarginLeft());
        float cright = right - PApplet.max(s.right, components[i].getMarginRight());
        components[i].setBounds(cleft, bottom - d.height, cright - cleft, d.height);
        bottom -= d.height;
        s.bottom = components[i].getMarginTop();
      } else if (EAST.equals(components[i].getLayoutHint())) {
        right -= PApplet.max(s.right, components[i].getMarginRight());
        float ctop = top + PApplet.max(s.top, components[i].getMarginTop());
        float cbottom = bottom - PApplet.max(s.bottom, components[i].getMarginBottom());
        components[i].setBounds(right - d.width, ctop, d.width, cbottom - ctop);
        right -= d.width;
        s.right = components[i].getMarginLeft();
      } else if (WEST.equals(components[i].getLayoutHint())) {
        left += PApplet.max(s.left, components[i].getMarginLeft());
        float ctop = top + PApplet.max(s.top, components[i].getMarginTop());
        float cbottom = bottom - PApplet.max(s.bottom, components[i].getMarginBottom());
        components[i].setBounds(left, ctop, d.width, cbottom - ctop);
        left += d.width;
        s.left = components[i].getMarginRight();
      }
    }
    if (center != null) {
      float cleft = left + PApplet.max(s.left, center.getMarginLeft());
      float cright = right - PApplet.max(s.right, center.getMarginRight());
      float ctop = top + PApplet.max(s.top, center.getMarginTop());
      float cbottom = bottom - PApplet.max(s.bottom, center.getMarginBottom());
      float cwidth = cright - cleft, cheight = cbottom - ctop;
      if (!stretchCenter) {
        TComponent.Dimension d = center.getPreferredSize();
        cleft += (cwidth - d.width)/2;
        ctop += (cheight - d.height)/2;
        cwidth = d.width;
        cheight = d.height;
      }
      center.setBounds(cleft, ctop, cwidth, cheight);
    }
  }
}