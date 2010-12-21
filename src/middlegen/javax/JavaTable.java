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
package middlegen.javax;

import java.util.Collection;
import java.util.Iterator;
import java.text.MessageFormat;
import middlegen.*;

/**
 * Baseclass for Table decorators that map to java types.
 *
 * @author <a href="mailto:aslak.hellesoy@bekk.no">Aslak Hellesøy</a>
 * @created 3. oktober 2001
 * @version $Id: JavaTable.java,v 1.1 2005/10/25 14:59:22 lusu Exp $
 */
public class JavaTable extends TableDecorator {
   /**
    * @todo-javadoc Describe the column
    */
   private String _baseClassName;

   /**
    * @todo-javadoc Describe the field
    */
   private String _package;

   /**
    * @todo-javadoc Describe the field
    */
   private String _sequenceName = null;

   /** Get static reference to Log4J Logger */
   private static org.apache.log4j.Category _log = org.apache.log4j.Category.getInstance(JavaTable.class.getName());


   /**
    * Describe what the JavaTable constructor does
    *
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for constructor
    * @todo-javadoc Write javadocs for method parameter
    * @param subject Describe what the parameter does
    */
   public JavaTable(Table subject) {
      super(subject);
      _sequenceName = getTableElement().getSequenceName();
      if (_sequenceName != null) {
         _sequenceName = _sequenceName.trim().toUpperCase();
      }

   }


   /**
    * Sets the Package attribute of the JavaTable object
    *
    * @todo-javadoc Write javadocs for return value
    * @param pakkage The new Package value
    * @return Describe the return value
    */
   public String setPackage(String pakkage) {
      return _package = pakkage;
   }


   /**
    * Sets the Plugin attribute of the JavaTable object
    *
    * @param plugin The new Plugin value
    */
   public void setPlugin(Plugin plugin) {
      super.setPlugin(plugin);
      JavaPlugin javaPlugin = (JavaPlugin)plugin;
      String packageName = javaPlugin.getPackage();
      if (packageName.indexOf("{0}") != -1) {
         // Parameterised package name. Replace {0} with lowercase table name.
         packageName = MessageFormat.format(packageName, new String[]{getName().toLowerCase()});
      }
      setPackage(packageName);
   }


   /**
    * Gets the SequenceName attribute of the JavaTable object. If a sequence
    * name was not supplied the sequenceName will be assumed to be <tt>
    * &lt;SQL-tablename&gt; + "_SEQ"</tt> .
    *
    * @return The SequenceName value
    */
   public String getSequenceName() {
      if (_sequenceName == null || _sequenceName.equals("")) {
         _sequenceName = getSqlName().toUpperCase() + "_SEQ";
      }
      return _sequenceName;
   }


   /**
    * Gets the BaseClassName attribute of the JavaTable object
    *
    * @return The BaseClassName value
    */
   public String getBaseClassName() {
      return _baseClassName;
   }


   /**
    * Gets the DestinationClassName attribute of the JavaTable object
    *
    * @return The DestinationClassName value
    */
   public String getDestinationClassName() {
      return getBaseClassName();
   }


   /**
    * Gets the SimplePkClassName attribute of the Entity11Plugin object
    *
    * @todo-javadoc Write javadocs for method parameter
    * @return The SimplePkClassName value
    */
   public String getSimplePkClassName() {
      JavaColumn pkColumn = (JavaColumn)getPkColumn();
      if (pkColumn != null) {
         return pkColumn.getJavaType();
      }
      else {
         return null;
      }
   }


   /**
    * Gets the QualifiedBaseClassName attribute of the JavaTable object
    *
    * @return The QualifiedBaseClassName value
    */
   public String getQualifiedBaseClassName() {
      String pakkage = ((JavaPlugin)getPlugin()).getPackage();
      return Util.getQualifiedClassName(pakkage, getBaseClassName());
   }


   /**
    * Returns the variable name to use for the relationshipRole. This method
    * will attempt to pluralise or singulrise the variable name. If the
    * relationshipRole's target represents a many side, we'll try to pluralise;
    * otherwise we'll singularise. This is based on simple English pluralisation
    * rules.
    *
    * @todo-javadoc Write javadocs for method parameter
    * @param relationshipRole Describe what the parameter does
    * @return The VariableName value
    */
   public String getVariableName(RelationshipRole relationshipRole) {
      if (relationshipRole.getOrigin(getPlugin()) != this) {
         throw new IllegalArgumentException("The relationshipRole's origin must be " + getSqlName() + " , but was " + relationshipRole.getOrigin().getSqlName());
      }
      String result;
      JavaTable target = (JavaTable)relationshipRole.getTarget(getPlugin());
      if (relationshipRole.isTargetMany()) {
         result = target.getPluralisedVariableName();
      }
      else {
         result = target.getSingularisedVariableName();
      }
      if (relationshipRole.getSuffix() != null) {
         result += relationshipRole.getSuffix();
      }
      return result;
   }


