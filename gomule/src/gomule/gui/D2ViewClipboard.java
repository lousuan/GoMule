/*******************************************************************************
 * 
 * Copyright 2007 Randall
 * 
 * This file is part of gomule.
 * 
 * gomule is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * gomule is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * gomlue; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 *  
 ******************************************************************************/
package gomule.gui;

import gomule.d2x.*;
import gomule.item.*;
import gomule.util.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import randall.util.*;

public class D2ViewClipboard extends JInternalFrame implements D2ItemContainer, D2ItemListListener
{
    private static final int   GRID_SIZE = 28;

    private D2FileManager      iFileManager;
    private D2ItemModel        iItemModel;
    private JTable             iTable;
    private RandallPanel       iContentPane;

    private static D2ViewClipboard iMouseItem;

    private ArrayList          iItems;

    private String			   iFileName;
    private D2Stash            iStash;

    private JTextField         iBank;

    public static D2ViewClipboard getInstance(D2FileManager pFileManager) throws Exception
    {
        if (iMouseItem == null)
        {
            iMouseItem = new D2ViewClipboard(pFileManager);
        }
        return iMouseItem;
    }
    
    public static D2ViewClipboard getInstance()
    {
        return iMouseItem;
    }

    private D2ViewClipboard(D2FileManager pFileManager)
    {
        super("Item Clipboard", true, false, false, true);
        iFileManager = pFileManager;

        iContentPane = new RandallPanel();

        try
        {
            iBank = new JTextField();
            iBank.setEditable(false);
            
            setProject(pFileManager.getProject());

            iItemModel = new D2ItemModel(iItems);
            iTable = new JTable(iItemModel);
            iTable.setDefaultRenderer(String.class, new D2CellStringRenderer() );
            JScrollPane lPane = new JScrollPane(iTable);
            iContentPane.addToPanel(lPane,0,10,2,RandallPanel.BOTH);
            
            iContentPane.addToPanel(new JLabel("GoMule Bank: "),0,0,1,RandallPanel.NONE);
            iContentPane.addToPanel(iBank,1,0,1,RandallPanel.HORIZONTAL);

            iTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            if (!iItems.isEmpty())
            {
                iTable.setRowSelectionInterval(iItems.size() - 1, iItems.size() - 1);
            }
        }
        catch (Exception pEx)
        {
            D2FileManager.displayErrorDialog(pEx);
            JTextArea lError = new JTextArea();
            JScrollPane lScroll = new JScrollPane(lError);
            lError.setText(pEx.getMessage());
            iContentPane.add(lError, BorderLayout.CENTER);
        }

        setContentPane(iContentPane);

        pack();

        setLocation(0, 300);
        setSize(300, 200);

        setVisible(true);
    }
    
    public void itemListChanged()
    {
        fireTableChanged();
        super.setTitle("Item Clipboard" + ( iStash.isModified() ? "*":"" ) );
    }

    class D2ItemModel implements TableModel
    {
        private ArrayList iTableModelListeners = new ArrayList();
        private ArrayList    iItems;

        public D2ItemModel(ArrayList pItems)
        {
            setItems(pItems);
        }

        public void setItems(ArrayList pItems)
        {
            iItems = pItems;
        }

        public int getRowCount()
        {
            return iItems.size();
        }

        public int getColumnCount()
        {
            return 3;
        }

        public String getColumnName(int pCol)
        {
            switch (pCol)
            {
            case 0:
                return "Name";
            case 1:
                return "Fingerprint";
            case 2:
                return "iLvl";
            default:
                return "";
            }
        }

        public Class getColumnClass(int pCol)
        {
            return String.class;
        }

        public boolean isCellEditable(int pRow, int pCol)
        {
            return false;
        }

        public Object getValueAt(int pRow, int pCol)
        {
            D2Item lItem = (D2Item) iItems.get(pRow);
            switch (pCol)
            {
            case 0:
                return new D2CellValue( lItem.getItemName(), lItem, iFileManager.getProject());
            case 1:
                return new D2CellValue( lItem.getFingerprint(), lItem, iFileManager.getProject());
            case 2:
                return new D2CellValue( lItem.getILvl(), lItem, iFileManager.getProject());
            default:
                return "";
            }
        }

        public void setValueAt(Object pValue, int pRow, int pCol)
        {
            // Do nothing
        }

        public void addTableModelListener(TableModelListener pListener)
        {
            iTableModelListeners.add(pListener);
        }

