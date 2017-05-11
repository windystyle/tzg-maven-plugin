package com.tzg.plugin.dependency.goal;

import com.tzg.plugin.dependency.support.DependencySupport;
import com.tzg.plugin.dependency.support.MongoDBSupport;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.util.List;

/**
 * @goal dep-gen
 */
public class DependencyGen extends AbstractMojo {

    /**
     * @component
     * @required
     */
    private Prompter prompter;

    @Override
    @SuppressWarnings( "unchecked" )
    public void execute() throws MojoExecutionException, MojoFailureException {

        String prompt = DependencySupport.getPrompt( "GENERATE" );

        try {

            String component = null;
            String index     = DependencySupport.getIndex( prompter, prompt );
            switch ( index ) {
                case "1":
                    component = "component-browser-starter";
                    break;
                case "2":
                    component = "component-mongodb";
                    DependencySupport.appendProperties( MongoDBSupport.getMongoDBMap(), MongoDBSupport.getMongoDBDeclaration(), component );
                    MongoDBSupport.genMongoDBModule();
                    break;
                case "3":
                    component = "web-auth";
                    break;
            }

            // 读取xml，根据输入的component进行查找，如果查找不到，则生成相关的组件，并写入pom.xml文件
            String pomPath = DependencySupport.getRootPath() + "/pom.xml";

            SAXReader reader   = new SAXReader();
            Document  document = reader.read( pomPath );

            Element         dependencies   = DependencySupport.getDependenciesElement( document );
            List< Element > dependencyList = DependencySupport.getDependencyElement( component, dependencies );

            if ( dependencyList.size() == 0 ) {
                // create <dependency> node
                Element dependencyElement = dependencies.addElement( "dependency" );
                dependencyElement.addElement( "groupId" ).addText( "com.tzg" );
                dependencyElement.addElement( "artifactId" ).addText( component );

                if ( component.contains( "web-" ) ) {
                    dependencyElement.addElement( "type" ).addText( "war" );

                    Element warClassifierDependency = dependencies.addElement( "dependency" );
                    warClassifierDependency.addElement( "groupId" ).addText( "com.tzg" );
                    warClassifierDependency.addElement( "artifactId" ).addText( component );
                    warClassifierDependency.addElement( "classifier" ).addText( "classes" );
                }
            }

            DependencySupport.pomWriter( pomPath, document );

        } catch ( DocumentException e ) {
            e.printStackTrace();
        } catch ( PrompterException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

}
