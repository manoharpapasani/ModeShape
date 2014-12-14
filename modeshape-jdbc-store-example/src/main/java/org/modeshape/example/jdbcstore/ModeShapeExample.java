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
import javax.jcr.version.VersionHistory;
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
	// private static Session session=null;
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

		Session session = null;
		JcrTools tools = new JcrTools();
		Node filesNode = null;
		try {
			// Get the repository
			System.out.println("repositoryName::" + repositoryName);
			repository = engine.getRepository(repositoryName);

			// Create a session ...
			session = repository.login("default");

			// Create the '/files' node that is an 'nt:folder' ...
			Node root = session.getRootNode();

			filesNode = root.addNode("files", "nt:folder");
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<,root>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			tools.printNode(root);
			tools.printSubgraph(root);

			InputStream stream = new BufferedInputStream(
					new FileInputStream(
							"C:\\Users\\ITON\\Pictures\\HelloWorldDemoPublicAccess.png"));

			// Create an 'nt:file' node at the supplied path ...
			Node fileNode = filesNode.addNode("HelloWorldDemoPublicAccess.png","nt:file");
			fileNode.addMixin("mix:versionable");
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<,file node>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			tools.printNode(fileNode);
			tools.printSubgraph(fileNode);
			// Upload the file to that node ...
			Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
			tools.printNode(contentNode);
			Binary binary = (Binary) session.getValueFactory().createBinary(
					stream);
			contentNode.setProperty("jcr:data", binary);

			
			// Reading from a file
			Node doc = session.getNode("/files/HelloWorldDemoPublicAccess.png");
			Node imageContent = doc.getNode("jcr:content");
			Binary content = (Binary) imageContent.getProperty("jcr:data")
					.getBinary();
			InputStream is = content.getStream();
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<,imageContent>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			tools.printNode(imageContent);
			tools.printSubgraph(imageContent);
			Image image = ImageIO.read(is);

			JFrame frame = new JFrame();
			JLabel label = new JLabel(new ImageIcon(image));
			frame.getContentPane().add(label, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
			//tools.printNode("fileNode--------------------"+fileNode);
			//tools.printSubgraph("root--------------------"+root);
			// Query Search
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			System.out.println("QueryManager-------------"+queryManager);
			// String sqlStatement =
			// "SELECT [jcr:path] FROM [nt:resource] WHERE contains([nt:resource].[jcr:data],'ModeShape')";
			String sqlStatement = "SELECT * FROM [nt:base] WHERE CONTAINS([nt:base],'files[40]')";
			Query query = queryManager
					.createQuery(sqlStatement, Query.JCR_SQL2);
			// execute query and fetch result
		    System.out.println("query----------"+query);
			javax.jcr.query.QueryResult qResult = query.execute();
            System.out.println(qResult.getColumnNames());
			NodeIterator it1 = qResult.getNodes();
            System.out.println(it1+">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			while(it1.hasNext()) {
				Node findedNode = it1.nextNode();
				System.out.println(findedNode.getName() + "---------------");
			}
			session.save();
			
			VersionHistory history =
						session.getWorkspace().getVersionManager()
						 .getVersionHistory(fileNode.getPath());
			System.out.println("history------------"+history);
			tools.printNode(root);
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<root >>>>>>>>>>>>>>>>>>>>>>>>>>");
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
	}
}
