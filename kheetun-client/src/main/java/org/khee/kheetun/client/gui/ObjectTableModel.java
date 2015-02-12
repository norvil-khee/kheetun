package org.khee.kheetun.client.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ObjectTableModel<T> extends AbstractTableModel {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    static final long serialVersionUID = 42;
    
    private Vector<String>      columnNames;
    private Vector<Class<?>>    columnClasses;
    private Vector<Method>      columnGetters;
    private Vector<Method>      columnSetters;
    private Vector<T>           data;
    private Vector<Boolean>     editable;
    
    public ObjectTableModel( Class<T> base ) {
        
        data            = new Vector<T>();
        columnNames     = new Vector<String>();
        columnClasses   = new Vector<Class<?>>();
        columnGetters   = new Vector<Method>();
        columnSetters   = new Vector<Method>();
        editable        = new Vector<Boolean>();
        
        
        Field[] fields;

        if ( base.isAnnotationPresent( TableModelAttributeOrder.class ) ) {
            
            ArrayList<Field> sorted = new ArrayList<Field>();
            
            for( String field : base.getAnnotation( TableModelAttributeOrder.class ).order() ) {
                try {
                    sorted.add( base.getDeclaredField( field ) );
                } catch( NoSuchFieldException e ) {
                    
                    logger.fatal( "Field " + field + " given in sort order for TableModelAttributeOrder, but not present" );
                    System.exit( 1 );
                } catch( Exception e ) {
                    
                    logger.error( e.getMessage() );
                    System.exit( 1 );
                }
            }
            
            fields = sorted.toArray( new Field[sorted.size()] );
            
        } else {
            fields = base.getDeclaredFields();
        }
        
        for( Field field : fields ) {
            
            logger.debug( "Handling field: " + field.getName() );
            
            if( field.isAnnotationPresent( TableModelAttribute.class ) ) {
                TableModelAttribute annotation = field.getAnnotation( TableModelAttribute.class );
                
                String getterName = "get" + Character.toUpperCase( field.getName().charAt( 0 ) ) + field.getName().substring( 1 ); 
                String setterName = "set" + Character.toUpperCase( field.getName().charAt( 0 ) ) + field.getName().substring( 1 ); 
                
                try {
                    Method getter = base.getDeclaredMethod( getterName );
                    columnGetters.add( getter );
                } catch ( NoSuchMethodException e ) {
                    
                    logger.fatal( "Field " + field.getName() + " in class " + base.getCanonicalName() + " is missing the getter method " + getterName );
                    System.exit( 1 );
                    
                } catch ( Exception e ) {
                    logger.error( e.getMessage() );
                }
                
                try {
                    Method setter = base.getDeclaredMethod( setterName, field.getType() );
                    columnSetters.add( setter );
                } catch ( NoSuchMethodException e ) {
                    
                    logger.fatal( "Field " + field.getName() + " in class " + base.getCanonicalName() + " is missing the setter method " + setterName + "( "  + field.getType().toString() + " )");
                    System.exit( 1 );
                    
                } catch ( Exception e ) {
                    logger.error( e.getMessage() );
                }

                columnClasses.add( field.getType() );
                columnNames.add( annotation.name().equals( "" ) ? field.getName() : annotation.name() );
                editable.add( annotation.editable() );
            }
        }
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        
        Object value = null;

        T object = data.get( rowIndex );
        Method method = columnGetters.get( columnIndex );
        
        try {
            value = method.invoke( object, (Object[])null ); 
        } catch ( Exception e ) {
            logger.error( e.getMessage() );
        }
        
        return value; 
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        T object = data.get( rowIndex );
        Method method = columnSetters.get( columnIndex );
        
        try {
            method.invoke( object, aValue ); 
        } catch ( Exception e ) {
            logger.error( e.getMessage() );
        }
        
        fireTableCellUpdated( rowIndex, columnIndex );
    }
    
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        
        return editable.get( columnIndex );
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses.get( columnIndex );
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames.get( column );
    }
    
    public void addRow( T object ) {
        data.add( object );
        fireTableDataChanged();
    }
    
    public void removeRow( T object ) {
        data.remove( object );
        fireTableDataChanged();
    }
    
    public void addAll( Vector<T> list ) {
        data.addAll( list );
        fireTableDataChanged();
    }
    
    public void addAll( ArrayList<T> list ) {
        data.addAll( list );
        fireTableDataChanged();
    }
    
    public void clear() {
        data.clear();
        fireTableDataChanged();
    }
    
    public T get( int rowIndex ) {
        
        if ( rowIndex == -1 ) {
            return null;
        }
        return data.get( rowIndex );
    }
    
    public int size() {
        return data.size();
    }
    
}
