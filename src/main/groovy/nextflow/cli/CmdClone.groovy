/*
 * Copyright (c) 2013-2015, Centre for Genomic Regulation (CRG).
 * Copyright (c) 2013-2015, Paolo Di Tommaso and the respective authors.
 *
 *   This file is part of 'Nextflow'.
 *
 *   Nextflow is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Nextflow is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Nextflow.  If not, see <http://www.gnu.org/licenses/>.
 */

package nextflow.cli
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.exception.AbortOperationException
import nextflow.scm.AssetManager
/**
 * CLI sub-command clone
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@Parameters(commandDescription = "Clone a pipeline into a folder")
class CmdClone extends CmdBase implements HubOptions {

    static final NAME = 'clone'

    @Parameter(required=true, description = 'name of the pipeline to clone')
    List<String> args

    @Parameter(names='-r', description = 'Revision to clone - It can be a git branch, tag or revision number')
    String revision

    @Override
    final String getName() { NAME }

    @Override
    void run() {
        // the pipeline name
        String pipeline = args[0]
        final manager = new AssetManager(pipeline: pipeline, hub: getHubProvider(), user: getHubUser(), pwd: getHubPassword())

        // the target directory is the second parameter
        // otherwise default the current pipeline name
        def target = new File(args.size()> 1 ? args[1] : manager.getBaseName())
        if( target.exists() ) {
            if( target.isFile() )
                throw new AbortOperationException("A file with the same name already exists: $target")
            if( !target.empty() )
                throw new AbortOperationException("Clone target directory must be empty: $target")
        }
        else if( !target.mkdirs() ) {
            throw new AbortOperationException("Cannot create clone target directory: $target")
        }

        manager.checkValidRemoteRepo()
        print "Cloning ${manager.pipeline}${revision ? ':'+revision:''} ..."
        manager.clone(target, revision)
        print "\r"
        println "${manager.pipeline} cloned to: $target"
    }
}
