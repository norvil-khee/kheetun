package org.khee.kheetun.client.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class Base {
    
    private static int counter                              = 0;
    private transient HashMap<Field, String>    errors      = new HashMap<Field, String>();
    private transient Integer                   id          = Base.counter++;
    
    public void clearErrors() {
        
        this.errors.clear();
    }
    
    public void addError( String field, String error ) {
        
        if ( error == null ) {
            return;
        }
        
        try {

            this.errors.put( this.getClass().getDeclaredField( field ), error );
            
        } catch ( NoSuchFieldException e ) {
            
            e.printStackTrace();
        }
    }
    
    public HashMap<Field, String> getErrors() {
        
        return this.errors;
    }
    
    public String getError( Field field ) {
        
        return this.errors.get( field );
    }
    
    public ArrayList<String> getReadableErrorList() {
        
        ArrayList<String> readable = new ArrayList<String>();
        
        for ( Field field : this.errors.keySet() ) {
            
            readable.add( field.getName() + ": " + this.errors.get( field ) );
        }
        
        return readable;
    }
    
    public abstract int hashCodeMeta();
    
    public int hashCodeCombined() {
        
        return new HashCodeBuilder().append( this.hashCode() ).append( this.hashCodeMeta() ).hashCode();
    }
    
    public abstract boolean equalsMeta( Object object );
    
    public boolean equalsCombined( Object obj ) {
        
        if ( ! ( obj instanceof Profile ) ) {
            return false;
        }
        
        Base compare = (Base)obj;
        
        return this.equals( compare ) && this.equalsMeta( compare );
    }
    
    public Integer getId() {
        return this.id;
    }
}
