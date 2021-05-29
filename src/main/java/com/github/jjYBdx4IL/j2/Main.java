/*
 * Copyright © ${project.inceptionYear} jjYBdx4IL (https://github.com/jjYBdx4IL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jjYBdx4IL.j2;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class Main {

    protected static final CommandLineParser parser = new DefaultParser();
    
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(null, "eclipse", false, "writes eclipse project files and opens eclipse");
        CommandLine cmd = parser.parse(options, args);
        
        new ClasspathBuilder().run(cmd);
    }
}
