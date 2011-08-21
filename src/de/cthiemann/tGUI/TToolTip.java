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
import java.security.MessageDigest;
import processing.core.PApplet;

public class TToolTip extends TWindow {

  protected TComponent comp = null;  // anchor for this tooltip
  protected String id = null;  // ID for storing preferences
  protected String hashID = "null";  // ID that is used when no ID is explicitly set

  public static final int BELOW = 0;  // tooltip will hang under its anchor component
  public static final int ABOVE = 1;  // tooltip will sit on top of its anchor component
  public static final int LEFT = 2;  // tooltip will hang on the left side of its anchor component
  public static final int RIGHT = 3;  // tooltip will hang on the right side of its anchor component
  public static final int BELOW_OR_ABOVE = 4;  // tooltip will be below or above anchor, depending on available space
  public static final int LEFT_OR_RIGHT = 5;  // tooltip will be left or right of anchor, depending on available space
  public static final int DEFAULT = 6;  // tooltip will be placed depending on anchor component type
  protected int preferredLocation = DEFAULT;

  protected float tfuse = 0;  // time since the mouse entered anchor component
  protected float t1 = Float.NaN;  // time (in seconds) until tooltip will be shown
  protected long tlast = -1;  // last time the tooltip was shown (Unix timestamp)

  public TToolTip(TComponent comp) { this(comp, new TBorderLayout()); }
  public TToolTip(TComponent comp, String str) { this(comp); setText(str); }
  public TToolTip(TComponent comp, TLayoutManager layout) {
    super(comp.gui, layout);
    this.comp = comp;
    setFocusable(false);
    capturesMouse = false;
    setVisibleAndEnabled(true);
    setBackgroundColor(new java.awt.Color(225, 225, 175, 225));
    setMargin(5);  // distance to anchor component
  }

  public String getID() { return id; }
  public void setID(String id) { this.id = id; }
  protected String getPrefID() { return (id != null) ? id : hashID; }

  public void setText(String str) {
    removeAll();
    add(gui.createLabel(str));
    setPadding(5);
    // calculate content hash
    try {
      MessageDigest md5 = MessageDigest.getInstance("md5");
      hashID = String.format("%032x", new java.math.BigInteger(1, md5.digest(str.getBytes())));
    } catch (Exception e) {
      hashID = "null";
    }
    t1 = Float.NaN; tlast = -1;
  }

  public void setToolTip(TToolTip tt) { throw new RuntimeException("ha ha, very funny..."); }

  public int getPreferredLocation() { return preferredLocation; }
  public void setPreferredLocation(int location) { preferredLocation = location; }

  public void validate() {
    if (valid) return;
    // at this point, we can be sure that the anchor component is already correctly positioned
    Dimension ptts = getPreferredSize();  // preferred tooltip size
    Point acl = comp.getLocationOnScreen();  // screen location of the anchor component
    Dimension acs = comp.getSize();  // anchor component size
    // determine general tooltip location
    if (preferredLocation == DEFAULT) preferredLocation =
      ((comp instanceof TChoice.Menu.Item) || (comp instanceof TPopupMenu.Item)) ? LEFT_OR_RIGHT : BELOW_OR_ABOVE;
    int location = preferredLocation;
    if (location == BELOW_OR_ABOVE) location = (gui.app.height - acl.y - acs.height > acl.y) ? BELOW : ABOVE;
    if (location == LEFT_OR_RIGHT) location = (acl.x > gui.app.width - acl.x - acs.width) ? LEFT : RIGHT;
    // set tooltip size (honor preferred size, but make sure it fits on the screen)
    bounds.width = PApplet.min(ptts.width,
      (((location == BELOW) || (location == ABOVE)) ? gui.app.width :
       ((location == LEFT) ? acl.x : gui.app.width - acl.x - acs.width)) - margin.left - margin.right);
    bounds.height = PApplet.min(ptts.height,
      (((location == LEFT) || (location == RIGHT)) ? gui.app.height :
       ((location == BELOW) ? gui.app.height - acl.y - acs.height : acl.y)) - margin.top - margin.bottom);
    // set tooltip location (snap to "outer" anchor corner but ensure tooltip is fully on-screen)
    bounds.x = (location == RIGHT) ? acl.x + acs.width + margin.left  // attach right
             : (location == LEFT) ? acl.x - bounds.width - margin.right  // attach left
             : (acl.x + acs.width/2 < gui.app.width/2) ? PApplet.min(acl.x, gui.app.width - bounds.width - margin.right)  // align left
             : PApplet.max(acl.x + acs.width - bounds.width - margin.left, 0) + margin.left;  // align right
    bounds.y = (location == BELOW) ? acl.y + acs.height + margin.top  // attach below
             : (location == ABOVE) ? acl.y - bounds.height - margin.bottom  // attach above
             : (acl.y + acs.height/2 < gui.app.height/2) ? PApplet.min(acl.y, gui.app.height - bounds.height - margin.bottom)  // align top
             : PApplet.max(acl.y + acs.height - bounds.height - margin.top, 0) + margin.top;  // align bottom
    // layout components within the tooltip
    invalidateAll(); // force re-layouting the tooltip window
    super.validate();  // TWindow.validate() -> TContainer.doLayout()
  }

  private boolean show = false;

  public void update() {
    t1 = 0;//FIXME temp//if (Float.isNaN(t1)) t1 = gui.prefs.getFloat(getPrefID() + ".delay", 0);
    //if (tlast == -1) tlast = gui.prefs.getLong(getPrefID() + ".last", )  // FIXME
    //
    if (gui.componentAtMouse == comp) {
      tfuse += gui.dt;
      if (!show && (tfuse > t1)) {
        validate();
        show = true;
        //FIXME temp//gui.prefs.putFloat(getPrefID() + ".delay", t1 = PApplet.min(2, t1 + .25f));
      }
    } else {
      tfuse = 0;
      invalidate();
      show = false;
    }
    //
    if (show)
      gui.showToolTip(this);
  }

}