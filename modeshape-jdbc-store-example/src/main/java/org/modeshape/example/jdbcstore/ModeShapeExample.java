package org.modeshape.example.jdbcstore;

/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.BorderLayout;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.api.Binary;
import org.modeshape.jcr.api.JcrTools;
import org.modeshape.jcr.api.query.QueryResult;

public class ModeShapeExample {
  private static Session session=null;
	public static void main(String[] argv) {

		Random rnd = new Random();

		// Create and start the engine ...
		ModeShapeEngine engine = new ModeShapeEngine();
		engine.start();

		// Load the configuration for a repository via the classloader (can also
		// use path to a file)...
		Repository repository = null;
		String repositoryName = null;
		try {
			URL url = ModeShapeExample.class.getClassLoader().getResource(
					"my-repository-config.json");
			RepositoryConfiguration config = RepositoryConfiguration.read(url);

			// We could change the name of the repository programmatically ...
			// config = config.withName("Some Other Repository");

			// Verify the configuration for the repository ...
			Problems problems = config.validate();
			if (problems.hasErrors()) {
				System.err.println("Problems starting the engine.");
				System.err.println(problems);
				System.exit(-1);
			}

			// Deploy the repository ...
			repository = engine.deploy(config);
			repositoryName = config.getName();
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(-1);
			return;
		}

		//Session session = null;
		JcrTools tools = new JcrTools();
		try {
			// Get the repository
			System.out.println("repositoryName::" + repositoryName);
			repository = engine.getRepository(repositoryName);

			// Create a session ...
			session = repository.login("default");

			// Create the '/files' node that is an 'nt:folder' ...
			Node root = session.getRootNode();

			Node filesNode = root.addNode("files", "nt:folder");

			InputStream stream = new BufferedInputStream(
					new FileInputStream(
							"C:\\Users\\ITON\\Pictures\\HelloWorldDemoPublicAccess.png"));

			// Create an 'nt:file' node at the supplied path ...
			Node fileNode = filesNode.addNode("HelloWorldDemoPublicAccess.png",
					"nt:file");

			// Upload the file to that node ...
			Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
			Binary binary = (Binary) session.getValueFactory().createBinary(
					stream);
			contentNode.setProperty("jcr:data", binary);

			session.save();

			Node doc = session.getNode("/files/HelloWorldDemoPublicAccess.png");
			Node imageContent = doc.getNode("jcr:content");
			Binary content = (Binary) imageContent.getProperty("jcr:data")
					.getBinary();
			InputStream is = content.getStream();

			Image image = ImageIO.read(is);

			JFrame frame = new JFrame();
			JLabel label = new JLabel(new ImageIcon(image));
			frame.getContentPane().add(label, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);

			tools.printSubgraph(root);
			try {
				// do something with the stream
			} finally {
				is.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException" + e);
		} catch (RepositoryException e) {
			System.out.println("RepositoryException" + e);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception" + e);
			e.printStackTrace();
		}
		try {
			// Get the repository
			repository = engine.getRepository(repositoryName);

			// Create a session ...
			session = repository.login("default");

			// Get the root node ...
			Node root = session.getRootNode();
			assert root != null;

			System.out.println("Found the root node in the \""
					+ session.getWorkspace().getName() + "\" workspace");

			Node n = root.addNode("Node" + rnd.nextInt());

			n.setProperty("key", "value");
			n.setProperty(
					"content",
					session.getValueFactory().createBinary(
							new ByteArrayInputStream(new byte[1000])));

			session.save();

			System.out.println("Added one node under root");
			System.out.println("+ Root childs");

			NodeIterator it = root.getNodes();
			while (it.hasNext()) {
				System.out.println("+---> " + it.nextNode().getName());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null)
				session.logout();
			System.out.println("Shutting down engine ...");
			try {
				engine.shutdown().get();
				System.out.println("Success!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		

	}
	public List getDatabases() throws RepositoryException {
        // Obtain the query manager for the session ...
		
        QueryManager queryManager = session.getWorkspace().getQueryManager();

        // Create a query object ...
        Query query = queryManager.createQuery("SELECT * FROM [mj:table]"
                , Query.JCR_SQL2);
        // Execute the query and get the results ...
        javax.jcr.query.QueryResult result = query.execute();

        // Iterate over the nodes in the results ...
        NodeIterator nodeIter = result.getNodes();

        List stringResult = new ArrayList();
        while (nodeIter.hasNext()) {
            stringResult.add(nodeIter.nextNode().getName());
        }

        return stringResult;
    }
}
