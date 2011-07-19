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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.datatransfer.*;

public class TTextField extends TComponent implements ClipboardOwner {
  protected String text = "";  // the actual content of the text field
  protected String strEmpty = "";  // text shown (in gray) when the text field has no content and is not focused
  protected int caretPos = 0, selectPos = -1;
  protected float caretPeriod = 3;
  protected float caretAlpha = 0;
  protected int caretPhase = 1;
  protected float scrollOffset = 0, targetScrollOffset = 0;
  protected String command = null;
  protected int hotKeyCode = 0;
  protected int hotKeyMods = 0;
  protected PGraphics img = null;

  public static final int ALIGN_LEFT = PApplet.LEFT;
  public static final int ALIGN_CENTER = PApplet.CENTER;
  public static final int ALIGN_RIGHT = PApplet.RIGHT;
  protected int align = ALIGN_LEFT;

  public TTextField(TransparentGUI gui) { this(gui, null); }
  public TTextField(TransparentGUI gui, String cmd) {
    super(gui); clickable = true;
    gui.registerForKeyEvents(this);
    setActionCommand(cmd);
  }

  public void setText(String s) { text = s; caretPos = s.length(); }
  public String getText() { return text; }

  public void setEmptyText(String s) { strEmpty = s; }
  public String getEmptyText() { return strEmpty; }

  public String getSelectedText() { return text.substring(PApplet.min(selectPos, caretPos), PApplet.max(selectPos, caretPos)); }
  public void clearSelection() { selectPos = -1; }
  public void setSelection(int i0, int i1) {
    selectPos = PApplet.max(0, PApplet.min(text.length(), i0));
    caretPos = PApplet.max(0, PApplet.min(text.length(), i1));
  }

  public String getActionCommand() { return (command != null) ? command : "TTextField"; }
  public void setActionCommand(String s) { command = s; }

  public boolean isFocusable() { return true; }

  public int getAlignment() { return align; }
  //public void setAlignment(int align) { this.align = align; }  // FIXME: draw() only handles ALIGN_LEFT correctly at the moment

  public TComponent.Dimension getMinimumSize() {
    gui.app.g.textFont(getFont());
    return new TComponent.Dimension(200, gui.app.g.textAscent() + 1.5f*gui.app.g.textDescent());
  }

  public void setHotKey(int c) { setHotKey(c, 0); }
  public void setHotKey(int c, int mods) {
    gui.unregisterFromKeyEvents(this);
    hotKeyCode = c;
    hotKeyMods = mods;
    if (c > 0) gui.registerForKeyEvents(this);
  }

