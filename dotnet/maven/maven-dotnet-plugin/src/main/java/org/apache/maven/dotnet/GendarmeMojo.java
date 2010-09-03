package org.apache.maven.dotnet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.dotnet.commons.project.VisualStudioProject;
import org.apache.maven.dotnet.commons.project.VisualStudioSolution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

/**
 * Generates a quality report for a .Net project or solution using mono gendarme
 * 
 * @goal gendarme
 * @phase site
 * @description generates a mono-gendarme report on a .Net project or solution
 * @author Alexandre Victoor
 * 
 */
public class GendarmeMojo extends AbstractCilRuleBasedMojo {

  private static final String MSCORLIB_DLL = "mscorlib.dll";

  /**
   * Name of the resource folder that contains the mono gendarme runtime
   */
  private final static String RESOURCE_DIR = "gendarme";
  /**
   * Name of the extracted folder that will contain the extracted gendarme.exe
   */
  private final static String EXPORT_PATH = "gendarme-runtime";

  /**
   * Location of the mono gendarme installation
   * 
   * @parameter expression="${gendarme.directory}"
   */
  private File gendarmeDirectory;


  /**
   * Name of the mono gendarme command line executable.
   * 
   * @parameter expression="${gendarme.executable}" default-value="gendarme.exe"
   */
  private String gendarmeExecutable = "gendarme.exe";

  /**
   * Name of the gendarme report file
   * 
   * @parameter alias="${gendarmeReportName}"
   *            default-value="gendarme-report.xml"
   */
  private String gendarmeReportName;

  /**
   * Path to the gendarme config file that specifies rule settings
   * 
   * @parameter alias="${gendarmeConfig}"
   */
  private String gendarmeConfigFile;


  /**
   * The gendarme.exe file 
   */
  private File executableFile;

  /**
   * Launches the report for a solution
   * 
   * @param solution
   *          the solution to check
   * @throws MojoFailureException
   * @throws MojoExecutionException
   */
  @Override
  protected void executeSolution(VisualStudioSolution solution)
      throws MojoFailureException, MojoExecutionException {
    List<File> checkedAssemblies = extractAssemblies(solution);
    launchReport(checkedAssemblies, solution.isSilverlightUsed());
  }

  /**
   * Launches the report for a project.
   * 
   * @param visualProject
   *          the project to execute
   * @throws MojoFailureException
   * @throws MojoExecutionException
   */
  @Override
  protected void executeProject(VisualStudioProject visualProject)
      throws MojoFailureException, MojoExecutionException {
    if (visualProject.isTest()) {
      getLog().info(
          "No gendarme report generated for the test project "
              + visualProject.getName());
      return;
    }
    File assembly = getGeneratedAssembly(visualProject);
    if (!assembly.exists()) {
      // No assembly found
      throw new MojoFailureException(
          "Cannot find the generated assembly to launch gendarme " + assembly);
    }
    launchReport(Collections.singletonList(assembly), visualProject.isSilverlightProject());
  }

  /**
   * Launches the reporting for a list of assemblies
   * 
   * @param assemblies
   *          the assemblies to check
   * @param silverlightUsed
   *          flag that indicates if silverlight is used in one of the assemblies
   * @throws MojoExecutionException
   *           if an execution problem occurred
   * @throws MojoFailureException
   *           in case of execution failure
   */
  private void launchReport(List<File> assemblies, boolean silverlightUsed)
      throws MojoExecutionException, MojoFailureException {
    final Log log = getLog();
    if (assemblies.isEmpty()) {
      log.info("No assembly to check with Gendarme");
      return;
    }
    
    if (silverlightUsed) {
      // mscorlib.dll need to be in the same directory
      // of one of the analyzed assemblies. We take
      // the first one of the list
      final File destinationDirectory 
        = assemblies.get(0).getParentFile();
      final File silverlightMscorlibLocation = getSilverlightMscorlibLocation();
      
      try {
        File mscorlibFile = new File(silverlightMscorlibLocation, MSCORLIB_DLL);
        FileUtils.copyFileToDirectory(mscorlibFile, destinationDirectory);
      } catch (IOException e) {
        log.error(e);
        throw new MojoFailureException(
            "Cannot copy custom mscorlib.dll file to " + destinationDirectory
           );
      }
    }
    

    // We retrieve the required files
    prepareExecutable();

    File reportFile = getReportFile(gendarmeReportName);

    // We build the command arguments
    List<String> commandArguments = new ArrayList<String>();

    // Defines the report file
    log.debug("- Report file  : " + reportFile);
    commandArguments.add("--xml");
    commandArguments.add(toCommandPath(reportFile));

    if (StringUtils.isNotEmpty(gendarmeConfigFile)) {
      commandArguments.add("--config");
      commandArguments.add(gendarmeConfigFile);
    }

    // Put in verbose mode if required
    if (verbose) {
      commandArguments.add("--v");
    }
    // Add the assemblies to check
    log.debug("- Scanned assemblies :");
    for (File checkedAssembly : assemblies) {
      log.debug("   o " + checkedAssembly);
      commandArguments.add(toCommandPath(checkedAssembly));
    }

    launchCommand(executableFile, commandArguments, "gendarme", 1, true);
    log.info("gendarme report generated");
    
    // clean up needed
    if (silverlightUsed) {
      File destinationDirectory = assemblies.get(0).getParentFile();
      new File(destinationDirectory, MSCORLIB_DLL).delete();
    }
  }

  /**
   * Prepares the Gendarme executable.
   * 
   * @throws MojoExecutionException
   */
  private void prepareExecutable() throws MojoExecutionException {
    if (gendarmeDirectory == null) {
      gendarmeDirectory = extractFolder(RESOURCE_DIR, EXPORT_PATH, "Gendarme");
    }
    executableFile = new File(gendarmeDirectory, gendarmeExecutable);
  }

}