        public void removeTableModelListener(TableModelListener pListener)
        {
            iTableModelListeners.remove(pListener);
        }

        public void fireTableChanged()
        {
            fireTableChanged(new TableModelEvent(this));
        }

        public void fireTableChanged(TableModelEvent pEvent)
        {
            for (int i = 0; i < iTableModelListeners.size(); i++)
            {
                ((TableModelListener) iTableModelListeners.get(i)).tableChanged(pEvent);
            }
            int lRowCount = iTable.getRowCount();
            if ( iTable.getSelectedRow() == -1 && iTable.getRowCount() > 0 )
            {
                iTable.setRowSelectionInterval(lRowCount-1, lRowCount-1);
            }
        }

    }

    public String getFileName()
    {
        return iFileName;
    }

    public void setProject(D2Project pProject) throws Exception
    {
        iFileName = pProject.getProjectDir() + File.separator + "Clipboard.d2x";
        iStash = new D2Stash(iFileName);
        iStash.addD2ItemListListener(this);
        iItems = iStash.getItemList();
        if (iItemModel != null)
        {
            iItemModel.setItems(iItems);
            iItemModel.fireTableChanged();
            iTable.repaint();
            if (!iItems.isEmpty())
            {
                iTable.setRowSelectionInterval(iItems.size() - 1, iItems.size() - 1);
            }
        }
        
        iBank.setText(Integer.toString(pProject.getBankValue()));
    }
    
    public static void refreshBank(D2Project pProject)
    {
        iMouseItem.iBank.setText(Integer.toString(pProject.getBankValue()));
    }

    public boolean isModified()
    {
        return iStash.isModified();
    }
    
    public D2ItemList getItemLists()
    {
        return iStash;
    }
    
    public void closeView()
    {
        iStash.removeD2ItemListListener(this);
    }
    
    public boolean isSC()
    {
        return true;
    }

    public boolean isHC()
    {
        return true;
    }
    
    public void saveView()
    {
        if ( iStash != null && iStash.isModified() )
        {
            iStash.save( iMouseItem.iFileManager.getProject() );
        }
    }

    public static ArrayList getItemList()
    {
        return iMouseItem.iItems;
    }
    
    public static ArrayList removeAllItems()
    {
        return iMouseItem.iStash.removeAllItems();
    }

    public static D2Item getItem()
    {
        return iMouseItem.getItemInternal();
    }

    private D2Item getItemInternal()
    {
        if (!iItems.isEmpty())
        {
            int lRow = iTable.getSelectedRow();
            if (lRow >= 0 && lRow < iItems.size())
            {
                return (D2Item) iItems.get(lRow);
            }
        }
        return null;
    }

    public static D2Item removeItem()
    {
        return iMouseItem.removeItemInternal();
    }

    private D2Item removeItemInternal()
    {
        if (!iItems.isEmpty())
        {
            int lRow = iTable.getSelectedRow();
            if (lRow >= 0 && lRow < iItems.size())
            {
                D2Item lItem = (D2Item) iItems.get(lRow);
                iStash.removeItem(lItem);
                if (!iItems.isEmpty() && iTable.getSelectedRow() == -1)
                {
                    iTable.clearSelection();
                    iTable.setRowSelectionInterval(iItems.size() - 1, iItems.size() - 1);
                }
                return lItem;
            }
        }
        return null;
    }

    public static void addItem(D2Item pItem)
    {
        iMouseItem.addItemInternal(pItem);
    }

    private void addItemInternal(D2Item pItem)
    {
        iStash.addItem(pItem);

        if (iTable.getSelectedRow() == -1)
        {
            if (!iItems.isEmpty())
            {
                //                System.err.println("Set To: " + (iItems.size()-1) );
                iTable.clearSelection();
                iTable.setRowSelectionInterval(iItems.size() - 1, iItems.size() - 1);
            }
        }
        else
        {
            if (!iItems.isEmpty())
            {
                if (iTable.getSelectedRow() == iItems.size() - 2)
                {
                    //                    System.err.println("Set To: " + (iItems.size()-1) );
                    iTable.clearSelection();
                    iTable.setRowSelectionInterval(iItems.size() - 1, iItems.size() - 1);
                }
            }
            else
            {
                iTable.clearSelection();
                //                iTable.setRowSelectionInterval(-1, -1);
            }
        }
        //        System.err.println( "Test: " + iTable.getSelectedRow() );

    }

    private void fireTableChanged()
    {
        iItemModel.fireTableChanged();
    }

}