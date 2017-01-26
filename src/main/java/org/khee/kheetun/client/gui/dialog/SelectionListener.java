package org.khee.kheetun.client.gui.dialog;

import org.khee.kheetun.client.config.Base;

public interface SelectionListener {
    
    public void selectionChanged( Class<? extends Base> c, Base object );
}