   /**
    * @return the name of the sub directory of the original directory
    */
   public String getSubDirPath() {
      return getPackage().replace('.', '/');
   }


   /**
    * Gets the ClassName attribute of the JavaTable object
    *
    * @todo-javadoc Write javadocs for method parameter
    * @param relationshipRole Describe what the parameter does
    * @return The ClassName value
    */
   public String getClassName(RelationshipRole relationshipRole) {
      if (relationshipRole.getOrigin(getPlugin()) != this) {
         throw new IllegalArgumentException("The relationshipRole's origin must be " + getSqlName() + " , but was " + relationshipRole.getOrigin().getSqlName());
      }
      String result;
      JavaTable target = (JavaTable)relationshipRole.getTarget(getPlugin());
      if (relationshipRole.isTargetMany()) {
         result = target.getManyClassName();
      }
      else {
         result = target.getQualifiedDestinationClassName();
      }
      return result;
   }


   /**
    * Gets the Package attribute of the JavaTable object
    *
    * @return The Package value
    */
   public String getPackage() {
      return _package;
   }


   /**
    * Gets the Java signature corresponding to the columns.
    *
    * @param columns the columns to use in the signature
    * @return The Signature value
    * @includeType whether or not to put the type in the signature
    * @returns a String that can be used as a method/constructor signature
    */
   public String getSignature(Collection columns) {
      return delimit(columns, true);
   }


   /**
    * Gets the Java signature corresponding to the columns.
    *
    * @param columns the columns to use in the signature
    * @return The Signature value
    * @includeType whether or not to put the type in the signature
    * @returns a String that can be used as a method/constructor signature
    */
   public String getParameters(Collection columns) {
      return delimit(columns, false);
   }


   /**
    * Gets the Java signature corresponding to the relationship roles' targets
    *
    * @param relationshipRoles the roles for which the signature is to be
    *      created
    * @return The RelationSignature value
    */
   public String getRelationSignature(Collection relationshipRoles) {
      StringBuffer sb = new StringBuffer();
      Iterator i = relationshipRoles.iterator();
      while (i.hasNext()) {
         RelationshipRole role = (RelationshipRole)i.next();
         JavaTable target = null;
         JavaTable origin = null;
         Table t = role.getTarget(getPlugin());
         Table o = role.getOrigin(getPlugin());
         try {
            target = (JavaTable)t;
            origin = (JavaTable)o;
         } catch (ClassCastException e) {
            // We don't need a separate check for target and origin; They will always be of same class.
            throw new IllegalStateException(getPlugin().getClass().getName() + " must override getTableDecoratorClass() and return a class which is " + JavaTable.class.getName() + " (or a subclass). It was " + t.getClass().getName());
         }
         if (sb.length() != 0) {
            sb.append(", ");
         }
         String targetType = target.getDestinationClassName();
         String targetName = origin.getVariableName(role);
         sb.append(targetType).append(" ").append(targetName);
      }
      return sb.toString();
   }


   /**
    * Gets the ColumnAndRelationSignature attribute of the JavaTable object
    *
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for method parameter
    * @param columns Describe what the parameter does
    * @param relationshipRoles Describe what the parameter does
    * @return The ColumnAndRelationSignature value
    */
   public String getColumnAndRelationSignature(Collection columns, Collection relationshipRoles) {
      String columnSignature = getSignature(columns);
      String relationSignature = getRelationSignature(relationshipRoles);
      if (!columnSignature.equals("") && !relationSignature.equals("")) {
         // both are non-empty strings
         return columnSignature + ", " + relationSignature;
      }
      else {
         // at least one of them is an empty string. just concatenate
         return columnSignature + relationSignature;
      }
   }


   /**
    * Gets the CapitalisedVariableName attribute of the JavaTable object
    *
    * @todo-javadoc Write javadocs for method parameter
    * @param relationshipRole Describe what the parameter does
    * @return The CapitalisedVariableName value
    */
   public String getCapitalisedVariableName(RelationshipRole relationshipRole) {
      return Util.capitalise(getVariableName(relationshipRole));
   }


   /**
    * Gets the GetterName attribute of the JavaTable object
    *
    * @todo-javadoc Write javadocs for method parameter
    * @param relationshipRole Describe what the parameter does
    * @return The GetterName value
    */
   public String getGetterName(RelationshipRole relationshipRole) {
      return "get" + getCapitalisedVariableName(relationshipRole);
   }


   /**
    * Gets the SetterName attribute of the JavaTable object
    *
    * @todo-javadoc Write javadocs for method parameter
    * @param relationshipRole Describe what the parameter does
    * @return The SetterName value
    */
   public String getSetterName(RelationshipRole relationshipRole) {
      return "set" + getCapitalisedVariableName(relationshipRole);
   }


   /**
    * Gets the ReplaceName attribute of the JavaTable object
    *
    * @return The ReplaceName value
    */
   public String getReplaceName() {
      return getBaseClassName();
   }