  public void handleKeyEvent(KeyEvent e) {
    super.handleKeyEvent(e);
    if (e.isConsumed()) return;
    if (!isFocusOwner()) {  // see if hot key was pressed
      if (e.isConsumed() || (e.getID() != KeyEvent.KEY_PRESSED)) return;
      if ((e.getKeyCode() == hotKeyCode) && (e.getModifiers() == hotKeyMods)) {
        handleMouseClicked();
        e.consume();
      } else
        return;  // don't process events not handled here if we don't have keyboard focus
    }
    if (e.getID() == KeyEvent.KEY_PRESSED) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
          if (e.isShiftDown() && (selectPos == -1))
            selectPos = caretPos;
          if (!e.isShiftDown() && (selectPos != -1)) {
            caretPos = PApplet.min(selectPos, caretPos); selectPos = -1;  // clear selection and move cursor to the left of previously selected material
          } else if (e.isControlDown() || e.isMetaDown()) {
            caretPos = 0;
          } else if (e.isAltDown()) {
            int i = text.lastIndexOf(' ', caretPos - 2);
            caretPos = (i > -1) ? i + 1 : 0;
          } else
            caretPos = PApplet.max(0, caretPos - 1);
          caretPeriod = PApplet.max(0.25f, caretPeriod - 0.1f);
          e.consume(); break;
        case KeyEvent.VK_RIGHT:
          if (e.isShiftDown() && (selectPos == -1))
            selectPos = caretPos;
          if (!e.isShiftDown() && (selectPos != -1)) {
            caretPos = PApplet.max(selectPos, caretPos); selectPos = -1;  // clear selection but leave cursor at the right of previously selected material
          } else if (e.isControlDown() || e.isMetaDown()) {
            caretPos = text.length();
          } else if (e.isAltDown()) {
            int i = text.indexOf(' ', caretPos + 1);
            caretPos = (i > -1) ? i : text.length();
          } else
            caretPos = PApplet.min(text.length(), caretPos + 1);
          caretPeriod = PApplet.max(0.25f, caretPeriod - 0.1f);
          e.consume(); break;
        case KeyEvent.VK_ESCAPE:  // erase text and drop focus
          text = ""; caretPos = 0; selectPos = -1;
          gui.fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand() + "##textChanged"));
          gui.requestFocus(null); break;
        case KeyEvent.VK_ENTER:  // drop focus, leave text and notify anyone who's interested
          gui.fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand() + "##enterKeyPressed"));
          gui.requestFocus(null); break;
        case KeyEvent.VK_A:  // select all
          if (e.isMetaDown() || e.isControlDown()) { selectPos = 0; caretPos = text.length(); }
          e.consume(); break;
        case KeyEvent.VK_C:  // copy to clipboard
          if ((e.isMetaDown() || e.isControlDown()) && (selectPos != -1)) copySelectionToClipboard();
          e.consume(); break;
        case KeyEvent.VK_X:  // cut to clipboard
          if ((e.isMetaDown() || e.isControlDown()) && (selectPos != -1)) { copySelectionToClipboard(); deleteSelection(); }
          e.consume(); break;
        case KeyEvent.VK_V:  // paste from clipboard
          if (e.isMetaDown() || e.isControlDown()) insertFromClipboard();
          e.consume(); break;
      }
    } else if (e.getID() == KeyEvent.KEY_TYPED) {
      caretPeriod = PApplet.max(0.25f, caretPeriod - 0.1f);
      switch (e.getKeyChar()) {
        case KeyEvent.VK_BACK_SPACE:
          if (selectPos != -1) deleteSelection();
          else if (caretPos > 0) text = text.substring(0, caretPos - 1) + text.substring(caretPos--);
          gui.fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand() + "##textChanged")); break;
        case KeyEvent.VK_DELETE:
          if (selectPos != -1) deleteSelection();
          else if (caretPos < text.length()) text = text.substring(0, caretPos) + text.substring(caretPos + 1);
          gui.fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand() + "##textChanged")); break;
        default:
          if (e.isActionKey() || e.isControlDown() || e.isMetaDown()) break;
          if (selectPos != -1) deleteSelection();
          text = text.substring(0, caretPos) + e.getKeyChar() + text.substring(caretPos); caretPos++;
          gui.fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand() + "##textChanged")); break;
      }
    }
    if (e.getModifiers() != PApplet.MENU_SHORTCUT)
      e.consume();  // don't consume events with Ctrl/Cmd because they should still trigger other buttons
  }

  public void deleteSelection() {
    int i0 = PApplet.min(selectPos, caretPos);
    int i1 = PApplet.max(selectPos, caretPos);
    text = text.substring(0, i0) + text.substring(i1);
    caretPos = i0; selectPos = -1;
  }

  public void lostOwnership(Clipboard clipboard, Transferable contents) {}  // to implement ClipboardOwner

  public void copySelectionToClipboard() {
    int i0 = PApplet.min(selectPos, caretPos);
    int i1 = PApplet.max(selectPos, caretPos);
    gui.app.getToolkit().getSystemClipboard().setContents(
      new java.awt.datatransfer.StringSelection(text.substring(i0, i1)), this);
  }

  public void insertFromClipboard() {
    try {
      String clip = (String)gui.app.getToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor);
      clip = clip.replace('\n', ' ');
      if (selectPos != -1) deleteSelection();
      text = text.substring(0, caretPos) + clip + text.substring(caretPos);
      caretPos += clip.length();
    } catch (UnsupportedFlavorException e) { /* ignore: if we can't get text, we don't want it anyway */ }
      catch (java.io.IOException e) { /* ignore: if it's no longer available, then that's just bad luck... */ }
  }

  // TODO: handle mouse-based caret positioning and text selection
  public void handleMouseClicked() { if (!isFocusOwner()) gui.requestFocus(this); caretAlpha = 1; }

  public void draw(PGraphics g) {
    super.draw(g);
    //
    if ((img == null) || (img.width != PApplet.ceil(bounds.width)+1) || (img.height != PApplet.ceil(bounds.height)+1))
      img = gui.app.createGraphics(PApplet.ceil(bounds.width)+1, PApplet.ceil(bounds.height)+1, PApplet.JAVA2D);
    img.beginDraw();
    img.smooth();
    img.textFont(getFont());
    img.textAlign(align, g.BASELINE);
    img.background(0, 0);
    //
    float x = padding.left;
    float dxCaret = img.textWidth(text.substring(0, caretPos));
    float dxSelect = (selectPos == -1) ? Float.NaN : img.textWidth(text.substring(0, selectPos));
    float y = bounds.height - padding.bottom - g.textDescent();
    float w = bounds.width - padding.left - padding.right;
    float h = g.textAscent() + g.textDescent();
    if (bounds.height - padding.top - padding.bottom > h)
      y -= (bounds.height - padding.top - padding.bottom - h)/2;
    //
    if (targetScrollOffset + dxCaret < 0) targetScrollOffset -= targetScrollOffset + dxCaret;
    if (targetScrollOffset + dxCaret > w) targetScrollOffset -= targetScrollOffset + dxCaret - w;
    targetScrollOffset = PApplet.max(targetScrollOffset, w - img.textWidth(text));
    targetScrollOffset = PApplet.min(targetScrollOffset, 0);
    scrollOffset += 10*(targetScrollOffset - scrollOffset)*PApplet.min(gui.dt, 1/10.f);
    //
    img.pushMatrix(); img.translate(scrollOffset, 0);
    if (isFocusOwner() && (selectPos != -1)) {
      img.rectMode(g.CORNERS); img.noStroke(); img.fill(200, 0, 0, 127);
      img.rect(x + dxSelect, y - img.textAscent(), x + dxCaret, y + img.textDescent());
    }
    img.noStroke();
    boolean showEmptyText = (text.length() == 0) && !isFocusOwner();
    img.fill(getForeground(), showEmptyText ? 127 : 255);
    img.text(showEmptyText ? strEmpty : text, x, y);
    caretPeriod = PApplet.min(3f, caretPeriod + 0.05f*PApplet.min(gui.dt, 1/3.f));
    if (isFocusOwner()) {  // draw caret
      caretAlpha += caretPhase*PApplet.min(gui.dt, 1/3.f)/(caretPeriod/(caretPhase > 0 ? 3f : 1.5f));
      if (caretAlpha > 1) { caretAlpha = 1; caretPhase = -1; }
      else if (caretAlpha < 0) { caretAlpha = 0; caretPhase = +1; }
      img.stroke(getForeground(), 255*caretAlpha); img.noFill();
      img.line(x + dxCaret, y - img.textAscent(), x + dxCaret, y + img.textDescent());
    }
    img.popMatrix();
    img.endDraw();
    //
    g.image(img, bounds.x, bounds.y);
  }
}