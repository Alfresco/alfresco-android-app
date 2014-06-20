package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class FileComparator implements Comparator<File>, Serializable
{
    private static final long serialVersionUID = 1L;

    private boolean asc;

    public FileComparator(boolean asc)
    {
        super();
        this.asc = asc;
    }

    public int compare(File f1, File f2)
    {
        int value = 0;

        if (f1.isDirectory() && !f2.isDirectory())
        {
            // Directory before non-directory
            value = -1;
        }
        else if (!f1.isDirectory() && f2.isDirectory())
        {
            // Non-directory after directory
            value = 1;
        }
        else
        {
            // Alphabetic order 
            value = f1.getName().compareToIgnoreCase(f2.getName());
        }

        return asc ? value : -value;

    }
}