   /**
    * Gets the QualifiedDestinationClassName attribute of the JavaTable object
    *
    * @return The QualifiedDestinationClassName value
    */
   public String getQualifiedDestinationClassName() {
      return Util.getQualifiedClassName(
            getPackage(),
            getDestinationClassName()
            );
   }


   /**
    * Gets the Java signature corresponding to the columns.
    *
    * @todo-javadoc Write javadocs for method parameter
    * @param columns the columns to use in the signature
    * @param includeType Describe what the parameter does
    * @return The Signature value
    * @includeType whether or not to put the type in the signature
    * @returns a String that can be used as a method/constructor signature or
    *      invocation parameters
    */
   public String delimit(Collection columns, boolean includeType) {
      StringBuffer sb = new StringBuffer();
      Iterator i = columns.iterator();
      while (i.hasNext()) {
         JavaColumn column = null;
         Object c = i.next();
         try {
            column = (JavaColumn)c;
         } catch (ClassCastException e) {
            throw new IllegalStateException(getPlugin().getClass().getName() + " must override getColumnDecoratorClass() and return a class which is " + JavaColumn.class.getName() + " (or a subclass). It was " + c.getClass().getName());
         }
         if (sb.length() != 0) {
            sb.append(", ");
         }
         if (includeType) {
            String columnType = column.getJavaType();
            sb.append(columnType).append(" ");
         }
         String columnName = column.getVariableName();
         sb.append(columnName);
      }
      return sb.toString();
   }


   /**
    * Sets the JavaType attribute of the Column object
    *
    * @param baseClassName The new BaseClassName value
    */
   protected void setBaseClassName(String baseClassName) {
      setPrefsValue("base-class-name", baseClassName);
      _baseClassName = baseClassName;
   }


   /**
    * Gets the ManyClassName attribute of the JavaTable object
    *
    * @return The ManyClassName value
    */
   protected String getManyClassName() {
      return "java.util.Collection";
   }


   /**
    * Describe what the method does
    *
    * @todo-javadoc Write javadocs for method
    */
   protected void init() {
      super.init();

      /*
       *  There are 3 ways to get the class name for a table. Attempts
       *  are done in the following order:
       *
       *  1) Look in prefs
       *  2) Use the singular name for the table (if specified)
       *  3) Use the DbNameConverter
       */
      String prefsBaseClassName = getPrefsValue("base-class-name");

      String computedBaseClassName;
      if (getTableElement().getSingular() != null) {
         computedBaseClassName = getTableElement().getSingular();
         computedBaseClassName = Util.capitalise(computedBaseClassName);
      }
      else {
         computedBaseClassName = DbNameConverter.getInstance().tableNameToVariableName(getName());
         computedBaseClassName = Util.singularise(computedBaseClassName);
      }
      String suffix = ((JavaPlugin)getPlugin()).getSuffix();
      computedBaseClassName += suffix;

      if (prefsBaseClassName != null && !prefsBaseClassName.equals(computedBaseClassName)) {
         _log.warn(
               "WARNING (" + getPlugin().getName() + "): " +
               "Your prefs file indicates that the base class name for table " + getSqlName() +
               " should be " + prefsBaseClassName + ", but according to your plugin settings " +
               "it should be " + computedBaseClassName + ". Middlegen will use " + prefsBaseClassName + ". " +
               "If you want it to be the other way around, please edit or delete your prefs file, " +
               "or modify the name in the gui."
               );
      }
      if (prefsBaseClassName != null) {
         setBaseClassName(prefsBaseClassName);
      }
      else {
         setBaseClassName(computedBaseClassName);
      }

      // some sanity:
//		if (getPkColumn() != null && getSimplePkClassName() == null) {
//			throw new IllegalStateException("The simple PK class name shouldn't be null when a single pkColumn exists! PkColumn:" + getPkColumn() + ". " + middlegen.Middlegen.BUGREPORT);
//		}
   }


   /**
    * Gets the PluralisedVariableName attribute of the JavaTable object
    *
    * @return The PluralisedVariableName value
    */
   private String getPluralisedVariableName() {
      if (getTableElement().getPlural() != null) {
         return getTableElement().getPlural();
      }
      else {
         return Util.pluralise(getVariableName());
      }
   }


   /**
    * Gets the SingularisedVariableName attribute of the JavaTable object
    *
    * @return The SingularisedVariableName value
    */
   private String getSingularisedVariableName() {
      if (getTableElement().getSingular() != null) {
         return getTableElement().getSingular();
      }
      else {
         return Util.singularise(getVariableName());
      }
   }


   /**
    * Gets the variable name. This method is intentionally private. The method
    * that takes a RelationshipRole as argument should be used from templates.
    *
    * @return The VariableName value
    */
   private String getVariableName() {
      return Util.decapitalise(getBaseClassName());
   }


   /**
    * Gets the OneClassName attribute of the JavaTable object
    *
    * @return The OneClassName value
    */
   private final String getOneClassName() {
      return getQualifiedDestinationClassName();
   }
}

