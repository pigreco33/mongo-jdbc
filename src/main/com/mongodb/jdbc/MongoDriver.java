// MongoDriver.java

/**
 *      Copyright (C) 2008 10gen Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.mongodb.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

import com.mongodb.DBAddress;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;

public class MongoDriver implements Driver {

    static final String PREFIX = "mongodb://";
    static final String JDBC_PREFIX = "jdbc:mongodb://";

    public static void main(String[] args) {

	}

    public MongoDriver(){
    }

    public boolean acceptsURL(String url){
        return (url.startsWith( PREFIX )|| url.startsWith(JDBC_PREFIX));
    }

    public Connection connect(String url, Properties info)
        throws SQLException {

    	if (url.startsWith(JDBC_PREFIX))
    	{
    		url =  url.substring(5);
    	}

        if ( url.startsWith( PREFIX ) )
            url = url.substring( PREFIX.length() );
        if ( url.indexOf( "/" ) < 0 )
            throw new MongoSQLException( "bad url: " + url );

        try {
            DBAddress addr = new DBAddress( url );
            String sDBName = addr.getDBName();
            MongoClient pMongoClient;
            if ( info != null && info.size() > 0 )
            {
            	MongoCredential credential = MongoCredential.createMongoCRCredential((String)info.get("user"), sDBName, ((String)info.get("password")).toCharArray());
        		pMongoClient = new MongoClient(addr, Arrays.asList(credential));

            }
            else
            {
        		pMongoClient = new MongoClient(addr);

            }

            return new MongoConnection( pMongoClient.getDB(sDBName));
        }
        catch ( java.net.UnknownHostException uh ){
            throw new MongoSQLException( "bad url: " + uh );
        }
        catch(NoClassDefFoundError p)
        {

        	p.printStackTrace(System.out);
        	throw p;
        }
    }

    public int getMajorVersion(){
        return 0;
    }
    public int getMinorVersion(){
        return 1;
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info){
        throw new UnsupportedOperationException( "getPropertyInfo doesn't work yet" );
    }

    public boolean jdbcCompliant(){
        return false;
    }

    public static void install(){
        // NO-OP, handled in static
    }

    static {
        try {
            DriverManager.registerDriver( new MongoDriver() );
        }
        catch ( SQLException e ){
            throw new RuntimeException( e );
        }
    }
}
