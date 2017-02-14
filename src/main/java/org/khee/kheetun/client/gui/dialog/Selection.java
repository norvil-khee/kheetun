package org.khee.kheetun.client.gui.dialog;

import java.util.ArrayList;

import org.khee.kheetun.client.config.Base;
import org.khee.kheetun.client.config.Forward;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;

public class Selection {
    
    private static Selection instance       = new Selection();
    
    private Forward                 selectedForward     = null;
    private Tunnel                  selectedTunnel      = null;
    private Profile                 selectedProfile     = null;
    private ConfigPanelRow<Forward> selectedRowForward  = null;
    private ConfigPanelRow<Tunnel>  selectedRowTunnel   = null;
    private ConfigPanelRow<Profile> selectedRowProfile  = null;
    
    private ArrayList<SelectionListener> listeners = new ArrayList<SelectionListener>();
    
    public Selection() {
    }
    
    public static Selection getInstance() {
        
        return Selection.instance;
    }
    
    public void clearSelection() {
        
        if ( this.selectedRowProfile != null ) {
            this.selectedRowProfile.unselect();
        }
        this.selectedRowProfile = null;
        this.selectedProfile = null;
        
        if ( this.selectedRowTunnel != null ) {
            this.selectedRowTunnel.unselect();
        }
        this.selectedRowTunnel = null;
        this.selectedTunnel = null;
        
        if ( this.selectedRowForward != null ) {
            this.selectedRowForward.unselect();
        }
        this.selectedRowForward = null;
        this.selectedForward = null;

        for ( SelectionListener listener : this.listeners ) {
            
            listener.selectionChanged( Profile.class, null );
            listener.selectionChanged( Tunnel.class, null );
            listener.selectionChanged( Forward.class, null );
        }
    }
    
    @SuppressWarnings("unchecked")
    public void setSelected( Class<? extends Base> c, ConfigPanelRow<? extends Base> row ) {
        
        Base object = null;
        
        if ( row != null ) {
            object = row.getObject();
        }
        
        if ( c == Forward.class && this.selectedForward != object ) {
            
            this.selectedForward = (Forward)object;
            
            if ( this.selectedRowForward != null ) {
                this.selectedRowForward.unselect();
            }
            this.selectedRowForward = (ConfigPanelRow<Forward>)row;
            if ( this.selectedRowForward != null ) {
                this.selectedRowForward.select();
            }
            
            for ( SelectionListener listener : this.listeners ) {
                
                listener.selectionChanged( Forward.class, this.selectedForward );
            }
            
        } else if ( c == Tunnel.class && this.selectedTunnel != object ) {
            
            this.selectedTunnel = (Tunnel)object;
            this.selectedForward = null;
            
            if ( this.selectedRowTunnel != null ) {
                this.selectedRowTunnel.unselect();
            }
            this.selectedRowTunnel = (ConfigPanelRow<Tunnel>)row;
            if ( this.selectedRowTunnel != null ) {
                this.selectedRowTunnel.select();
            }
            
            for ( SelectionListener listener : this.listeners ) {
                
                listener.selectionChanged( Tunnel.class, this.selectedTunnel );
                listener.selectionChanged( Forward.class, this.selectedForward );
            }
        
        } else if ( c == Profile.class && this.selectedProfile != object ) {
            
            this.selectedProfile = (Profile)object;
            this.selectedTunnel = null;
            this.selectedForward = null;
            
            if ( this.selectedRowProfile != null ) {
                this.selectedRowProfile.unselect();
            }
            this.selectedRowProfile = (ConfigPanelRow<Profile>)row;
            if ( this.selectedRowProfile != null ) {
                this.selectedRowProfile.select();
            }
            
            for ( SelectionListener listener : this.listeners ) {
                
                listener.selectionChanged( Profile.class, this.selectedProfile );
                listener.selectionChanged( Tunnel.class, this.selectedTunnel );
                listener.selectionChanged( Forward.class, this.selectedForward );
            }
        }
    }
    
    public boolean isSelected( Base object ) {
        
        return
                ( object instanceof Forward && object == this.selectedForward )
             || ( object instanceof Tunnel && object == this.selectedTunnel )
             || ( object instanceof Profile && object == this.selectedProfile );
    }

    public Forward getSelectedForward() {
        return selectedForward;
    }

    public Tunnel getSelectedTunnel() {
        return selectedTunnel;
    }

    public Profile getSelectedProfile() {
        return selectedProfile;
    }

    public void addSelectionListener( SelectionListener listener ) {
        
        this.listeners.add( listener );
    }

}
