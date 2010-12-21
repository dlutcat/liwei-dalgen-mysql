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
package middlegen.swing;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.*;

import middlegen.*;

/**
 * @author <a href="mailto:aslak.hellesoy@bekk.no">Aslak Hellesøy</a>
 * @created 3. oktober 2001
 * @version $Id: JMiddlegenFrame.java,v 1.1 2005/10/25 14:59:22 lusu Exp $
 */
public class JMiddlegenFrame extends JFrame {
   /**
    * @todo-javadoc Describe the column
    */
   private final JDatabasePanel _databasePanel;
   /**
    * @todo-javadoc Describe the field
    */
   private final JSplitPane _split;

   /**
    * @todo-javadoc Describe the column
    */
   private final Middlegen _middlegen;

   /**
    * This action generates sources
    *
    * @todo refactor the meat to a separate method
    */
   private Action _generateAction =
      new AbstractAction("Generate") {
         public void actionPerformed(ActionEvent evt) {
            try {
               setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               try {
                  _middlegen.writeSource();
               } catch (MiddlegenException e) {
                  error(e);
               }
            } finally {
               setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
         }
      };

   /** Get static reference to Log4J Logger */
   private static org.apache.log4j.Category _log = org.apache.log4j.Category.getInstance(JMiddlegenFrame.class.getName());


   /**
    * The main program for the MiddlemanFrame class
    *
    * @todo describe the command line arguments
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for method parameter
    * @todo-javadoc Write javadocs for method parameter
    * @param middlegen Describe what the parameter does
    * @param title Describe what the parameter does
    */
   public JMiddlegenFrame(Middlegen middlegen, String title) {
      super(title);
      _middlegen = middlegen;
      setKuststoffLF();

      // make panel with label header and tabs in center.
      JLabel header = new JLabel("", JLabel.CENTER);
      JSettingsTabPane settingsTabs = new JSettingsTabPane(middlegen.getPlugins());
      JPanel headerTabs = new JPanel(new BorderLayout());

      headerTabs.add(header, BorderLayout.NORTH);
      // headerTabs.add(settingsTabs, BorderLayout.CENTER);
      JToolBar pluginToolBar = new JToolBar();
      pluginToolBar.add(settingsTabs);
      headerTabs.add(pluginToolBar, BorderLayout.CENTER);

      _databasePanel = new JDatabasePanel(settingsTabs, header);

      JScrollPane scroll = new JScrollPane(_databasePanel);
      _split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, headerTabs);
      _split.setDividerLocation(0.5);
      getContentPane().add(_split, BorderLayout.CENTER);

      JToolBar toolBar = new JToolBar();
      toolBar.add(_generateAction);
      toolBar.addSeparator();
      toolBar.add(new JLabel("CTRL-Click relations to modify their cardinality"));
      toolBar.addSeparator();
      toolBar.add(new JLabel("SHIFT-Click relations to modify their directionality"));
      getContentPane().add(toolBar, BorderLayout.NORTH);

      ImageIcon icon = new ImageIcon(getClass().getResource("m.gif"));
      setIconImage(icon.getImage());

      // dispose Frame on closing, so VM will exit when Ant Task has finished
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

      // I have a dodgy feeling that a Mac would try to exit the VM when the
      // window closes. Try to uncomment this line on a Mac.
      // System.setSecurityManager(new NoExitSecurityManager());

      addWindowListener(
               new WindowAdapter() {

                  public void windowClosing(WindowEvent evt) {
                     // Tell the halted Ant task to resume.
                     synchronized (MiddlegenTask.getLock()) {
                        // Update positions of tables
                        _databasePanel.setPrefs();
                        // Let the ant task to continue
                        MiddlegenTask.getLock().notify();
                     }
                  }
               });
   }


   /** Sets the Splitter attribute of the JMiddlegenFrame object */
   public void setSplitter() {
      int fheight = this.getHeight();
      int sheight = Toolkit.getDefaultToolkit().getScreenSize().height;
      if (fheight >= sheight) {
         _split.setDividerLocation(sheight - 480);
      }
      _split.setOneTouchExpandable(true);
   }


   /**
    * Sets the Visible attribute of the JMiddlegenFrame object
    *
    * @param flag The new Visible value
    */
   public void setVisible(boolean flag) {
      super.setVisible(flag);
      _databasePanel.reset(_middlegen);
      pack();
   }


   /** Sets the cool kunststoff L&F. */
   private void setKuststoffLF() {
      /*
       *  try {
       *  com.incors.plaf.kunststoff.KunststoffLookAndFeel kunststoffLnF
       *  = new com.incors.plaf.kunststoff.KunststoffLookAndFeel();
       *  kunststoffLnF.setCurrentTheme(new com.incors.plaf.kunststoff.KunststoffTheme());
       *  UIManager.setLookAndFeel(kunststoffLnF);
       *  / this line needs to be implemented in order to make JWS work properly
       *  UIManager.getLookAndFeelDefaults().put("ClassLoader", getClass().getClassLoader());
       *  } catch (Throwable ignore) {
       *  }
       */
   }


   /**
    * Describe what the method does
    *
    * @todo-javadoc Write javadocs for method
    * @todo-javadoc Write javadocs for method parameter
    * @param t Describe what the parameter does
    */
   private void error(Throwable t) {
      t.printStackTrace();
      JOptionPane.showMessageDialog(this, t);
   }


   /**
    * This security manager will cause an ExitException to be thrown whenever
    * System.exit is called instead of terminating the VM.
    *
    * @author Aslak Hellesøy
    * @created 2. desember 2002
    */
   public static class NoExitSecurityManager extends SecurityManager {
      /**
       * Throws an ExitException instead of terminating the VM.
       *
       * @todo-javadoc Write javadocs for method parameter
       * @param status Describe what the parameter does
       */
      public void checkExit(int status) {
         throw new ExitException(status);
      }


      /**
       * Allows anything.
       *
       * @todo-javadoc Write javadocs for method parameter
       * @param p Describe what the parameter does
       */
      public void checkPermission(java.security.Permission p) {
      }
   }


   /**
    * Describe what this class does
    *
    * @author Aslak Hellesøy
    * @created 2. desember 2002
    * @todo-javadoc Write javadocs
    */
   public static class ExitException extends RuntimeException {
      /**
       * @todo-javadoc Describe the field
       */
      private int _status;


      /**
       * Describe what the ExitException constructor does
       *
       * @todo-javadoc Write javadocs for constructor
       * @todo-javadoc Write javadocs for method parameter
       * @param status Describe what the parameter does
       */
      public ExitException(int status) {
         _status = status;
      }


      /**
       * Gets the Status attribute of the ExitException object
       *
       * @return The Status value
       */
      public int getStatus() {
         return _status;
      }
   }
}

