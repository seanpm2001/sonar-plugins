/**
 * Maven and Sonar plugin for .Net
 * Copyright (C) 2010 Jose Chillan and Alexandre Victoor
 * mailto: jose.chillan@codehaus.org or alexandre.victoor@codehaus.org
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package net.sourceforge.pmd.cpd;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * File name filter used to get c# files needed for cpd analysis 
 *  
 * @author avictoor101408
 *
 */
public class CsFilenameFilter implements FilenameFilter {

	private final static Set<String> fileNamesToExclude = 
		new HashSet<String>(Arrays.asList(new String[]{"Reference.cs", "AssemblyInfo.cs"}));
	
	@Override
	public boolean accept(File dir, String name) {
		boolean result = new File(dir, name).isDirectory() || 
			(name.toLowerCase().endsWith(".cs") 
			&& !fileNamesToExclude.contains(name) 
			&& !name.toLowerCase().contains("designer."));
		return result;
	}

}
