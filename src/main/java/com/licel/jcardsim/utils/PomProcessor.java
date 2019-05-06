/*
 * Copyright 2015 Licel Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.licel.jcardsim.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * Injects jCardSim’s code into Java Card Api Reference Classes
 */
public class PomProcessor {

    public static void main(String[] args) throws Exception {
        if (args.length == 0){
            throw new IllegalArgumentException("Build directory is required");
        }

        File buildDir = new File(args[0]);
        if (!buildDir.exists() || !buildDir.isDirectory()) {
            throw new RuntimeException("Invalid directory: " + buildDir);
        }

        final File depPom = new File(buildDir, "dependency-reduced-pom.xml");
        if (!depPom.exists()){
            System.err.println("POM not found: " + depPom.getAbsolutePath());
            return;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(depPom);

            deleteElement(doc);

            //write the updated document to file or console
            doc.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(depPom);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
            System.out.println("XML file updated successfully");

        } catch(Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static void deleteElement(Document doc) {
        NodeList projects = doc.getElementsByTagName("project");
        if (projects.getLength() < 1){
            System.err.println("Invalid number of project elements");
            return;
        }
        
        final Node project = projects.item(0);
        if (project.getNodeType() != Node.ELEMENT_NODE){
            System.err.println("Invalid project node type");
            return;
        }

        Element elProject = (Element) project;
        final NodeList dependencies = elProject.getElementsByTagName("dependencies");
        if (dependencies.getLength() < 1){
            System.err.println("Invalid num of dependencies");
            return;
        }

        boolean found = false;
        int depIdx = 0;
        for(depIdx = 0; depIdx < dependencies.getLength(); depIdx++){
            if (dependencies.item(depIdx).getParentNode() == elProject){
                found = true;
                break;
            }
        }

        if (!found){
            System.err.println("Project.dependencies not found");
            return;
        }

        final Node nDeps = dependencies.item(depIdx);
        if (nDeps.getNodeType() != Node.ELEMENT_NODE){
            System.err.println("Invalid dependencies node type");
            return;
        }

        Element elDeps = (Element) nDeps;
        final NodeList depsNodeList = elDeps.getElementsByTagName("dependency");
        if (depsNodeList.getLength() < 1){
            System.err.println("No deps found");
            return;
        }

        for(int i=0; i < depsNodeList.getLength(); i++){
            final Element elDep = (Element)depsNodeList.item(i);
            final NodeList nArtifacts = elDep.getElementsByTagName("artifactId");
            if (nArtifacts.getLength() == 0){
                continue;
            }

            final Element elArtifact = (Element)nArtifacts.item(0);
            final Node alChild = elArtifact.getFirstChild();
            if (alChild.getNodeType() == Node.TEXT_NODE && alChild.getNodeValue().equalsIgnoreCase("api_classic")){
                System.err.println("api_classic found in dependencies");
                elDeps.removeChild(elDep);
                return;
            }
        }
    }
}
