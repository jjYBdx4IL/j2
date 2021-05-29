package com.github.jjYBdx4IL.j2;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.cli.CommandLine;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class ClasspathBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ClasspathBuilder.class);
    
    public ClasspathBuilder() {
    }

    public void run(CommandLine cmd) throws Exception {
        System.out.println("------------------------------------------------------------");
        System.out.println(ClasspathBuilder.class.getSimpleName());

        
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader("pom.xml"));        
        checkNotNull(model);
        
        List<org.apache.maven.model.Dependency> deps = model.getDependencies();
        
        
        
        RepositorySystem system = newRepositorySystem();

        RepositorySystemSession session = newRepositorySystemSession(system);

        Artifact artifact = new DefaultArtifact("org.apache.maven:maven-resolver-provider:3.6.0");

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, ""));
        collectRequest.setRepositories(new ArrayList<>(Collections.singletonList(
            new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build())));

        CollectResult collectResult = system.collectDependencies(session, collectRequest);

        collectResult.getRoot().accept(new ConsoleDependencyGraphDumper());

    }

    public static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository("target/local-repo");
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        session.setTransferListener(new ConsoleTransferListener());
        session.setRepositoryListener(new ConsoleRepositoryListener());

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }
    
    public static RepositorySystem newRepositorySystem()
    {
        /*
         * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
         * prepopulated DefaultServiceLocator, we only need to register the repository connector and transporter
         * factories.
         */
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );

        locator.setErrorHandler( new DefaultServiceLocator.ErrorHandler()
        {
            @Override
            public void serviceCreationFailed( Class<?> type, Class<?> impl, Throwable exception )
            {
               LOG.error( "Service creation failed for {} with implementation {}",
                        type, impl, exception );
            }
        } );

        return locator.getService( RepositorySystem.class );
    }
}
