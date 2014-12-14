package org.modeshape.example.sequencing;

import java.awt.BorderLayout;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.GraphI18n;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.api.JcrTools;

public class ModeShapeExample {

	private static boolean print = true;

	public static void main(String[] argv) {

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
			System.out.println("Throwable:" + e);
			e.printStackTrace();
			System.exit(-1);
			return;
		}

		Session session = null;
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
			System.out
					.println("Stored some custom properties in json files, checkout the getPath"
							+ filesNode.getPath() + " folder");
			System.out
					.println("Stored some custom properties in json files, checkout the getName "
							+ filesNode.getName() + " folder");
			System.out
					.println("Stored some custom properties in json files, checkout the JCR_VERSION_LABELS"
							+ filesNode.JCR_VERSION_LABELS + " folder");
			System.out
					.println("Stored some custom properties in json files, checkout the JCR_ROOT_VERSION"
							+ filesNode.JCR_ROOT_VERSION + " folder");
			
			
			InputStream stream = new BufferedInputStream(new FileInputStream(
					"D:\\Desert.jpg"));
			//tools.printNode(filesNode);
			System.out
					.println("----------------------------------------------------------------");

			// Create an 'nt:file' node at the supplied path ...
			Node fileNode = filesNode.addNode("Desert.jpg", "nt:file");
			fileNode.addMixin("mix:versionable");
			System.out
					.println("Stored some custom properties in json files, checkout the fileNode.getPath()"
							+ fileNode.getPath() + " folder");
			System.out
					.println("Stored some custom properties in json files, checkout the fileNode.getName()"
							+ fileNode.getName() + " folder");
			System.out
					.println("Stored some custom properties in json files, checkout the JCR_VERSION_LABELS"
							+ fileNode.JCR_VERSION_LABELS + " folder");
			System.out
					.println("Stored some custom properties in json files, checkout the JCR_ROOT_VERSION"
							+ fileNode.JCR_ROOT_VERSION + " folder");
			tools.printNode(fileNode);
			System.out
					.println("----------------------------------------------------------------");
			for (PropertyIterator pi1 = fileNode.getProperties(); pi1.hasNext();) {
				Object type = pi1.next();
				System.out.println("type" + type.toString());

			}

			// Upload the file to that node ...
			Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
			Binary binary = session.getValueFactory().createBinary(stream);
			contentNode.setProperty("jcr:data", binary);
			tools.printNode(contentNode);
			for (PropertyIterator pi1 = contentNode.getProperties(); pi1
					.hasNext();) {
				Object type = pi1.next();
				System.out.println("type" + type.toString());

			}
			System.out
					.println("----------------------------------------------------------------");
			// Query Search
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			System.out.println("QueryManager-------------" + queryManager);
			// String sqlStatement =
			// "SELECT [jcr:path] FROM [nt:resource] WHERE contains([nt:resource].[jcr:data],'ModeShape')";
		//	String sqlStatement = "SELECT * FROM [nt:base] WHERE CONTAINS([nt:folder],'files')";
			String sqlStatement = "SELECT * FROM [nt:base]  WHERE CONTAINS([nt:folder],'Desert.jpg') ";
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			Query query = queryManager
					.createQuery(sqlStatement, Query.JCR_SQL2);
			// execute query and fetch result
			System.out.println("query----------" + query);
			javax.jcr.query.QueryResult qResult = query.execute();
			System.out.println(qResult.getNodes());
			System.out.println(qResult.getColumnNames());
			NodeIterator it1 = qResult.getNodes();
			System.out.println(it1 + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			while (it1.hasNext()) {
				Node findedNode = it1.nextNode();
				System.out.println(findedNode.getName() + "---------------");
			}
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			session.save();
			VersionHistory history = session.getWorkspace().getVersionManager()
					.getVersionHistory(fileNode.getPath());
			System.out.println("<><><><><><><><>getVersionableIdentifier"
					+ history.getVersionableIdentifier());
			System.out.println("<><><><><><><><>getRootVersion"
					+ history.getRootVersion());
			System.out.println("<><><><><><><><>getVersionLabels"
					+ history.getVersionLabels());
			System.out.println("<><><><><><><><>toString" + history.toString());
			for (VersionIterator vi = history.getAllVersions(); vi.hasNext();) {
				Object type = vi.next();
				System.out.println("type" + type.toString());

			}

			Node doc = session.getNode("/files/Desert.jpg");
			Node imageContent = doc.getNode("jcr:content");
			Binary content = imageContent.getProperty("jcr:data").getBinary();
			InputStream is = content.getStream();
			System.out
					.println("Stored some custom properties in json files, checkout the doc.JCR_ROOT_VERSION"
							+ doc.JCR_ROOT_VERSION + " folder");
			System.out
					.println("Stored some custom properties in json files, checkout the doc.JCR_VERSION_LABELS"
							+ doc.JCR_VERSION_LABELS + " folder");
			System.out
					.println("Stored some custom properties in json files, checkout the imageContent.JCR_ROOT_VERSION"
							+ imageContent.JCR_ROOT_VERSION + " folder");
			System.out
					.println("Stored some custom properties in json files, checkout the imageContent.JCR_VERSION_LABELS"
							+ imageContent.JCR_VERSION_LABELS + " folder");
			for (PropertyIterator pi1 = doc.getProperties(); pi1.hasNext();) {
				Object type = pi1.next();
				System.out.println("type" + type.toString());

			}
			System.out
					.println("----------------------------------------------------------------");
			for (PropertyIterator pi1 = imageContent.getProperties(); pi1
					.hasNext();) {
				Object type = pi1.next();
				System.out.println("type" + type.toString());

			}
			tools.printNode(imageContent);
			System.out
					.println("----------------------------------------------------------------");
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

	public static InputStream getFile(String path) {
		// First try to read from the file system ...
		File file = new File(path);
		if (file.exists() && file.canRead()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				System.out.println("FileNotFoundException:" + e);
				// continue
			}
		}
		// If not found, try to read from the classpath ...
		return ModeShapeExample.class.getClassLoader()
				.getResourceAsStream(path);
	}

	public static Node findNodeAndWait(Session session, String path,
			long maxWaitTime, TimeUnit unit) throws RepositoryException,
			InterruptedException {
		long start = System.currentTimeMillis();
		long maxWaitInMillis = TimeUnit.MILLISECONDS.convert(maxWaitTime, unit);

		do {
			try {
				// This method either returns a non-null Node reference, or
				// throws an exception ...
				return session.getNode(path);
			} catch (PathNotFoundException e) {
				System.out.println("PathNotFoundException" + e);

				// The node wasn't there yet, so try again ...
			}
			Thread.sleep(10L);
		} while ((System.currentTimeMillis() - start) <= maxWaitInMillis);
		throw new PathNotFoundException("Failed to find node '" + path
				+ "' even after waiting " + maxWaitTime + " " + unit);
	}
}
