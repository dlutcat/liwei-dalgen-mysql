/*
 * Copyright (c) 2001, Aslak HellesÃ¸y, BEKK Consulting
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of BEKK Consulting nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package middlegen;

import java.sql.*;

/**
 * Describe what this class does
 *
 * @author Aslak Hellesøy
 * @created 21. april 2002
 * @todo-javadoc Write javadocs
 */
public class StandardDatabase implements Database {
   /**
    * @todo-javadoc Describe the column
    */
   private final String _driver;
   /**
    * @todo-javadoc Describe the column
    */
   private final String _url;
   /**
    * @todo-javadoc Describe the column
    */
   private final String _userName;
   /**
    * @todo-javadoc Describe the column
    */
   private final String _password;

   /** Get static reference to Log4J Logger */
   private static org.apache.log4j.Category _log = org.apache.log4j.Category.getInstance(StandardDatabase.class.getName());


   /**
    * Describe what the StandardDatabase constructor does
    *
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for constructor
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for method parameter
    * @param driver Describe what the parameter does
    * @param url Describe what the parameter does
    * @param userName Describe what the parameter does
    * @param password Describe what the parameter does
    */
   public StandardDatabase(String driver, String url, String userName, String password) {
      _driver = driver;
      _url = url;
      _userName = userName;
      _password = password;
   }


   /**
    * Gets the Connection attribute of the StandardDatabase object
    *
    * @todo-javadoc Write javadocs for exception
    * @return The Connection value
    * @exception MiddlegenException Describe the exception
    */
   public Connection getConnection() throws MiddlegenException {
      try {
         _log.debug("Database URL=" + _url);
         _log.debug("User Name=" + _userName);
         _log.debug("Password=" + _password);
         Class.forName(_driver).newInstance();
         Connection connection = DriverManager.getConnection(_url, _userName, _password);
         return connection;
      } catch (ClassNotFoundException e) {
         throw new MiddlegenException("Couldn't load JDBC driver " + _driver + ". Make sure it's on your classpath.");
      } catch (InstantiationException e) {
         throw new MiddlegenException("Couldn't instantiate JDBC driver " + _driver + ". That's pretty bad news for your driver.");
      } catch (IllegalAccessException e) {
         throw new MiddlegenException("Couldn't instantiate JDBC driver " + _driver + ". That's pretty bad news for your driver.");
      } catch (SQLException e) {
         throw new MiddlegenException("Couldn't connect to database: " + e.getMessage());
      }
   }
}
