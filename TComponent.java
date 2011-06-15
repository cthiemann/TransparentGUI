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
import java.lang.reflect.Method;
import processing.core.PFont;
import processing.core.PGraphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class TComponent extends Object {
  
  public static class Point {
    public float x, y;
    public Point() { this(0, 0); }
    public Point(Point p) { this(p.x, p.y); }
    public Point(float x, float y) { this.x = x; this.y = y; }
    public String toString() { return getClass().getName() + "[x=" + x + ",y=" + y + "]"; };
  }
  
  public static class Dimension {
    public float width, height;
    public Dimension() { this(0, 0); }
    public Dimension(Dimension d) { this(d.width, d.height); }
    public Dimension(float w, float h) { width = w; height = h; }
    public String toString() { return getClass().getName() + "[width=" + width + ",height=" + height + "]"; };
  }
  
  public static class Rectangle {
    public float x, y, width, height;
    public Rectangle() { this(0, 0, 0, 0); }
    public Rectangle(Rectangle r) { this(r.x, r.y, r.width, r.height); }
    public Rectangle(float x, float y, float w, float h) { this.x = x; this.y = y; width = w; height = h; }
    public boolean contains(float x, float y) {
      return (x >= this.x) && (x <= this.x + width) &&
             (y >= this.y) && (y <= this.y + height); }
   public String toString() { return getClass().getName() + "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]"; };
  }
  
  public static class Spacing {
    public float top, right, bottom, left;
    public Spacing() { this(0, 0, 0, 0); }
    public Spacing(Spacing s) { this(s.top, s.right, s.bottom, s.left); }
    public Spacing(float trbl) { this(trbl, trbl, trbl, trbl); }
    public Spacing(float tb, float rl) { this(tb, rl, tb, rl); }
    public Spacing(float t, float rl, float b) { this(t, rl, b, rl); }
    public Spacing(float t, float r, float b, float l) { top = t; right = r; bottom = b; left = l; }
    public float get() { return ((top == right) && (top == bottom) && (top == left)) ? top : -1; }
    public String toString() { return getClass().getName() + "[top=" + top + ",right=" + right + ",bottom=" + bottom + ",right=" + right + "]"; };
  }
  
  public static class BorderRadius {
    public float topleft, topright, bottomright, bottomleft;
    public BorderRadius() { this(0, 0, 0, 0); }
    public BorderRadius(BorderRadius br) { this(br.topleft, br.topright, br.bottomright, br.bottomleft); }
    public BorderRadius(float r) { this(r, r, r, r); }
    public BorderRadius(float tl, float tr, float br, float bl) { topleft = tl; topright = tr; bottomright = br; bottomleft = bl; }
    public float get() { return ((topleft == topright) && (topleft == bottomright) && (topleft == bottomleft)) ? topleft : -1; }
    public String toString() { return getClass().getName() + "[topleft=" + topleft + ",topright=" + topright + ",bottomright=" + bottomright + ",bottomleft=" + bottomleft + "]"; };
  }
  
  protected final static int MOUSE_NONE = 0;
  protected final static int MOUSE_OVER = 1;
  protected final static int MOUSE_DOWN = 2;
  
  protected TransparentGUI gui = null;
  protected TContainer parent = null;
  protected boolean valid = false;
  protected boolean visible = true;
  protected boolean enabled = true;
  protected boolean focusable = true;

  protected Object hint = null;
  protected Rectangle bounds = new Rectangle(0, 0, 0, 0);
  protected Spacing margin = new Spacing(1, 3);
  protected Spacing padding = new Spacing(0, 10);

  protected Spacing border = new Spacing(0);
  protected BorderRadius borderRadius = new BorderRadius(8, 8, 8, 8);
  protected PFont fn = null;
  protected Color fg = null;
  protected Color bg = null;
  protected Color borderColor = null;
  
  protected int mouseState = MOUSE_NONE;
  protected boolean capturesMouse = true;
  protected boolean clickable = false;
  protected float bgAlpha = 0.f, bgAlpha_target = 0.f;
  
  protected TPopupMenu pmContextMenu = null;
  protected Object actionEventHandler = null;
  protected Method actionEventMethod = null;
  
  protected TToolTip tooltip = null;
  
  public TComponent(TransparentGUI gui) { this.gui = gui; }
  public TransparentGUI getGUI() { return gui; }
  public TContainer getParent() { return parent; }
  public Object getLayoutHint() { return hint; }
  public void setLayoutHint(Object o) { hint = o; invalidate(); }
  
  public boolean isValid() { return valid; }
  public boolean isVisible() { return visible; }
  public boolean isShowing() { return isVisible() && (parent != null) && parent.isShowing(); }
  public boolean isEnabled() { return enabled && ((parent == null) || (parent.isEnabled())); }
  public boolean isFocusable() { return focusable; }
  public boolean isFocusOwner() { return gui.getFocusOwner() == this; }
  public void setEnabled(boolean b) { enabled = b; }
  public void setVisible(boolean b) { if (visible != b) { visible = b; if (parent != null) parent.invalidate(); } }
  public void setVisibleAndEnabled(boolean b) { setVisible(b); setEnabled(b); }
  public void setFocusable(boolean b) { focusable = b; }
  
  public void requestFocus() { gui.requestFocus(this); }
  public void handleFocusGained() {}
  public void handleFocusLost() {}
  public void transferFocus() { if (parent != null) gui.requestFocus(parent.getFocusableComponentAfter(this)); }
  public void transferFocusBackward() { if (parent != null) gui.requestFocus(parent.getFocusableComponentBefore(this)); }
  
  public Color getForegroundColor() {
    Color c =  (fg != null) ? new Color(fg.getRGB(), true)
                            : new Color(gui.style.getForegroundColor(this).getRGB(), true);
    return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha()*(isEnabled() ? 1 : .25f)));
  }
  public void setForegroundColor(Color c) { fg = (c != null) ? new Color(c.getRGB(), true) : null; }
  public int getForeground() { Color c = getForegroundColor(); return (c != null) ? c.getRGB() : 0; }
  public void setForeground(int c) { fg = new Color(c, true); }
  public boolean isForegroundSet() { return fg != null; }

  public Color getBackgroundColor() {
    Color c =  (bg != null) ? new Color(bg.getRGB(), true)
                            : new Color(gui.style.getBackgroundColor(this).getRGB(), true);
    return new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()*(isEnabled() ? 1 : 0));
  }
  public void setBackgroundColor(Color c) { bg = (c != null) ? new Color(c.getRGB(), true) : null; }
  public int getBackground() { Color c = getBackgroundColor(); return (c != null) ? c.getRGB() : 0; }
  public void setBackground(int c) { bg = new Color(c, true); }
  public boolean isBackgroundSet() { return bg != null; }
  
  public PFont getFont() { return (fn != null) ? fn : gui.style.getFont(this); }
  public void setFont(PFont f) { fn = f; invalidate(); }
  public boolean isFontSet() { return fn != null; }
  
  public Point getLocation() {
    return new Point(bounds.x, bounds.y);
  }
  public Point getLocationOnScreen() {
    Point p = parent.getLocationOnScreen();
    return new Point(p.x + bounds.x, p.y + bounds.y);
  }
  public void setLocation(float x, float y) { bounds.x = x; bounds.y = y; }
  public void setLocation(Point p) { setLocation(p.x, p.y); }
  
  public Dimension getSize() {
    return new Dimension(bounds.width, bounds.height);
  }
  public void setSize(float w, float h) { bounds.width = w; bounds.height = h; }
  public void setSize(Dimension d) { bounds.width = d.width; bounds.height = d.height; }
  
  public Rectangle getBounds() { return new Rectangle(bounds); }
  public void setBounds(float x, float y, float w, float h) { bounds.x = x; bounds.y = y; bounds.width = w; bounds.height = h; }
  public void setBounds(Rectangle r) { bounds = new Rectangle(r); }
  public float getX() { return bounds.x; }
  public float getY() { return bounds.y; }
  public float getWidth() { return bounds.width; }
  public float getHeight() { return bounds.height; }

  public Spacing getMargin() { return new Spacing(margin); }
  public void setMargin(float t, float r, float b, float l) { margin.top = t; margin.right = r; margin.bottom = b; margin.left = l; invalidate(); }
  public void setMargin(float t, float rl, float b) { margin.top = t; margin.right = margin.left = rl; margin.bottom = b; invalidate(); }
  public void setMargin(float tb, float rl) { margin.top = margin.bottom = tb; margin.right = margin.left = rl; invalidate(); }
  public void setMargin(float trbl) { margin.top = margin.right = margin.bottom = margin.left = trbl; invalidate(); }
  public void setMargin(Spacing s) { margin = new Spacing(s); invalidate(); }
  public float getMarginTop() { return margin.top; }
  public float getMarginRight() { return margin.right; }
  public float getMarginBottom() { return margin.bottom; }
  public float getMarginLeft() { return margin.left; }
  
  public Spacing getPadding() { return new Spacing(padding); }
  public void setPadding(float t, float r, float b, float l) { padding.top = t; padding.right = r; padding.bottom = b; padding.left = l; invalidate(); }
  public void setPadding(float t, float rl, float b) { padding.top = t; padding.right = padding.left = rl; padding.bottom = b; invalidate(); }
  public void setPadding(float tb, float rl) { padding.top = padding.bottom = tb; padding.right = padding.left = rl; invalidate(); }
  public void setPadding(float trbl) { padding.top = padding.right = padding.bottom = padding.left = trbl; invalidate(); }
  public void setPadding(Spacing s) { padding = new Spacing(s); invalidate(); }
  public float getPaddingTop() { return padding.top; }
  public float getPaddingRight() { return padding.right; }
  public float getPaddingBottom() { return padding.bottom; }
  public float getPaddingLeft() { return padding.left; }

  public Spacing getBorder() { return new Spacing(border); }
  public void setBorder(float t, float r, float b, float l) { border.top = t; border.right = r; border.bottom = b; border.left = l; invalidate(); }
  public void setBorder(float t, float rl, float b) { border.top = t; border.right = border.left = rl; border.bottom = b; invalidate(); }
  public void setBorder(float tb, float rl) { border.top = border.bottom = tb; border.right = border.left = rl; invalidate(); }
  public void setBorder(float trbl) { border.top = border.right = border.bottom = border.left = trbl; invalidate(); }
  public void setBorder(Spacing s) { border = new Spacing(s); invalidate(); }
  public float getBorderTop() { return border.top; }
  public float getBorderRight() { return border.right; }
  public float getBorderBottom() { return border.bottom; }
  public float getBorderLeft() { return border.left; }
  
  public BorderRadius getBorderRadius() { return new BorderRadius(borderRadius); }
  public void setBorderRadius(float tl, float tr, float br, float bl) { borderRadius.topleft = tl; borderRadius.topright = tr; borderRadius.bottomright = br; borderRadius.bottomleft = bl; }
  public void setBorderRadius(float r) { borderRadius.topleft = borderRadius.topright = borderRadius.bottomright = borderRadius.bottomleft = r; }
  public void setBorderRadiusTop(float r) { borderRadius.topleft = borderRadius.topright = r; }
  public void setBorderRadiusRight(float r) { borderRadius.topright = borderRadius.bottomright = r; }
  public void setBorderRadiusBottom(float r) { borderRadius.bottomright = borderRadius.bottomleft = r; }
  public void setBorderRadiusLeft(float r) { borderRadius.topleft = borderRadius.bottomleft = r; }
  public void setBorderRadius(BorderRadius br) { borderRadius = new BorderRadius(br); }
  public float getBorderRadiusTopLeft() { return borderRadius.topleft; }
  public float getBorderRadiusTopRight() { return borderRadius.topright; }
  public float getBorderRadiusBottomRight() { return borderRadius.bottomright; }
  public float getBorderRadiusBottomLeft() { return borderRadius.bottomleft; }

  public Color getBorderColorObj() {
    return (borderColor != null) ? new Color(borderColor.getRGB(), true)
                                 : new Color(gui.style.getBorderColor(this).getRGB(), true); }
  public void setBorderColor(Color c) { borderColor = (c != null) ? new Color(c.getRGB(), true) : null; }
  public int getBorderColor() { Color c = getBorderColorObj(); return (c != null) ? c.getRGB() : 0; }
  public void setBorderColor(int c) { borderColor = new Color(c, true); }
  public boolean isBorderColorSet() { return borderColor != null; }
  
  public Dimension getPreferredSize() {
    Dimension d = getMinimumSize();
    d.width += padding.left + padding.right;
    d.height += padding.top + padding.bottom;
    return d;
  }
  public Dimension getMinimumSize() { return new Dimension(0, 0); }
  public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }
  
  public TPopupMenu getContextMenu() { return pmContextMenu; }
  public void setContextMenu(TPopupMenu menu) { pmContextMenu = menu; }
  
  public Object getActionEventHandler() { return actionEventHandler; }
  public Method getActionEventMethod() { return actionEventMethod; }
  public void setActionEventHandler(Object o, Method m) {
    if ((o != null) && (m != null)) { actionEventHandler = o; actionEventMethod = m; } }
  public void setActionEventHandler(Object o, String name) {
    try { setActionEventHandler(o, o.getClass().getMethod(name, new Class[] { String.class })); }
    catch (Exception e) { /* silently ignore this... */ } }
  public void setActionEventHandler(Object o) { if (o != null) setActionEventHandler(o, "actionPerformed"); }
  
  public TToolTip getToolTip() { return tooltip; }
  public void setToolTip(TToolTip tooltip) { this.tooltip = tooltip; }
  public void setToolTip(String str) { setToolTip(new TToolTip(this, str)); }
  
  public void doLayout() {}
  public void validate() {
    doLayout();  // in TContainer, this will trigger the actual layout algorithm
    valid = true;  // ok, ready to be displayed
  }
  public void invalidate() {
    valid = false;   // something's wrong with this component
    if (tooltip != null) tooltip.invalidate();  // tooltip position might have to be updated
    if (parent != null) parent.invalidate();  // the container this component is in will have to redo its layout
  }
  
  public boolean contains(float x, float y) { return bounds.contains(x, y); }
  public boolean contains(Point p) { return contains(p.x, p.y); }
  public TComponent getComponentAt(float x, float y) { return contains(x, y) && visible ? this : null; }
  public TComponent getComponentAt(Point p) { return getComponentAt(p.x, p.y); }
  
  public void handleKeyEvent(KeyEvent e) {
    if (!isFocusOwner()) return;
    if ((e.getKeyCode() == KeyEvent.VK_TAB) || (e.getKeyChar() == '\t')) {
      if (e.getID() == KeyEvent.KEY_PRESSED) {
        if (e.isShiftDown()) transferFocusBackward();
        else transferFocus();
      }
      e.consume();
    }
  }
  
  public void handleMouseEvent(MouseEvent e) {
    if ((pmContextMenu != null) && e.isPopupTrigger())
      pmContextMenu.show(gui, gui.app.mouseX, gui.app.mouseY);
    else switch (e.getID()) {
      case MouseEvent.MOUSE_PRESSED:
        if (clickable) mouseState = mouseState | MOUSE_DOWN;
        break;
      case MouseEvent.MOUSE_RELEASED:
        if ((mouseState & MOUSE_DOWN) != 0)
          handleMouseClicked();
        mouseState = mouseState & ~MOUSE_DOWN;
        break;
      case MouseEvent.MOUSE_CLICKED:
        // Java only generates MOUSE_CLICKED for PRESSED+RELEASED without move.  We want MOUSE_CLICKED
        // if both PRESSED and RELEASED were over this component.  Thus, we call handleMouseClicked()
        // on MOUSE_RELEASED.
        break;
    }
    e.consume();
  }
  public void handleMouseClicked() {}  // called by handleMouseEvent when both PRESSED and RELEASED occured with this == componentAtMouse
  public void handleMouseEntered() {  // called by TransparentGUI when this component becomes componentAtMouse
    if ((gui.componentMouseClicked == null) || (gui.componentMouseClicked == this)) mouseState = MOUSE_OVER; }
  public void handleMouseExited() { mouseState = MOUSE_NONE; }  // called by TransparentGUI when this component is no longer componentAtMouse

  protected final static float RNDCTRLFACTOR = (1 - 4.f*(float)(Math.sqrt(2) - 1)/3.f);

  protected void drawRoundRectangle(PGraphics g) { drawRoundRectangle(g, borderRadius); }
  protected void drawRoundRectangle(PGraphics g, float rnd) { drawRoundRectangle(g, new BorderRadius(rnd)); }
  protected void drawRoundRectangle(PGraphics g, BorderRadius br) {
    float rndTL = br.topleft;
    float rndTR = br.topright;
    float rndBR = br.bottomright;
    float rndBL = br.bottomleft;
    float rndctrlTL = rndTL * RNDCTRLFACTOR;
    float rndctrlTR = (rndTR == rndTL) ? rndctrlTL : rndTR * RNDCTRLFACTOR;
    float rndctrlBR = (rndBR == rndTL) ? rndctrlTL : rndBR * RNDCTRLFACTOR;
    float rndctrlBL = (rndBL == rndTL) ? rndctrlTL : rndBL * RNDCTRLFACTOR;
    g.beginShape();
    g.vertex(bounds.x + rndTL, bounds.y);
    g.vertex(bounds.x + bounds.width - rndTR, bounds.y);
    g.bezierVertex(bounds.x + bounds.width - rndctrlTR, bounds.y, bounds.x + bounds.width, bounds.y + rndctrlTR, bounds.x + bounds.width, bounds.y + rndTR);
    g.vertex(bounds.x + bounds.width, bounds.y + bounds.height - rndBR);
    g.bezierVertex(bounds.x + bounds.width, bounds.y + bounds.height - rndctrlBR, bounds.x + bounds.width - rndctrlBR, bounds.y + bounds.height, bounds.x + bounds.width - rndBR, bounds.y + bounds.height);
    g.vertex(bounds.x + rndBL, bounds.y + bounds.height);
    g.bezierVertex(bounds.x + rndctrlBL, bounds.y + bounds.height, bounds.x, bounds.y + bounds.height - rndctrlBL, bounds.x, bounds.y + bounds.height - rndBL);
    g.vertex(bounds.x, bounds.y + rndTL);
    g.bezierVertex(bounds.x, bounds.y + rndctrlTL, bounds.x + rndctrlTL, bounds.y, bounds.x + rndTL, bounds.y);
    g.endShape();
  }
  
  protected void drawBackground(PGraphics g) {
    if ((bounds.width == 0) || (bounds.height == 0)) return;  // nothing to draw here
    Color bg = getBackgroundColor();
    if (clickable && isEnabled()) {  // adjust background alpha
      bgAlpha_target = ((mouseState & MOUSE_DOWN) != 0) ? 1.0f : ((mouseState & MOUSE_OVER) != 0) ? 0.5f : 0.0f;
      bgAlpha += 10*(bgAlpha_target - bgAlpha)*gui.dt;
      bgAlpha = Math.max(0, Math.min(1, bgAlpha));
      bg = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), (int)(bg.getAlpha()*bgAlpha));
    }
    if (bg.getAlpha() == 0) return;  // nothing to draw here
    g.noStroke(); g.fill(bg.getRGB());
    if (borderRadius.get() == 0) {
      g.rectMode(g.CORNER); g.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    } else
      drawRoundRectangle(g);
  }
  
  protected void drawBorder(PGraphics g) {
    if ((bounds.width == 0) || (bounds.height == 0)) return;  // nothing to draw here
    Color bc = getBorderColorObj();
    float bw = border.get();  // this will be the border width for all four edges or -1 if they are different
    if (isFocusOwner()) { bw = 1; bc = new java.awt.Color(255, 0, 0); }  // FIXME: should this be a style option?
    if (this instanceof TFrame) {
      if (((TWindow)this).isFocused()) { bw = 2; bc = new java.awt.Color(200, 0, 0, 200); }  // FIXME: style
      else if (((TWindow)this).isActive()) { bw = 2; bc = new java.awt.Color(127, 127, 127, 200); }  // FIXME: style
    }
    if ((pmContextMenu != null) && pmContextMenu.isShowing()) bw = 2;  // FIXME: should this be a style option?
    if ((bc.getAlpha() == 0) || (bw == 0)) return;  // nothing to draw here
    g.noFill(); g.strokeWeight(bw); g.stroke(bc.getRGB());
    if (bw != -1) {
      // draw full border at once
      if (borderRadius.get() == 0) {
        g.rectMode(g.CORNER); g.rect(bounds.x, bounds.y, bounds.width, bounds.height);
      } else
        drawRoundRectangle(g);
    } else {
      // draw individual borders
      if (border.top > 0) { g.strokeWeight(border.top); g.line(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y); }
      if (border.right > 0) { g.strokeWeight(border.right); g.line(bounds.x + bounds.width, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height); }
      if (border.bottom > 0) { g.strokeWeight(border.bottom); g.line(bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y + bounds.height); }
      if (border.left > 0) { g.strokeWeight(border.left); g.line(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height); }
    }
    g.strokeWeight(1);  // restore stroke weight to standard value
  }
  
  protected void drawLayout(PGraphics g) {
    // for layout debugging...
    g.stroke(gui.app.color(0));
    g.noFill();
    g.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    g.noStroke();
    g.fill(gui.app.color(255, 0, 0, 100));
    g.rect(bounds.x - margin.left, bounds.y - margin.top, margin.left + bounds.width + margin.right, margin.top + bounds.height + margin.bottom);
    g.fill(gui.app.color(0, 255, 0, 100));
    g.rect(bounds.x + padding.left, bounds.y + padding.top, bounds.width -padding.left - padding.right, bounds.height - padding.top - padding.bottom);
  }
  
  public void draw(PGraphics g) {
    if (!valid) validate();
    drawBackground(g);
    drawBorder(g);
    //drawLayout(g);
    if (tooltip != null) tooltip.update();
  }
}