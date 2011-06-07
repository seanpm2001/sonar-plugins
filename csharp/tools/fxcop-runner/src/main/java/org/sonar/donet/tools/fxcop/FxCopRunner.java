/*
 * .NET tools :: FxCop Runner
 * Copyright (C) 2011 Jose Chillan, Alexandre Victoor and SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.donet.tools.fxcop;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandExecutor;
import org.sonar.plugins.csharp.api.visualstudio.VisualStudioProject;
import org.sonar.plugins.csharp.api.visualstudio.VisualStudioSolution;

/**
 * Class that runs the FxCop program.
 */
public class FxCopRunner { // NOSONAR : can't mock it otherwise

  private static final Logger LOG = LoggerFactory.getLogger(FxCopRunner.class);

  private static final int MINUTES_TO_MILLISECONDS = 60000;

  private File fxCopExecutable;

  private FxCopRunner() {
  }

  /**
   * Creates a new {@link FxCopRunner} object for the given executable file.
   * 
   * @param fxCopPath
   *          the full path of the executable. For instance: "C:/Program Files/Microsoft FxCop 10.0/FxCopCmd.exe".
   */
  public static FxCopRunner create(String fxCopPath) throws FxCopException {
    FxCopRunner runner = new FxCopRunner();
    runner.fxCopExecutable = new File(fxCopPath);
    return runner;
  }

  /**
   * Creates a pre-configured {@link FxCopCommandBuilder} that needs to be completed before running the {@link #execute(Command, int)}
   * method.
   * 
   * @param solution
   *          the solution to analyse
   * @return the command to complete.
   */
  public FxCopCommandBuilder createCommandBuilder(VisualStudioSolution solution) {
    FxCopCommandBuilder builder = FxCopCommandBuilder.createBuilder(solution);
    builder.setExecutable(fxCopExecutable);
    return builder;
  }

  /**
   * Creates a pre-configured {@link FxCopCommandBuilder} that needs to be completed before running the {@link #execute(Command, int)}
   * method.
   * 
   * @param project
   *          the VS project to analyse
   * @return the command to complete.
   */
  public FxCopCommandBuilder createCommandBuilder(VisualStudioProject project) {
    FxCopCommandBuilder builder = FxCopCommandBuilder.createBuilder(project);
    builder.setExecutable(fxCopExecutable);
    return builder;
  }

  /**
   * Executes the given FxCop command.
   * 
   * @param command
   *          the command
   * @param timeoutMinutes
   *          the timeout for the command
   * @throws FxCopException
   *           if FxCop fails to execute
   */
  public void execute(Command command, int timeoutMinutes) throws FxCopException {
    LOG.debug("Executing FxCop program...");
    int exitCode = CommandExecutor.create().execute(command, timeoutMinutes * MINUTES_TO_MILLISECONDS);
    if (exitCode != 0) {
      throw new FxCopException("FxCop execution failed with return code '" + exitCode
          + "'. Check FxCop documentation for more information.");
    }
  }

}
