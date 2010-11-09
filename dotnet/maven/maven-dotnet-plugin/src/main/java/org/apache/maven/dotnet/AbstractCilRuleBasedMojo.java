/*
 * Maven and Sonar plugin for .Net
 * Copyright (C) 2010 Jose Chillan and Alexandre Victoor
 * mailto: jose.chillan@codehaus.org or alexvictoor@codehaus.org
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

package org.apache.maven.dotnet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.dotnet.commons.project.VisualStudioProject;
import org.apache.maven.dotnet.commons.project.VisualStudioSolution;
import org.apache.maven.plugin.MojoFailureException;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Support class for rule based tools that analyze compiled assemblies.
 * 
 * @author Alexandre Victoor
 *
 */
public abstract class AbstractCilRuleBasedMojo extends AbstractDotNetMojo {

  /**
   * Location of the dotnet mscorlib.dll file to use when analyzing silverlight3
   * projects
   * 
   * @parameter expression="${silverlight.3.mscorlib.location}"
   */
  protected File silverlight_3_MscorlibLocation;

  /**
   * Location of the dotnet mscorlib.dll file to use when analyzing silverlight4
   * projects
   * 
   * @parameter expression="${silverlight.4.mscorlib.location}"
   */
  protected File silverlight_4_MscorlibLocation;

  /**
   * Version of Silverlight used in the analysed solution Possible values are 3
   * and 4
   * 
   * @parameter expression="${silverlight.version}" default-value="4"
   */
  protected String silverlightVersion;

  /**
   * Enable/disable the verbose mode
   * 
   * @parameter expression="${verbose}"
   */
  protected boolean verbose;
  
  /**
   * List of the excluded projects, using ',' as delimiter. No violation on files
   * of these projects should be reported. 
   * 
   * @parameter expression="${skippedProjects}"
   */
  private String skippedProjects;

  /**
   * @return the directory where to find silverlight mscorlib.dll
   * @throws MojoFailureException 
   */
  protected File getSilverlightMscorlibLocation() throws MojoFailureException {
    final File silverlightMscorlibLocation;
    if ("3".equals(silverlightVersion)) {
      silverlightMscorlibLocation = silverlight_3_MscorlibLocation;
    } else {
      silverlightMscorlibLocation = silverlight_4_MscorlibLocation;
    }
    
    if (silverlightMscorlibLocation == null
        || !silverlightMscorlibLocation.exists()
        || !silverlightMscorlibLocation.isDirectory()) {

      throw new MojoFailureException("incorrect silverlight "
          + silverlightVersion + " mscorlib path: "
          + silverlightMscorlibLocation);
    }

    return silverlightMscorlibLocation;
  }

  /**
   * @param solution
   *          the current solution
   * @return the assembly files generated by this solution (except test ones)
   * @throws MojoFailureException
   */
  private List<File> extractAssemblies(VisualStudioSolution solution,
      Boolean silverlightFilter) throws MojoFailureException {
    List<VisualStudioProject> projects = solution.getProjects();
    List<File> assemblies = new ArrayList<File>();
    
    Set<String> skippedProjectSet = new HashSet<String>();
    if (skippedProjects!=null) {
      skippedProjectSet.addAll(Arrays.asList(StringUtils.split(skippedProjects,',')));
    }
    
    for (VisualStudioProject visualStudioProject : projects) {
      if (visualStudioProject.isTest()) {
        // We skip all the test assemblies
        getLog().info(
            "Skipping the test project " + visualStudioProject.getName());

      } else if (skippedProjectSet.contains(visualStudioProject.getName())) {
        getLog().info("Skipping project " + visualStudioProject.getName());
        
      } else if (visualStudioProject.isWebProject()) {
        // ASP project
        assemblies.addAll(visualStudioProject.getWebAssemblies());

      } else if (silverlightFilter == null
          || silverlightFilter.equals(visualStudioProject
              .isSilverlightProject())) {

        File assembly = getGeneratedAssembly(visualStudioProject);
        if (assembly.exists()) {
          assemblies.add(assembly);
        } else {
          getLog().info("Skipping the non generated assembly: " + assembly);
        }
      }
    }
    return assemblies;
  }

  /**
   * @param solution
   *          the current solution
   * @return the assembly files generated by this solution (except test ones)
   * @throws MojoFailureException
   */
  protected final List<File> extractAssemblies(VisualStudioSolution solution)
      throws MojoFailureException {
    return extractAssemblies(solution, null);
  }

  /**
   * @param solution
   *          the current solution
   * @return the assembly files generated by this solution (except test and non
   *         silverlight ones)
   * @throws MojoFailureException
   */
  protected final List<File> extractSilverlightAssemblies(
      VisualStudioSolution solution) throws MojoFailureException {

    return extractAssemblies(solution, true);
  }

  /**
   * @param solution
   *          the current solution
   * @return the assembly files generated by this solution (except test and
   *         silverlight ones)
   * @throws MojoFailureException
   */
  protected final List<File> extractNonSilverlightAssemblies(
      VisualStudioSolution solution) throws MojoFailureException {

    return extractAssemblies(solution, false);
  }

}
