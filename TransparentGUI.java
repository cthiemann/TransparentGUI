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
import processing.core.PApplet;
import processing.core.PFont;
import java.util.Vector;
import java.util.prefs.Preferences;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class TransparentGUI extends TWindow {
  protected PApplet app = null;
  protected Preferences prefs = null;
  
  public TComponent componentAtMouse = null;  // the component currently under mouse cursor
  protected boolean componentAtMouseValid = false;
  public TComponent componentMouseClicked = null;  // between MOUSE_PRESSED and MOUSE_RELEASED, this is the component which was under the mouse cursor at MOUSE_PRESSED
  protected TComponent componentKeyFocus = null;  // the component which currently has keyboard focus
  protected Vector<TComponent> keyEventComponents = new Vector<TComponent>();
  public ActionEvent actionEvent = null;  // the current action event when any component calls its action event (simplified) listeners
  public TToolTip visibleToolTip = null;  // currently visible tooltip, if any (null otherwise)
  
  public float t = -1, tt = -1, dt = -1;  // for animation purposes
  
  public TContainer rootContainer;
  public Style style;
  
  public class Style {
    TransparentGUI gui;
    PFont fnNorm, fnBold;
    Color cFgDefault, cBgDefault, cBdDefault;  // default colors for all components
    Color cFgToggleOff;  // color for deselected toggle buttons
    Color cBgWindow, cBgCompactGroup;  // colors for top level containers and compact groups
    Color cBgMainWinComponent, cBdMainWinComponent;  // colors for components that are direct children of TransparentGUI
    
    public Style(TransparentGUI gui) {
      this.gui = gui;
      fnNorm = createFont("GillSans", 12);
      fnBold = createFont("GillSans-Bold", 12);
      cFgDefault = new Color(0);
      cFgToggleOff = new Color(127, 127, 127);
      cBgDefault = new Color(127, 127, 127, 225);
      cBdDefault = new Color(0, 0, 0, 225);
      cBgMainWinComponent = new Color(255, 255, 255, 225);
      cBdMainWinComponent = new Color(127, 127, 127, 225);
      cBgWindow = new Color(200, 200, 200, 225);
      cBgCompactGroup = new Color(225, 225, 225, 225);
    }
    
    public PFont getFont() { return fnNorm; }
    public PFont getFont(boolean bold) { return bold ? fnBold : fnNorm; }
    public PFont getFont(TComponent comp) { return fnNorm; }
    public PFont getFont(TComponent comp, boolean bold) { return bold ? fnBold : fnNorm; }
    
    public Color getForegroundColor(TComponent comp) { return cFgDefault; }
    public Color getForegroundColor(TToggleButton comp, boolean selected) { return selected ? cFgDefault : cFgToggleOff; }
    public Color getBackgroundColor(TComponent comp) {
      if (comp instanceof TWindow) return cBgWindow;
      if (comp instanceof TConsole || (comp instanceof TLabel && !(comp instanceof TButton))) return new Color(0, 0, 0, 0);
      //if (comp.parent == gui) return cBgMainWinComponent;
      return cBgDefault;
    }
    public Color getBackgroundColorForCompactGroups() { return cBgCompactGroup; }
    public Color getBorderColor(TComponent comp) {
      if (comp.parent == gui) return cBdMainWinComponent;
      return cBdDefault;
    }
  }
  
  public TransparentGUI(PApplet app) {
    this.gui = this;
    this.app = app;
    this.prefs = Preferences.userRoot().node("/net/spato/tGUI/" + app.getClass().getName().replaceAll(".", "__"));
    this.style = new Style(this);
    this.capturesMouse = false;
    app.mouseX = app.mouseY = -1;  // mouseX/mouseY are 0 at the beginning, which is unfortunate
    app.registerPre(this);
    app.registerDraw(this);
    app.registerMouseEvent(this);
    app.registerKeyEvent(this);
    //app.registerSize(this);  // FIXME: this seems not to work in the current version of Processing (file bug report)
    app.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(java.awt.event.ComponentEvent e) {
        size(e.getComponent().getWidth(), e.getComponent().getHeight()); } }); 
    app.registerDispose(this);
    setLayout(new TBorderLayout());
    rootContainer = new TContainer(this) {
      public boolean isShowing() { return true; }
      public Point getLocationOnScreen() { return new TComponent.Point(0, 0); }
    };
    rootContainer.add(this);
    rootContainer.setBackground(0);
    rootContainer.setActionEventHandler(app);  // set applet as fallback action event handler (if method is implemented there)
    this.setBackground(0);
    this.setBounds(0, 0, app.width, app.height);
  }
  
  public TPanel createPanel() { return new TPanel(this); }
  public TPanel createPanel(TLayoutManager layout) { return new TPanel(this, layout); }
  public TPanel createPanel(TComponent[] components) { return createPanel(components, new TFlowLayout()); }
  public TPanel createPanel(TComponent[] components, TLayoutManager layout) {
    TPanel panel = createPanel(layout);
    for (int i = 0; i < components.length; i++)
      panel.add(components[i]);
    return panel;
  }
  public TPanel createCompactGroup(TComponent[] components) {
    return createCompactGroup(components, new TComponent.Spacing(1, 3)); }
  public TPanel createCompactGroup(TComponent[] components, float addPadding) {
    return createCompactGroup(components, new TComponent.Spacing(1, 3), addPadding); }
  public TPanel createCompactGroup(TComponent[] components, TComponent.Spacing margin) {
    return createCompactGroup(components, margin, 0); }
  public TPanel createCompactGroup(TComponent[] components, TComponent.Spacing margin, float addPadding) {
    TPanel panel = createPanel(components, new TCompactGroupLayout(addPadding));
    panel.setMargin(margin); panel.capturesMouse = true;
    return panel;
  }
  
  public TLabel createLabel(String text) { return new TLabel(this, text); }
  public TButton createButton(String text) { return new TButton(this, text); }
  public TToggleButton createToggleButton(String text) { return new TToggleButton(this, text); }
  public TToggleButton createToggleButton(String text, boolean selected) { TToggleButton btn = new TToggleButton(this, text); btn.setSelected(selected); return btn; }
  public TToggleButton createToggleButton(String text, TButtonGroup group) { return new TToggleButton(this, text, group); }
  public TCheckBox createCheckBox(String text) { return new TCheckBox(this, text); }
  public TCheckBox createCheckBox(String text, boolean selected) { TCheckBox chkbox = new TCheckBox(this, text); chkbox.setSelected(selected); return chkbox; }
  public TCheckBox createCheckBox(String text, TButtonGroup group) { return new TCheckBox(this, text, group); }
  public TChoice createChoice() { return new TChoice(this); }
  public TChoice createChoice(String actionCmdPrefix) { return new TChoice(this, actionCmdPrefix); }
  public TChoice createChoice(String actionCmdPrefix, boolean allowNone) {
    TChoice choice = new TChoice(this, actionCmdPrefix); choice.setAllowNone(allowNone); return choice; }
  public TTextField createTextField(String cmd) { return new TTextField(this, cmd); }
  public TSlider createSlider(String cmd) { return new TSlider(this, cmd); }

  public TConsole createConsole() { return new TConsole(this); }
  public TConsole createConsole(String tag) { return new TConsole(this, tag); }
  public TConsole createConsole(boolean debug) { return new TConsole(this, debug); }
  
  public TPopupMenu createPopupMenu() { return new TPopupMenu(this); }
  public TPopupMenu createPopupMenu(String[] items) {
    TPopupMenu pm = createPopupMenu();
    if (items != null)
      for (int i = 0; i < items.length; i++)
        if (items[i] != null)
          pm.add(items[i]);
        else
          pm.addSeparator();
    return pm;
  }
  public TPopupMenu createPopupMenu(String[][] items) {
    TPopupMenu pm = createPopupMenu();
    if (items != null)
      for (int i = 0; i < items.length; i++)
        if ((items[i] != null) && (items[i].length > 0) && (items[i][1] != null))
          pm.add(items[i][0], (items[i].length > 1) ? items[i][1] : items[i][0]);
        else
          pm.addSeparator((items[i] != null) ? items[i][0] : "");
    return pm;
  }
  
  /** Tries to create a font.  If Processing's createFont fails,
   * it searches for a TTF resource and uses that. If that fails as well,
   * the substitute font by PApplet.createFont() is returned. */
  public PFont createFont(String name, float size) {
    // try to create using Processing's createFont()
    PFont font = app.createFont(name, size);
    if (font.getPostScriptName().equals(name))
      return font;
    // try to load font from TTF in tGUI resources folder
    try {
      font = new PFont(Font.createFont(Font.TRUETYPE_FONT,
        this.getClass().getResourceAsStream("/tGUI/resources/" + name + ".ttf")).deriveFont(size), true);
    } catch (Exception e) { /* ignore silently */ }
    // if that fails or if we don't have the best font rendering capabilities, try loading from pre-rendered bitmap font file
    if (app.platform != PApplet.MACOSX) try {
      font = new PFont(this.getClass().getResourceAsStream("/tGUI/resources/" + name + "-" + size + ".vlw"));
    } catch (Exception e) { /* ignore silently */ }
    // return whatever we got (worst case, this is the default substitute font returned by createFont())
    return font;
  }
  
  public void fireActionEvent(ActionEvent event) {
    actionEvent = event;
    // call first event handler we encounter while traversing up the component hierarchy
    // (as a fallback, the rootContainer has actionPerformed in the applet registered as an event handler)
    for (TComponent comp = (TComponent)event.getSource(); comp != null; comp = comp.getParent())
      if ((comp.actionEventHandler != null) && (comp.actionEventMethod != null))
        try { comp.actionEventMethod.invoke(comp.actionEventHandler, new Object[] { event.getActionCommand() }); return; }
        catch (Exception e) { e.printStackTrace(); }
  }
  
  public void add(TWindow win) { add(win, -1); }
  public void add(TWindow win, int index) { rootContainer.add(win, null, index); componentAtMouseValid = false; }
  public void remove(TWindow win) { rootContainer.remove(win); componentAtMouseValid = false; }

  protected void checkComponentAtMouse() {
    if (componentAtMouseValid) return;
    rootContainer.validate();  // ensure that root container is correctly layed out
    TComponent newComponentAtMouse = rootContainer.getComponentAt(app.mouseX, app.mouseY);
    while ((newComponentAtMouse != null) && !newComponentAtMouse.capturesMouse)
      newComponentAtMouse = newComponentAtMouse.getParent();  // if this component doesn't handle mouse events, try parent
    if (newComponentAtMouse != componentAtMouse) {
      if (componentAtMouse != null) componentAtMouse.handleMouseExited();
      componentAtMouse = newComponentAtMouse;
      if (componentAtMouse != null) componentAtMouse.handleMouseEntered();
    }
    componentAtMouseValid = true;
  }
  
  public void pre() {
    checkComponentAtMouse();
    if (tt == -1) t = app.millis()/1000.f;  // set for the first time
    tt = t;  // save last frame's time
    t = app.millis()/1000.f;  // get this frame's time
    dt = t - tt;  // set delta
  }
  
  public void draw() {
    app.g.pushStyle();
    // The synchronized block here allows applications to synchronize
    // code fragments which might change the GUI layout etc.
    // Hope this works... concurrent stuff is weird and evil...
    synchronized (this) {
      visibleToolTip = null;
      rootContainer.draw(app.g);
      if (visibleToolTip != null)
        visibleToolTip.draw(app.g);
    }
    app.g.popStyle();
  }
  
  public void showToolTip(TToolTip tt) { visibleToolTip = tt; }
  
  protected boolean checkFragileWindows(float x, float y) {
    boolean b = false;
    for (int i = 0; i < rootContainer.getComponentCount(); i++)
      if (((TWindow)rootContainer.getComponent(i)).isFragile() &&
          rootContainer.getComponent(i).isValid() &&
          !rootContainer.getComponent(i).contains(x, y)) {
        rootContainer.remove(i--); b = true; }
    return b;
  }
  
  public TComponent getFocusOwner() { return componentKeyFocus; }
  public TWindow getFocusedWindow() {
    TComponent w = getFocusOwner();
    while ((w != null) && !(w instanceof TWindow))
      w = w.getParent();
    return (TWindow)w;
  }
  public TWindow getActiveWindow() {
    TComponent w = getFocusOwner();
    while ((w != null) && !(w instanceof TFrame))
      w = w.getParent();
    return (TWindow)w;
  }
  
  public void requestFocus(TComponent comp) {
    if ((comp != null) && (!comp.isFocusable() || !comp.isShowing() || !comp.isEnabled())) return;
    if (componentKeyFocus == comp) return;
    if (componentKeyFocus != null) componentKeyFocus.handleFocusLost();
    componentKeyFocus = comp;
    if (componentKeyFocus != null) componentKeyFocus.handleFocusGained();
  }
  
  public void mouseEvent(MouseEvent e) {
    int id = e.getID();
    // general mouse handling
    switch (e.getID()) {
      case MouseEvent.MOUSE_PRESSED:
      case MouseEvent.MOUSE_RELEASED:
        componentMouseClicked = null;
        // if this click removes fragile windows or keyboard focus we should not pass it on
        if (checkFragileWindows(app.mouseX, app.mouseY))
          e.consume();
        if ((componentKeyFocus != null) && (componentAtMouse == null)) {
          requestFocus(null); e.consume(); }
        break;
      case MouseEvent.MOUSE_MOVED:
      case MouseEvent.MOUSE_DRAGGED:
      case MouseEvent.MOUSE_ENTERED:
        componentAtMouseValid = false;  // re-evaluate component at mouse in next pre()-call
        // many mouse moved/dragged events might be queued, so we use this delayed re-evaluation
        // to minimize performance overhead
        break;
      case MouseEvent.MOUSE_EXITED:
        componentAtMouseValid = false;
        app.mouseX = app.mouseY = -10000;  // invalidate mouse position
        break;
    }
    // event forwarding to components
    if (e.isConsumed())
      return;
    if ((componentAtMouse != null) && componentAtMouse.isEnabled()) {
      if (e.getID() == MouseEvent.MOUSE_PRESSED)
        componentMouseClicked = componentAtMouse;  // remember the component which was clicked
      componentAtMouse.handleMouseEvent(e);  // forward mouse event to component under mouse
    }
    if ((componentMouseClicked != null) && (componentMouseClicked != componentAtMouse)) {
      componentMouseClicked.handleMouseEvent(e);  // forward event to component which the last MOUSE_PRESSED has been on
      e.consume();  // make sure the event counts as consumed
    }
  }
  
  public void keyEvent(KeyEvent e) {
    if (componentKeyFocus != null)  // give precedence to focused component
      componentKeyFocus.handleKeyEvent(e);
    for (int i = 0; i < keyEventComponents.size(); i++) {
      TComponent comp = keyEventComponents.get(i);
      if (comp.isEnabled())
        comp.handleKeyEvent(e);
      if (e.isConsumed()) break;  // skip sending it to all other components if the event was consumed
    }
    if (app.key == PApplet.ESC) app.key = 0;  // this makes sure PApplet.handleKeyEvent doesn't kill the application on VK_ESCAPE
    // FIXME: the above line could confuse key event handlers called after this one which read PApplet.key
  }
  
  public void registerForKeyEvents(TComponent comp) { keyEventComponents.add(comp); }
  public void unregisterFromKeyEvents(TComponent comp) { keyEventComponents.remove(comp); }
  
  public void size(int width, int height) {
    setBounds(0, 0, width, height);
    invalidateAll();
  }
  
  public void dispose() {}
}
