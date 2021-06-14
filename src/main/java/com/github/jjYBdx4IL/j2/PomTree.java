package com.github.jjYBdx4IL.j2;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PomTree {

    private static final Logger LOG = LoggerFactory.getLogger(PomTree.class);

    public PomTree root = null;
    public PomTree parent = null; // null iff root
    public Set<String> enabledProfiles = new HashSet<>();

    public Path dir = null;
    public List<PomTree> childs = new ArrayList<>();
    public Model model = null;

    public PomTree(Path dir, String enabledProfiles) {
        checkNotNull(dir);
        this.dir = dir.toAbsolutePath().normalize();
        this.root = this;
        if (enabledProfiles != null) {
            this.enabledProfiles.addAll(Arrays.asList(enabledProfiles.split(",")));
        }
    }

    public PomTree(PomTree root, PomTree parent, Path dir) {
        checkNotNull(root);
        checkNotNull(parent);
        checkNotNull(dir);
        
        this.root = root;
        this.parent = parent;
        this.dir = dir;
    }

    public void load() throws FileNotFoundException, IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        model = reader.read(new FileReader(dir.resolve("pom.xml").toFile()));
        checkNotNull(model);

        LOG.info("{} - {}", dir, model.toString());

        Set<String> modules = new HashSet<>();
        modules.addAll(model.getModules());
        for (Profile p : model.getProfiles()) {
            if (root.enabledProfiles.contains(p.getId())) {
                modules.addAll(p.getModules());
            }
        }
        LOG.info("{}", modules);

        for (String m : modules) {
            PomTree child = new PomTree(root, this, dir.resolve(m));
            child.load();
            childs.add(child);
        }
        
        //List<org.apache.maven.model.Dependency> deps = model.getDependencies();

        //LOG.info("{}", deps);
    }
}
