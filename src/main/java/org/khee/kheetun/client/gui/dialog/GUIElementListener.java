package org.khee.kheetun.client.gui.dialog;

public interface GUIElementListener {
    
    public void guiElementBeginEdit( GUIElement element );
    public void guiElementEndEdit( GUIElement element, boolean confirm );
}
